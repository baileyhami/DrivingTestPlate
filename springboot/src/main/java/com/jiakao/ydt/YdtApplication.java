package com.jiakao.ydt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 驾考一点通 - 后端启动类
 */
@SpringBootApplication
@MapperScan("com.jiakao.ydt.mapper")
@EnableMethodSecurity
@EnableCaching
public class YdtApplication {

    public static void main(String[] args) {
        SpringApplication.run(YdtApplication.class, args);
    }
}
