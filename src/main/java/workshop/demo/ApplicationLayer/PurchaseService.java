package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Purchase.Purchase;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;

public class PurchaseService {

    private final StockRepository stockRepo;
    private final StoreRepository storeRepo;
    private final IAuthRepo authRepo;
    private final ShoppingCartRepo shoppingCartRepo;
    private final OrderRepository orderRepo;

    public PurchaseService(IAuthRepo authRepo,
                           StockRepository stockRepo,
                           StoreRepository storeRepo,
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
        return purchase.executePurchase(true, ownerId);
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
        return purchase.executePurchase(false, ownerId);
    }
}