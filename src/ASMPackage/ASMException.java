package ASMPackage;

public class ASMException extends Exception {
    String message;

    public ASMException(String msg) {
        message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
