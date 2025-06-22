package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
// import jakarta.transaction.Transactional;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.IStockRepoDB;
import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountScope;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
// import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

    private final IAuthRepo authRepo;
    private final IStockRepo stockRepo;
    private final IStoreRepo storeRepo;
    // private final IUserRepo userRepo;
    private final IPurchaseRepo purchaseRepo;
    private IOrderRepo orderRepo;
    private final IPaymentService paymentService;
    private final ISupplyService supplyService;
    private UserSuspensionJpaRepository suspensionJpaRepo;
    private UserJpaRepository regRepo;
    private GuestJpaRepository guestRepo;
    private IStoreRepoDB storeJpaRepo;
    private IStockRepoDB stockJpaRepo;
    private IStoreStockRepo storeStockRepo;

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Autowired
    public PurchaseService(IAuthRepo authRepo, IStockRepo stockRepo, IStoreRepo storeRepo,
            IPurchaseRepo purchaseRepo, IOrderRepo orderRepo, IPaymentService paymentService,
            ISupplyService supplyService, UserSuspensionJpaRepository usersuspentionjpa, UserJpaRepository regsRepo,
            GuestJpaRepository guestRepo, IStoreRepoDB storeJpaRepo, IStoreStockRepo storeStockRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        // this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
        this.supplyService = supplyService;
        this.suspensionJpaRepo = usersuspentionjpa;
        this.guestRepo = guestRepo;
        this.regRepo = regsRepo;
        this.storeJpaRepo = storeJpaRepo;
        this.storeStockRepo = storeStockRepo;
    }

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
        if (user.emptyCart())
            throw new UIException("Shopping cart is empty or not found", ErrorCodes.CART_NOT_FOUND);
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
            }

            else
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
        } else {
            Optional<Registered> user = regRepo.findById(userId);
            if (user.isPresent()) {

                return user.get();
            } else
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
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
        UserSpecialItemCart item = new UserSpecialItemCart(storeId, card.randomId, userId, SpecialType.Random);
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
        List<SingleBid> winningBids = new ArrayList<>();
        List<ParticipationInRandomDTO> winningRandoms = new ArrayList<>();
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        List<UserSpecialItemCart> allSpecialItems = user.getAllSpecialItems();
        List<UserSpecialItemCart> toPay = new ArrayList<>();
        for (UserSpecialItemCart specialItem : allSpecialItems) {

            if (specialItem.type == SpecialType.Random) {
                ParticipationInRandomDTO card = stockRepo.getRandomCardforuser( // had to change this to get card for
                                                                                // user to be able to do refund if the
                                                                                // card stays null we never reach the
                                                                                // refund statement
                        specialItem.storeId, specialItem.specialId, userId);

                if (card != null && card.isWinner && card.ended) {
                    winningRandoms.add(card); // Won

                } else if ((card != null && !card.isWinner && card.ended) || card == null) {
                    user.removeSpecialItem(specialItem); // Lost or not found → remove
                } else if (card.mustRefund()) {
                    // If the card must be refunded, we remove it from the user's cart
                    user.removeSpecialItem(specialItem);
                    // And we refund the payment
                    // paymentService.externalRefund(card.transactionIdForPayment);
                    stockRepo.returnProductToStock(specialItem.storeId, card.productId, 1, specialItem.specialId);
                    stockRepo.markRefunded(specialItem.specialId);
                } else if (card.mustRefund()) {
                    // If the card must be refunded, we remove it from the user's cart
                    user.removeSpecialItem(specialItem);
                    // And we refund the payment

                    paymentService.processRefund(payment, card.amountPaid);
                }

            } else { // BID or AUCTION
                SingleBid bid = stockRepo.getBidIfWinner(
                        specialItem.storeId, specialItem.specialId, specialItem.bidId, specialItem.type);

                if (bid != null && bid.isWinner() && bid.isEnded()) {
                    winningBids.add(bid); // Won
                } else if ((bid != null && !bid.isWinner() && bid.isEnded()) || bid == null) {
                    user.removeSpecialItem(specialItem); // Lost or not found → remove
                }
            }
        }

        double sumToPay = setRecieptMapForBids(winningBids, storeToProducts);
        setRecieptMapForRandoms(storeToProducts, winningRandoms);

        if (supplyService.processSupply(supply) && paymentService.processPayment(payment, sumToPay)) {
            // user.removeBoughtSpecialItems(userId, winningBids, winningRandoms); // Only
            // remove bought
            user.clearSpecialCart();
            return saveReceipts(userId, storeToProducts);
        }

        throw new DevException("Something went wrong with supply or payment");
    }

    private UIException notLoggedInException() {
        return new UIException("user not loggedIn", ErrorCodes.USER_NOT_LOGGED_IN);
    }

    public double setRecieptMapForBids(List<SingleBid> winningBids, Map<Integer, List<ReceiptProduct>> res)
            throws Exception {
        double price = 0;

        // Handle Auction & Accepted Bids
        for (SingleBid bid : winningBids) {
            Product product = stockJpaRepo.findById(bid.productId())
                    .orElseThrow(() -> new UIException("product not found!", ErrorCodes.PRODUCT_NOT_FOUND));
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
            orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
            logger.info("Saved receipt for storeId={}, total={}", storeId, total);

        }
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
            orderRepo.setOrderToStore(storeId, userId, receipt, storeName);
            logger.info("Saved receipt for storeId={}, total={}", storeId, total);

        }
        return receipts.toArray(new ReceiptDTO[0]);
    }
}
