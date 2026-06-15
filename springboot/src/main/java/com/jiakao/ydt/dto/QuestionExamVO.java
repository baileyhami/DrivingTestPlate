package com.jiakao.ydt.dto;

import lombok.Data;

/**
 * 模拟考试题目（不含答案）
 */
@Data
public class QuestionExamVO {
    private Long id;
    private Integer subject;
    private Integer qType;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String imageUrl;
    private String chapter;
}
