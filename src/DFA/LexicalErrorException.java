package DFA;

public class LexicalErrorException extends Exception{
    private String message;

    public LexicalErrorException(String s) {
        message = s;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
