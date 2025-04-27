package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Purchase.Purchase;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;

public class PurchaseService {

    private final StockRepository stockRepo;
    private final ShoppingCart shoppingCartRepo;
    private IStoreRepo storeRepo;
    private IAuthRepo authRepo;

     public PurchaseService(IAuthRepo authRepo, StockRepository stockRepo, StoreRepository storeRepo, ShoppingCart shoppingCart) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.shoppingCartRepo = shoppingCart;
    }

    
    public List<ReceiptDTO> buyGuestCart(String token) throws Exception {
        // Step 1: Validate token
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        // Step 2: Get guest user ID from token
        int ownerId = authRepo.getUserId(token);

        // Step 3: Get guest's shopping cart
        ShoppingCart shoppingCart = shoppingCartRepo.getCart(ownerId);
        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for guest user.");
        }
        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo);
        List<ReceiptDTO> receipts = purchase.executePurchase(true);

        // Save order to order history (not implemented yet)

        return receipts;
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

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo);
        List<ReceiptDTO> receipts = purchase.executePurchase(false);
        return receipts;
    }
}


