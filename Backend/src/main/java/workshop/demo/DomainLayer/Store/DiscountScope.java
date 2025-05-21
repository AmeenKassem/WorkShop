package workshop.demo.DomainLayer.Store;

import workshop.demo.DTOs.ItemStoreDTO;

import java.util.List;

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

    public int getQuantityOfProduct(int productId) {
        return items.stream()
                .filter(i -> i.getId() == productId)
                .mapToInt(ItemStoreDTO::getQuantity)
                .sum();
    }

    public boolean containsCategory(String categoryName) {
        return items.stream()
                .anyMatch(i -> i.getCategory().name().equalsIgnoreCase(categoryName));
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(ItemStoreDTO::getQuantity).sum();
    }
}
