package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class StockService {

    private IStockRepo stockRepo;
    private IUserRepo userRepo;
    private IAuthRepo authRepo;

    public StockService(IStockRepo stockRepo,IUserRepo userRepo,IAuthRepo authRepo ){
        this.stockRepo = stockRepo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public ProductDTO[] searchByNameInAllSystem(String token,String name){
        if(authRepo.validToken(token)){
            return stockRepo.searchByName(name);
        }else throw new TokenNotFoundException();
    }

    public ProductDTO[] searchByNameInCategory(String token,String name, Category category){
        if(authRepo.validToken(token)){
            return stockRepo.searchByName(name,category);
        }else throw new TokenNotFoundException();
    }

}
