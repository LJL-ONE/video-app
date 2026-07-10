package com.video.module.user.dto;

import com.video.common.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageDTO {

    /**
     * 角色（管理员/普通用户）
     */
    private String role;

    /**
     * 状态（0-正常 1-禁用）
     */
    private Integer deleted;
}