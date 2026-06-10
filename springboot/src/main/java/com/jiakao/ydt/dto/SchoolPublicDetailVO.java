package com.jiakao.ydt.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SchoolPublicDetailVO {
    private Long id;
    private String name;
    private String shortName;
    private String city;
    private String licenseNo;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private String workHours;
    private BigDecimal priceC1;
    private BigDecimal priceC2;
    private BigDecimal ratingAvg;
    private Long ratingCount;
    private List<CoachInSchoolVO> coaches;
}
