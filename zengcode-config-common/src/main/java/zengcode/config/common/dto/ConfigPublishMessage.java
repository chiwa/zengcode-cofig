package zengcode.config.common.dto;


import zengcode.config.common.utillity.PublishMessageOperation;

public record ConfigPublishMessage(
        String collection,
        PublishMessageOperation operation,
        String key,
        Object value,
        String lastSnapshotId
) {}
