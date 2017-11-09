/**
 * 非法标号/非法字符异常
 */
public class InvalidLabelException extends Exception{

    private String message;

    InvalidLabelException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
