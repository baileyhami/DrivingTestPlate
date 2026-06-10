package com.jiakao.ydt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 官方考试时间 / 考试能力公布页对接信息（由后端统一下发 URL，避免前端写死）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficialExamScheduleInfoDto {
    private String title;
    private String description;
    /** 完整官方页面 URL（含 hash） */
    private String officialPageUrl;
    /** 不含 hash 的基础 URL，便于服务端探测 */
    private String probeBaseUrl;
    /** 最近一次探测官方站点是否 HTTP 成功（不代表页面业务可用） */
    private Boolean remoteReachable;
    private String lastCheckedAt;
}
