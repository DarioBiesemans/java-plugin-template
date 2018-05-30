package be.jstack.dummyplugin;

public class DummyPluginException extends Exception {
    public DummyPluginException(String message) {
        super(message);
    }

    public DummyPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
