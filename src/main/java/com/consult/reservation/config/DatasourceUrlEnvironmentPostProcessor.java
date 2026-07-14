package com.consult.reservation.config;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Railway DATABASE_URL / SPRING_DATASOURCE_URL 이
 * postgresql://... (jdbc 없음) 형태로 yml을 덮어쓰는 것을 막고,
 * application.yml 의 JDBC 설정을 최우선으로 고정한다.
 */
public class DatasourceUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String URL =
            "jdbc:postgresql://ep-long-morning-aowm9402-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&tcpKeepAlive=true";
    private static final String USERNAME = "neondb_owner";
    private static final String PASSWORD = "npg_LsPZkHzW7Eu9";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addFirst(new MapPropertySource(
                "datasource-yml-override",
                Map.of(
                        "spring.datasource.url", URL,
                        "spring.datasource.username", USERNAME,
                        "spring.datasource.password", PASSWORD,
                        "spring.datasource.driver-class-name", "org.postgresql.Driver"
                )
        ));
    }
}
