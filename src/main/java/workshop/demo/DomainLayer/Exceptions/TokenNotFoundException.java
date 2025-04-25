package workshop.demo.DomainLayer.Exceptions;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException() {
        super("Token is not valid!");
    }
}
