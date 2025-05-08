package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
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
    private final IPaymentService paymentService;
    private final ISupplyService supplyService;
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    public PurchaseService(IAuthRepo authRepo, IStockRepo stockRepo, IStoreRepo storeRepo, IUserRepo userRepo, IPurchaseRepo purchaseRepo, IOrderRepo orderRepo, IPaymentService paymentService, ISupplyService supplyService) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
        this.supplyService = supplyService;
    }

    public ReceiptDTO[] buyGuestCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        return processCart(userId, true, paymentdetails, supplydetails);
    }

    public ReceiptDTO[] buyRegisteredCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        return processCart(userId, false, paymentdetails, supplydetails);
    }

    private ReceiptDTO[] processCart(int userId, boolean isGuest, PaymentDetails payment, SupplyDetails supply) throws Exception {
        ShoppingCart cart = userRepo.getUserCart(userId);
        if (cart == null || cart.getAllCart().isEmpty()) {
            throw new UIException("Shopping cart is empty or not found", ErrorCodes.CART_NOT_FOUND);
        }

        if (isGuest && !storeRepo.checkAvailability(cart.getAllCart())) {
            throw new UIException("Not all items are available for guest purchase", ErrorCodes.PRODUCT_NOT_FOUND);
        }

        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
        for (ShoppingBasket basket : cart.getBaskets().values()) {
            List<ReceiptProduct> boughtItems = storeRepo.processCartItemsForStore(basket.getStoreId(), basket.getItems(), isGuest);
            double total = storeRepo.calculateTotalPrice(boughtItems);
            paymentService.processPayment(payment, total);
            supplyService.processSupply(supply);
            storeToProducts.put(basket.getStoreId(), boughtItems);
        }
        return saveReceipts(userId, storeToProducts);
    }

    public ParticipationInRandomDTO participateInRandom(String token, int randomId, int storeId, double amountPaid, PaymentDetails paymentDetails) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException(String.format("User %d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        }
        ParticipationInRandomDTO card = storeRepo.validatedParticipation(userId, randomId, storeId, amountPaid);
        userRepo.ParticipateInRandom(card);
        purchaseRepo.saveRandomParticipation(card);
        paymentService.processPayment(paymentDetails, amountPaid);
        logger.info("User {} successfully participated in random draw {}", userId, randomId);
        return card;
    }

    public void finalizeRandomWinnings(String token, SupplyDetails supply) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException(String.format("User %d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        }
        logger.info("User {} finalizing random winnings", userId);

        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
        for (ParticipationInRandomDTO card : userRepo.getWinningCards(userId)) {
            storeRepo.validateAndDecreaseStock(card.storeId, card.productId, 1);
            Product product = stockRepo.findById(card.productId);
            String storeName = storeRepo.getStoreNameById(card.storeId);

            ReceiptProduct receiptProduct = new ReceiptProduct(
                product.getName(), product.getCategory(), product.getDescription(),
                storeName, 1, 0
            );

            storeToProducts.computeIfAbsent(card.storeId, k -> new ArrayList<>()).add(receiptProduct);
            supplyService.processSupply(supply);
        }
        saveReceipts(userId, storeToProducts);
    }

    public void finalizeAuctionWins(String token, PaymentDetails payment) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException(String.format("User %d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        }
        logger.info("User {} finalizing auction wins", userId);

        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : userRepo.getWinningBids(userId)) {
            if (bid.getType() != SpecialType.Auction) continue;

            Product product = stockRepo.findById(bid.getId());
            if (product == null) {
                throw new UIException("Product not available", ErrorCodes.PRODUCT_NOT_FOUND);
            }
            String storeName = storeRepo.getStoreNameById(bid.getStoreId());
            storeRepo.validateAndDecreaseStock(bid.getStoreId(), bid.getId(), bid.getAmount());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                product.getName(), product.getCategory(), product.getDescription(),
                storeName, bid.getAmount(), (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            paymentService.processPayment(payment, (int) bid.getBidPrice());
        }
        saveReceipts(userId, storeToProducts);
    }

    public void finalizeAcceptedBids(String token, PaymentDetails payment) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException(String.format("User %d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        }
        logger.info("User {} finalizing accepted bids", userId);

        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : userRepo.getWinningBids(userId)) {
            if (bid.getType() != SpecialType.BID) continue;

            Product product = stockRepo.findById(bid.getId());
            String storeName = storeRepo.getStoreNameById(bid.getStoreId());
            storeRepo.validateAndDecreaseStock(bid.getStoreId(), bid.getId(), bid.getAmount());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                product.getName(), product.getCategory(), product.getDescription(),
                storeName, bid.getAmount(), (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            paymentService.processPayment(payment, (int) bid.getBidPrice());
        }
        saveReceipts(userId, storeToProducts);
    }

    public void submitBid(String token, SingleBid bid) throws UIException {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!userRepo.isRegistered(userId)) {
            throw new UIException(String.format("User %d is not registered to the system!", userId), ErrorCodes.USER_NOT_FOUND);
        }
        logger.info("User {} is submitting a BID: {}", userId, bid);
        purchaseRepo.saveBid(bid);
        logger.info("BID was saved successfully");
    }

    public String searchProductInStore(String token, int storeId, int productId) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        Product product = stockRepo.findById(productId);
        if (product == null) {
            throw new UIException("Product not found.", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
        if (itemInStore == null) {
            throw new UIException("Product not found in store.", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        return "Product: " + product.getName() + ", Price: " + itemInStore.getPrice() + ", Store: " + storeRepo.getStoreNameById(storeId);
    }

    private ReceiptDTO[] saveReceipts(int userId, Map<Integer, List<ReceiptProduct>> storeToProducts) throws UIException {
        List<ReceiptDTO> receipts = new ArrayList<>();
        for (Map.Entry<Integer, List<ReceiptProduct>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeRepo.getStoreNameById(storeId);
            List<ReceiptProduct> items = entry.getValue();
            double total = items.stream().mapToDouble(ReceiptProduct::getPrice).sum();

            ReceiptDTO receipt = new ReceiptDTO(storeName, LocalDate.now().toString(), items, total);
            receipts.add(receipt);
            orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
        }
        return receipts.toArray(new ReceiptDTO[0]);
    }
}
