public class GrammarException extends Exception {
    private String message;

    public GrammarException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
