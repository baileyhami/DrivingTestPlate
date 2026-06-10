package com.jiakao.ydt.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewPublicVO {
    private String schoolName;
    private String coachName;
    private Integer schoolStars;
    private Integer coachStars;
    private String reviewerLabel;
    private LocalDateTime createTime;
}
