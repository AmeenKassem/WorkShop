package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    public item getProductById(int id) {
        for (List<item> items : stock.values()) {
            for (item item : items) {
                if (item.getProdutId() == id) {
                    return item;
                }
            }
        }
        return null; // not found
    }

    // add product  -> in repo: 1.getProductById if->not excist 2. add MainStock 3. calling this func:
    public void addItem(item newItem) {
        List<item> items;
        synchronized (stock) {
            items = stock.get(newItem.getCategory());
            if (items == null) {
                items = new ArrayList<>();
                stock.put(newItem.getCategory(), items);
            }
        }

        synchronized (items) {
            for (item item : items) {
                if (item.getProdutId() == newItem.getProdutId()) {
                    item.setQuantity(item.getQuantity() + 1);
                    return;
                }
            }
            items.add(newItem);
        }
    }

    // remove product -> quantity=0
    public void removeItem(int itemId) {
        for (List<item> items : stock.values()) {
            synchronized (items) {
                Iterator<item> iterator = items.iterator();
                while (iterator.hasNext()) {
                    item item = iterator.next();
                    if (item.getProdutId() == itemId) {
                        item.setQuantity(0);
                        iterator.remove();
                        return;
                    }
                }
            }
        }
    }

    // update price
    public void updatePrice(int itemId, int newPrice) {
        for (List<item> items : stock.values()) {
            synchronized (items) {
                for (item item : items) {
                    if (item.getProdutId() == itemId) {
                        item.setPrice(newPrice);
                        return;
                    }
                }
            }
        }
    }

    // rank product 
    //display products in store
    // search product by name 
    //search product by category 
}
