package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.Purchase;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.item;

public class PurchaseService {

    private final IAuthRepo authRepo;
    private final IStockRepo stockRepo;
    private final IStoreRepo storeRepo;
    private final IUserRepo userRepo;
    private final IPurchaseRepo purchaseRepo;
    private IOrderRepo orderRepo;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    public PurchaseService(IAuthRepo authRepo, IStockRepo stockRepo, IStoreRepo storeRepo, IUserRepo userRepo, IPurchaseRepo purchaseRepo, IOrderRepo orderRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
        this.orderRepo = orderRepo;
    }

    public List<ReceiptDTO> buyGuestCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(userId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }


        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo,orderRepo, userRepo);
        return purchase.processRegularPurchase(true, userId);
    }

    public List<ReceiptDTO> buyRegisteredCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(userId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo, orderRepo, userRepo);
        return purchase.processRegularPurchase(false, userId);
    }

    public ParticipationInRandomDTO participateInRandomForUser(String token, int randomId, int storeId, double amountPaid) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("User %d is not registered to the system!", userId));
        }

        double requiredPrice = storeRepo.getProductPrice(storeId, randomId);
        if (amountPaid < requiredPrice) {
            throw new Exception(String.format("Insufficient payment: %.2f required, but only %.2f provided", requiredPrice, amountPaid));
        }

        logger.info("User {} is participating in random draw {} at store {} with payment {}", userId, randomId, storeId, amountPaid);
        ParticipationInRandomDTO card = storeRepo.participateInRandom(userId, randomId, storeId, amountPaid);
        userRepo.ParticipateInRandom(card);
        purchaseRepo.saveRandomParticipation(card);
        logger.info("User {} successfully participated in random draw {}", userId, randomId);

        return card;
    }

    public void finalizeRandomWinnings(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("User %d is not registered to the system!", userId));
        }

        logger.info("User {} is processing random winnings", userId);
        ShoppingCart cart = userRepo.getUserCart(userId);
        Purchase purchase = new Purchase(cart, stockRepo, storeRepo, orderRepo, userRepo);
        purchase.processRandomWinnings(userId);
        logger.info("Random winnings were processed successfully for user {}", userId);
    }

    public void finalizeAuctionWins(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("User %d is not registered to the system!", userId));
        }

        logger.info("User {} is finalizing auction wins", userId);
        ShoppingCart cart = userRepo.getUserCart(userId);
        Purchase purchase = new Purchase(cart, stockRepo, storeRepo, orderRepo, userRepo);
        purchase.processAuctionWinnings(userId);
        logger.info("Auction wins were processed successfully for user {}", userId);
    }

    public void finalizeAcceptedBids(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("User %d is not registered to the system!", userId));
        }

        logger.info("User {} is finalizing accepted BID offers", userId);
        ShoppingCart cart = userRepo.getUserCart(userId);
        Purchase purchase = new Purchase(cart, stockRepo, storeRepo, orderRepo, userRepo);
        purchase.processBids(userId);
        logger.info("Accepted bids processed successfully for user {}", userId);
    }

    public void submitBid(String token, SingleBid bid) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new Exception(String.format("User %d is not registered to the system!", userId));
        }

        logger.info("User {} is submitting a BID: {}", userId, bid);
        purchaseRepo.saveBid(bid);
        logger.info("BID was saved successfully");
    }

    public String searchProductInStore(String token, int storeId, int productId) throws Exception {
    if (!authRepo.validToken(token)) {
        throw new TokenNotFoundException();
    }

    Product product = stockRepo.findById(productId);
    if (product == null) {
        throw new Exception("Product not found.");
    }

    item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
    if (itemInStore == null) {
        throw new Exception("Product not found in store.");
    }

    return "Product: " + product.getName() +
           ", Price: " + itemInStore.getPrice() +
           ", Store: " + storeRepo.getStoreNameById(storeId);
}
}

