package com.video.module.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.video.common.exception.BusinessException;
import com.video.common.minio.MinioService;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.module.video.dto.VideoQueryDTO;
import com.video.module.video.entity.Video;
import com.video.module.video.mapper.VideoMapper;
import com.video.module.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private static final long MAX_SIZE = 200L * 1024 * 1024; // 200MB
    private static final List<String> ALLOWED_EXT = List.of(".mp4", ".mov", ".avi", ".mkv");

    private final VideoMapper videoMapper;
    private final MinioService minioService;

    @Override
    public List<Video> list() {
        return videoMapper.selectList(null);
    }

    @Override
    public PageResult<Video> page(VideoQueryDTO query) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        // 搜索关键字（标题）
        if (query.getKeyword() != null) {
            wrapper.like(Video::getTitle, query.getKeyword());
        }

        // 分类筛选
        if (query.getCategory() != null) {
            wrapper.eq(Video::getCategory, query.getCategory());
        }

        // 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(Video::getStatus, query.getStatus());
        }

        // 上传者筛选
        if (query.getUploader() != null) {
            wrapper.eq(Video::getUploader, query.getUploader());
        }

        // 排序：默认按创建时间降序
        wrapper.orderByDesc(Video::getCreateTime);

        Page<Video> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Video> result = videoMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), query.getPageNum(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Video upload(MultipartFile file, String title, String category, String uploader) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VIDEO_FILE_EMPTY);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "文件大小不能超过 200MB");
        }
        String original = file.getOriginalFilename();
        if (original != null) {
            String lower = original.toLowerCase();
            boolean ok = ALLOWED_EXT.stream().anyMatch(lower::endsWith);
            if (!ok) {
                throw new BusinessException(ResultCode.VIDEO_FILE_TYPE_ERROR);
            }
        }
        if (!StringUtils.hasText(title) || !StringUtils.hasText(category)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "标题和分类不能为空");
        }

        // 上传 MinIO
        String key = minioService.upload(file, "video/");
        String url = minioService.getPublicUrl(key);

        Video v = new Video();
        v.setTitle(title);
        v.setCategory(category);
        v.setFileKey(key);
        v.setFileUrl(url);
        v.setSize(file.getSize());
        v.setStatus("已发布");
        v.setUploader(StringUtils.hasText(uploader) ? uploader : "admin");
        videoMapper.insert(v);
        log.info("视频上传成功 id={} key={}", v.getId(), key);
        return v;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Video v = videoMapper.selectById(id);
        if (v == null) {
            throw new BusinessException(ResultCode.VIDEO_NOT_EXIST);
        }
        videoMapper.deleteById(id);
        // 异步删 MinIO（此处同步即可）
        minioService.delete(v.getFileKey());
    }

    @Override
    public void updateStatus(Long id, String status) {
        Video v = videoMapper.selectById(id);
        if (v == null) {
            throw new BusinessException(ResultCode.VIDEO_NOT_EXIST);
        }
        v.setStatus(status);
        videoMapper.updateById(v);
    }

    @Override
    public Video getById(Long id) {
        return videoMapper.selectById(id);
    }
}
