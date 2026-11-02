package com.assistant.aiinterviewassistant.infrastructure.storage;

import com.assistant.aiinterviewassistant.infrastructure.storage.config.S3Config;
import com.assistant.aiinterviewassistant.infrastructure.storage.config.StorageConfigProperties;
import com.assistant.aiinterviewassistant.infrastructure.storage.service.FileStorageService;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ContentDisposition;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真实 RustFS 联调：按上传、下载、删除顺序执行，共用本次上传的临时对象。
 * 设置 RUN_STORAGE_INTEGRATION_TESTS=true 后运行整个测试类；不启动应用或数据库迁移。
 * 连接参数使用 STORAGE_ENDPOINT、STORAGE_BUCKET、STORAGE_REGION；
 * 凭证优先使用 STORAGE_ACCESS_KEY、STORAGE_SECRET_KEY，兼容 RUSTFS 同名凭证变量。
 */
@Tag("integration")
@DisplayName("RustFS 文件上传、预签名下载与删除")
@EnabledIfEnvironmentVariable(named = "RUN_STORAGE_INTEGRATION_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileStorageIntegrationTests {

    private static final String ORIGINAL_FILENAME = "存储联调简历.pdf";
    private final byte[] fileContent = createPdfFixture();
    private AnnotationConfigApplicationContext context;
    private FileStorageService storageService;
    private S3Client client;
    private StorageConfigProperties properties;
    private String fileKey;

    @BeforeAll
    void setUp() {
        properties = new StorageConfigProperties();
        properties.setEndpoint(environmentOrDefault("STORAGE_ENDPOINT", "http://localhost:9000"));
        properties.setBucket(environmentOrDefault("STORAGE_BUCKET", "ai-interview-assistant"));
        properties.setRegion(environmentOrDefault("STORAGE_REGION", "us-east-1"));
        properties.setAccessKey(requiredCredential("STORAGE_ACCESS_KEY", "RUSTFS_ACCESS_KEY"));
        properties.setSecretKey(requiredCredential("STORAGE_SECRET_KEY", "RUSTFS_SECRET_KEY"));

        context = new AnnotationConfigApplicationContext();
        context.registerBean(StorageConfigProperties.class, () -> properties);
        context.register(S3Config.class, FileStorageService.class);
        context.refresh();
        storageService = context.getBean(FileStorageService.class);
        client = context.getBean(S3Client.class);
    }

    @Test
    @Order(1)
    @DisplayName("1 上传简历，检查对象存在且大小和类型正确")
    void uploadResume() {
        MockMultipartFile file = new MockMultipartFile(
                "file", ORIGINAL_FILENAME, "application/pdf", fileContent);
        fileKey = storageService.uploadResume(file);

        assertTrue(fileKey.startsWith("resumes/"));
        assertTrue(storageService.hasObject(fileKey));
        var metadata = client.headObject(HeadObjectRequest.builder()
                .bucket(properties.getBucket()).key(fileKey).build());
        assertEquals((long) fileContent.length, metadata.contentLength().longValue());
        assertEquals("application/pdf", metadata.contentType());
    }

    @Test
    @Order(2)
    @DisplayName("2 使用一分钟预签名链接下载，核对字节及中文附件名")
    void downloadResume() throws Exception {
        assertNotNull(fileKey, "请运行整个测试类，先执行上传测试");
        URI uri = URI.create(storageService.fileDownload(fileKey, ORIGINAL_FILENAME));
        assertTrue(List.of(uri.getRawQuery().split("&")).contains("X-Amz-Expires=60"));

        try (HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build()) {
            var request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());
            assertArrayEquals(fileContent, response.body());
            String header = response.headers().firstValue("Content-Disposition").orElseThrow();
            ContentDisposition disposition = ContentDisposition.parse(header);
            assertEquals("attachment", disposition.getType());
            assertEquals(ORIGINAL_FILENAME, disposition.getFilename());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3 删除本次上传的简历，确认不存在且重复删除不报错")
    void deleteResume() {
        assertNotNull(fileKey, "请运行整个测试类，先执行上传测试");
        storageService.deleteResume(fileKey);
        assertFalse(storageService.hasObject(fileKey));
        storageService.deleteResume(fileKey);
        fileKey = null;
    }

    @AfterAll
    void cleanUp() {
        try {
            // 下载断言失败时也清理本次生成的对象，不删除桶或任何已有文件。
            if (storageService != null && fileKey != null) {
                storageService.deleteResume(fileKey);
            }
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredCredential(String primary, String fallback) {
        String value = environmentOrDefault(primary, System.getenv(fallback));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("请配置环境变量 " + primary + " 或 " + fallback);
        }
        return value;
    }

    /** 生成一个最小 PDF，仅验证原文件字节往返，不涉及正文解析或 AI 分析。 */
    private static byte[] createPdfFixture() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        bytes.writeBytes("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        List<String> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Resources << >> >>");
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(bytes.size());
            bytes.writeBytes(((i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n")
                    .getBytes(StandardCharsets.US_ASCII));
        }
        int xrefOffset = bytes.size();
        bytes.writeBytes("xref\n0 4\n0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int offset : offsets) {
            bytes.writeBytes(String.format(Locale.ROOT, "%010d 00000 n \n", offset)
                    .getBytes(StandardCharsets.US_ASCII));
        }
        bytes.writeBytes(("trailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n"
                + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
        return bytes.toByteArray();
    }
}
