package com.consult.reservation.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Railway Variables / 로컬 yml에서 DataSource를 직접 구성한다.
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean
    @Primary
    DataSource dataSource(Environment env) {
        String url = firstNonBlank(
                env.getProperty("SPRING_DATASOURCE_URL"),
                env.getProperty("spring.datasource.url"),
                env.getProperty("DATABASE_URL"),
                env.getProperty("JDBC_DATABASE_URL"));
        String username = firstNonBlank(
                env.getProperty("SPRING_DATASOURCE_USERNAME"),
                env.getProperty("spring.datasource.username"),
                env.getProperty("PGUSER"));
        String password = firstNonBlank(
                env.getProperty("SPRING_DATASOURCE_PASSWORD"),
                env.getProperty("spring.datasource.password"),
                env.getProperty("PGPASSWORD"));

        if (!StringUtils.hasText(url)) {
            throw new IllegalStateException(
                    "DB URL이 없습니다. Railway → Service → Variables 에 "
                            + "SPRING_DATASOURCE_URL 을 추가하세요. "
                            + "예: jdbc:postgresql://ep-xxxx.neon.tech/neondb?sslmode=require");
        }

        url = normalizeJdbcUrl(url);

        boolean credentialsInUrl = url.contains("@")
                && url.indexOf('@') > url.indexOf("://") + 3;
        if (!credentialsInUrl && (!StringUtils.hasText(username) || !StringUtils.hasText(password))) {
            throw new IllegalStateException(
                    "DB 계정 정보가 없습니다. "
                            + "SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD 를 설정하세요.");
        }

        log.info("[DataSource] url={}, username={}", maskUrl(url),
                StringUtils.hasText(username) ? username : "(in-url)");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (StringUtils.hasText(username)) {
            config.setUsername(username);
        }
        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30_000);
        config.setInitializationFailTimeout(10_000);

        try {
            return new HikariDataSource(config);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "DB 연결 실패. Neon suspend 여부 / URL / 비밀번호를 확인하세요. "
                            + "url=" + maskUrl(url) + " cause=" + ex.getMessage(),
                    ex);
        }
    }

    static String normalizeJdbcUrl(String raw) {
        String url = raw.trim();
        if ((url.startsWith("\"") && url.endsWith("\""))
                || (url.startsWith("'") && url.endsWith("'"))) {
            url = url.substring(1, url.length() - 1).trim();
        }
        if (url.startsWith("postgres://")) {
            url = "jdbc:postgresql://" + url.substring("postgres://".length());
        } else if (url.startsWith("postgresql://")) {
            url = "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        if (!url.startsWith("jdbc:")) {
            throw new IllegalStateException(
                    "DB URL은 jdbc:postgresql:// 형식이어야 합니다. 현재=" + maskUrl(url));
        }
        if (!url.contains("sslmode=")) {
            url += (url.contains("?") ? "&" : "?") + "sslmode=require";
        }
        return url;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static String maskUrl(String url) {
        int at = url.indexOf('@');
        if (at > 0) {
            int scheme = url.indexOf("://");
            String prefix = scheme > 0 ? url.substring(0, scheme + 3) : "";
            return prefix + "***@" + url.substring(at + 1);
        }
        return url.length() > 64 ? url.substring(0, 64) + "..." : url;
    }
}
