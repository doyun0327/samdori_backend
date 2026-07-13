package com.consult.reservation.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Firebase Admin SDK 초기화
 * - 로컬: firebase.credentials-path (파일)
 * - Railway: FIREBASE_CREDENTIALS_JSON (환경변수 JSON 문자열)
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream stream = openCredentialsStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK 초기화 완료");
        } catch (IOException ex) {
            log.error("Firebase Admin SDK 초기화 실패", ex);
            throw ex;
        }
    }

    private InputStream openCredentialsStream() throws IOException {
        if (StringUtils.hasText(credentialsJson)) {
            log.info("Firebase credentials: 환경변수 JSON 사용");
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(credentialsPath)) {
            Resource resource = resourceLoader.getResource(credentialsPath);
            if (resource.exists()) {
                log.info("Firebase credentials: 파일 사용 ({})", resource.getFilename());
                return resource.getInputStream();
            }
        }
        throw new IllegalStateException(
                "Firebase credentials가 없습니다. FIREBASE_CREDENTIALS_JSON 또는 firebase.credentials-path를 설정하세요.");
    }
}
