package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.item;

public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    
    private IStockRepo stockRepo;
    private IAuthRepo authRepo;
    private IStoreRepo storeRepo;

    public StockService(IStockRepo stockRepo, IStoreRepo storeRepo, IAuthRepo authRepo) {
        this.stockRepo = stockRepo;
        this.authRepo = authRepo;
        this.storeRepo = storeRepo;
    }

    public ItemStoreDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchProducts with criteria: {}", criteria);
        if (!authRepo.validToken(token)) {
            logger.warn("Invalid token during searchProducts");
        if (authRepo.validToken(token)) {
            ProductDTO[] matchesProducts = stockRepo.getMatchesProducts(criteria);
            return storeRepo.getMatchesItems(criteria, matchesProducts);
        } else {
            throw new TokenNotFoundException();
        }

        ProductDTO[] matchesProducts = stockRepo.getMatchesProducts(criteria);
        logger.debug("Found {} matching products in stock", matchesProducts.length);

        ItemStoreDTO[] matchedItems = storeRepo.getMatchesItems(criteria, matchesProducts);
        logger.info("Returning {} matched items to client", matchedItems.length);
        return matchedItems;
    }

    public String searchProductInStore(String token, int storeId, int productId) throws Exception {
        logger.info("Searching for productId {} in storeId {}", productId, storeId);

        if (!authRepo.validToken(token)) {
            logger.warn("Unauthorized access to searchProductInStore with token: {}", token);
            throw new TokenNotFoundException();
        }
        Product product = stockRepo.findById(productId);
        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            throw new Exception("Product not found");
        }

        item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
        if (itemInStore == null) {
            logger.warn("Product {} not sold in store {}", productId, storeId);
            throw new Exception("Product not sold in this store");
        }

        String storeName = storeRepo.getStoreNameById(storeId);
        logger.info("Product {} found in store {} (ID {})", product.getName(), storeName, storeId);

        return "Product: " + product.getName() + ", Price: " + itemInStore.getPrice() + ", Store: " + storeName;
    }

    public ProductDTO getProductInfo(String token, int productId) throws Exception {
        logger.info("Fetching product info for ID {}", productId);

        if (!authRepo.validToken(token)) {
            logger.warn("Invalid token used to get product info for ID {}", productId);
            throw new TokenNotFoundException();
        }

        Product product = stockRepo.findById(productId);
        if (product == null) {
            logger.error("Product not found for ID {}", productId);
            throw new Exception("Product not found.");
        }

        ProductDTO dto = new ProductDTO(
            product.getProductId(),
            product.getName(),
            product.getCategory(),
            product.getDescription()
        );

        logger.info("Successfully retrieved product info: {}", dto.getName());
        return dto;
       
    }
    
}
