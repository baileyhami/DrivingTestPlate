package com.jiakao.ydt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPointDto {
    private BigDecimal longitude;
    private BigDecimal latitude;
    /** 高德原始 location 字符串 "lng,lat" */
    private String rawLocation;
    /** 结构化地址（若接口返回） */
    private String formattedAddress;
}
