package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
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
import workshop.demo.DomainLayer.Stock.Random;

import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.UserAuctionBid;
import workshop.demo.DomainLayer.Store.Store;

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
    private ApplicationContext context;

    @Autowired
    private IAuthRepo authRepo;

    @Autowired
    private IActivePurchasesRepo activePurchasesRepo;

    @Autowired
    private StockService stock;

    @Autowired
    private LockManager lockManager;

    // private Timer timer = new Timer();

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleSpecialPurchases() {
        logger.info("loading all auctions !!!!");
        List<ActivePurcheses> special = activePurchasesRepo.findAll();
        for (ActivePurcheses active : special) {
            StoreStock storeStock = storeStockRepo.findById(active.getStoreId()).orElseThrow();
            Store store = storeJpaRepo.findById(active.getStoreId()).orElseThrow();
            for (Auction auction : active.getActiveAuctions()) {
                if (!auction.isEnded()) {
                    auction.loadBids();
                    scheduleAuctionEnd(active, auction.getRestMS(), storeStock, auction.getId(), auction.getProductId(),
                            auction.getAmount(), store);
                }
            }
            for (Random random : active.getActiveRandoms()) {
                if (random.isActive()) {
                    scheduleRandomEnd(active, random.getRestMS(), storeStock, random.getRandomId(),
                            random.getProductId(),
                            random.getQuantity(), store, random);
                }
            }
        }

    }

    @Transactional
    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId);
        // adding auction here:
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
        Auction auction = active.addProductToAuction(productId, quantity, time, startPrice);
        StoreStock storeStock = storeStockRepo.findById(storeId).orElse(null);
        if (!storeStock.decreaseQuantitytoBuy(productId, quantity)) {
            throw new UIException("quantity fsh ", ErrorCodes.INVALID_QUANTITY);
        }
        storeStockRepo.saveAndFlush(storeStock);
        activePurchasesRepo.save(active);
        // timer : after auction.getTimeLeft() ->>
        // auction.end() + notify all paricipates . notify winner . notify onwers + if
        // the auction has no winner return back the stock
        Store store = storeJpaRepo.findById(storeId).orElse(null);
        scheduleAuctionEnd(active, time, storeStock, auction.getId(), productId, quantity, store);
        List<Integer> ownersIds = new ArrayList<>();
        suConnectionRepo.getOwnersInStore(storeId).forEach(user -> ownersIds.add(user.getMyId()));
        notifier.sendMessageForUsers(
                "Owner " + userRepo.findById(userId).get().getUsername() + " set a product to auction in your store",
                ownersIds);

        return auction.getId();
    }

    public void scheduleRandomEnd(ActivePurcheses active, long time, StoreStock storeStock, int randomId,
            int productId, int quantity, Store store, Random random) {
        if (time <= 0)
            time = 1;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // instead of directly calling `runAuctionEndTask(...)`
                    // get the proxy of the current bean from Spring context
                    ActivePurchasesService self = context.getBean(ActivePurchasesService.class);
                    self.runRandomEndTask(active, storeStock, randomId, productId, quantity, store, random);
                } catch (UIException | DevException e) {
                    e.printStackTrace();
                }
            }

        }, time);
    }

    public void scheduleAuctionEnd(ActivePurcheses active, long time, StoreStock storeStock, int auctionId,
            int productId, int quantity, Store store) {
        if (time <= 0)
            time = 1;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // instead of directly calling `runAuctionEndTask(...)`
                    // get the proxy of the current bean from Spring context
                    ActivePurchasesService self = context.getBean(ActivePurchasesService.class);
                    self.runAuctionEndTask(active, auctionId, storeStock, productId, store);
                } catch (UIException | DevException e) {
                    e.printStackTrace();
                }
            }

        }, time);
    }

    @Transactional
    public void runRandomEndTask(ActivePurcheses active, StoreStock storeStock, int randomId,
            int productId, int quantity, Store store, Random random) throws UIException, DevException {
        synchronized (lockManager.getRandomLock(randomId)) {
            try {
                StoreStock tempStoreStock = storeStockRepo.findById(storeStock.getStoreStockId()).orElseThrow();
                ActivePurcheses tempActivePurchases = activePurchasesRepo.findById(active.getStoreId()).orElseThrow();
                Random tempRandom = tempActivePurchases.getRandom(randomId);
                logger.info("random must be endddddddd! , random id:" + tempRandom.getRandomId());
                // random.endRandom();
                tempRandom.setActivePurchases(tempActivePurchases);

                if (tempRandom.isActive()) {
                    tempRandom.setActive(false);
                    tempRandom.setCancel(true);
                    logger.info("must refund the quantity of random!");
                    tempStoreStock.IncreaseQuantitytoBuy(productId, quantity);
                    storeStockRepo.saveAndFlush(tempStoreStock);
                    tempRandom.mustRefundAllParticipations();
                } else {
                    logger.info("random already ended, no need to refund quantity");
                }

                activePurchasesRepo.saveAndFlush(tempActivePurchases);
                List<Integer> paricpationIds = tempRandom.getParticipationsUsersIds();
                notifier.sendMessageForUsers(
                        "Random on store :" + store.getStoreName() + ", on product:" + productId + " has ended!",
                        paricpationIds);
                List<Integer> ownersIds = new ArrayList<>();
                suConnectionRepo.getOwnersInStore(store.getstoreId())
                        .forEach(user -> ownersIds.add(user.getMyId()));
                notifier.sendMessageForUsers(
                        " Random has ended!",
                        ownersIds);

            } catch (UIException | DevException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Transactional
    public void runAuctionEndTask(ActivePurcheses active, int auctionId, StoreStock storeStock, int productId,
            Store store) throws UIException, DevException {
        logger.info("Running auction end task for auction id: {}", auctionId);
        ActivePurcheses a = activePurchasesRepo.findById(active.getStoreId()).orElseThrow();
        Auction auction = a.getAuctionById(auctionId);
        auction.loadBids();
        auction.endAuction();
        auction.setActivePurchases(a);

        String productName = stock.getProductById(auction.getProductId()).getName();
        activePurchasesRepo.saveAndFlush(a);

        if (auction.mustReturnToStock()) {
            logger.info("Refunding auction quantity to stock");
            storeStock.IncreaseQuantitytoBuy(productId, auction.getAmount());
            storeStockRepo.saveAndFlush(storeStock);
        }

        List<Integer> participants = auction.getBidsUsersIds();
        notifier.sendMessageForUsers(
                "Auction on store: " + store.getStoreName() + " with product " + productName + " has ended!",
                participants);

        List<Integer> owners = suConnectionRepo.getOwnersInStore(store.getstoreId())
                .stream().map(user -> user.getMyId()).toList();
        notifier.sendMessageForUsers("Auction on " + productName + " has ended!", owners);
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
        AuctionDTO[] auctions = active.getAuctions();
        for (AuctionDTO auctionDTO : auctions) {
            if (auctionDTO.winnerUserId != -1) {
                auctionDTO.winnerUserName = userRepo.findById(auctionDTO.winnerUserId).orElse(new Registered())
                        .getUsername();
            }
            auctionDTO.productName = stock.getProductById(auctionDTO.productId).getName();
        }
        logger.info("we have to return to te user an array of auctions. size: " + auctions.length);
        return auctions;
    }

    // public AuctionDTO[] getAllAuctions(String token, int storeId) throws
    // Exception {
    // checkUserAndStore(token, storeId);
    // ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
    // return active.getAuctions();
    // }

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
                synchronized
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
        int userLoosedTopId = active.getCurrAuctionTop(auctionId);
        UserAuctionBid bid = active.addUserBidToAuction(auctionId, userId, price);
        logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
        if (userLoosedTopId != userId && userLoosedTopId != -1)
            notifier.sendMessageToUser(userLoosedTopId, "You are loosing the top of auction!");
        activePurchasesRepo.flush();
        logger.info("bid saved to user!");
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, auctionId, bid.getId(),
                SpecialType.Auction, bid.getProductId());
        userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
        return true;

    }

    @Transactional
    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long randomTime) throws UIException, DevException, Exception {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId);
        // adding random here:
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
        Random random = active.addProductToRandom(productId, quantity, productPrice, storeId, randomTime);
        StoreStock storeStock = storeStockRepo.findById(storeId).orElse(null);
        if (!storeStock.decreaseQuantitytoBuy(productId, quantity)) {
            throw new UIException("quantity fsh ", ErrorCodes.INVALID_QUANTITY);
        }
        storeStockRepo.saveAndFlush(storeStock);
        activePurchasesRepo.save(active);
        Store store = storeJpaRepo.findById(storeId).orElse(null);
        scheduleRandomEnd(active, randomTime, storeStock, random.getRandomId(), productId, quantity, store, random);
        List<Integer> ownersIds = new ArrayList<>();
        suConnectionRepo.getOwnersInStore(storeId).forEach(user -> ownersIds.add(user.getMyId()));
        notifier.sendMessageForUsers(
                "Owner " + userRepo.findById(userId).get().getUsername() + " set a product to random in your store",
                ownersIds);
        return random.getRandomId();
    }

    public RandomDTO[] getAllRandoms_user(String token, int storeId) throws Exception {
        logger.info("User requesting all randoms in store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        logger.info("Returning random list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow();

        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        return active.getRandoms();
    }

    public RandomDTO[] searchActiveRandoms(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchActiveRandoms with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        List<Product> matchProducts = stock.getMatchProducts(criteria);
        List<RandomDTO> allRandoms = new ArrayList<>();
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
                allRandoms.addAll(activePurcheses.getRandomsForProduct(product.getProductId(), store.getStoreName(),
                        product.getName()));
            }
        }
        // System.out.println("all randoms size: " + allRandoms.size());
        return allRandoms.toArray(new RandomDTO[0]);
    }

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException {
        synchronized (lockManager.getRandomLock(randomId)) {
            logger.info("User {} trying to participate in random: {}, store: {}", userId, randomId, storeId);
            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
            ParticipationInRandomDTO res = active.participateInRandom(userId, randomId ,amountPaid);
            return res;
        }
    }
}
