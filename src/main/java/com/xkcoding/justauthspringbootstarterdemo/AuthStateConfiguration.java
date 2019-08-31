package com.xkcoding.justauthspringbootstarterdemo;

import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * 自定义缓存装配
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/8/31 12:29
 */
// @Configuration
public class AuthStateConfiguration {
    @Bean
    public AuthStateCache authStateCache() {
        return new MyAuthStateCache();
    }
}
