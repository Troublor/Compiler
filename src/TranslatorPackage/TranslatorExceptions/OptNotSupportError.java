package TranslatorPackage.TranslatorExceptions;


public class OptNotSupportError extends Exception {
    String message;

    public OptNotSupportError(String type, String opt) {
        message = type + " do not support " + opt;
    }

    @Override
    public String getMessage() {
        return message;
    }
}