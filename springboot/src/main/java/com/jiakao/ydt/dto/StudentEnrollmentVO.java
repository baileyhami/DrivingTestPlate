package com.jiakao.ydt.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentEnrollmentVO {
    private boolean enrolled;
    private Long schoolId;
    private String schoolName;
    private Long coachId;
    private String coachName;
    private LocalDate enrollDate;
    private String phone;
    private String email;
    private Boolean reviewSubmitted;
    private Boolean canSubmitReview;
}
