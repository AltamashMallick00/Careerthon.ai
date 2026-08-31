package com.careerthon.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Universal Database Configuration.
 * Automatically adapts between Cloud PostgreSQL (Render / Supabase / Neon / Railway)
 * and Local Persistent H2 disk database with zero manual configuration.
 */
@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${spring.datasource.url:jdbc:h2:file:./data/careerthon;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1}")
    private String defaultH2Url;

    @Value("${spring.datasource.username:sa}")
    private String defaultUsername;

    @Value("${spring.datasource.password:password}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // 1. Detect Cloud PostgreSQL (Render / Supabase / Neon / Railway)
        if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
            String cleanUrl = databaseUrl.trim();

            if (cleanUrl.startsWith("jdbc:postgresql://") || cleanUrl.startsWith("jdbc:postgres://")) {
                config.setJdbcUrl(cleanUrl);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setConnectionTimeout(30000);
                System.out.println("🐘 Connected to Cloud PostgreSQL Database via direct JDBC URL");
                return new HikariDataSource(config);
            }

            if (cleanUrl.startsWith("postgres://") || cleanUrl.startsWith("postgresql://")) {
                try {
                    // Parse standard URL e.g. postgres://user:password@host:port/dbname
                    if (cleanUrl.startsWith("postgres://")) {
                        cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
                    }
                    
                    URI uri = new URI(cleanUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath(); // /dbname
                    String dbName = (path != null && path.length() > 1) ? path.substring(1) : "careerthon";

                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                    if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                        jdbcUrl += "?" + uri.getQuery();
                    } else if (!jdbcUrl.contains("sslmode")) {
                        jdbcUrl += "?sslmode=prefer";
                    }

                    config.setJdbcUrl(jdbcUrl);
                    config.setDriverClassName("org.postgresql.Driver");

                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        config.setUsername(parts[0]);
                        config.setPassword(parts[1]);
                    }

                    config.setMaximumPoolSize(5);
                    config.setMinimumIdle(1);
                    config.setConnectionTimeout(30000);
                    config.setIdleTimeout(600000);
                    config.setMaxLifetime(1800000);
                    System.out.println("🐘 Connected to Cloud PostgreSQL Database: " + host + "/" + dbName);
                    return new HikariDataSource(config);
                } catch (Exception e) {
                    System.err.println("⚠️ Could not parse PostgreSQL DATABASE_URL, falling back to default datasource: " + e.getMessage());
                }
            }
        }

        // 2. Fallback to Persistent H2 File on Disk
        config.setJdbcUrl(defaultH2Url);
        config.setDriverClassName("org.h2.Driver");
        config.setUsername(defaultUsername);
        config.setPassword(defaultPassword);
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }
}
