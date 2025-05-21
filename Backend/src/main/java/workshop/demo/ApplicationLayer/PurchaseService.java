package workshop.demo.ApplicationLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.*;
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
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountScope;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.IUserRepo;
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
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Autowired
    public PurchaseService(IAuthRepo authRepo, IStockRepo stockRepo, IStoreRepo storeRepo, IUserRepo userRepo,
            IPurchaseRepo purchaseRepo, IOrderRepo orderRepo, IPaymentService paymentService,
            ISupplyService supplyService, IUserSuspensionRepo susRepo) {
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.purchaseRepo = purchaseRepo;
        this.orderRepo = orderRepo;
        this.paymentService = paymentService;
        this.supplyService = supplyService;
        this.susRepo = susRepo;
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

        ShoppingCart cart = userRepo.getUserCart(userId);
        if (cart == null || cart.getAllCart().isEmpty()) {
            logger.warn("Cart is empty for userId={}", userId);

            throw new UIException("Shopping cart is empty or not found", ErrorCodes.CART_NOT_FOUND);
        }

        if (isGuest && !stockRepo.checkAvailability(cart.getAllCart())) {
            logger.warn("Product availability check failed for guest userId={}", userId);

            throw new UIException("Not all items are available for guest purchase", ErrorCodes.PRODUCT_NOT_FOUND);
        }

        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
        for (ShoppingBasket basket : cart.getBaskets().values()) {
            logger.info("Processing basket for storeId={}", basket.getStoreId());
            String storeName = storeRepo.getStoreNameById(basket.getStoreId());
            List<ReceiptProduct> boughtItems = stockRepo.processCartItemsForStore(basket.getStoreId(), //HERE!!!!ASDLKJSAD
                    basket.getItems(), isGuest);
            for (ReceiptProduct product : boughtItems) {
                product.setstoreName(storeName); // need to change
            }
            List<ItemStoreDTO> itemStoreDTOS = new ArrayList<>();
            for(ReceiptProduct p : boughtItems){
                itemStoreDTOS.add(new ItemStoreDTO(p.getProductId(), p.getQuantity(), p.getPrice(), p.getCategory(), 0, basket.getStoreId(), p.getProductName()
                ));
            }
            DiscountScope scope = new DiscountScope(itemStoreDTOS);
            Store store = storeRepo.findStoreByID(basket.getStoreId()); // changed the  get list to use this function instead

            Discount discount = store.getDiscount();
            double discountAmount = (discount!=null)? discount.apply(scope): 0.0;
            double total = stockRepo.calculateTotalPrice(boughtItems);
            double finalTotal = total-discountAmount;
            logger.info("Store={}, Original={}, Discount={}, Final={}", storeName, total, discountAmount, finalTotal);
            paymentService.processPayment(payment, finalTotal);
            supplyService.processSupply(supply);
            storeToProducts.put(basket.getStoreId(), boughtItems);
        }
        return saveReceipts(userId, storeToProducts);
    }

    public ParticipationInRandomDTO participateInRandom(String token, int randomId, int storeId, double amountPaid,
            PaymentDetails paymentDetails) throws Exception {
        logger.info("participateInRandom called with randomId={}, storeId={}", randomId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        ParticipationInRandomDTO card = stockRepo.validatedParticipation(userId, randomId, storeId, amountPaid);
        UserSpecialItemCart item = new UserSpecialItemCart(storeId, card.randomId, -1, SpecialType.Random);
        userRepo.addSpecialItemToCart(item, userId);
        paymentService.processPayment(paymentDetails, amountPaid);
        logger.info("User {} participated in random draw {}", userId, randomId);
        return card;
    }

    public ReceiptDTO[] finalizeSpecialCart(String token, PaymentDetails payment, SupplyDetails supply)
            throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.info("The user " + userId + " finalizing the special cart.");
        List<SingleBid> winningBids = new ArrayList<>(); // Auction wins and Bid wins
        List<ParticipationInRandomDTO> winingRandoms = new ArrayList<>();
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
        for (UserSpecialItemCart specialItem : userRepo.getAllSpecialItems(userId)) {
            if (specialItem.type == SpecialType.Random) {
                ParticipationInRandomDTO card = stockRepo.getRandomCardIfWinner(specialItem.storeId,
                        specialItem.specialId, userId);
                if (card != null)
                    winingRandoms.add(card);
            } else {
                SingleBid bid = stockRepo.getBidIfWinner(specialItem.storeId, specialItem.specialId, specialItem.bidId,
                        specialItem.type);
                if (bid != null)
                    winningBids.add(bid);
            }
        }
        double sumToPay = setRecieptMapForBids(winningBids, storeToProducts);
        setRecieptMapForRandoms(storeToProducts, winingRandoms);
        if (supplyService.processSupply(supply) && paymentService.processPayment(payment, sumToPay)) {
            return saveReceipts(userId, storeToProducts);
        }
        throw new DevException("something went wrong with supply or payment");

    }

    private double setRecieptMapForBids(List<SingleBid> winningBids, Map<Integer, List<ReceiptProduct>> res)
            throws Exception {
        double price = 0;

        // Handle Auction & Accepted Bids
        for (SingleBid bid : winningBids) {
            Product product = stockRepo.findByIdInSystem_throwException(bid.getId());
            if (product == null) {
                logger.warn("Product not found in finalizeSpecialCart for productId={}", bid.getId());
                throw new UIException("Product not available", ErrorCodes.PRODUCT_NOT_FOUND);
            }

            String storeName = storeRepo.getStoreNameById(bid.getStoreId());
            price += bid.getBidPrice();

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice());

            res.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            // paymentService.processPayment(payment, (int) bid.getBidPrice());
        }
        // return res;
        return price;
    }

    private void setRecieptMapForRandoms(Map<Integer, List<ReceiptProduct>> storeToProducts,
            List<ParticipationInRandomDTO> pars) throws Exception {
        for (ParticipationInRandomDTO card : pars) {
            stockRepo.validateAndDecreaseStock(card.storeId, card.productId, 1);
            Product product = stockRepo.findByIdInSystem_throwException(card.productId);
            String storeName = storeRepo.getStoreNameById(card.storeId);

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(), product.getCategory(), product.getDescription(),
                    storeName, 1, 0);

            storeToProducts.computeIfAbsent(card.storeId, k -> new ArrayList<>()).add(receiptProduct);
            // supplyService.processSupply(supply);
        }
    }

    private ReceiptDTO[] saveReceipts(int userId, Map<Integer, List<ReceiptProduct>> storeToProducts   )
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
}
