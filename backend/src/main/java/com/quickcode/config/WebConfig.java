package com.quickcode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源访问路径
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-path:/uploads}")
    private String uploadPath;

    /**
     * 配置静态资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        String resourceLocation;
        if (uploadPath.startsWith("/")) {
            // 绝对路径
            resourceLocation = "file:" + uploadPath + "/";
        } else {
            // 相对路径，转换为绝对路径
            resourceLocation = "file:" + System.getProperty("user.dir") + "/" + uploadPath + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
