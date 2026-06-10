package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_exam_registration")
public class BizExamRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private Long studentId;
    private Long venueId;
    private String examSubject;
    private LocalDate examDate;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String studentNickname;
    @TableField(exist = false)
    private String venueName;
}
