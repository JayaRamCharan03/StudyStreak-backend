package com.studystreak.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String finalUrl = url;
        String finalUser = username;
        String finalPass = password;

        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = System.getenv("DB_URL");
        }
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = url;
        }

        // If running in cloud (Render sets PORT=10000) and no custom DB was configured (defaulted to localhost:3306)
        if (rawUrl != null && rawUrl.contains("localhost:3306") && System.getenv("PORT") != null) {
            rawUrl = "jdbc:postgresql://dpg-db5kdk59fdbs73d6fnng-a:5432/studystreak_yhri";
            finalUser = "studystreak_yhri_user";
            finalPass = "xNvwaTOXQeo1VnewAOXOy4lk9pt5J7Ed";
            log.info("Detected cloud environment with PORT={}. Defaulting to Render PostgreSQL database.", System.getenv("PORT"));
        }

        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(rawUrl.replace("postgresql://", "postgres://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    finalUser = parts[0];
                    finalPass = parts[1];
                }
                finalUrl = "jdbc:postgresql://" + host + ":" + port + path;
                log.info("Converted PostgreSQL URI to JDBC URL: jdbc:postgresql://{}:{}{}", host, port, path);
            } catch (Exception e) {
                log.warn("Failed to parse PostgreSQL URI: {}", e.getMessage());
            }
        } else {
            finalUrl = rawUrl;
        }

        if (System.getenv("DB_USER") != null && !System.getenv("DB_USER").isBlank()) {
            finalUser = System.getenv("DB_USER");
        }
        if (System.getenv("DB_PASSWORD") != null && !System.getenv("DB_PASSWORD").isBlank()) {
            finalPass = System.getenv("DB_PASSWORD");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(finalUrl);
        config.setUsername(finalUser);
        config.setPassword(finalPass);

        if (finalUrl != null && finalUrl.startsWith("jdbc:postgresql:")) {
            config.setDriverClassName("org.postgresql.Driver");
        } else if (finalUrl != null && finalUrl.startsWith("jdbc:mysql:")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }

        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        log.info("Initializing DataSource with URL: {}", finalUrl);
        return new HikariDataSource(config);
    }
}
