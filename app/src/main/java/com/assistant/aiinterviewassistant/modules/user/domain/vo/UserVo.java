package com.assistant.aiinterviewassistant.modules.user.domain.vo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.OffsetDateTime;


@Data
public class UserVo {
    /** 用户主键，由数据库自增生成。 */
    private Long id;

    /** 登录账号，唯一，区分大小写。 */
    private String username;

    /** 已验证邮箱，唯一性不区分大小写。 */
    private String email;

    /** 昵称，允许重复。 */
    private String nickname;

    /** 头像的稳定文件标识或地址，未设置时为空。 */
    private String avatar;

    /** 角色：USER 普通用户，ADMIN 管理员；注册时由服务端赋值。 */
    private String role;

    /** 账号状态：ACTIVE 正常，DISABLED 禁用。 */
    private String status;

    /** 创建时间，未指定时使用数据库默认值。 */

    private OffsetDateTime createdAt;

    /** 更新时间，插入时有数据库默认值，后续更新由应用维护。 */

    private OffsetDateTime updatedAt;
}
