package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ItemCartDTO;

public class ShoppingBasket {

    private List<ItemCartDTO> itemsOnCart=new ArrayList<>();
    
    private int storeId ;

    public ShoppingBasket(int id){
        storeId= id;
    }

    public void addItem(ItemCartDTO item) {
        itemsOnCart.add(item);
    }

    public List<ItemCartDTO> getItems() {
        return itemsOnCart;
    }

    public int getStoreId() {
        return storeId;
    }

}
