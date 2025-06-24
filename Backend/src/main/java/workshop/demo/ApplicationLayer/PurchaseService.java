package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.Order;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.UserAuctionBid;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@Service
public class PurchaseService {

    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private IStockRepo stockRepo;
    @Autowired
    private IOrderRepoDB orderJpaRepo;
    // private final IUserRepo userRepo;
    @Autowired
    private IPurchaseRepo purchaseRepo;
    @Autowired
    private IPaymentService paymentService;
    @Autowired
    private ISupplyService supplyService;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    @Autowired
    private UserJpaRepository regRepo;
    @Autowired
    private GuestJpaRepository guestRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private IStockRepoDB stockJpaRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private IActivePurchasesRepo activeRepo;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Transactional(rollbackFor = UIException.class)
    public ReceiptDTO[] buyGuestCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails)
            throws Exception {
        logger.info("buyGuestCart called with token");

        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in buyGuestCart");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        return processCart(userId, true, paymentdetails, supplydetails);
    }

    @Transactional(rollbackFor = UIException.class)
    public ReceiptDTO[] buyRegisteredCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails)
            throws Exception {
        logger.info("buyRegisteredCart called with token");

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        return processCart(userId, false, paymentdetails, supplydetails);
    }

    @Transactional(rollbackFor = UIException.class)
    public ReceiptDTO[] processCart(int userId, boolean isGuest, PaymentDetails payment, SupplyDetails supply)
            throws Exception {
        logger.info("processCart called for userId={}, isGuest={}", userId, isGuest);
        Guest user = getUser(isGuest, userId);
        if (user.emptyCart()) {
            throw new UIException("Shopping cart is empty or not found", ErrorCodes.CART_NOT_FOUND);
        }
        Map<Integer, Pair<List<ReceiptProduct>, Double>> storeToProducts = new HashMap<>();
        double finalTotal = 0;
        for (ShoppingBasket basket : user.getBaskets()) {
            double totalForStore = 0;
            int storeId = basket.getStoreId();
            Store store = storeJpaRepo.findById(storeId)
                    .orElseThrow(() -> new UIException("store not found on db!", ErrorCodes.STORE_NOT_FOUND));
            if (store == null || !store.isActive()) {
                if (isGuest) {
                    throw new UIException(store.getStoreName(), ErrorCodes.STORE_NOT_FOUND);
                }
                logger.info("Skipping inactive or missing storeId={}", storeId);
                continue; // Skip this store
            }

            String storeName = store.getStoreName();
            logger.info("Processing basket for active storeId={} ({})", storeId, storeName);
            List<ReceiptProduct> boughtItems = new ArrayList<>();
            StoreStock stock = storeStockRepo.findById(storeId).orElseThrow();
            for (CartItem itemOnUserCart : basket.getItems()) {
                if (stock.decreaseQuantitytoBuy(itemOnUserCart.productId, itemOnUserCart.quantity)) {
                    user.removeItem(itemOnUserCart.getId());
                    // ADD DISSCOUNT HERE ... HMODE
                    double price = itemOnUserCart.price * itemOnUserCart.quantity;
                    // ADD DISSCOUNT HERE ... HMODE
                    ReceiptProduct boughtItem = new ReceiptProduct(itemOnUserCart.name, storeName,
                            itemOnUserCart.quantity, itemOnUserCart.price, itemOnUserCart.productId,
                            itemOnUserCart.category, itemOnUserCart.storeId);
                    boughtItems.add(boughtItem);

                    totalForStore += price;
                } else if (isGuest) {
                    logger.info("user guest tring to buy items with not enough stock on the store ... " + userId);
                    throw new UIException(store.getStoreName(), ErrorCodes.INSUFFICIENT_STOCK);
                }
            }
            finalTotal += totalForStore;
            storeToProducts.put(storeId, Pair.of(boughtItems, totalForStore));
        }
        if (!paymentService.processPayment(payment, finalTotal) || !supplyService.processSupply(supply)) {
            logger.info("payment failed!!");
            throw new UIException("payment not successeded!!!", ErrorCodes.PAYMENT_ERROR);
        }
        storeStockRepo.flush();
        return saveReceiptsWithDiscount(userId, storeToProducts);
    }

    @Transactional
    public Guest getUser(boolean isGuest, int userId) throws UIException {
        if (isGuest) {
            Optional<Guest> guset = guestRepo.findById(userId);

            if (guset.isPresent()) {

                return guset.get();
            } else {
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
            }
        } else {
            Optional<Registered> user = regRepo.findById(userId);
            if (user.isPresent()) {

                return user.get();
            } else {
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
            }
        }
    }

    public ParticipationInRandomDTO participateInRandom(String token, int randomId, int storeId, double amountPaid,
            PaymentDetails paymentDetails) throws Exception {
        logger.info("participateInRandom called with randomId={}, storeId={}", randomId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        Registered user = regRepo.findById(userId)
                .orElseThrow(() -> new UIException("user not logged in!", ErrorCodes.USER_NOT_LOGGED_IN));
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        ParticipationInRandomDTO card = stockRepo.validatedParticipation(userId, randomId, storeId, amountPaid);
        UserSpecialItemCart item = new UserSpecialItemCart(storeId, card.randomId, userId, SpecialType.Random, -1);
        user.addSpecialItemToCart(item);
        boolean done = paymentService.processPayment(paymentDetails, amountPaid);
        if (!done) {
            logger.error("Payment failed for userId={}, amountPaid={}", userId, amountPaid);
            throw new UIException("Payment failed", ErrorCodes.PAYMENT_ERROR);
        } else {
            // i really dont know ??
            card.transactionIdForPayment = 1;
        }
        logger.info("User {} participated in random draw {}", userId, randomId);
        regRepo.save(user);
        return card;
    }

    // @Transactional
    public ReceiptDTO[] finalizeSpecialCart(String token, PaymentDetails payment, SupplyDetails supply)
            throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        logger.info("The user " + userId + " finalizing the special cart.");
        Registered user = regRepo.findById(userId).orElseThrow(() -> notLoggedInException());
        // winning!!
        List<SingleBid> winningBids = new ArrayList<>();
        List<ParticipationInRandomDTO> winningRandoms = new ArrayList<>();
        List<UserAuctionBid> winningAuctions = new ArrayList<>();

        List<UserSpecialItemCart> itemsToRemove = new ArrayList<>();
        List<UserSpecialItemCart> allSpecialItems = user.getAllSpecialItems();
        for (UserSpecialItemCart specialItem : allSpecialItems) {
            logger.info("one special item on the cart!!");
            if (specialItem.type == SpecialType.Random) {
                // DO NOT DELETE THIS CODE!!!!!!!!!!!!
                // ParticipationInRandomDTO card =
                // stockRepo.getRandomCardforuser(specialItem.storeId, specialItem.specialId,
                // userId);
                // if (card != null && card.isWinner && card.ended) {
                // winningRandoms.add(card); // Won
                // } else if ((card != null && !card.isWinner && card.ended) || card == null) {
                // user.removeSpecialItem(specialItem); // Lost or not found → remove
                // } else if (card.mustRefund()) {
                // // If the card must be refunded, we remove it from the user's cart
                // user.removeSpecialItem(specialItem);
                // paymentService.processRefund(payment, card.amountPaid);
                // }
                // DO NOT DELETE THIS CODE!!!!!!!!!!!!
            } else if (specialItem.type == SpecialType.Auction) { // AUCTION
                ActivePurcheses active = activeRepo.findById(specialItem.storeId).orElse(null);
                Auction auction = active.getAuctionById(specialItem.specialId);
                auction.endAuction();
                if (auction.isEnded()) {
                    if (auction.bidIsWinner(specialItem.bidId)) {
                        winningAuctions.add(auction.getBid(specialItem.bidId));
                        logger.info("user win bid. id:" + specialItem.bidId);
                    }
                    // user.removeSpecialItem(specialItem);
                    itemsToRemove.add(specialItem);
                    logger.info("one auction bid must be removed bid id:" + specialItem.bidId);
                }
            }
        }
        logger.info("hiiiiiiiiiiiii");
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
        double sumToPay = setRecieptMapForBids(winningBids, storeToProducts);
        sumToPay += setRecieptMapForAuctions(winningAuctions, storeToProducts);
        setRecieptMapForRandoms(storeToProducts, winningRandoms);
        logger.info("finilizing cart with price:" + sumToPay);
        if (!supplyService.processSupply(supply) || !paymentService.processPayment(payment, sumToPay)) {
            logger.info("payment for user failed ");
            throw new UIException("payment or supply went wrong!", ErrorCodes.PAYMENT_ERROR);
        }
        user.clearSpecialCart(itemsToRemove);
        activeRepo.flush();
        regRepo.saveAndFlush(user);
        logger.info("saving changes with user cart !");
        return saveReceipts(userId, storeToProducts);
    }

    private double setRecieptMapForAuctions(List<UserAuctionBid> winningAuctions,
            Map<Integer, List<ReceiptProduct>> res) throws UIException {
        logger.info("-------------");
        double price = 0;

        // Handle Auction & Accepted Bids
        for (UserAuctionBid bid : winningAuctions) {
            logger.info("one bid must be converted to recipt!!");
            Product product = stockJpaRepo.findById(bid.getProductId()).orElse(null);
            if (product == null) {
                logger.warn("Product not found in finalizeSpecialCart for productId={}", bid.getId());
                throw new UIException("Product not available", ErrorCodes.PRODUCT_NOT_FOUND);
            }

            String storeName = storeJpaRepo.findById(bid.getStoreId())
                    .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                    .getStoreName();
            price += bid.getBidPrice();

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice(),
                    bid.getProductId(),
                    product.getCategory(), bid.getStoreId());

            res.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            // paymentService.processPayment(payment, (int) bid.getBidPrice());

        }
        // return res;
        return price;
    }

    private UIException notLoggedInException() {
        return new UIException("user not loggedIn", ErrorCodes.USER_NOT_LOGGED_IN);
    }

    public double setRecieptMapForBids(List<SingleBid> winningBids, Map<Integer, List<ReceiptProduct>> res)
            throws Exception {
        double price = 0;

        // Handle Auction & Accepted Bids
        for (SingleBid bid : winningBids) {
            Product product = stockJpaRepo.findById(bid.productId()).orElse(null);
            if (product == null) {
                logger.warn("Product not found in finalizeSpecialCart for productId={}", bid.getId());
                throw new UIException("Product not available", ErrorCodes.PRODUCT_NOT_FOUND);
            }

            String storeName = storeJpaRepo.findById(bid.getStoreId())
                    .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                    .getStoreName();
            price += bid.getBidPrice();

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice(),
                    bid.productId(),
                    product.getCategory(), bid.getStoreId());

            res.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            // paymentService.processPayment(payment, (int) bid.getBidPrice());

        }
        // return res;
        return price;
    }

    private void setRecieptMapForRandoms(Map<Integer, List<ReceiptProduct>> storeToProducts,
            List<ParticipationInRandomDTO> pars) throws Exception {
        for (ParticipationInRandomDTO card : pars) {
            // stockRepo.validateAndDecreaseStock(card.storeId, card.productId, 1);
            Product product = stockJpaRepo.findById(card.productId)
                    .orElseThrow(() -> new UIException("product not found!", ErrorCodes.PRODUCT_NOT_FOUND));
            String storeName = storeJpaRepo.findById(card.storeId)
                    .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                    .getStoreName();

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    storeName,
                    1,
                    0,
                    product.getProductId(),
                    product.getCategory(), card.storeId);

            storeToProducts.computeIfAbsent(card.storeId, k -> new ArrayList<>()).add(receiptProduct);
            // supplyService.processSupply(supply);
        }
    }

    private ReceiptDTO[] saveReceipts(int userId, Map<Integer, List<ReceiptProduct>> storeToProducts)
            throws UIException {
        logger.info("saveReceipts called for userId={}", userId);

        List<ReceiptDTO> receipts = new ArrayList<>();
        for (Map.Entry<Integer, List<ReceiptProduct>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeJpaRepo.findById(storeId)
                    .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                    .getStoreName();
            List<ReceiptProduct> items = entry.getValue();
            double total = items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            // changed this to do price*qunatity instead of just price itself
            ReceiptDTO receipt = new ReceiptDTO(storeName, LocalDate.now().toString(), items, total);
            receipts.add(receipt);
            // orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
            Order order = new Order(userId, receipt, storeName);
            orderJpaRepo.save(order);

            logger.info("Saved receipt for storeId={}, total={}", storeId, total);

        }
        logger.info(receipts.size()+" reciept items must be return");
        return receipts.toArray(new ReceiptDTO[0]);
    }

    @Transactional
    private ReceiptDTO[] saveReceiptsWithDiscount(int userId,
            Map<Integer, Pair<List<ReceiptProduct>, Double>> storeToProducts)
            throws UIException {
        logger.info("saveReceipts called for userId={}", userId);

        List<ReceiptDTO> receipts = new ArrayList<>();
        for (Map.Entry<Integer, Pair<List<ReceiptProduct>, Double>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeJpaRepo.findById(storeId)
                    .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                    .getStoreName();
            List<ReceiptProduct> items = entry.getValue().getLeft();
            double discountedTotal = entry.getValue().getRight();
            double total = items.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            // changed this to do price*qunatity instead of just price itself
            ReceiptDTO receipt = new ReceiptDTO(storeName, LocalDate.now().toString(), items, discountedTotal);
            receipts.add(receipt);
            // orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
            Order order = new Order(userId, receipt, storeName);
            orderJpaRepo.save(order);
            logger.info("Saved receipt for storeId={}, total={}", storeId, total);

        }
        return receipts.toArray(new ReceiptDTO[0]);
    }
}
