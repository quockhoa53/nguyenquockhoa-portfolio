package com.portfolio.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "portfolio_profile",
                "portfolio_skills",
                "portfolio_experiences",
                "portfolio_projects",
                "portfolio_project_detail",
                "work_items",
                "work_item_detail",
                "knowledge_categories",
                "knowledge_articles",
                "knowledge_article_detail");
    }
}
