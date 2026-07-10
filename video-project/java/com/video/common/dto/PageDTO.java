package com.video.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageDTO {

    /**
     * 当前页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序（默认降序）
     */
    private Boolean isAsc = false;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 计算偏移量
     */
    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}