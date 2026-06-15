package com.jiakao.ydt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentReviewRequest {
    @NotNull @Min(1) @Max(5) private Integer schoolStars;
    @NotNull @Min(1) @Max(5) private Integer coachStars;
}
