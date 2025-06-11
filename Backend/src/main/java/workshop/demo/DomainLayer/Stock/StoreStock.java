package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.CartItem;

public class StoreStock {

    private final Map<Integer, item> stock;//productId, item
    private int storeID;
    //discounts for this store 

    public StoreStock(int storeID) {
        this.stock = new ConcurrentHashMap<>();
        this.storeID = storeID;
    }

    // public item getProductById(int id) {
    //     return stock.get(id);
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

    //update quantity
    public void changeQuantity(int itemId, int quantity) throws UIException {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(quantity);
    }

    //decrase quantity to buy: -> check if I need synchronized the item???
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
        //foundItem.changeQuantity(foundItem.getQuantity() - quantity);
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
                ranks[newRank - 1].incrementAndGet();  // thread-safe increment
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
    //     List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
    //     for (item i : stock.values()) {
    //         // If item is mutable and accessed by multiple threads, synchronize on the item
    //         synchronized (i) {
    //             ItemStoreDTO toAdd = new ItemStoreDTO(
    //                     i.getProductId(),
    //                     i.getQuantity(),
    //                     i.getPrice(),
    //                     i.getCategory(),
    //                     i.getFinalRank(),
    //                     storeID
    //             );
    //             itemStoreDTOList.add(toAdd);
    //         }
    //     }
    //     return itemStoreDTOList;
    // }
    //may be changed later:
    public List<ReceiptProduct> ProcessCartItems(List<ItemCartDTO> cartItems, boolean isGuest, String storeName) throws UIException {
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
                    item.category
            ));
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
    }}
    //just for testing
    public List<item> getItemsByCategoryObject(Category category) {
        List<item> result = new ArrayList<>();
        for (item i : stock.values()) {
            if (i.getCategory().equals(category)) {
                result.add(i);
            }
        }
        return result;
    }

    //FOR STORE AND TESTING
    public item getItemByProductId(int productId) {
        return stock.get(productId);
    }

    public int getStoreStockId() {
        return this.storeID;
    }
}
