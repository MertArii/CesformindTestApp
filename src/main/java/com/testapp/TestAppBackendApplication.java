package com.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TestAppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestAppBackendApplication.class, args);
    }
}
