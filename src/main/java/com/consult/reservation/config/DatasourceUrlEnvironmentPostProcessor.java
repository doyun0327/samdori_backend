package com.consult.reservation.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Railway 등에서 datasource 환경변수가 비어 있거나
 * postgresql:// (jdbc: 없음) 로 들어오면 yml 설정을 망가뜨리는 경우를 보정한다.
 */
public class DatasourceUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String URL =
            "jdbc:postgresql://ep-long-morning-aowm9402-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USERNAME = "neondb_owner";
    private static final String PASSWORD = "npg_LsPZkHzW7Eu9";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> fixes = new HashMap<>();

        String url = environment.getProperty("spring.datasource.url");
        if (!StringUtils.hasText(url)) {
            fixes.put("spring.datasource.url", URL);
        } else if (url.startsWith("postgresql://")) {
            fixes.put("spring.datasource.url", "jdbc:" + url);
        }

        if (!StringUtils.hasText(environment.getProperty("spring.datasource.username"))) {
            fixes.put("spring.datasource.username", USERNAME);
        }
        if (!StringUtils.hasText(environment.getProperty("spring.datasource.password"))) {
            fixes.put("spring.datasource.password", PASSWORD);
        }

        if (!fixes.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("datasource-url-fix", fixes));
        }
    }
}
