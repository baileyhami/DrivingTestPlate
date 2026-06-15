package com.jiakao.ydt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 第三方集成（交管公开页、高德地图等），生产环境建议用环境变量覆盖密钥类配置。
 */
@Data
@ConfigurationProperties(prefix = "ydt.integration")
public class IntegrationProperties {

    /**
     * 湖北省交管「考试能力公布」等官方页面完整 URL（含 hash 路由）。
     */
    private String officialExamScheduleUrl =
            "https://hb.122.gov.cn/web/html/?type=1&ticket=NIL&code=E_A_1023#/nView/exam/limitpub";

    /**
     * 高德 Web 服务 Key（地理编码等 REST 接口）。
     */
    private String amapKey = "";

    /**
     * 高德安全密钥（Web 服务数字签名 sig 用）；未开启签名时可留空。
     */
    private String amapSecret = "";

    /**
     * 当预约状态变为 COMPLETED 时，是否自动在培训表中创建一条记录（默认 true）。
     * 可通过 application.yml 或环境变量覆盖该值以禁用自动生成培训。
     */
    private boolean autoCreateTrainingOnAppointmentComplete = true;
}
