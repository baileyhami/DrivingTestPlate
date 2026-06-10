package com.jiakao.ydt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考场线路：高德路径规划结果（供前端绘制 Polyline）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanningResultDto {
    /**
     * 折线点序列，每项为 [lng, lat]。
     */
    private List<List<Double>> path;
    /**
     * DRIVING：驾车规划；WALKING_SEGMENT：步行分段拼接；ANCHOR_STRAIGHT：按锚点直连兜底。
     */
    private String planMode;
    private Integer distanceMeters;
    private Integer durationSeconds;
    /** 高德 info 或说明（失败原因等） */
    private String amapInfo;
}
