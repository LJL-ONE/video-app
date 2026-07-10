package com.video.module.meeting.controller;

import com.video.common.result.PageResult;
import com.video.common.result.Result;
import com.video.module.meeting.dto.MeetingQueryDTO;
import com.video.module.meeting.dto.MeetingRoomSaveDTO;
import com.video.module.meeting.entity.MeetingRoom;
import com.video.module.meeting.service.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    /** 房间列表（不分页）：GET /api/meeting/list */
    @GetMapping("/list")
    public Result<List<MeetingRoom>> list() {
        return Result.success(meetingRoomService.list());
    }

    /** 房间列表（分页）：GET /api/meeting/page */
    @GetMapping("/page")
    public Result<PageResult<MeetingRoom>> page(@Valid MeetingQueryDTO query) {
        return Result.success(meetingRoomService.page(query));
    }

    /** 新建/编辑：POST /api/meeting/save */
    @PostMapping("/save")
    public Result<MeetingRoom> save(@RequestBody @Valid MeetingRoomSaveDTO dto) {
        return Result.success("保存成功", meetingRoomService.save(dto));
    }

    /** 删除：DELETE /api/meeting/{id} */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        meetingRoomService.delete(id);
        return Result.success("删除成功", null);
    }

    /** 切换状态：PUT /api/meeting/{id}/status */
    @PutMapping("/{id}/status")
    public Result<?> toggleStatus(@PathVariable Long id) {
        meetingRoomService.toggleStatus(id);
        return Result.success("状态已更新", null);
    }
}
