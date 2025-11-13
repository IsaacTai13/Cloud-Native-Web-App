package com.isaactai.cloudnativeweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CloudNativeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudNativeWebApplication.class, args);
    }

}
