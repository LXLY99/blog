package org.lxly.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // ✅ 只保留这个：让根路径直接返回 index.html
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    // 🗑️ 已彻底删除 addCorsMappings 方法
    // 原因：CORS 已在 SecurityConfig 中全局配置。此处保留会导致 allowCredentials 和 allowedOrigins(*) 冲突报错。
}