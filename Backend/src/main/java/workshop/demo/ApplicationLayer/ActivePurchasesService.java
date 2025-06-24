package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.UserAuctionBid;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@Service
public class ActivePurchasesService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    @Autowired
    private SUConnectionRepository suConnectionRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private NotificationService notifier;

    @Autowired
    private IAuthRepo authRepo;

    @Autowired
    private IActivePurchasesRepo activePurchasesRepo;

    @Autowired
    private StockService stock;

    private Timer timer = new Timer();

    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId);
        // adding auction here:
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
        int randomId = active.addProductToAuction(productId, quantity, time, startPrice);
        StoreStock storeStock = storeStockRepo.findById(storeId).orElse(null);
        if(!storeStock.decreaseQuantitytoBuy(productId, quantity)){
            throw new UIException("quantity fsh ", ErrorCodes.INVALID_QUANTITY);
        }
        activePurchasesRepo.save(active);
        // timer : after auction.getTimeLeft() ->>
        // auction.end() + notify all paricipates . notify winner . notify onwers + if
        // the auction has no winner return back the stock
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    active.endAuction(randomId);
                    storeStock.IncreaseQuantitytoBuy(productId, quantity);
                } catch ( UIException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }, time);
        for (Node worker : suConnectionRepo.getOwnersInStore(storeId)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName, "Owner "
                    + userRepo.findById(userId).get().getUsername() + " set a product to auction in your store");
        }
        // return stockRepo.addAuctionToStore(storeId, productId, quantity, time,
        // startPrice);

        return -1;
    }

    private void checkUserRegisterOnline_ThrowException(int userId) throws UIException {
        Optional<Registered> user = userRepo.findById(userId);
        if (!user.isPresent())
            throw new UIException("stock service:user not found!", ErrorCodes.USER_NOT_FOUND);
    }

    private int checkUserAndStore(String token, int storeId) throws Exception {

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        // must add the exceptions here:
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found on db!", ErrorCodes.STORE_NOT_FOUND));
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        return userId;
    }

    public AuctionDTO[] getAllAuctions_user(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        logger.info("Returning auction list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow();

        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        return active.getAuctions();
    }

    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        checkUserAndStore(token, storeId);
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        return active.getAuctions();
    }

    public AuctionDTO[] searchActiveAuctions(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchAuctions with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        List<Product> matchProducts = stock.getMatchProducts(criteria);
        List<AuctionDTO> allAuctions = new ArrayList<>();
        List<ActivePurcheses> actives = new ArrayList<>();
        if (criteria.specificStore()) {
            actives.add(activePurchasesRepo.findById(criteria.getStoreId()).orElseThrow());
        } else {
            actives.addAll(activePurchasesRepo.findAll());
        }
        for (ActivePurcheses activePurcheses : actives) {
            Store store = storeJpaRepo.findById(activePurcheses.getStoreId()).orElse(null);
            if (store == null || !store.isActive())
                continue;
            for (Product product : matchProducts) {
                allAuctions.addAll(activePurcheses.getAuctionsForProduct(product.getProductId(), store.getStoreName(),
                        product.getName()));
            }
        }
        return allAuctions.toArray(new AuctionDTO[0]);
    }

    @Transactional
    public boolean addBidOnAucction(String token, int auctionId, int storeId, double price)
            throws UIException, DevException {
        logger.info("User trying to bid on auction: {}, store: {}", auctionId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        // SingleBid bid = stockRepo.bidOnAuction(storeId, userId, auctionId, price);
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        UserAuctionBid bid = active.addUserBidToAuction(auctionId, userId, price);
        logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
        activePurchasesRepo.flush();
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, auctionId, bid.getId(),
                SpecialType.Auction, bid.getProductId());
        userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
        return true;

    }

}
