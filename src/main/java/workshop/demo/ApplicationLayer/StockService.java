package workshop.demo.ApplicationLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.DomainLayer.Stock.ProductFilter;

public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private IStockRepo stockRepo;
    // private IUserRepo userRepo;
    private IAuthRepo authRepo;
    // private ProductFilter productFilter;  
    private IStoreRepo storeRepo;

    public StockService(IStockRepo stockRepo, IStoreRepo storeRepo, IAuthRepo authRepo) {
        this.stockRepo = stockRepo;
        // this.userRepo = userRepo;
        this.authRepo = authRepo;
        // this.productFilter = productFilter;  
        this.storeRepo = storeRepo;
    }

    public ItemStoreDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        if(authRepo.validToken(token)) {
            ProductDTO[] matchesProducts = stockRepo.getMatchesProducts(criteria);
            return storeRepo.getMatchesItems(criteria,matchesProducts);
        } else {
            throw new TokenNotFoundException();
        }
    }

    public String searchProductInStore(String token, int storeId, int productId) throws Exception {
    if (!authRepo.validToken(token)) {
        throw new TokenNotFoundException();
    }
    Product product = stockRepo.findById(productId);
    if (product == null) {
        throw new Exception("Product not found");
    }

    item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
    if (itemInStore == null) {
        throw new Exception("Product not sold in this store");
    }

    String storeName = storeRepo.getStoreNameById(storeId);
    return "Product: " + product.getName() +", Price: " + itemInStore.getPrice() +", Store: " + storeName;
}

public String getProductInfo(String token, int productId) throws Exception {
    if (!authRepo.validToken(token)) {
        throw new TokenNotFoundException();
    }
    Product product = stockRepo.findById(productId);
    if (product == null) {
        throw new Exception("Product not found.");
    }
    return "Product ID: " + product.getProductId() +", Name: " + product.getName() +", Category: " + product.getCategory() +", Description: " + product.getDescription();
}
}