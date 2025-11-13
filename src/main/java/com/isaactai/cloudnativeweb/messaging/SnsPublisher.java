package com.isaactai.cloudnativeweb.messaging;

import com.isaactai.cloudnativeweb.config.AwsProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class SnsPublisher {
    private final SnsClient snsClient;
    private final AwsProps awsProps;

    public void publishEmailVerification(String email, String token) {
        String payload = """
                {
                    "type": "EMAIL_VERIFICATION",
                    "email": "%s",
                    "token": "%s"
                }
                """.formatted(email, token);

        PublishRequest req = PublishRequest.builder()
                        .topicArn(awsProps.sns().topicArn())
                        .message(payload)
                        .build();

        snsClient.publish(req);
    }
}
