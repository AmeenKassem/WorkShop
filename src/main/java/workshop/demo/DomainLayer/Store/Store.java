package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workshop.demo.DomainLayer.Stock.Product;

public class Store {

    private int stroeID;
    private String storeName;
    private String category;
    private boolean active;
    private Map<Product.Category, List<item>> stock;//map of category -> item
    //must add something for messages

    public Store(int storeID, String storeName, String category) {
        this.stroeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        stock = new HashMap<>();
    }

    public int getStroeID() {
        return stroeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    // add product 
    // remove product -> quantity=0
    // update price
    // rank product 
    //display products in store
    // search product by name 
    //search product by category 

}
