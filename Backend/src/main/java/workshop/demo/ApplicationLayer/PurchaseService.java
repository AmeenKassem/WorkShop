package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
import workshop.demo.DomainLayer.Purchase.ISupplyService;
import workshop.demo.DomainLayer.Stock.IStockRepo;
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
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

@Service
public class PurchaseService {

    private final IAuthRepo authRepo;
    private final IStockRepo stockRepo;
    private final IStoreRepo storeRepo;
    private final IUserRepo userRepo;
    private final IPurchaseRepo purchaseRepo;
    private IOrderRepo orderRepo;
    private final IPaymentService paymentService;
    private final ISupplyService supplyService;
    private IUserSuspensionRepo susRepo;
    private UserJpaRepository regRepo;
    private GuestJpaRepository guestRepo;
    private IStoreRepoDB storeJpaRepo;
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Autowired
    public PurchaseService(IAuthRepo authRepo, IStockRepo stockRepo, IStoreRepo storeRepo, IUserRepo userRepo,
            IPurchaseRepo purchaseRepo, IOrderRepo orderRepo, IPaymentService paymentService,
            ISupplyService supplyService, IUserSuspensionRepo susRepo, UserJpaRepository regsRepo,
            GuestJpaRepository guestRepo,IStoreRepoDB storeJpaRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
        this.supplyService = supplyService;
        this.susRepo = susRepo;
        this.guestRepo = guestRepo;
        this.regRepo = regsRepo;
        this.storeJpaRepo= storeJpaRepo;
    }

