package com.video.common.result;

import lombok.Getter;

/**
 * 业务响应码枚举
 * 区间划分：
 *   200       - 成功
 *   400-409   - 客户端错误（参数、未授权、禁止等）
 *   500-509   - 服务端错误
 *   1xxx      - 用户模块业务码
 *   2xxx      - 视频模块业务码
 *   3xxx      - 会议模块业务码
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不被允许"),
    SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // ============ 用户模块 1xxx ============
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "账号或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_ALREADY_EXIST(1004, "账号已存在"),

    // ============ 视频模块 2xxx ============
    VIDEO_NOT_EXIST(2001, "视频不存在"),
    VIDEO_UPLOAD_FAIL(2002, "视频上传失败"),
    VIDEO_FILE_EMPTY(2003, "上传文件不能为空"),
    VIDEO_FILE_TYPE_ERROR(2004, "视频格式不支持"),

    // ============ 会议模块 3xxx ============
    MEETING_NOT_EXIST(3001, "会议房间不存在"),
    MEETING_ROOM_BUSY(3002, "会议房间正在使用中");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
