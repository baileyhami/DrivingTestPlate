package com.jiakao.ydt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 兼容已存在库：若无 route_path_json 列则自动补齐，避免考场线路接口 SQL 报错（500）。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class ExamRouteSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer n = jdbcTemplate.queryForObject(
                    """
                            SELECT COUNT(*) FROM information_schema.COLUMNS
                            WHERE TABLE_SCHEMA = DATABASE()
                              AND TABLE_NAME = 'biz_exam_route'
                              AND COLUMN_NAME = 'route_path_json'
                            """,
                    Integer.class);
            if (n != null && n > 0) {
                return;
            }
            jdbcTemplate.execute(
                    """
                            ALTER TABLE biz_exam_route
                            ADD COLUMN route_path_json text NULL COMMENT '线路轨迹JSON [[lng,lat],...]' AFTER map_image_url
                            """);
            log.info("已为 biz_exam_route 表添加 route_path_json 列");
        } catch (Exception e) {
            log.warn("检查或添加 route_path_json 列时出现问题（若已手动执行迁移可忽略）: {}", e.getMessage());
        }
    }
}
