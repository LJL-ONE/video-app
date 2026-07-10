package com.video.module.meeting.dto;

import com.video.common.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会议室查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MeetingQueryDTO extends PageDTO {

    /**
     * 状态（空闲/使用中）
     */
    private String status;

    /**
     * 创建者
     */
    private String creator;
}