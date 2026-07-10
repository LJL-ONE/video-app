package com.video.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSaveDTO {

    /** 编辑时传，新增时为 null */
    private Long id;

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 角色：管理员 / 普通用户 */
    @NotBlank(message = "角色不能为空")
    private String role;
}
