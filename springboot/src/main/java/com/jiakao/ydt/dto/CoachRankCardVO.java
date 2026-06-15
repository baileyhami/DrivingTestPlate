package com.jiakao.ydt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoachRankCardVO {
    private Long coachId;
    private String coachName;
    private String schoolName;
    private String schoolShortName;
    private BigDecimal ratingAvg;
    private Long ratingCount;
}
