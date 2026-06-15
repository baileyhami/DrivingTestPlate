package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_enrollment")
public class BizEnrollment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long schoolId;
    private String applicantName;
    private String phone;
    private String idCard;
    private String source;
    private String status;
    private String remark;
    private Long handlerUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
