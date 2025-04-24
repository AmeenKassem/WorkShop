package workshop.demo.DomainLayer.User;

public class Guest {
    
    private int id;
    private ShoppingCart cart=new ShoppingCart();
    
    public Guest(int id2) {
        id = id2;
        
    }
    
    public int getId(){
        return id;
    }

    

    
}
