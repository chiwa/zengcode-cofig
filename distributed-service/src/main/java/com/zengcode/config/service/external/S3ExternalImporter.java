package com.zengcode.config.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zengcode.config.configuration.AwsS3Properties;
import com.zengcode.config.service.IConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import zengcode.config.common.dto.ConfigRequest;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class S3ExternalImporter extends AExternalImporter {

    private final AwsS3Properties awsS3Properties;
    private final S3Client s3Client;
    private static final ObjectMapper mapper = new ObjectMapper();

    public S3ExternalImporter(IConfiguration configurationService,
                              S3Client s3Client,
                              AwsS3Properties awsS3Properties) {
        super(configurationService);
        this.s3Client = s3Client;
        this.awsS3Properties = awsS3Properties;
    }

    @Override
    ConfigRequest readFromSource() {
        String bucketName = awsS3Properties.getBucket().getName();
        String incomingPrefix = awsS3Properties.getBucket().getFolder().getIncoming();

        ListObjectsV2Response listResp = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(incomingPrefix)
                .build());

        for (S3Object object : listResp.contents()) {
            String key = object.key();
            if (key.endsWith(".json")) {
                log.info("📦 Found config file: {}", key);

                try {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

                    ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
                    String jsonContent = new String(s3ObjectStream.readAllBytes(), StandardCharsets.UTF_8);

                    log.info("📄 Content of [{}]: {}", key, jsonContent);

                    ConfigRequest data = mapper.readValue(jsonContent, ConfigRequest.class);

                    if (!isFilenameMatchConfig(key, data)) {
                        moveFile(key, awsS3Properties.getBucket().getFolder().getError());
                        continue;
                    }

                    log.info("✅ Validated config: [{}]", key);
                    moveFile(key, awsS3Properties.getBucket().getFolder().getCompleted());
                    return data;
                } catch (Exception ex) {
                    log.error("❌ Failed to process [{}]: {}", key, ex.getMessage(), ex);
                    moveFile(key, awsS3Properties.getBucket().getFolder().getError());
                }
            }
        }

        log.warn("⚠️ No config .json file found in prefix [{}]", incomingPrefix);
        return null;
    }

    private void moveFile(String key, String destinationFolder) {
        String bucketName = awsS3Properties.getBucket().getName();
        String incomingPrefix = awsS3Properties.getBucket().getFolder().getIncoming();
        String fileName = key.substring(incomingPrefix.length());
        String targetKey = destinationFolder + fileName;

        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(key)
                .destinationBucket(bucketName)
                .destinationKey(targetKey)
                .build());

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        log.info("📤 Moved file [{}] → [{}]", key, targetKey);
    }

    private boolean isFilenameMatchConfig(String key, ConfigRequest data) {
        try {
            String incomingPrefix = awsS3Properties.getBucket().getFolder().getIncoming();
            String filename = key.substring(incomingPrefix.length(), key.length() - ".json".length());
            String[] parts = filename.split("_");

            if (parts.length < 3) {
                log.warn("⚠️ Invalid filename format: {}", filename);
                return false;
            }

            boolean match = parts[0].equals(data.getService()) &&
                    parts[1].equals(data.getEnv()) &&
                    parts[2].equals(data.getVersion());

            if (!match) {
                log.warn("❌ Mismatch in [{}]: filename={}, payload={}", key, filename, data);
            }

            return match;
        } catch (Exception ex) {
            log.error("❌ Error validating [{}]: {}", key, ex.getMessage(), ex);
            return false;
        }
    }
}