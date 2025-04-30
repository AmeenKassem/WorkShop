package workshop.demo.DomainLayer.Stock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.DTOs.ProductDTO;


@Component
public class ProductFilter {

    private StockRepository stockRepository;
    private StoreRepository storeRepository;

    public ProductFilter(StockRepository stockRepository, StoreRepository storeRepository) {
        this.stockRepository = stockRepository;
        this.storeRepository = storeRepository;
    }

    public ProductDTO[] handleSearch(ProductSearchCriteria entity) throws Exception {
        if (entity.isStoreSpecified()) {
            return handleStoreSearch(entity);
        }
    
        String searchBy = entity.getSearchType();
        ProductDTO[] result = new ProductDTO[0]; 

        if ("keyWord".equalsIgnoreCase(searchBy)) {
            result = stockRepository.searchByKeyword(entity.getKeywordFilter());
        } else if ("productName".equalsIgnoreCase(searchBy)) {
            result = stockRepository.searchByName(entity.getProductNameFilter());
        } else if ("category".equalsIgnoreCase(searchBy)) {
            result = stockRepository.searchByCategory(Category.valueOf(entity.getCategoryFilter()));
        }
    
        if (entity.isStoreRatingSpecified()) {
            result = filterByStoreRating(result, entity);
        }

        handleResult(result, entity);
        return result;
    }

    private ProductDTO[] handleStoreSearch(ProductSearchCriteria entity) throws Exception {
        int storeId = entity.getStoreId();
        List<ItemStoreDTO> itemsInStore = stockRepository.getItemsByStoreId(storeId);
        List<ProductDTO> result = new ArrayList<>();
        
        for (ItemStoreDTO item : itemsInStore) {
            Product product = stockRepository.findById(item.getId());
            if (product != null) {
                ProductDTO productDTO = new ProductDTO(
                    product.getProductId(), 
                    product.getName(), 
                    product.getCategory(), 
                    product.getDescription()
                );
                result.add(productDTO);
            }
        }

        ProductDTO[] resultArray = result.toArray(new ProductDTO[0]);
        handleStoreResult(resultArray, entity, itemsInStore);
        return resultArray;
    }

    
    private ProductDTO[] filterByStoreRating(ProductDTO[] result, ProductSearchCriteria entity) throws Exception {
        double minStoreRating = entity.getMinStoreRating();
        double maxStoreRating = entity.getMaxStoreRating();
    
        List<ProductDTO> filteredList = new ArrayList<>();
    
        for (ProductDTO productDTO : result) {
            //  we have to get the list of ItemStoreDTO for each product - each product can be in multiple stores (more than one item) 
            List<ItemStoreDTO> itemsInStore = stockRepository.getItemsByProductId(productDTO.getProductId());
    
            for (ItemStoreDTO itemStoreDTO : itemsInStore) {
                int storeId = itemStoreDTO.getStoreId(); 
                double storeRating = storeRepository.getStoreRating(storeId); 
                if (storeRating >= minStoreRating && storeRating <= maxStoreRating) {
                    filteredList.add(productDTO); 
                    break; 
                }
            }
        }
    
        return filteredList.toArray(new ProductDTO[0]);  // Convert the list back to ProductDTO[]
    }
    private void handleResult(ProductDTO[] result, ProductSearchCriteria entity) {
        for (int i = 0; i < result.length; i++) {
            ProductDTO pdto = result[i];

            if (entity.getProductNameFilter() != null && !entity.getProductNameFilter().isEmpty()) {
                if (!pdto.getName().toLowerCase().contains(entity.getProductNameFilter().toLowerCase())) {
                    result[i] = null;
                    continue;
                }
            }

            if (entity.isCategorySpecified()) {
                if (!pdto.getCategory().name().equalsIgnoreCase(entity.getCategoryFilter())) {
                    result[i] = null;
                    continue;
                }
            }

            if (entity.isProductRatingSpecified()) {
                if (pdto.getRating() < entity.getMinPrice() || pdto.getRating() > entity.getMaxPrice()) {
                    result[i] = null;
                }
            }
        }
    }

    private void handleStoreResult(ProductDTO[] products, ProductSearchCriteria entity, List<ItemStoreDTO> itemsInStore) {
        for (int i = 0; i < products.length; i++) {
            ProductDTO product = products[i];
            if (product == null) continue;
            ItemStoreDTO item = itemsInStore.get(i);

            if (entity.isCategorySpecified() && !product.getCategory().name().equalsIgnoreCase(entity.getCategoryFilter())) {
                products[i] = null;
                continue;
            }

            if (entity.isPriceRangeSpecified() && (item.price < entity.getMinPrice() || item.price > entity.getMaxPrice())) {
                products[i] = null;
                continue;
            }

            if (entity.isProductRatingSpecified() && (item.rank < 3)) {
                products[i] = null;
            }
        }
    }

    public ProductDTO[] searchByProductName(String productName) {
        return stockRepository.searchByName(productName);
    }

    public ProductDTO[] searchByCategory(String categoryName) {
        Category category = Category.valueOf(categoryName);
        return stockRepository.searchByCategory(category);
    }

    public ProductDTO[] searchByKeyword(String keyword) {
        return stockRepository.searchByKeyword(keyword);
    }
}
