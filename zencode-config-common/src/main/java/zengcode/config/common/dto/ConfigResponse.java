package zengcode.config.common.dto;

import java.util.Map;

public record ConfigResponse(
        String service,
        String env,
        String version,
        Map<String, Object> configs
) {}
