package com.video.module.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.video.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video")
public class Video extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频标题 */
    private String title;

    /** 分类 */
    private String category;

    /** MinIO 中的对象 key */
    private String fileKey;

    /** 视频访问 URL */
    private String fileUrl;

    /** 封面图 URL */
    private String coverUrl;

    /** 文件大小（字节） */
    private Long size;

    /** 时长（秒） */
    private Integer duration;

    /** 状态：待审核 / 已发布 / 已下架 */
    private String status;

    /** 上传者账号 */
    private String uploader;
}
