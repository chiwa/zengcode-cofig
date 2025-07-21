package zengcode.config.common.exception;

public class ResolveConfigurationException extends RuntimeException {

    public ResolveConfigurationException() {
        super();
    }

    public ResolveConfigurationException(String message) {
        super(message);
    }

    public ResolveConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResolveConfigurationException(Throwable cause) {
        super(cause);
    }
}