    public ReceiptDTO[] buyGuestCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails)
            throws Exception {
        logger.info("buyGuestCart called with token");

        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in buyGuestCart");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return processCart(userId, true, paymentdetails, supplydetails);
    }

    public ReceiptDTO[] buyRegisteredCart(String token, PaymentDetails paymentdetails, SupplyDetails supplydetails)
            throws Exception {
        logger.info("buyRegisteredCart called with token");

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return processCart(userId, false, paymentdetails, supplydetails);
    }

    private ReceiptDTO[] processCart(int userId, boolean isGuest, PaymentDetails payment, SupplyDetails supply)
            throws Exception {
        logger.info("processCart called for userId={}, isGuest={}", userId, isGuest);

        Guest user = getUser(isGuest, userId);

        if (user.emptyCart())
            throw new UIException("Shopping cart is empty or not found", ErrorCodes.CART_NOT_FOUND);

        if (isGuest && checkStockAvalibality(user.getBaskets()))
            throw new UIException("Not all items are available for guest purchase", ErrorCodes.PRODUCT_NOT_FOUND);

        Map<Integer, Pair<List<ReceiptProduct>, Double>> storeToProducts = new HashMap<>();

        for (ShoppingBasket basket : user.getBaskets()) {
            int storeId = basket.getStoreId();
            Store store =storeJpaRepo.findById(storeId).orElseThrow(()-> new UIException("store not found on db!", ErrorCodes.STORE_NOT_FOUND));
            if (store == null || !store.isActive()) {
                if(isGuest){
                    throw new UIException(store.getStoreName(), ErrorCodes.STORE_NOT_FOUND);
                }
                logger.info("Skipping inactive or missing storeId={}", storeId);
                continue; // Skip this store
            }

            String storeName = store.getStoreName();
            logger.info("Processing basket for active storeId={} ({})", storeId, storeName);

            List<ReceiptProduct> boughtItems = stockRepo.processCartItemsForStore(
                    storeId, basket.getItems(), isGuest, storeName);

            for (ReceiptProduct product : boughtItems) {
                product.setstoreName(storeName);
            }

            List<ItemStoreDTO> itemStoreDTOS = new ArrayList<>();
            for (ReceiptProduct p : boughtItems) {
                itemStoreDTOS.add(new ItemStoreDTO(
                        p.getProductId(), p.getQuantity(), p.getPrice(), p.getCategory(),
                        0, storeId, p.getProductName(), storeName));
            }

            DiscountScope scope = new DiscountScope(itemStoreDTOS);
            // Hmode
            UserDTO buyer = userRepo.getUserDTO(userId);
            store.assertPurchasePolicies(buyer, itemStoreDTOS);
            // Hmode
            Discount discount = store.getDiscount();
            double discountAmount = (discount != null) ? discount.apply(scope) : 0.0;
            double total = stockRepo.calculateTotalPrice(boughtItems);
            double finalTotal = total - discountAmount;

            logger.info("Store={}, Original={}, Discount={}, Final={}", storeName, total, discountAmount, finalTotal);

            paymentService.processPayment(payment, finalTotal);
            supplyService.processSupply(supply);
            stockRepo.changequantity(storeId, basket.getItems(), isGuest, storeName);

            storeToProducts.put(storeId, Pair.of(boughtItems, finalTotal));
        }
        user.clearCart();
        guestRepo.save(user);

        return saveReceiptsWithDiscount(userId, storeToProducts);
    }

    private boolean checkStockAvalibality(Collection<ShoppingBasket> baskets) {
        for (ShoppingBasket shoppingBasket : baskets) {
            StoreStock stock4store = stockRepo.findStoreStockById(shoppingBasket.getStoreId());
            for (CartItem item  : shoppingBasket.getItems()) {
                if(!stock4store.isAvaliable(item.productId, item.quantity))return false;
            }
        }
        return true;
    }

    private Guest getUser(boolean isGuest, int userId) throws UIException {
        if (isGuest) {
            Optional<Guest> guset = guestRepo.findById(userId);
            if (guset.isPresent())
                return guset.get();
            else
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
        } else {
            Optional<Registered> user = regRepo.findById(userId);
            if (user.isPresent())
                return user.get();
            else
                throw new UIException("there is no guest with given id", ErrorCodes.USER_NOT_FOUND);
        }
    }

    public ParticipationInRandomDTO participateInRandom(String token, int randomId, int storeId, double amountPaid,
            PaymentDetails paymentDetails) throws Exception {
        logger.info("participateInRandom called with randomId={}, storeId={}", randomId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        ParticipationInRandomDTO card = stockRepo.validatedParticipation(userId, randomId, storeId, amountPaid);
        UserSpecialItemCart item = new UserSpecialItemCart(storeId, card.randomId, userId, SpecialType.Random);
        userRepo.addSpecialItemToCart(item, userId);
        boolean done = paymentService.processPayment(paymentDetails, amountPaid);
        if (!done) {
            logger.error("Payment failed for userId={}, amountPaid={}", userId, amountPaid);
            throw new UIException("Payment failed", ErrorCodes.PAYMENT_ERROR);
        } else {
            // i really dont know ??
            card.transactionIdForPayment = 1;
        }
        logger.info("User {} participated in random draw {}", userId, randomId);
        return card;
    }

    public ReceiptDTO[] finalizeSpecialCart(String token, PaymentDetails payment, SupplyDetails supply)
            throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.info("The user " + userId + " finalizing the special cart.");

        List<SingleBid> winningBids = new ArrayList<>();
        List<ParticipationInRandomDTO> winningRandoms = new ArrayList<>();
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        List<UserSpecialItemCart> allSpecialItems = new ArrayList<>(userRepo.getAllSpecialItems(userId));

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
                    userRepo.removeSpecialItem(userId, specialItem); // Lost or not found → remove
                } else if (card.mustRefund()) {
                    // If the card must be refunded, we remove it from the user's cart
                    userRepo.removeSpecialItem(userId, specialItem);
                    // And we refund the payment
                    // paymentService.externalRefund(card.transactionIdForPayment);
                    stockRepo.returnProductToStock(specialItem.storeId, card.productId, 1, specialItem.specialId);
                    stockRepo.markRefunded(specialItem.specialId);
                } else if (card.mustRefund()) {
                    // If the card must be refunded, we remove it from the user's cart
                    userRepo.removeSpecialItem(userId, specialItem);
                    // And we refund the payment

                    paymentService.processRefund(payment, card.amountPaid);
                }

            } else { // BID or AUCTION
                SingleBid bid = stockRepo.getBidIfWinner(
                        specialItem.storeId, specialItem.specialId, specialItem.bidId, specialItem.type);

                if (bid != null && bid.isWinner() && bid.isEnded()) {
                    winningBids.add(bid); // Won
                } else if ((bid != null && !bid.isWinner() && bid.isEnded()) || bid == null) {
                    userRepo.removeSpecialItem(userId, specialItem); // Lost or not found → remove
                }
            }
        }

        double sumToPay = setRecieptMapForBids(winningBids, storeToProducts);
        setRecieptMapForRandoms(storeToProducts, winningRandoms);

        if (supplyService.processSupply(supply) && paymentService.processPayment(payment, sumToPay)) {
            userRepo.removeBoughtSpecialItems(userId, winningBids, winningRandoms); // Only remove bought
            return saveReceipts(userId, storeToProducts);
        }

        throw new DevException("Something went wrong with supply or payment");
    }

    public double setRecieptMapForBids(List<SingleBid> winningBids, Map<Integer, List<ReceiptProduct>> res)
            throws Exception {
        double price = 0;

        // Handle Auction & Accepted Bids
        for (SingleBid bid : winningBids) {
            Product product = stockRepo.findByIdInSystem_throwException(bid.productId());
            if (product == null) {
                logger.warn("Product not found in finalizeSpecialCart for productId={}", bid.getId());
                throw new UIException("Product not available", ErrorCodes.PRODUCT_NOT_FOUND);
            }

            String storeName = storeRepo.getStoreNameById(bid.getStoreId());
            price += bid.getBidPrice();

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice(),
                    bid.productId(),
                    product.getCategory());

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
            Product product = stockRepo.findByIdInSystem_throwException(card.productId);
            String storeName = storeRepo.getStoreNameById(card.storeId);

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    storeName,
                    1,
                    0,
                    product.getProductId(),
                    product.getCategory());

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
            String storeName = storeRepo.getStoreNameById(storeId);
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

    private ReceiptDTO[] saveReceiptsWithDiscount(int userId,
            Map<Integer, Pair<List<ReceiptProduct>, Double>> storeToProducts)
            throws UIException {
        logger.info("saveReceipts called for userId={}", userId);

        List<ReceiptDTO> receipts = new ArrayList<>();
        for (Map.Entry<Integer, Pair<List<ReceiptProduct>, Double>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeRepo.getStoreNameById(storeId);
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
