package com.portfolio.infrastructure.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Automatically repair checksum differences between database history and local migration files
            flyway.repair();
            flyway.migrate();
        };
    }
}
