package com.consult.reservation.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/** C:/FirebaseSDK 의 서비스 계정 JSON으로 Firebase Admin SDK 초기화 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private Resource credentialsPath;

    @PostConstruct
    void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream stream = credentialsPath.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK 초기화 완료: {}", credentialsPath.getFilename());
        } catch (IOException ex) {
            log.error("Firebase Admin SDK 초기화 실패. credentials-path={}", credentialsPath, ex);
            throw ex;
        }
    }
}
