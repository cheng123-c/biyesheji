package com.health.assessment.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 *
 * 说明：
 * - MyBatis 配置已完全迁移到 application.yml
 * - 此类仅用于 Mapper 接口扫描
 * - 其他配置由 Spring Boot 自动配置处理
 */
@Configuration
@MapperScan("com.health.assessment.mapper")
public class MyBatisConfiguration {

}

