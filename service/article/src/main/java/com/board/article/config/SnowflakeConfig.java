package com.board.article.config;

import com.board.common.snowflake.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * snowflake를 build.gradle로 가져왔지만 해당 에러 발생
 * Consider defining a bean of type 'com.board.common.snowflake.Snowflake' in your configuration.
 *
 * Bean이 자동으로 등록되지 않아 직접 Bean을 주입 했습니다. (new 방식은 SOLID 관점에서 사용하지 않습니다.)
 */

@Configuration
public class SnowflakeConfig {
    @Bean
    public Snowflake snowflake() {
        return new Snowflake();
    }
}
