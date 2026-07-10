package com.video.module.video.dto;

import com.video.common.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VideoQueryDTO extends PageDTO {

    /**
     * 分类
     */
    private String category;

    /**
     * 状态（待审核/已发布/已下架）
     */
    private String status;

    /**
     * 上传者
     */
    private String uploader;
}