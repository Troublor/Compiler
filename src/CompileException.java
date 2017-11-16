public class CompileException extends Exception{

    private String message;

    public CompileException( String message1) {
        this.message = message1;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
