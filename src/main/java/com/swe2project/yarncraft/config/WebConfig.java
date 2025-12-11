package com.swe2project.yarncraft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins(
                        "http://localhost:3000", // React, Next.js
                        "http://localhost:5173", // Vite (React, Vue, Svelte)
                        "http://localhost:4200", // Angular
                        "http://localhost:8080", // Vue CLI
                        "http://localhost:8081"  // Alternative port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}