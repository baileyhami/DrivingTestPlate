package com.jiakao.ydt.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MockExamResultVO {
    private Long id;
    private Integer subject;
    private Integer score;
    private Integer totalCount;
    private Integer correctCount;
    private Integer durationSec;
    /** 科目一/四模拟：>=90 为及格 */
    private Boolean passed;
    private LocalDateTime createTime;
}
