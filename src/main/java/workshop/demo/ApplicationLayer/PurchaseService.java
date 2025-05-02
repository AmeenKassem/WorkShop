package workshop.demo.ApplicationLayer;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.Purchase;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.DomainLayer.Stock.IStockRepo;

public class PurchaseService {

    private final IAuthRepo authRepo;
    private final OrderRepository orderRepo;
    private final IUserRepo userRepo;
    private final IStockRepo stockRepo;
    private final StoreRepository storeRepo;
    private final IPurchaseRepo purchaseRepo;
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    public PurchaseService(IAuthRepo authRepo,
                           IStockRepo stockRepo,
                           StoreRepository storeRepo,
                           OrderRepository orderRepo,
                           IUserRepo userRepo,
                           IPurchaseRepo purchaseRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
    }

    public List<ReceiptDTO> buyGuestCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int ownerId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(ownerId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for guest user.");
        }

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo, orderRepo, userRepo);
        return purchase.processRegularPurchase(true, ownerId);
    }

    public List<ReceiptDTO> buyRegisteredCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int ownerId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(ownerId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }

        Purchase purchase = new Purchase(shoppingCart, stockRepo, storeRepo, orderRepo, userRepo);
        return purchase.processRegularPurchase(false, ownerId);
    }

    public ParticipationInRandomDTO participateInRandomForUser(String token, int randomId, int storeId, double amountPaid) throws Exception {
        try {
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

        } catch (Exception e) {
            logger.error("Failed to participate in random draw. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public void finalizeRandomWinnings(String token) throws Exception {
        try {
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

        } catch (Exception e) {
            logger.error("Failed to process random winnings. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public void finalizeAuctionWins(String token) throws Exception {
        try {
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

        } catch (Exception e) {
            logger.error("Failed to process auction wins. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public void finalizeAcceptedBids(String token) throws Exception {
        try {
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

        } catch (Exception e) {
            logger.error("Failed to process accepted bids. ERROR: {}", e.getMessage());
            throw e;
        }
    }

    public void submitBid(String token, SingleBid bid) throws Exception {
        try {
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

        } catch (Exception e) {
            logger.error("Failed to submit bid. ERROR: {}", e.getMessage());
            throw e;
        }
    }
}
