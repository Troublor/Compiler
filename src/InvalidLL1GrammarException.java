public class InvalidLL1GrammarException extends Exception{
    private String message;
    InvalidLL1GrammarException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
