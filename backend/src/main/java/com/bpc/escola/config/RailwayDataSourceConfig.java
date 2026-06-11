package com.bpc.escola.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Monta o DataSource para Docker/Railway.
 * Prioriza DATABASE_URL (referência única do Postgres na Railway) sobre PG* avulsas.
 */
@Configuration
@Profile("docker")
public class RailwayDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String databaseUrl = firstNonBlank(
                env.getProperty("DATABASE_URL"),
                env.getProperty("DATABASE_PRIVATE_URL"));
        String pgHost = firstNonBlank(env.getProperty("PGHOST"));
        String sslMode = firstNonBlank(env.getProperty("POSTGRES_SSLMODE"), "require");

        HikariConfig config = new HikariConfig();

        if (databaseUrl != null) {
            applyDatabaseUrl(config, databaseUrl, sslMode);
        } else if (pgHost != null) {
            config.setJdbcUrl(String.format(
                    "jdbc:postgresql://%s:%s/%s?sslmode=%s",
                    pgHost,
                    firstNonBlank(env.getProperty("PGPORT"), "5432"),
                    firstNonBlank(env.getProperty("PGDATABASE"), "railway"),
                    sslMode));
            config.setUsername(firstNonBlank(env.getProperty("PGUSER"), "postgres"));
            config.setPassword(env.getProperty("PGPASSWORD", ""));
        } else {
            config.setJdbcUrl("jdbc:postgresql://postgres:5432/escola_remo?sslmode=disable");
            config.setUsername("escola");
            config.setPassword("escola123");
        }

        return new HikariDataSource(config);
    }

    private static void applyDatabaseUrl(HikariConfig config, String databaseUrl, String sslMode) {
        String normalized = databaseUrl
                .replaceFirst("^postgresql://", "")
                .replaceFirst("^postgres://", "");
        int at = normalized.lastIndexOf('@');
        if (at < 0) {
            throw new IllegalStateException("DATABASE_URL inválida: " + databaseUrl);
        }

        String userInfo = normalized.substring(0, at);
        String hostAndDb = normalized.substring(at + 1);
        String[] credentials = userInfo.split(":", 2);

        int slash = hostAndDb.indexOf('/');
        String hostPort = slash >= 0 ? hostAndDb.substring(0, slash) : hostAndDb;
        String database = slash >= 0 ? hostAndDb.substring(slash + 1) : "railway";
        int query = database.indexOf('?');
        if (query >= 0) {
            database = database.substring(0, query);
        }

        int colon = hostPort.lastIndexOf(':');
        String host = colon >= 0 ? hostPort.substring(0, colon) : hostPort;
        String port = colon >= 0 ? hostPort.substring(colon + 1) : "5432";

        config.setJdbcUrl(String.format(
                "jdbc:postgresql://%s:%s/%s?sslmode=%s", host, port, database, sslMode));
        config.setUsername(decode(credentials[0]));
        config.setPassword(credentials.length > 1 ? decode(credentials[1]) : "");
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
