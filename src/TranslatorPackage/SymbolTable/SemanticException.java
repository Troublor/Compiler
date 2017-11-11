package TranslatorPackage.SymbolTable;

public class SemanticException extends Exception {
    String message;

    public SemanticException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
