package com.assistant.aiinterviewassistant.infrastructure.storage.service;

import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import com.assistant.aiinterviewassistant.infrastructure.storage.config.StorageConfigProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client client;
    private final StorageConfigProperties storageproperties;
    private final S3Presigner presigner;

    /**
     * 上传简历原文件。
     *
     * @param file 已由业务层完成校验的上传文件
     * @return 对象存储 key，使用 resumes 前缀
     */
    public String uploadResume(MultipartFile file) {
        return fileUpload(file, "resumes");
    }

    /**
     * 上传知识库文档。
     *
     * @param file 已由业务层完成校验的上传文件
     * @return 对象存储 key，使用 knowledgebases 前缀
     */
    public String uploadKnowledgeBase(MultipartFile file) {
        return fileUpload(file, "knowledgebases");
    }

    /**
     * 彻底删除简历原文件，移入回收站时不调用。
     *
     * @param fileKey 已由业务层校验归属的文件存储 key
     */
    public void deleteResume(String fileKey) {
        fileDelete(fileKey);
    }

    /**
     * 删除知识库原文件。
     *
     * @param fileKey 已由业务层校验归属的文件存储 key
     */
    public void deleteKnowledgeBase(String fileKey) {
        fileDelete(fileKey);
    }

    /** 上传文件并返回对象存储 key；由本方法关闭打开的文件输入流。 */
    private String fileUpload(MultipartFile file, String prefix) {
        String fileKey = generateFileKey(prefix);
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageproperties.getBucket())
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
            return fileKey;
        } catch (IOException e) {
            log.error("读取或关闭上传文件输入流失败", e);
            throw new BusinessException(FileErrorCode.FILE_READ_FAILED, e);
        } catch (S3Exception e) {
            log.error("对象存储拒绝文件上传: key={}", fileKey, e);
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED, e);
        } catch (SdkClientException e) {
            log.error("连接对象存储或发送上传请求失败: key={}", fileKey, e);
            throw new BusinessException(FileErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * 生成一分钟有效的附件下载链接，使用原始文件名保存。
     * 调用前由业务层完成用户归属及回收站状态检查。
     *
     * @param key 数据库保存的文件存储 key
     * @param originalFilename 数据库保存的原始文件名，包含扩展名
     * @return 供浏览器访问的预签名下载链接
     */
    public String fileDownload(String key, String originalFilename) {
        return generateDownloadUrl(key, originalFilename);
    }

    /** 为下载响应设置附件方式和 UTF-8 文件名，再将响应头参数纳入签名。 */
    private String generateDownloadUrl(String key, String originalFilename) {
        String contentDisposition = ContentDisposition.attachment()
                .filename(originalFilename, StandardCharsets.UTF_8)
                .build()
                .toString();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(storageproperties.getBucket())
                .key(key)
                .responseContentDisposition(contentDisposition)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(1))
                .build();

        PresignedGetObjectRequest signedRequest =
                presigner.presignGetObject(presignRequest);

        return signedRequest.url().toExternalForm();
    }

    /**
     * 删除文件，对象不存在时直接返回。
     * @param key 数据库保存的文件存储 key
     */
    public void fileDelete(String key){
        if (key == null || key.isEmpty()) {
            log.debug("文件键为空，跳过删除");
            return;
        }
        try{
            if(!hasObject(key)){
                log.debug("文件不存在，跳过删除: key={}", key);
                return;
            }
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageproperties.getBucket())
                    .key(key)
                    .build();

            client.deleteObject(request);
        }catch (S3Exception e){
            log.error("对象存储处理文件删除请求失败: key={}", key, e);
            throw new BusinessException(FileErrorCode.FILE_DELETE_FAILED, e);

        }
        catch (SdkClientException e) {
            log.error("连接对象存储或发送删除请求失败: key={}", key, e);
            throw new BusinessException(FileErrorCode.FILE_DELETE_FAILED, e);
        }
    }

    /**
     *判断文件是否存在
     */
    public boolean hasObject(String key){
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(storageproperties.getBucket())
                .key(key)
                .build();
        try{
            client.headObject(request);
            return true;
        } catch (S3Exception e) {
            if(e.statusCode() == 404){
                return false;
            }
            throw e;
        }
    }
    /**
     * 生成文件存储 key，格式为 {prefix}/{yyyy/MM/dd}/{uuid}。
     * 原始文件名由业务保存，对象 key 不附加固定扩展名。
     */
    private String generateFileKey(String prefix) {
        String uuid = UUID.randomUUID().toString();
        String datePath = LocalDate.now().format(DATE_PATH_FORMAT);
        return prefix + "/" + datePath + "/" + uuid;
    }
}
