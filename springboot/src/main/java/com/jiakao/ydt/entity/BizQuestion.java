package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_question")
public class BizQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 1 科目一 4 科目四 */
    private Integer subject;
    /** 1单选 2判断 3多选 */
    private Integer qType;
    private String title;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String analysis;
    private String imageUrl;
    private String chapter;
    private LocalDateTime createTime;
}
