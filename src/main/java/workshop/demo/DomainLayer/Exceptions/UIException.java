package workshop.demo.DomainLayer.Exceptions;

public class UIException extends Exception {
    private final int errorCode;

    public UIException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "Error " + errorCode + ": " + getMessage();
    }

    // }

    public int getNumber() {
        return this.errorCode;
    }

}
