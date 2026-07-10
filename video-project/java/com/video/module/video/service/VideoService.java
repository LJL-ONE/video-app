package com.video.module.video.service;

import com.video.common.exception.BusinessException;
import com.video.common.result.PageResult;
import com.video.common.result.ResultCode;
import com.video.module.video.dto.VideoQueryDTO;
import com.video.module.video.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    /** 列表（全量，与前端一致由前端分页筛选） */
    List<Video> list();

    /** 分页查询 */
    PageResult<Video> page(VideoQueryDTO query);

    /** 上传视频文件到 MinIO */
    Video upload(MultipartFile file, String title, String category, String uploader);

    /** 删除视频 */
    void delete(Long id);

    /** 修改状态（审核/下架） */
    void updateStatus(Long id, String status);

    /** 根据 ID 查询 */
    Video getById(Long id);

    /** 业务异常工具 */
    default BusinessException biz(ResultCode rc) {
        return new BusinessException(rc);
    }
}
