package com.jiakao.ydt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoachInSchoolVO {
    private Long coachId;
    private String nickname;
    private String username;
    private String specialty;
    private BigDecimal ratingAvg;
    private Long ratingCount;
}
