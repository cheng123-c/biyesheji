package com.health.assessment.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Jackson 全局配置
 *
 * 作用：
 * - 注册 JavaTimeModule，支持 LocalDateTime/LocalDate 序列化/反序列化
 * - 统一时间格式为可读字符串，避免前后端时间格式不兼容问题
 * - 反序列化时同时兼容带秒（yyyy-MM-dd HH:mm:ss）和不带秒（yyyy-MM-dd HH:mm）的格式
 */
@Configuration
public class JacksonConfig {

    /** 日期时间格式（序列化输出格式） */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 日期格式 */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 兼容多种 LocalDateTime 格式的反序列化器：
     *   - "yyyy-MM-dd HH:mm:ss"（带秒）
     *   - "yyyy-MM-dd HH:mm"  （不带秒，来自前端 datetime-local 控件）
     *   - "yyyy-MM-dd'T'HH:mm:ss"（ISO 格式带秒）
     *   - "yyyy-MM-dd'T'HH:mm"  （ISO 格式不带秒）
     */
    private static class FlexibleLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart().appendLiteral('T').optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .appendPattern("HH:mm")
                .optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalEnd()
                .toFormatter();

        FlexibleLocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String text = p.getText().trim();
            return LocalDateTime.parse(text, FORMATTER);
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 自定义 LocalDateTime 序列化（输出始终带秒）
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

        // 自定义 LocalDateTime 反序列化（兼容带秒/不带秒两种格式）
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        // 自定义 LocalDate 序列化/反序列化
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        mapper.registerModule(javaTimeModule);

        // 禁止将日期时间写为时间戳数组
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}

