package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_review")
public class BizReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentUserId;
    private Long schoolId;
    private Long coachId;
    private Integer schoolStars;
    private Integer coachStars;
    private LocalDateTime createTime;
}
