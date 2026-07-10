package com.video.module.meeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.video.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_room")
public class MeetingRoom extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房间名称 */
    private String roomName;

    /** 房间描述 */
    private String description;

    /** 创建者账号 */
    private String creator;

    /** 状态：空闲 / 使用中 */
    private String status;
}
