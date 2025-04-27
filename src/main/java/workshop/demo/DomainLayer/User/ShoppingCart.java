package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

public class ShoppingCart {

    private HashMap<Integer,ShoppingBasket> storeBaskets;

    
    public List<CartItem> getAllCart(){
        return null;
    }

    

    public void addItem(int storeId, CartItem item) {
        if(!storeBaskets.containsKey(storeId))
            storeBaskets.put(storeId,new ShoppingBasket(storeId));
        storeBaskets.get(storeId).addItem(item);
        
    }



    public Object getBaskets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBaskets'");
    }
}
