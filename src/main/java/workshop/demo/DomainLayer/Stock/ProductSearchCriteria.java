package workshop.demo.DomainLayer.Stock;

public class ProductSearchCriteria {
    private String searchType;
    private String productNameFilter;
    private String categoryFilter;
    private String keywordFilter;
    private boolean isPriceRangeSpecified;
    private double minPrice;
    private double maxPrice;
    
    private boolean isProductRatingSpecified;
    private boolean isStoreRatingSpecified;
    private boolean isCategorySpecified;
    private boolean isStoreSpecified;
    private Integer storeId;

    public ProductSearchCriteria(
        String searchType, 
        String productNameFilter, 
        String categoryFilter, 
        String keywordFilter,
        boolean isPriceRangeSpecified, 
        double minPrice, 
        double maxPrice, 
        boolean isProductRatingSpecified, 
        boolean isStoreRatingSpecified, 
        boolean isCategorySpecified,
        boolean isStoreSpecified,
        Integer storeId
    ) {
        this.searchType = searchType;
        this.productNameFilter = productNameFilter;
        this.categoryFilter = categoryFilter;
        this.keywordFilter = keywordFilter;
        this.isPriceRangeSpecified = isPriceRangeSpecified;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.isProductRatingSpecified = isProductRatingSpecified;
        this.isStoreRatingSpecified = isStoreRatingSpecified;
        this.isCategorySpecified = isCategorySpecified;
        this.isStoreSpecified = isStoreSpecified;
        this.storeId = storeId;
    }

    public ProductSearchCriteria() {}

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getProductNameFilter() {
        return productNameFilter;
    }

    public void setProductNameFilter(String productNameFilter) {
        this.productNameFilter = productNameFilter;
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public String getKeywordFilter() {
        return keywordFilter;
    }

    public void setKeywordFilter(String keywordFilter) {
        this.keywordFilter = keywordFilter;
    }

    public boolean isPriceRangeSpecified() {
        return isPriceRangeSpecified;
    }

    public void setPriceRangeSpecified(boolean isPriceRangeSpecified) {
        this.isPriceRangeSpecified = isPriceRangeSpecified;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public boolean isProductRatingSpecified() {
        return isProductRatingSpecified;
    }

    public void setProductRatingSpecified(boolean isProductRatingSpecified) {
        this.isProductRatingSpecified = isProductRatingSpecified;
    }

    public boolean isStoreRatingSpecified() {
        return isStoreRatingSpecified;
    }

    public void setStoreRatingSpecified(boolean isStoreRatingSpecified) {
        this.isStoreRatingSpecified = isStoreRatingSpecified;
    }

    public boolean isCategorySpecified() {
        return isCategorySpecified;
    }

    public void setCategorySpecified(boolean isCategorySpecified) {
        this.isCategorySpecified = isCategorySpecified;
    }

    public boolean isStoreSpecified() {
        return isStoreSpecified;
    }

    public void setStoreSpecified(boolean storeSpecified) {
        isStoreSpecified = storeSpecified;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }
}
