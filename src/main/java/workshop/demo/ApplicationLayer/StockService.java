package workshop.demo.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.Stock.ProductFilter;

public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private IStockRepo stockRepo;
    private IUserRepo userRepo;
    private IAuthRepo authRepo;
    private ProductFilter productFilter;

    public StockService(IStockRepo stockRepo, IUserRepo userRepo, IAuthRepo authRepo, ProductFilter productFilter) {
        this.stockRepo = stockRepo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.productFilter = productFilter;
    }

    public ProductDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new TokenNotFoundException();
            }

            logger.info("User {} is searching for products with criteria {}", authRepo.getUserId(token), criteria);
            ProductDTO[] results = productFilter.handleSearch(criteria);
            logger.info("Product search completed. Found {} results", results.length);
            return results;

        } catch (Exception e) {
            logger.error("Failed to search products. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public int addGlobalProduct(String token, String name, Category category, String description) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new TokenNotFoundException();
            }

            int productId = stockRepo.addProduct(name, category, description);
            logger.info("User {} added new global product: {} (ID: {})", authRepo.getUserId(token), name, productId);
            return productId;

        } catch (Exception e) {
            logger.error("Failed to add global product. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public ProductDTO[] getAllProducts(String token) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new TokenNotFoundException();
            }

            ProductDTO[] all = stockRepo.getAllProducts();
            logger.info("User {} requested all products. Returned {} items", authRepo.getUserId(token), all.length);
            return all;

        } catch (Exception e) {
            logger.error("Failed to retrieve all products. ERROR: {}", e.getMessage());
            throw e;
        }
    }
}