package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_student")
public class SysStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long schoolId;
    private Long coachId;
    private String idCardNo;
    private LocalDate enrollDate;
    /** 1-4 学车阶段，5 已结业可评价 */
    private Integer learningStage;
    private LocalDateTime createTime;
}
