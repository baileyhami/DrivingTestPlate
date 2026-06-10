package com.jiakao.ydt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MockStartRequest {
    @NotNull(message = "科目不能为空")
    private Integer subject;

    /** RANDOM / SEQUENTIAL / SIMULATION */
    private String mode = "RANDOM";

    /** 顺序做题起始题号（1-based） */
    @Min(1)
    private Integer startQuestionNo = 1;

    @Min(5)
    @Max(100)
    private Integer questionCount = 20;
}
