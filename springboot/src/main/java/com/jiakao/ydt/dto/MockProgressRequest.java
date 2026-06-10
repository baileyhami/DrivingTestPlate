package com.jiakao.ydt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MockProgressRequest {
    @NotNull
    private Integer subject;
    @NotEmpty
    @Valid
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull
        private Long questionId;
        @NotNull
        private String userAnswer;
    }
}

