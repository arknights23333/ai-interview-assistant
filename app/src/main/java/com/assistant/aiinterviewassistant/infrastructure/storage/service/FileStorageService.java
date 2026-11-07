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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.assistant.aiinterviewassistant.infrastructure.storage.domain.StorageDeleteModels;
import com.assistant.aiinterviewassistant.infrastructure.storage.domain.StorageDeleteModels.DeleteFailureCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final DateTimeFormatter DATE_PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client client;
    private final StorageConfigProperties storageProperties;
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
                    .bucket(storageProperties.getBucket())
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
        try {

            return generateDownloadUrl(key, originalFilename);
        } catch (S3Exception | SdkClientException | IllegalArgumentException e) {
            throw new BusinessException(FileErrorCode.FILE_DOWNLOAD_FAILED, e);
        }
    }

    /**
     * 下载对象文件为字节数组，调用前由业务层完成用户归属及回收站状态检查。
     * @param key
     * @return
     */
    public byte[] fileDownload(String key) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(key)
                .build();

         try{
             return client.getObjectAsBytes(objectRequest).asByteArray();
         }catch(NoSuchKeyException e){
             log.error("文件不存在，key: {}", key, e);
             throw new BusinessException(FileErrorCode.FILE_NOT_FOUND, e);
         }catch (S3Exception | SdkClientException e) {
            log.error("从对象存储下载文件失败，key: {}", key, e);
            throw new BusinessException(FileErrorCode.FILE_DOWNLOAD_FAILED, e);
         }
    }

    /** 为下载响应设置附件方式和 UTF-8 文件名，再将响应头参数纳入签名。 */
    private String generateDownloadUrl(String key, String originalFilename) {
        if(!hasObject(key)){
            log.error("文件不存在，key: {}", key);
            throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
        }
        String contentDisposition = ContentDisposition.attachment()
                .filename(originalFilename, StandardCharsets.UTF_8)
                .build()
                .toString();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucket())
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
        if (key == null || key.isBlank()) {
            log.error("删除对象失败：对象键为空");
            throw new BusinessException(FileErrorCode.FILE_DELETE_FAILED);
        }
        try{
            if(!hasObject(key)){
                log.debug("文件不存在，跳过删除: key={}", key);
                return;
            }
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
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
     * 批量删除对象，逐项返回结果。无效对象键只影响对应的记录。
     *
     * @param idByKey 对象键与业务 ID 的对应关系
     * @return 已确认成功的 ID 以及失败或结果未确认的对象
     */
    public <T> StorageDeleteModels.BatchDeleteResult<T> fileDeleteBatch(Map<String, T> idByKey) {
        List<T> successIds = new ArrayList<>();
        List<StorageDeleteModels.DeleteFailure<T>> failures = new ArrayList<>();
        List<String> validKeys = new ArrayList<>();

        // 逐项校验对象键和业务 ID，无效项单独记为失败，其余项继续删除。
        for (Map.Entry<String, T> entry : idByKey.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                failures.add(new StorageDeleteModels.DeleteFailure<>(
                        key, entry.getValue(), DeleteFailureCode.INVALID_OBJECT_KEY, null, "对象键为空"));
                continue;
            }
            if (entry.getValue() == null) {
                failures.add(new StorageDeleteModels.DeleteFailure<>(
                        key, null, DeleteFailureCode.INVALID_BUSINESS_ID, null, "业务 ID 为空"));
                continue;
            }
            validKeys.add(key);
        }

        // 没有可提交的对象时，直接返回已收集的结果。
        if (validKeys.isEmpty()) {
            return new StorageDeleteModels.BatchDeleteResult<>(successIds, failures);
        }

        List<ObjectIdentifier> objects = validKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();
        // quiet(false) 要求 S3 返回逐项成功和失败结果，便于按 key 关联业务 ID。
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(storageProperties.getBucket())
                .delete(Delete.builder().objects(objects).quiet(false).build())
                .build();

        DeleteObjectsResponse response;
        try {
            response = client.deleteObjects(request);
        } catch (S3Exception | SdkClientException e) {
            // 请求异常时无法确认对象是否已删除，业务层据此保留对应记录。
            log.error("对象存储批量删除请求失败，对象数量={}", validKeys.size(), e);
            for (String key : validKeys) {
                failures.add(new StorageDeleteModels.DeleteFailure<>(
                        key, idByKey.get(key), DeleteFailureCode.DELETE_RESULT_UNKNOWN, null, "无法确认对象删除结果"));
            }
            return new StorageDeleteModels.BatchDeleteResult<>(successIds, failures);
        }

        // 只接受本次提交的 key，每处理一条结果就移除对应 key，避免重复计入。
        Set<String> pendingKeys = new LinkedHashSet<>(validKeys);
        // 先收集明确失败的对象，同一个 key 如果也出现在成功列表，仍按失败处理。
        for (S3Error error : response.errors()) {
            String key = error.key();
            if (!pendingKeys.remove(key)) {
                log.warn("批量删除的失败结果无法匹配待确认对象，key={}", key);
                continue;
            }
            failures.add(new StorageDeleteModels.DeleteFailure<>(
                    key, idByKey.get(key), DeleteFailureCode.S3_DELETE_FAILED, error.code(), error.message()));
        }
        // 仅将 S3 明确确认删除的对象对应到成功 ID。
        for (DeletedObject deletedObject : response.deleted()) {
            String key = deletedObject.key();
            if (!pendingKeys.remove(key)) {
                log.warn("批量删除的成功结果无法匹配待确认对象，key={}", key);
                continue;
            }
            successIds.add(idByKey.get(key));
        }
        // 剩余 key 未收到逐项结果，标记为未确认，供业务层保留记录并重试。
        for (String key : pendingKeys) {
            log.warn("对象存储未返回批量删除结果，key={}", key);
            failures.add(new StorageDeleteModels.DeleteFailure<>(
                    key, idByKey.get(key), DeleteFailureCode.DELETE_RESULT_UNKNOWN, null, "无法确认对象删除结果"));
        }
        return new StorageDeleteModels.BatchDeleteResult<>(successIds, failures);
    }
    /**
     *判断文件是否存在
     */
    public boolean hasObject(String key){
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(storageProperties.getBucket())
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
