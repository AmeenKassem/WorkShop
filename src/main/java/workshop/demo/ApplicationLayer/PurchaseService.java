package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Random;
import workshop.demo.DomainLayer.Store.Store;
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

    public ReceiptDTO[] buyGuestCart(String token) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(userId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }
        return processRegularPurchase(true, userId);
    }

    public ReceiptDTO[] buyRegisteredCart(String token,PaymentDetails) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new Exception("Invalid token!");
        }

        int userId = authRepo.getUserId(token);
        ShoppingCart shoppingCart = userRepo.getUserCart(userId);

        if (shoppingCart == null) {
            throw new Exception("Shopping cart not found for user.");
        }
        return processRegularPurchase(false, userId);
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
        processRandomWinnings(userId);
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
        processAuctionWinnings(userId);
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
        processBids(userId);
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

    return "Product: " + product.getName() +", Price: " + itemInStore.getPrice() +", Store: " + storeRepo.getStoreNameById(storeId);
    }
        private void mockPayment(CartItem item) {
        System.out.println("Payment successful for: " + item.getProductId());
    }

    private void mockPayment(SingleBid bid) {
        System.out.println("Payment successful for auction bid: "
                + "user=" + bid.getUserId()
                + ", store=" + bid.getStoreId()
                + ", amount=" + bid.getAmount()
                + ", price=" + bid.getBidPrice());
    }

    private void mockSupply(CartItem item) {
        System.out.println("Supply successful for: " + item.getProductId());
    }

    public ReceiptDTO[] processRegularPurchase(boolean isGuest, int userId) throws Exception {
        List<ReceiptDTO> receipts = new ArrayList<>();
        ShoppingCart shoppingCart = userRepo.getUserCart(userId);

        // If guest, validate availability for all items
        if (isGuest) {
            List<ItemCartDTO> allItems = new ArrayList<>();
            for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
                allItems.addAll(basket.getItems());
            }
            if (!storeRepo.checkAvailability(allItems)) {
                throw new Exception("Guest purchase failed: Not all products are available.");
            }
        }

        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            double totalPrice = 0;
            List<ReceiptProduct> boughtItems = new ArrayList<>();
            String storeName = storeRepo.getStoreNameById(basket.getStoreId());

            Store store = storeRepo.findStoreByID(basket.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + basket.getStoreId());
            }

            for (ItemCartDTO dto : basket.getItems()) {
                CartItem item = new CartItem(dto);
                item storeItem = store.getItemByProductId(item.getProductId());

                if (storeItem != null && storeItem.getQuantity() >= item.getQuantity()) {
                    totalPrice += item.getPrice() * item.getQuantity();

                    boughtItems.add(new ReceiptProduct(
                            item.getName(),
                            item.getCategory(),
                            item.getDescription(),
                            storeName,
                            item.getQuantity(),
                            item.getPrice()
                    ));

                    store.decreaseQtoBuy(item.getProductId(), item.getQuantity());
                    mockPayment(item);
                    mockSupply(item);

                } else if (!isGuest) {
                    continue;
                } else {
                    throw new Exception("Unexpected missing product during Guest purchase.");
                }
            }

            if (!boughtItems.isEmpty()) {
                String date = LocalDate.now().toString();
                int finalPrice = (int) totalPrice;

                ReceiptDTO receipt = new ReceiptDTO(storeName, date, boughtItems, finalPrice);
                receipts.add(receipt);
                orderRepo.setOrderToStore(basket.getStoreId(), userId, receipt, storeName);
            } else {
                throw new DevException("bought items is empty!");
            }
        }
        return receipts.toArray(new ReceiptDTO[0]);     
    }

    //buying a random ticket
    public ParticipationInRandomDTO buyRandomTicket(int userId, int randomId, double amountPaid) throws Exception {
        Random random = storeRepo.getRandomById(randomId);
        ParticipationInRandomDTO participation = random.participateInRandom(userId, amountPaid);
        userRepo.ParticipateInRandom(participation); // Save participation in userRepo

        mockPayment(userId, amountPaid, randomId);
        return participation;
    }

    private void mockPayment(int userId, double amountPaid, int randomId) {
        System.out.println("Mock payment: user " + userId + " paid " + amountPaid + " for random ID " + randomId);
    }

    private void processRandomWinnings(int userId) throws Exception {
        List<ParticipationInRandomDTO> cards = userRepo.getWinningCards(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (ParticipationInRandomDTO card : cards) {
            Store store = storeRepo.findStoreByID(card.storeId);
            if (store == null) {
                throw new Exception("Store not found for ID: " + card.storeId);
            }

            item storeItem = store.getItemByProductId(card.productId);
            if (storeItem == null || storeItem.getQuantity() < 1) {
                throw new Exception("Product unavailable for supply in random win");
            }

            store.decreaseQtoBuy(card.productId, 1);

            Product product = stockRepo.findById(card.productId);
            String storeName = storeRepo.getStoreNameById(card.storeId);

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    1,
                    0 // already paid when buying the random ticket
            );
            storeToProducts.computeIfAbsent(card.storeId, k -> new ArrayList<>()).add(receiptProduct);
            System.out.println("Supplying random-won product " + product.getName() + " to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    private void processAuctionWinnings(int userId) throws Exception {
        List<SingleBid> winningBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : winningBids) {
            if (bid.getType() != SpecialType.Auction) {
                continue;
            }

            Store store = storeRepo.findStoreByID(bid.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + bid.getStoreId());
            }

            item storeItem = store.getItemByProductId(bid.getId());
            if (storeItem == null || storeItem.getQuantity() < bid.getAmount()) {
                throw new Exception("Product unavailable for auction bid supply");
            }

            store.decreaseQtoBuy(bid.getId(), bid.getAmount());

            Product product = stockRepo.findById(bid.getId());
            String storeName = storeRepo.getStoreNameById(bid.getStoreId());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            mockPayment(bid);

            System.out.println("Auction product " + product.getName() + " supplied to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    private void processBids(int userId) throws Exception {
        List<SingleBid> acceptedBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : acceptedBids) {
            if (bid.getType() != SpecialType.BID) {
                continue;
            }

            Store store = storeRepo.findStoreByID(bid.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + bid.getStoreId());
            }

            item storeItem = store.getItemByProductId(bid.getId());
            if (storeItem == null || storeItem.getQuantity() < bid.getAmount()) {
                throw new Exception("Unavailable product for bid supply");
            }

            store.decreaseQtoBuy(bid.getId(), bid.getAmount());

            Product product = stockRepo.findById(bid.getId());
            String storeName = storeRepo.getStoreNameById(bid.getStoreId());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            mockPayment(bid);

            System.out.println("Bid product " + product.getName() + " supplied to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    private void createReceiptsPerStore(Map<Integer, List<ReceiptProduct>> storeToProducts, int userId) throws Exception {
        for (Map.Entry<Integer, List<ReceiptProduct>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeRepo.getStoreNameById(storeId);
            List<ReceiptProduct> products = entry.getValue();
            String date = LocalDate.now().toString();
            double finalPrice = products.stream().mapToInt(ReceiptProduct::getPrice).sum();

            ReceiptDTO receipt = new ReceiptDTO(storeName, date, products, finalPrice);
            orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
        }

    }

}

