package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_exam_route")
public class BizExamRoute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long venueId;
    private String routeName;
    private String mapImageUrl;
    /** 线路轨迹 JSON：[[lng,lat],[lng,lat],...] ，供高德地图 Polyline 展示 */
    private String routePathJson;
    private String routeDesc;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
