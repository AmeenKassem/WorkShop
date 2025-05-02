package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.DomainLayer.Stock.ProductFilter;

public class StockService {

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
}
