package com.isaactai.cloudnativeweb.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * @author tisaac
 */
@Configuration
@Profile("!ci")
@EnableConfigurationProperties(AwsProps.class)
public class AwsConfig {

    @Bean
    public SnsClient snsClient(AwsProps props) {
        return SnsClient.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
