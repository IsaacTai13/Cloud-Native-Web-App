package com.isaactai.cloudnativeweb.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.function.Supplier;

/**
 * @author tisaac
 */
@Component
@RequiredArgsConstructor
public class TimedS3 {
    private final S3Client s3;
    private final MeterRegistry reg;

    private <T> T timeS3Call(String metric, String bucket, Supplier<T> call) {
        Timer.Sample sample = Timer.start(reg);
        try {
            return call.get();
        } finally {
            sample.stop(Timer.builder(metric)
                    .tag("bucket", bucket)
                    .register(reg));
        }
    }

    public PutObjectResponse putObject(PutObjectRequest req, RequestBody body) {
        return timeS3Call("s3.put.time", req.bucket(), () -> s3.putObject(req, body));
    }

    public DeleteObjectResponse deleteObject(DeleteObjectRequest req) {
        return timeS3Call("s3.delete.time", req.bucket(), () -> s3.deleteObject(req));
    }

    public GetObjectResponse getObject(GetObjectRequest req, java.nio.file.Path dest) {
        return timeS3Call("s3.get.time", req.bucket(), () -> s3.getObject(req, dest));
    }
}
