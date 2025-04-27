package workshop.demo.DomainLayer.Stock;

import org.springframework.stereotype.Component;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.InfrastructureLayer.StockRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductFilter { 

    private final StockRepository stockRepository;

    public ProductFilter(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public ProductDTO[] handleSearch(ProductSearchCriteria entity) {
        if (entity.isStoreSpecified()) {
            return handleStoreSearch(entity);
        }

        String searchBy = entity.getSearchType();
        ProductDTO[] result = new ProductDTO[0];

        switch (searchBy) {
            case "keyWord":
                result = stockRepository.searchByKeyword(entity.getKeywordFilter());
                break;
            case "productName":
                result = stockRepository.searchByName(entity.getProductNameFilter());
                break;
            case "category":
                result = stockRepository.searchByCategory(Category.valueOf(entity.getCategoryFilter()));
                break;
            default:
                break;
        }
        handleResult(result, entity);
        return result;
    }

    private ProductDTO[] handleStoreSearch(ProductSearchCriteria entity) {
        int storeId = entity.getStoreId();
        List<ItemCartDTO> itemsInStore = stockRepository.getItemsByStoreId(storeId);
        List<ProductDTO> result = new ArrayList<>();

        for (ItemCartDTO item : itemsInStore) {
            Product product = stockRepository.findById(item.id);
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

    private void handleResult(ProductDTO[] products, ProductSearchCriteria entity) {
        for (int i = 0; i < products.length; i++) {
            ProductDTO pdto = products[i];
            if (pdto == null) continue;
    
            if (entity.getProductNameFilter() != null && !entity.getProductNameFilter().isEmpty()) {
                if (!pdto.getName().toLowerCase().contains(entity.getProductNameFilter().toLowerCase())) {
                    products[i] = null;
                    continue;
                }
            }
    
            if (entity.isCategorySpecified()) {
                if (!pdto.getCategory().name().equalsIgnoreCase(entity.getCategoryFilter())) {
                    products[i] = null;
                    continue;
                }
            }
    
            if (entity.isProductRatingSpecified()) {
                if (pdto.getRating() < entity.getMinPrice() || pdto.getRating() > entity.getMaxPrice()) {
                    products[i] = null;
                }
            }
        }
    }
    private void handleStoreResult(ProductDTO[] products, ProductSearchCriteria entity, List<ItemCartDTO> itemsInStore) {
        for (int i = 0; i < products.length; i++) {
            ProductDTO product = products[i];
            if (product == null) continue;
            ItemCartDTO item = itemsInStore.get(i);
    
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

 
}    