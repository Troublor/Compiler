package TranslatorPackage.SymbolTable;

public class SemanticExcption extends Exception {
    String message;

    public SemanticExcption(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
