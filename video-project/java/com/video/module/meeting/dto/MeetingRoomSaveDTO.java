package com.video.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeetingRoomSaveDTO {

    private Long id;

    @NotBlank(message = "房间名称不能为空")
    private String roomName;

    private String description;

    private String creator;
}
