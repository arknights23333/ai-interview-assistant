package com.assistant.aiinterviewassistant.infrastructure.document.service;

import com.assistant.aiinterviewassistant.common.exception.enums.FileErrorCode;
import com.assistant.aiinterviewassistant.common.exception.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 文件哈希服务类,计算文件哈希
 */
@Service
@RequiredArgsConstructor
public class FileHashService {
    /**
     * 分批读取输入流计算文件哈希，适用于体积较大的文件
     * @param inputStream 输入流，调用方负责关闭
     * @return 转换成16进制的文件哈希值
     */
    public String calculateHash(InputStream inputStream) {
        return calculateHash(inputStream, new byte[0]);
    }

    private String calculateHash(InputStream inputStream, byte[] prefix) {
        try{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            digest.update(prefix);
            byte[] buffer=new byte[8192];
            int bytesRead=0;
            while((bytesRead = inputStream.read(buffer))!=-1){
                digest.update(buffer,0,bytesRead);
            }
            return byteToHex(digest.digest());
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_READ_FAILED, e);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(FileErrorCode.FILE_HASH_FAILED, e);
        }
    }

    /**
     * 计算字节数组的哈希值
     * @param bytes 字节数组
     * @return 转换成16进制的文件哈希值
     */
    public String calculateHash(byte[ ] bytes){
        try{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            return byteToHex(digest.digest(bytes));
        }catch(NoSuchAlgorithmException e){
            throw new BusinessException(FileErrorCode.FILE_HASH_FAILED,e);
        }
    }

    /**
     * 计算文件的哈希值
     * @param file
     * @return转换成16进制的文件哈希值
     */
    public String calculateHash(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            return calculateHash(stream);
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_READ_FAILED, e);
        }
    }

    /**
     * 将原始文件名和文件内容一起计算为一个 SHA-256 哈希。
     */
    public String calculateHashWithFilename(MultipartFile file) {
        String fileName = Objects.toString(file.getOriginalFilename(), "");
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        // 在文件名之前写入字节长度，明确文件名与正文的边界。
        byte[] prefix = ByteBuffer.allocate(Integer.BYTES + fileNameBytes.length)
                .putInt(fileNameBytes.length)
                .put(fileNameBytes)
                .array();

        try (InputStream stream = file.getInputStream()) {
            return calculateHash(stream, prefix);
        } catch (IOException e) {
            throw new BusinessException(FileErrorCode.FILE_READ_FAILED, e);
        }
    }

    /**
     * 字节转16进制
     * @param bytes
     * @return
     */
    public String byteToHex(byte[ ] bytes){
        StringBuilder builder= new StringBuilder(bytes.length*2);
        for(byte b : bytes)
        {
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }
}
