package com.video.module.meeting.service;

import com.video.common.exception.BusinessException;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.module.meeting.dto.MeetingQueryDTO;
import com.video.module.meeting.dto.MeetingRoomSaveDTO;
import com.video.module.meeting.entity.MeetingRoom;

import java.util.List;

public interface MeetingRoomService {

    /**
     * 列表（全量）
     */
    List<MeetingRoom> list();

    /**
     * 分页查询
     */
    PageResult<MeetingRoom> page(MeetingQueryDTO query);

    /**
     * 新建/编辑
     */
    MeetingRoom save(MeetingRoomSaveDTO dto);

    /**
     * 删除
     */
    void delete(Long id);

    /**
     * 切换状态（空闲/使用中）
     */
    void toggleStatus(Long id);

    /**
     * 根据 ID 查询
     */
    MeetingRoom getById(Long id);

    /**
     * 业务异常工具
     */
    default BusinessException biz(ResultCode rc) {
        return new BusinessException(rc);
    }
}
