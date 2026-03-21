package com.health.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Spring Boot 应用启动类
 */
@SpringBootApplication
//@ComponentScan(basePackages = {"com.health.assessment"})
//@MapperScan(basePackages = {"com.health.assessment.mapper"})
public class HealthAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthAssessmentApplication.class, args);
    }

    /**
     * 配置 CORS
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}

