package workshop.demo.DomainLayer.User;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;


public class User {
    
    @Id 
    private int id;
    @Transient
    private ShoppingCart cart = new ShoppingCart();

}
