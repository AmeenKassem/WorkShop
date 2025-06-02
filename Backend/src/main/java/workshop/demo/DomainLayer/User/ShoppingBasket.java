package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

public class ShoppingBasket {

    //private List<ItemCartDTO> itemsOnCart=new ArrayList<>();
    private List<CartItem> itemsOnCart = new ArrayList<>();
    private int storeId;

    public ShoppingBasket(int id) {
        storeId = id;
    }

    public void addItem(CartItem item) {
        itemsOnCart.add(item);
    }

    public List<CartItem> getItems() {
        return itemsOnCart;
    }

    public int getStoreId() {
        return storeId;
    }

    public void ModifyCartAddQToBuy(int productId, int quantity) {
        for (CartItem item : itemsOnCart) {
            if (item.productId == productId) {
                item.quantity = quantity;
                return;
            }
        }
    }

}
