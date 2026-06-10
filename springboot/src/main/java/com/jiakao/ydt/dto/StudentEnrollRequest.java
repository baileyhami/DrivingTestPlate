package com.jiakao.ydt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentEnrollRequest {
    @NotNull private Long schoolId;
    @NotNull private Long coachId;
    @NotBlank private String phone;
    @NotBlank private String email;
}
