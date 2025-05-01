package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.Purchase;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;


public class PurchaseService {
    private final IAuthRepo authRepo;
    private final ShoppingCartRepo shoppingCartRepo;
    private final OrderRepository orderRepo;
    private IUserRepo userRepo;
    private IStockRepo stockRepo;
    private IStoreRepo storeRepo;

    public PurchaseService(IAuthRepo authRepo,
            IStockRepo stockRepo,
            IStoreRepo storeRepo,
            ShoppingCartRepo shoppingCartRepo,
            OrderRepository orderRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.shoppingCartRepo = shoppingCartRepo;
        this.orderRepo = orderRepo;
    }

    public List<ReceiptDTO> buyGuestCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int ownerId = authRepo.getUserId(token);

        ShoppingCart shoppingCart = shoppingCartRepo.getCart(ownerId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for guest user.");
        }

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo, orderRepo);
        return purchase.processRegularPurchase(true, ownerId);
    }

    public List<ReceiptDTO> buyRegisteredCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int ownerId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = shoppingCartRepo.getCart(ownerId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo, orderRepo);
        return purchase.processRegularPurchase(false, ownerId);
    }

    public ParticipationInRandomDTO participateInRandomForUser(String token, int randomId, int storeId, double amountPaid) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("timeout!");
        }
        
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException("you have to sign in!");
        }
        
        // double cardPrice=storeRepo.getProductPrice(storeId,randomId);
        if(true){//must check Payment
            ParticipationInRandomDTO card = storeRepo.participateInRandom(userId, randomId, storeId, amountPaid);
            userRepo.ParticipateInRandom(card);
            return card;
        }else{
            throw new UIException("you cant pay");
        }   
    }
}