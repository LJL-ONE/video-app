package com.video.module.meeting.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.video.common.exception.BusinessException;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.common.security.UserContext;
import com.video.module.meeting.dto.MeetingQueryDTO;
import com.video.module.meeting.dto.MeetingRoomSaveDTO;
import com.video.module.meeting.entity.MeetingRoom;
import com.video.module.meeting.mapper.MeetingRoomMapper;
import com.video.module.meeting.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;

    @Override
    public List<MeetingRoom> list() {
        return meetingRoomMapper.selectList(null);
    }

    @Override
    public PageResult<MeetingRoom> page(MeetingQueryDTO query) {
        LambdaQueryWrapper<MeetingRoom> wrapper = new LambdaQueryWrapper<>();

        // 搜索关键字（房间名称）
        if (query.getKeyword() != null) {
            wrapper.like(MeetingRoom::getRoomName, query.getKeyword());
        }

        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(MeetingRoom::getStatus, query.getStatus());
        }

        // 创建者筛选
        if (query.getCreator() != null) {
            wrapper.eq(MeetingRoom::getCreator, query.getCreator());
        }

        // 排序：默认按创建时间降序
        wrapper.orderByDesc(MeetingRoom::getCreateTime);

        Page<MeetingRoom> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<MeetingRoom> result = meetingRoomMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingRoom save(MeetingRoomSaveDTO dto) {
        if (dto.getId() == null) {
            MeetingRoom r = new MeetingRoom();
            BeanUtils.copyProperties(dto, r);
            r.setStatus("空闲"); // 新建会议室默认空闲
            // 优先使用 DTO 中的创建者，否则从当前登录用户获取
            if (!StringUtils.hasText(r.getCreator())) {
                String currentUser = UserContext.getCurrentUsername();
                r.setCreator(currentUser != null ? currentUser : "admin");
            }
            meetingRoomMapper.insert(r);
            log.info("新建会议室: id={}, name={}", r.getId(), r.getRoomName());
            return r;
        } else {
            MeetingRoom r = meetingRoomMapper.selectById(dto.getId());
            if (r == null) {
                throw new BusinessException(ResultCode.MEETING_NOT_EXIST);
            }
            r.setRoomName(dto.getRoomName());
            r.setDescription(dto.getDescription());
            meetingRoomMapper.updateById(r);
            log.info("更新会议室: id={}, name={}", r.getId(), r.getRoomName());
            return r;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MeetingRoom r = meetingRoomMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.MEETING_NOT_EXIST);
        }
        meetingRoomMapper.deleteById(id);
        log.info("删除会议室: id={}, name={}", id, r.getRoomName());
    }

    @Override
    public void toggleStatus(Long id) {
        MeetingRoom r = meetingRoomMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(ResultCode.MEETING_NOT_EXIST);
        }
        String newStatus = "使用中".equals(r.getStatus()) ? "空闲" : "使用中";
        r.setStatus(newStatus);
        meetingRoomMapper.updateById(r);
        log.info("切换会议室状态: id={}, status={}", id, newStatus);
    }

    @Override
    public MeetingRoom getById(Long id) {
        return meetingRoomMapper.selectById(id);
    }
}
