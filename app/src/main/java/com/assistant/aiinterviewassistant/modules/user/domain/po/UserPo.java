package com.assistant.aiinterviewassistant.modules.user.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户持久化对象，对应 app_user 表。
 *
 * <p>仅用于数据库读写，接口响应使用独立的响应对象，避免暴露密码摘要。
 */
@Getter
@Setter
@TableName("app_user")
public class UserPo {

  /** 用户主键，由数据库自增生成。 */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /** 登录账号，唯一，区分大小写。 */
  private String username;

  /** 已验证邮箱，唯一性不区分大小写。 */
  private String email;

  /** BCrypt 密码摘要，不存储明文密码。 */
  @TableField("password_hash")
  private String passwordHash;

  /** 昵称，允许重复。 */
  private String nickname;

  /** 头像的稳定文件标识或地址，未设置时为空。 */
  private String avatar;

  /** 角色：USER 普通用户，ADMIN 管理员；注册时由服务端赋值。 */
  private String role;

  /** 账号状态：ACTIVE 正常，DISABLED 禁用。 */
  private String status;

  /** 创建时间，未指定时使用数据库默认值。 */
  @TableField("created_at")
  private OffsetDateTime createdAt;

  /** 更新时间，插入时有数据库默认值，后续更新由应用维护。 */
  @TableField("updated_at")
  private OffsetDateTime updatedAt;
}
