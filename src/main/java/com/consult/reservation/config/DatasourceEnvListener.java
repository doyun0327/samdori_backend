package com.consult.reservation.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * 기동 초기에 DB 관련 환경변수 존재 여부를 로그로 남긴다.
 * Railway Variables 누락을 바로 확인할 수 있다.
 */
@Slf4j
public class DatasourceEnvListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();
        String url = first(env,
                "SPRING_DATASOURCE_URL",
                "spring.datasource.url",
                "DATABASE_URL",
                "JDBC_DATABASE_URL");
        String user = first(env,
                "SPRING_DATASOURCE_USERNAME",
                "spring.datasource.username");

        log.info("========== DB ENV CHECK ==========");
        log.info("SPRING_DATASOURCE_URL set? {}", StringUtils.hasText(url));
        log.info("SPRING_DATASOURCE_USERNAME set? {}", StringUtils.hasText(user));
        if (StringUtils.hasText(url)) {
            log.info("URL prefix: {}", url.length() > 40 ? url.substring(0, 40) + "..." : url);
        } else {
            log.error("DB URL 없음! Railway Variables에 SPRING_DATASOURCE_URL 을 넣으세요.");
            logPropertyNamesContaining(env, "DATASOURCE", "DATABASE", "JDBC", "POSTGRES");
        }
        log.info("=================================");
    }

    private void logPropertyNamesContaining(ConfigurableEnvironment env, String... needles) {
        for (PropertySource<?> ps : env.getPropertySources()) {
            if (!(ps instanceof EnumerablePropertySource<?> eps)) {
                continue;
            }
            for (String name : eps.getPropertyNames()) {
                String upper = name.toUpperCase();
                for (String needle : needles) {
                    if (upper.contains(needle)) {
                        log.warn("found related property: {} (source={})", name, ps.getName());
                        break;
                    }
                }
            }
        }
    }

    private String first(ConfigurableEnvironment env, String... keys) {
        for (String key : keys) {
            String value = env.getProperty(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
