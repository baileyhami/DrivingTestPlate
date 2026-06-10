package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_mock_exam")
public class BizMockExam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer subject;
    private Integer score;
    private Integer totalCount;
    private Integer correctCount;
    private Integer durationSec;
    private String detailJson;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String studentNickname;
}
