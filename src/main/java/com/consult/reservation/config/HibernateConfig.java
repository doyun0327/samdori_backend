package com.consult.reservation.config;

import java.util.Map;
import org.hibernate.dialect.PostgreSQLDialect;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Hibernate 7: Dialect를 코드로 강제해 JDBC metadata 없이도 기동 가능하게 함 */
@Configuration
public class HibernateConfig {

    @Bean
    HibernatePropertiesCustomizer postgresqlDialectCustomizer() {
        return (Map<String, Object> properties) -> {
            properties.put("hibernate.dialect", PostgreSQLDialect.class.getName());
            properties.put("hibernate.boot.allow_jdbc_metadata_access", Boolean.FALSE);
        };
    }
}
