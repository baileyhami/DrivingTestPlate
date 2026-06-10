package com.jiakao.ydt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SchoolPublicCardVO {
    private Long id;
    private String name;
    private String shortName;
    private String city;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private String workHours;
    private BigDecimal priceC1;
    private BigDecimal priceC2;
    private BigDecimal ratingAvg;
    private Long ratingCount;
}
