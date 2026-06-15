package com.jiakao.ydt.service;

import cn.hutool.http.HttpRequest;
import com.jiakao.ydt.config.IntegrationProperties;
import com.jiakao.ydt.config.RedisCacheConfig;
import com.jiakao.ydt.dto.OfficialExamScheduleInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 对接交管 122 官方「考试能力/时间」公开页：统一下发 URL，并可选探测站点连通性。
 */
@Service
@RequiredArgsConstructor
public class OfficialExamScheduleService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IntegrationProperties integrationProperties;

    @Cacheable(cacheNames = RedisCacheConfig.OFFICIAL_EXAM_SCHEDULE_INFO, key = "'default'")
    public OfficialExamScheduleInfoDto getInfo() {
        String full = integrationProperties.getOfficialExamScheduleUrl();
        String base = stripHash(full);
        boolean ok = probe(base);
        return new OfficialExamScheduleInfoDto(
                "交管官方 · 考试能力公布",
                "以下链接跳转湖北省交通安全综合服务管理平台公开查询页，考试计划与能力以官方实时数据为准。",
                full,
                base,
                ok,
                LocalDateTime.now().format(TS)
        );
    }

    private static String stripHash(String url) {
        int i = url.indexOf('#');
        return i > 0 ? url.substring(0, i) : url;
    }

    private boolean probe(String baseUrl) {
        try {
            int status = HttpRequest.get(baseUrl)
                    .timeout(8_000)
                    .execute()
                    .getStatus();
            return status >= 200 && status < 400;
        } catch (Exception e) {
            return false;
        }
    }
}
