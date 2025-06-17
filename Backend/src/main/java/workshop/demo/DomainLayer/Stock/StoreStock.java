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
    private List<item> items;

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
                stock.put(item.getProductId(), item);
            }
        }
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
    public void decreaseQuantitytoBuy(int itemId, int quantity) throws UIException {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        synchronized (foundItem) {
            if (foundItem.getQuantity() < quantity) {
                throw new UIException("Insufficient stock", ErrorCodes.INSUFFICIENT_STOCK);
            }
            foundItem.changeQuantity(foundItem.getQuantity() - quantity);
        }
        // foundItem.changeQuantity(foundItem.getQuantity() - quantity);
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
            AtomicInteger[] ranks = currenItem.getRank();
            if (newRank >= 1 && newRank <= ranks.length) {
                ranks[newRank - 1].incrementAndGet(); // thread-safe increment
            } else {
                throw new UIException("Invalid rank index: " + newRank, ErrorCodes.INVALID_RANK);
            }
        } else {
            throw new UIException("Product ID not found: " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
    }

    public Map<Integer, item> getStock() {
        return this.stock;
    }

    // //display products in store
    // public List<ItemStoreDTO> getProductsInStore() {
    // List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
    // for (item i : stock.values()) {
    // // If item is mutable and accessed by multiple threads, synchronize on the
    // item
    // synchronized (i) {
    // ItemStoreDTO toAdd = new ItemStoreDTO(
    // i.getProductId(),
    // i.getQuantity(),
    // i.getPrice(),
    // i.getCategory(),
    // i.getFinalRank(),
    // storeID
    // );
    // itemStoreDTOList.add(toAdd);
    // }
    // }
    // return itemStoreDTOList;
    // }
    // may be changed later:
    public List<ReceiptProduct> ProcessCartItems(List<ItemCartDTO> cartItems, boolean isGuest, String storeName)
            throws UIException {
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
            boughtItems.add(new ReceiptProduct(
                    item.getName(),
                    storeName,
                    item.getQuantity(),
                    item.getPrice(),
                    item.productId,
                    item.category));
        }
        return boughtItems;
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
        return stock.get(productId);
    }

    public int getStoreStockId() {
        return this.storeID;
    }

    public void IncreaseQuantitytoBuy(int productId, int quantity) throws UIException {
        item foundItem = getItemByProductId(productId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        synchronized (foundItem) {
            if (foundItem.getQuantity() < quantity) {
                throw new UIException("Insufficient stock", ErrorCodes.INSUFFICIENT_STOCK);
            }
            foundItem.changeQuantity(foundItem.getQuantity() + quantity);
        }
    }

    public boolean isAvaliable(int productId, int quantity) {
        if (!stock.containsKey(productId))
            return false;
        if (stock.get(productId).getQuantity() < quantity)
            return false;
        return true;
    }

    public void setStoreId(int storeId2) {
        this.storeID = storeId2;
    }

}
