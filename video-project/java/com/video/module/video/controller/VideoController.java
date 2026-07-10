package com.video.module.video.controller;

import com.video.common.result.PageResult;
import com.video.common.result.Result;
import com.video.module.video.dto.VideoQueryDTO;
import com.video.module.video.entity.Video;
import com.video.module.video.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** 视频列表（不分页）：GET /api/video/list */
    @GetMapping("/list")
    public Result<List<Video>> list() {
        return Result.success(videoService.list());
    }

    /** 视频列表（分页）：GET /api/video/page */
    @GetMapping("/page")
    public Result<PageResult<Video>> page(@Valid VideoQueryDTO query) {
        return Result.success(videoService.page(query));
    }

    /** 视频上传：POST /api/video/upload */
    @PostMapping("/upload")
    public Result<Video> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam(value = "uploader", required = false) String uploader) {
        Video v = videoService.upload(file, title, category, uploader);
        return Result.success("上传成功", v);
    }

    /** 删除视频：DELETE /api/video/{id} */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        videoService.delete(id);
        return Result.success("删除成功", null);
    }

    /** 更新视频状态：PUT /api/video/{id}/status */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        videoService.updateStatus(id, status);
        return Result.success("状态已更新", null);
    }
}
