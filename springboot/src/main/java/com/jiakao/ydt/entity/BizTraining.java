package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_training")
public class BizTraining {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long schoolId;
    private Long studentId;
    private Long coachId;
    private String subjectType;
    private LocalDate trainingDate;
    private LocalDateTime checkInTime;
    private BigDecimal durationHours;
    private String content;
    private Integer score;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String studentNickname;
    @TableField(exist = false)
    private String coachNickname;
}
