package com.zengcode.config.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    private String region;
    private String accessKey;
    private String secretKey;
    private Bucket bucket;

    @Data
    public static class Bucket {
        private String name;
        private Folder folder;

        @Data
        public static class Folder {
            private String incoming;
            private String completed;
            private String error;
        }
    }
}