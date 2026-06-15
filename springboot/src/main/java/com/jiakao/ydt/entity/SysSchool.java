package com.jiakao.ydt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_school")
public class SysSchool {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String city;
    private String shortName;
    private String licenseNo;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private String contactName;
    private String workHours;
    private BigDecimal priceC1;
    private BigDecimal priceC2;
    private Long userId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
