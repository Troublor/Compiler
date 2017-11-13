package TranslatorPackage.TranslatorExceptions;


public class TypeError extends Exception {
    String message;

    public TypeError(String id, String type, String supposed_type) {
        message = "supposed to be " + supposed_type + " but id:<" + id + "> is " + type ;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
