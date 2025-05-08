package workshop.demo.DomainLayer.Stock;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Store.item;

public class ProductSearchCriteria {

    private String productNameFilter;
    private Category categoryFilter;
    private String keywordFilter;
    private int storeId;
    private double minPrice;
    private double maxPrice;
    private double minStoreRating;
    private double maxStoreRating;

    public ProductSearchCriteria(
            String productNameFilter,
            Category categoryFilter,
            String keywordFilter,
            int storeId,
            double minPrice,
            double maxPrice,
            double minStoreRating,
            double maxStoreRating) {
        this.productNameFilter = productNameFilter;
        this.categoryFilter = categoryFilter;
        this.keywordFilter = keywordFilter;
        this.storeId = storeId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minStoreRating = minStoreRating;
        this.maxStoreRating = maxStoreRating;
    }

 
    public boolean matchesForStore(item item) {
        if (item == null) {
            return false;
        }

        // Category match
        if (categoryFilter != null && !categoryFilter.equals(item.getCategory())) {
            return false;
        }

        // Price range check (minPrice ≤ price ≤ maxPrice)
        if (minPrice > 0 || maxPrice > 0) {
            int price = item.getPrice();
            if (minPrice > 0 && price < minPrice) {
                return false;
            }
            if (maxPrice > 0 && price > maxPrice) {
                return false;
            }
        }

        // Store rating check
        if (minStoreRating > 0 || maxStoreRating > 0) {
            double rank = item.getFinalRank();
            if (minStoreRating > 0 && rank < minStoreRating) {
                return false;
            }
            if (maxStoreRating > 0 && rank > maxStoreRating) {
                return false;
            }
        }

        return true;
    }

    public boolean specificCategory() {
        return categoryFilter != null;
    }

    public boolean productIsMatch(Product product) {
        if (product == null) {
            return false;
        }

        // Product name filter (case-insensitive substring match)
        if (productNameFilter != null && !product.getName().toLowerCase().contains(productNameFilter.toLowerCase())) {
            return false;
        }

        // Category match
        if (categoryFilter != null && !categoryFilter.equals(product.getCategory())) {
            return false;
        }

        // Keyword match
        if (keywordFilter != null && (product.getKeywords() == null
                || product.getKeywords().stream().noneMatch(k -> k.toLowerCase().contains(keywordFilter.toLowerCase())))) {
            return false;
        }

        return true;
    }

    public Category getCategory() {
        return this.categoryFilter;
    }

    public int getStoreId() {
        return this.storeId;
    }

}
