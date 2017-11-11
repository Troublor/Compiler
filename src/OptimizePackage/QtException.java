package OptimizePackage;

public class QtException extends Exception {
    private String message;

    public QtException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
