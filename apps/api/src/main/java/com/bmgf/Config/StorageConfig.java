package com.bmgf.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.concurrent.TimeUnit;

// 完全替代原WebConfig的最终版StorageConfig
@Configuration
public class StorageConfig implements WebMvcConfigurer {
    @Value("${storage.media.video.location}")
    private String videoPath;
    @Value("${storage.media.avatar.location}")
    private String avatarPath;
    @Value("${storage.media.post.location}")
    private String postPath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 视频资源（增强配置）
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + videoPath + "/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver());
        // 头像资源（增强配置）
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:" + avatarPath + "/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        // 头像资源（增强配置）
        registry.addResourceHandler("/posts/**")
                .addResourceLocations("file:" + postPath + "/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
        // 兼容旧配置（如有必要）
        registry.addResourceHandler("/legacy/avatars/**")
                .addResourceLocations("file:" + avatarPath + "/");
    }
}