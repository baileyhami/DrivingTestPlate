package com.jiakao.ydt.dto;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String roleCode;
    private Long schoolId;
    private Long studentId;
    private Long coachId;
    /** 学员：学车阶段 1-4，5 已结业可评价 */
    private Integer learningStage;
    private Boolean enrolled;
    private Boolean reviewSubmitted;
    private Boolean canSubmitReview;
}
