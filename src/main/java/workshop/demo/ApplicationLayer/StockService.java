package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
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
            logger.error("Invalid token during searchProducts");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
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
            logger.error("Unauthorized access to searchProductInStore with token: {}", token);
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        Product product = stockRepo.findById(productId);
        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            throw new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
        if (itemInStore == null) {
            logger.warn("Product {} not sold in store {}", productId, storeId);
            throw new UIException("Product not sold in this store", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        String storeName = storeRepo.getStoreNameById(storeId);
        logger.info("Product {} found in store {} (ID {})", product.getName(), storeName, storeId);

        return "Product: " + product.getName() + ", Price: " + itemInStore.getPrice() + ", Store: " + storeName;
    }

    public ProductDTO getProductInfo(String token, int productId) throws UIException {
        logger.info("Fetching product info for ID {}", productId);

        if (!authRepo.validToken(token)) {
            logger.error("Invalid token used to get product info for ID {}", productId);
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }

        ProductDTO dto = stockRepo.GetProductInfo(productId);
        if (dto == null) {
            logger.error("Product not found for ID {}", productId);
            throw new UIException("Product not found.", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        logger.info("Successfully retrieved product info: {}", dto.getName());
        return dto;
    }
}
