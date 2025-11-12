package com.isaactai.cloudnativeweb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProps(String region, SnsProps sns) {
    public record SnsProps(String topicArn) {};
}
