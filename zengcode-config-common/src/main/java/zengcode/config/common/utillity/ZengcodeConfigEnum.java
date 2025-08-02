package zengcode.config.common.utillity;

public enum ZengcodeConfigEnum {
    CONFIGS("configs"),
    SERVICE("service"),
    ENV("env"),
    VERSION("version"),
    KEY("key"),
    VALUE("value"),
    UPDATED_BY("updatedBy"),
    LAST_UPDATED("lastUpdated"),
    COLLECTION_NAME("collectionName");

    private final String value;

    ZengcodeConfigEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
