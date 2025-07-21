package zengcode.config.common.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class ConfigRequest {
    private String service;
    private String env;
    private String version;
    private Map<String, Object> configs;
}
