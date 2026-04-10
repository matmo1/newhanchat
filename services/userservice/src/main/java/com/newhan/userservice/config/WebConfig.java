package com.newhan.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:/app/users}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // When a request hits /api/users/media/some_image.jpg...
        registry.addResourceHandler("/api/users/media/**")
                // ...look for it in the physical directory we mapped in Docker
                .addResourceLocations(
                    "file:" + uploadDir + "/"
                );
    }
}