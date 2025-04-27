

package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import workshop.demo.DTOs.Category;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.DTOs.ProductDTO;


@Component
public class ProductFilter { 

    private StockRepository stockRepository;
    private static Map<String, List<Integer>> keywordSearch;// keyword ==> List of product ID's

    public ProductFilter(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public static void addToKeyWords(String keyword, int prodID){
        keywordSearch.computeIfAbsent(keyword, k -> new ArrayList<>()).add(prodID);
    }


    public List<ProductDTO> handleSearch(ProductSearchCriteria entity) {
        String searchBy = entity.getSearchType();
        List<ProductDTO> result = new ArrayList<>();

        switch (searchBy) {
            case "keyWord":
                result = searchByKeyword(entity.getKeywordFilter());
                break;
            case "productName":
                result = searchByProductName(entity.getProductNameFilter());
                break;
            case "categotry":
                result = searchByCategory(entity.getCategoryFilter());
                break;
            default:
                break;
        }
        handleResult(result, entity);
        return result;
    }
    public void handleResult(List<ProductDTO> result, ProductSearchCriteria entity) {
        // Filter by product name if a product name filter is specified
        if (entity.getProductNameFilter() != null && !entity.getKeywordFilter().isEmpty()) {
            result.removeIf(pdto -> !pdto.getName().toLowerCase().contains(entity.getProductNameFilter().toLowerCase()));
        }
    
        if (entity.isCategorySpecified()) {
            result.removeIf(pdto -> !pdto.getCategory().name().equalsIgnoreCase(entity.getCategoryFilter()));
        }
    
        // Filter by product rating if prodRating is specified
        if (entity.isProductRatingSpecified()) {
            result.removeIf(pdto -> pdto.getRating() < entity.getMinPrice() || pdto.getRating() > entity.getMaxPrice());
        }
    }


    public List<ProductDTO> searchByProductName(String productName) {
        return stockRepository.searchByName(productName);
    }

    public List<ProductDTO> searchByCategory(String categoryName) {
        Category category = Category.valueOf(categoryName); // Convert string to enum
        return stockRepository.searchByCategory(category);
    }

        // Search by keyword (using StockRepository to get products)
        public List<ProductDTO> searchByKeyword(String keyword) {
            List<ProductDTO> result = new ArrayList<>();
            List<Integer> productIds = keywordSearch.get(keyword);
    
            // Check if there are matching product IDs for the given keyword
            if (productIds != null) {
                for (Integer productId : productIds) {
                    Product product = stockRepository.findById(productId); 
                    if (product != null) {
                        result.add(new ProductDTO(product.getProductId(), product.getName(), product.getCategory(), product.getDescription()));
                    }
                }
            }
            return result;
        }


}

