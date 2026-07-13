package com.consult.reservation.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

/**
 * Railway/Neon 이 postgresql:// 형태로 넣는 URL을 jdbc:postgresql:// 로 보정한다.
 */
public class DatasourceUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("spring.datasource.url"),
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("JDBC_DATABASE_URL"));
        if (!StringUtils.hasText(raw)) {
            return;
        }

        String normalized = normalizeJdbcUrl(raw.trim());
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.url", normalized);
        map.put("SPRING_DATASOURCE_URL", normalized);

        // URL에 user:pass@ 가 있으면 username/password도 분리 추출
        ParsedCredentials credentials = extractCredentials(normalized);
        if (credentials != null) {
            if (!StringUtils.hasText(environment.getProperty("spring.datasource.username"))
                    && !StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_USERNAME"))) {
                map.put("spring.datasource.username", credentials.username());
                map.put("SPRING_DATASOURCE_USERNAME", credentials.username());
            }
            if (!StringUtils.hasText(environment.getProperty("spring.datasource.password"))
                    && !StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_PASSWORD"))) {
                map.put("spring.datasource.password", credentials.password());
                map.put("SPRING_DATASOURCE_PASSWORD", credentials.password());
            }
            // credentials 제거한 URL로 교체
            map.put("spring.datasource.url", credentials.urlWithoutCredentials());
            map.put("SPRING_DATASOURCE_URL", credentials.urlWithoutCredentials());
        }

        environment.getPropertySources()
                .addFirst(new MapPropertySource("normalizedDatasourceUrl", map));
    }

    static String normalizeJdbcUrl(String raw) {
        String url = raw;
        if ((url.startsWith("\"") && url.endsWith("\""))
                || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length() - 1).trim();
        }
        if (url.startsWith("postgres://")) {
            url = "jdbc:postgresql://" + url.substring("postgres://".length());
        } else if (url.startsWith("postgresql://")) {
            url = "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        if (!url.contains("sslmode=")) {
            url += (url.contains("?") ? "&" : "?") + "sslmode=require";
        }
        return url;
    }

    private static ParsedCredentials extractCredentials(String jdbcUrl) {
        // jdbc:postgresql://user:pass@host/db?...
        String prefix = "jdbc:postgresql://";
        if (!jdbcUrl.startsWith(prefix)) {
            return null;
        }
        String rest = jdbcUrl.substring(prefix.length());
        int at = rest.indexOf('@');
        int colon = rest.indexOf(':');
        if (at <= 0 || colon <= 0 || colon >= at) {
            return null;
        }
        String username = rest.substring(0, colon);
        String password = rest.substring(colon + 1, at);
        String hostAndRest = rest.substring(at + 1);
        return new ParsedCredentials(
                username,
                password,
                prefix + hostAndRest);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private record ParsedCredentials(String username, String password, String urlWithoutCredentials) {
    }
}
