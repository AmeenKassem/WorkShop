package workshop.demo.DomainLayer.Store;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import workshop.demo.DTOs.ItemStoreDTO;

public class DiscountScope {

    private final List<ItemStoreDTO> items;

    public DiscountScope(List<ItemStoreDTO> items) {
        this.items = items;
    }

    public List<ItemStoreDTO> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public boolean containsItem(int itemId) {
        return items.stream().anyMatch(i -> i.getProductId() == itemId);
    }

    public int getQuantityOfProduct(int productId) {
        return items.stream()
                .filter(i -> i.getProductId() == productId)
                .mapToInt(ItemStoreDTO::getQuantity)
                .sum();
    }

    public boolean containsCategory(String categoryName) {
        return items.stream()
                .anyMatch(i -> i.getCategory().name().equalsIgnoreCase(categoryName));

    }

    public boolean containsStore(int storeId) {
        return items.stream().anyMatch(i -> i.getStoreId() == storeId);
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(ItemStoreDTO::getQuantity).sum();
    }
}
