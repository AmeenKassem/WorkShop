package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.CartItem;

@Entity
public class StoreStock {

    @Transient
    private final Map<Integer, item> stock = new ConcurrentHashMap<>();// productId, item

    @ElementCollection
    private List<item> items = new ArrayList<>();

    @Id
    private int storeID;
    // discounts for this store

    public StoreStock(int storeID) {
        // this.stock = new ConcurrentHashMap<>();
        this.storeID = storeID;
    }

    public StoreStock() {

    }

    @PostLoad
    private void populateStockMap() {
        System.out.println("load the stock!!");
        if (items != null) {
            for (item item : items) {
                item.setStoreId(storeID);
                stock.put(item.getProductId(), item);
            }
        }
        System.out.println("loaded stock!!");

    }

    // Fill items list before saving to DB
    @PrePersist
    @PreUpdate
    private void populateItemsFromStock() {
        items = new ArrayList<>();
        items.clear();
        items.addAll(stock.values());
    }

    // public item getProductById(int id) {
    // return stock.get(id);
    // }
    public List<item> getAllItemsInStock() {
        return new ArrayList<>(stock.values());
    }

    public void addItem(item newItem) {
        synchronized (this.stock) {
            item existingItem = stock.get(newItem.getProductId());
            if (existingItem != null) {
                existingItem.AddQuantity();
            } else {
                newItem.setStoreId(storeID);
                System.out.println("-----------------helo ");
                stock.put(newItem.getProductId(), newItem);
                items.add(newItem);
            }
        }

    }

    // remove product -> quantity=0
    public void removeItem(int itemId) throws UIException {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(0);
    }

    // update quantity
    public void changeQuantity(int itemId, int quantity) throws UIException {
        System.out.println("updating quantity!");
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(quantity);
        System.out.println("updated quantity!");

    }

    // decrase quantity to buy: -> check if I need synchronized the item???
    public boolean decreaseQuantitytoBuy(int itemId, int quantity) throws UIException {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            System.out.println("product " + itemId + " not found!");
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        synchronized (foundItem) {
            if (foundItem.getQuantity() < quantity) {
                return false;
            }
            foundItem.changeQuantity(foundItem.getQuantity() - quantity);
        }
        // foundItem.changeQuantity(foundItem.getQuantity() - quantity);
        return true;
    }

    public void returnProductToStock(int productId, int quantity) throws UIException {
        item storeItem = stock.get(productId);
        if (storeItem == null) {
            throw new UIException("Item not found with ID " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        storeItem.changeQuantity(storeItem.getQuantity() + quantity);
    }

    public void updatePrice(int itemId, int newPrice) throws UIException {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.setPrice(newPrice);
    }

    // rank product
    public void rankProduct(int productId, int newRank) throws UIException {
        item currenItem = getItemByProductId(productId);
        if (currenItem != null) {
            currenItem.rankItem(newRank);
        } else {
            throw new UIException("Product ID not found: " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
    }

    public Map<Integer, item> getStock() {
        return this.stock;
    }


    public void changequantity(List<ItemCartDTO> cartItems, boolean isGuest, String storeName) throws UIException {
        List<ReceiptProduct> boughtItems = new ArrayList<>();
        for (ItemCartDTO dto : cartItems) {
            CartItem item = new CartItem(dto);
            item storeItem = getItemByProductId(item.getProductId());

            if (storeItem == null || storeItem.getQuantity() < item.getQuantity()) {
                if (isGuest) {
                    throw new UIException("Insufficient stock during guest purchase.", ErrorCodes.INSUFFICIENT_STOCK);
                } else {
                    continue;
                }
            }
            decreaseQuantitytoBuy(item.getProductId(), item.getQuantity());
        }
    }

    // just for testing
    public List<item> getItemsByCategoryObject(Category category) {
        List<item> result = new ArrayList<>();
        for (item i : stock.values()) {
            if (i.getCategory().equals(category)) {
                result.add(i);
            }
        }
        return result;
    }

    // FOR STORE AND TESTING
    public item getItemByProductId(int productId) {
        for (item item : items) {
            if(item.getProductId()==productId) return item;
        }
        return stock.get(productId);
    }

    public int getStoreStockId() {
        return this.storeID;
    }

    @Transactional
    public void IncreaseQuantitytoBuy(int productId, int quantity) throws UIException {
        item foundItem = getItemByProductId(productId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        synchronized (foundItem) {

            foundItem.changeQuantity(foundItem.getQuantity() + quantity);
        }
    }

    public boolean isAvaliable(int productId, int quantity) {
        if (!stock.containsKey(productId)) {
            return false;
        }
        if (stock.get(productId).getQuantity() < quantity) {
            return false;
        }
        return true;
    }

    public void setStoreId(int storeId2) {
        this.storeID = storeId2;
    }

}
