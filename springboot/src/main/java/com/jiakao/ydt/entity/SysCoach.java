package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_coach")
public class SysCoach {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long schoolId;
    private String licenseNo;
    private String specialty;
    private String intro;
    private LocalDateTime createTime;
}
