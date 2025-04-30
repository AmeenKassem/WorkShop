package workshop.demo.ApplicationLayer;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Stock.ProductFilter;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

public class ProductSearchService {

    private final ProductFilter productFilter;

    public ProductSearchService(ProductFilter productFilter) {
        this.productFilter = productFilter;
    }

    public ProductDTO[] searchProductsFromUI(String productName, String category, String keyword, 
                                          double minPrice, double maxPrice, boolean isPriceRangeSpecified, 
                                          boolean isProductRatingSpecified, double minProductRating, double maxProductRating, 
                                          boolean isStoreRatingSpecified, double minStoreRating, double maxStoreRating, 
                                          int storeId) throws Exception  {

    ProductSearchCriteria criteria = new ProductSearchCriteria();

    criteria.setProductNameFilter(productName);
    criteria.setCategoryFilter(category);
    criteria.setKeywordFilter(keyword);

    if (minPrice == 0 && maxPrice == 0) {
        criteria.setPriceRangeSpecified(false);  
    } else {
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setPriceRangeSpecified(isPriceRangeSpecified); 
    }

    if (minProductRating == 0 && maxProductRating == 0) {
        criteria.setProductRatingSpecified(false);
    } else {
        criteria.setProductRatingSpecified(isProductRatingSpecified); 
        criteria.setMinPrice(minProductRating); 
        criteria.setMaxPrice(maxProductRating); 
    }

    if (minStoreRating == 0 && maxStoreRating == 0) {
        criteria.setStoreRatingSpecified(false); 
    } else {
        criteria.setStoreRatingSpecified(isStoreRatingSpecified); 
        criteria.setMinPrice(minStoreRating); 
        criteria.setMaxPrice(maxStoreRating); 
    }

    criteria.setStoreId(storeId);
    return productFilter.handleSearch(criteria);  
    }
}