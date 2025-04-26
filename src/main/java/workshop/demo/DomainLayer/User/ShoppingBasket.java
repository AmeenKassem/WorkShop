package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

public class ShoppingBasket {

    private List<CartItem> itemsOnCart=new ArrayList<>();
    
    private int storeId ;

    public ShoppingBasket(int id){
        storeId= id;
    }

    public void addItem(CartItem item) {
        itemsOnCart.add(item);
    }


}
