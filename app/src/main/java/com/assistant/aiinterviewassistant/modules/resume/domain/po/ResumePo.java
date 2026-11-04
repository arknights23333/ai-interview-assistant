package com.assistant.aiinterviewassistant.modules.resume.domain.po;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@TableName("resume")
public class ResumePo {
    /**简历id，数据库自增主键 */
    @TableId(value="id",type= IdType.AUTO)
    private Long id;

    /**用户id，关联用户表 */
    private Long userId;

    /**创建时间 */
    @TableField("create_at")
    private OffsetDateTime createAt;

    /**文件哈希值，用于识别文件是否是同一份 */
    @TableField("file_hash")
    private String fileHash;

    /**文件大小 */
    @TableField("file_size")
    private Long fileSize;

    /**文件名 */
    @TableField("file_name")
    private String fileName;

    /**简历内容 */
    @TableField("resume_content")
    private String resumeContent;

    /** 原始文件在对象存储桶内的对象键。 */
    @TableField("storage_key")
    private String storageKey;

    /**内容类型 */
    @TableField("content_type")
    private String contentType;

    /**解析状态 */
    @TableField("parse_status")
    private String parseStatus;

    /**解析错误信息 */
    @TableField("parse_error")
    private String parseError;

    /**移入回收站时间，为空表示未删除 */
    @TableField("delete_time")
    private OffsetDateTime deleteTime;
}
