package zengcode.config.common.utillity;

public enum PublishMessageOperation {
    INITIAL("INITIAL"),
    NEW("NEW"),
    UPDATED("UPDATED"),
    DELETED("DELETED");

    private final String value;

    PublishMessageOperation(String value) {
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
