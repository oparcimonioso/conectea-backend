package com.conectea.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.conectea.backend.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class ConecteaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConecteaApplication.class, args);
    }
}