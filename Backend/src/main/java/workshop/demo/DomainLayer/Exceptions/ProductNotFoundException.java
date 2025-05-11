package workshop.demo.DomainLayer.Exceptions;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
}
