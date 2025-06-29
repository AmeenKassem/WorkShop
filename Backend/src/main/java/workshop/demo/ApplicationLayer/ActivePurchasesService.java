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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.BID;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.Random;
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
            StoreStock storeStock = storeStockRepo.findById(active.getStoreId()).orElse(null);
            Store store = storeJpaRepo.findById(active.getStoreId()).orElse(null);

            if (storeStock == null || store == null) {
                logger.warn("Skipping active purchase with storeId={} due to missing store or stock",
                        active.getStoreId());
                continue;
            }

            for (Auction auction : active.getActiveAuctions()) {
                if (!auction.isEnded()) {
                    auction.loadBids();
                    scheduleAuctionEnd(
                            active,
                            auction.getRestMS(),
                            storeStock,
                            auction.getId(),
                            auction.getProductId(),
                            auction.getAmount(),
                            store);
                }
            }
        }
    }

    
    @PersistenceContext
    protected EntityManager entityManager;
    // ======================AUCTION========================
    @Transactional
    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId, true);
        // adding auction here:
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
        Auction auction = active.addProductToAuction(productId, quantity, time, startPrice);
        StoreStock storeStock = storeStockRepo.findById(storeId).orElse(null);
        if (!storeStock.decreaseQuantitytoBuy(productId, quantity)) {
            throw new UIException("quantity fsh ", ErrorCodes.INVALID_QUANTITY);
        }
        storeStockRepo.saveAndFlush(storeStock);
        activePurchasesRepo.saveAndFlush(active);
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
        entityManager.flush();
        return auction.getId();

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
    // @Transactional
    // public void runAuctionEndTask(ActivePurcheses active, int auctionId,
    // StoreStock storeStock, int productId,
    // Store store) throws UIException, DevException {
    // logger.info("Running auction end task for auction id: {}", auctionId);
    // ActivePurcheses a =
    // activePurchasesRepo.findById(active.getStoreId()).orElseThrow();
    // Auction auction = a.getAuctionById(auctionId);
    // auction.loadBids();
    // auction.endAuction();
    // auction.setActivePurchases(a);

    // String productName = stock.getProductById(auction.getProductId()).getName();
    // activePurchasesRepo.saveAndFlush(a);

    // if (auction.mustReturnToStock()) {
    // logger.info("Refunding auction quantity to stock");
    // storeStock.IncreaseQuantitytoBuy(productId, auction.getAmount());
    // storeStockRepo.saveAndFlush(storeStock);
    // }

    // List<Integer> participants = auction.getBidsUsersIds();
    // notifier.sendMessageForUsers(
    // "Auction on store: " + store.getStoreName() + " with product " + productName
    // + " has ended!",
    // participants);

    // List<Integer> owners = suConnectionRepo.getOwnersInStore(store.getstoreId())
    // .stream().map(user -> user.getMyId()).toList();
    // notifier.sendMessageForUsers("Auction on " + productName + " has ended!",
    // owners);
    // }

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

    private int checkUserAndStore(String token, int storeId, boolean checkWorker) throws Exception {

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
        if (checkWorker && (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType))) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        return userId;
    }

    @Transactional
    public AuctionDTO[] getAllActiveAuctions_user(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        int userId = checkUserAndStore(token, storeId, false);
        logger.info("Returning auction list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow();

        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<Auction> auctions = active.getActiveAuctions();
        List<AuctionDTO> auctionDTOs = new ArrayList<>();
        for (Auction auction : auctions) {
            AuctionDTO auctionDTO = auction.getDTO();
            if (auction.getWinnenId() != -1) {
                auctionDTO.winnerUserName = userRepo.findById(auctionDTO.winnerUserId).orElse(new Registered())
                        .getUsername();
            }
            auctionDTO.productName = stock.getProductById(auctionDTO.productId).getName();
            auctionDTO.storeName = store.getStoreName();
            auctionDTOs.add(auctionDTO);
        }
        logger.info("we have to return to te user an array of auctions. size: " + auctionDTOs.size());
        return auctionDTOs.toArray(new AuctionDTO[0]);
    }

    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        checkUserAndStore(token, storeId, true);
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<AuctionDTO> auctions = new ArrayList<>();
        for (AuctionDTO auctionDTO : active.getAuctions()) {
            if (auctionDTO.winnerId != -1) {
                auctionDTO.winnerUserName = userRepo.findById(auctionDTO.winnerUserId).orElse(new Registered())
                        .getUsername();
            }
            auctionDTO.productName = stock.getProductById(auctionDTO.productId).getName();
            auctionDTO.storeName = storeJpaRepo.findById(storeId).orElseThrow().getStoreName();
            auctions.add(auctionDTO);
        }
        return auctions.toArray(new AuctionDTO[0]);
    }

    @Transactional
    public AuctionDTO[] searchActiveAuctions(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchAuctions with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        List<Product> matchProducts = stock.getMatchProducts(criteria);
        List<AuctionDTO> allAuctions = new ArrayList<>();
        List<ActivePurcheses> actives = new ArrayList<>();
        if (criteria.specificStore()) {
            logger.info("we have to find active purchases with id :" + criteria.getStoreId());
            actives.add(activePurchasesRepo.findById(criteria.getStoreId()).orElseThrow());
        } else {
            actives.addAll(activePurchasesRepo.findAll());
        }
        for (ActivePurcheses activePurcheses : actives) {
            if (activePurcheses == null) {
                logger.info("something went wrong with it loading auctions");
            }
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
            throws UIException, DevException, Exception {
        synchronized (lockManager.getAuctionLock(auctionId)) {
            logger.info("User trying to bid on auction: {}, store: {}", auctionId, storeId);
            int userId = checkUserAndStore(token, storeId, false);
            // SingleBid bid = stockRepo.bidOnAuction(storeId, userId, auctionId, price);
            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
            List<Auction> auctions = active.getActiveAuctions();
            int userLoosedTopId=-1;
            UserAuctionBid bid=null;
            for (Auction auction : auctions) {
                if(auction.getId()==auctionId){
                    bid =auction.bid(userId, price);
                    userLoosedTopId = auction.getTopId();
                }
            }
            // UserAuctionBid bid = active.addUserBidToAuction(auctionId, userId, price);
            logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
            // int userLoosedTopId = active.getCurrAuctionTop(auctionId);
            if (userLoosedTopId != userId && userLoosedTopId != -1)
                notifier.sendMessageToUser(userLoosedTopId, "You are loosing the top of auction!");
            activePurchasesRepo.flush();
            logger.info("bid saved to user!");
            UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, auctionId, bid.getId(),
                    SpecialType.Auction, bid.getProductId());
            userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
            return true;
        }

    }

    // ======================RANDOM========================
    @Transactional
    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long randomTime) throws UIException, DevException, Exception {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId, true);
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

    @Transactional
    public RandomDTO[] getAllActiveRandoms_user(String token, int storeId) throws Exception {
        logger.info("User requesting all randoms in store: {}", storeId);
        int userId = checkUserAndStore(token, storeId, false);
        logger.info("Returning random list to user: {}", userId);
        // Store store = storeJpaRepo.findById(storeId).orElseThrow();
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<Random> randoms = new ArrayList<>();
        for (Random random : active.getActiveRandoms()) {
            if (random.isActive()) {
                randoms.add(random);
            }
        }
        return randoms.stream()
                .map(rand -> rand.getDTO().setStoreNameAndProductName(
                        stock.getProductById(rand.getProductId()).getName(),
                        storeJpaRepo.findById(storeId).orElseThrow().getStoreName()))
                .toArray(RandomDTO[]::new);

    }

    public RandomDTO[] getAllRandoms(String token, int storeId) throws Exception {
        checkUserAndStore(token, storeId, true);
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<RandomDTO> randoms = new ArrayList<>();
        for (RandomDTO randomDTO : active.getRandoms()) {

            randomDTO.setStoreNameAndProductName(
                    stock.getProductById(randomDTO.getProductId()).getName(),
                    storeJpaRepo.findById(storeId).orElseThrow().getStoreName());
            if (randomDTO.winner != null)
                randomDTO.userName = randomDTO.winner.userName;
            randoms.add(randomDTO);
        }
        return randoms.toArray(new RandomDTO[0]);
    }

    @Transactional
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

    @Transactional
    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException {
        synchronized (lockManager.getRandomLock(randomId)) {
            logger.info("User {} trying to participate in random: {}, store: {}", userId, randomId, storeId);
            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
            String userName = userRepo.findById(userId)
                    .orElseThrow(() -> new UIException("User not found", ErrorCodes.USER_NOT_FOUND)).getUsername();
            ParticipationInRandomDTO res = active.participateInRandom(userId, randomId, amountPaid, userName);
            if (res.isEnded()) {
                logger.info("Random {} has ended, no participation allowed", randomId);
                List<Integer> participationsIds = new ArrayList<>();
                active.getRandom(randomId).getParticipationsUsersIds()
                        .forEach(participationId -> participationsIds.add(participationId));

                notifier.sendMessageForUsers(
                        "Random on store: " + storeJpaRepo.findById(storeId).get().getStoreName() + ", on product: "
                                + stock.getProductById(res.getProductId()).getName() + " has ended.",
                        participationsIds);
                notifier.sendDelayedMessageToUser(active.getRandom(randomId).getWinner().getUserName(),
                        "Congratulations! You have won the random on product: "
                                + stock.getProductById(res.getProductId()).getName() + " in store: "
                                + storeJpaRepo.findById(storeId).get().getStoreName()
                                + ". Please check your cart for details.");

            }
            return res;
        }
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

    // ======================BID========================
    @Transactional
    public int setProductToBid(String token, int storeId, int productId, int quantity) throws Exception {
        logger.info("User attempting to set product {} as bid in store {}", productId, storeId);
        int userId = checkUserAndStore(token, storeId, true);
        // adding bid here:
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
        int bidId = active.addProductToBid(productId, quantity);
        StoreStock storeStock = storeStockRepo.findById(storeId).orElse(null);
        if (!storeStock.decreaseQuantitytoBuy(productId, quantity)) {
            throw new UIException("quantity fsh ", ErrorCodes.INVALID_QUANTITY);
        }
        storeStockRepo.saveAndFlush(storeStock);
        activePurchasesRepo.save(active);
        List<Integer> ownersIds = new ArrayList<>();
        suConnectionRepo.getOwnersInStore(storeId).forEach(user -> ownersIds.add(user.getMyId()));
        notifier.sendMessageForUsers(
                "Owner " + userRepo.findById(userId).get().getUsername() + " set a product :"
                        + stock.getProductById(productId).getName() + " to BID with quantity: " + quantity
                        + " in your store",
                ownersIds);

        return bidId;
    }

    public BidDTO[] getAllActiveBids_user(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        int userId = checkUserAndStore(token, storeId, false);
        logger.info("Returning auction list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow();

        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<BID> bids = new ArrayList<>();
        for (BID bid : active.getActiveBids()) {
            BidDTO bidDTO = bid.getDTO();
            // if (bid.getWinner() != null) {
            // bidDTO.winnerUserName = userRepo.findById(bidDTO.winnerUserId).orElse(new
            // Registered())
            // .getUsername();
            // }
            bidDTO.productName = stock.getProductById(bidDTO.productId).getName();
            bidDTO.storeName = store.getStoreName();
            bids.add(bid);
        }

        logger.info("we have to return to te user an array of auctions. size: " + bids.size());
        return bids.stream()
                .map(BID::getDTO)
                .toArray(BidDTO[]::new);
    }

    public BidDTO[] getAllBids(String token, int storeId) throws Exception {
        checkUserAndStore(token, storeId, true);
        ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
        List<BidDTO> bids = new ArrayList<>();
        for (BidDTO bidDTO : active.getBids()) {
            // if (bidDTO.winnerUserId != -1) {
            // bidDTO.winnerUserName = userRepo.findById(bidDTO.winnerUserId).orElse(new
            // Registered())
            // .getUsername();
            // }
            bidDTO.productName = stock.getProductById(bidDTO.productId).getName();
            bidDTO.storeName = storeJpaRepo.findById(storeId).orElseThrow().getStoreName();
            bids.add(bidDTO);
        }
        return bids.toArray(new BidDTO[0]);
    }

    public BidDTO[] searchActiveBids(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchBids with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        List<Product> matchProducts = stock.getMatchProducts(criteria);
        List<BidDTO> allBids = new ArrayList<>();
        List<ActivePurcheses> actives = new ArrayList<>();
        if (criteria.specificStore()) {
            actives.add(activePurchasesRepo.findById(criteria.getStoreId()).orElseThrow());
            System.out.println("searching in specific store: " + criteria.getStoreId());
        } else {
            actives.addAll(activePurchasesRepo.findAll());
            System.out.println("searching in all stores");
        }
        for (ActivePurcheses activePurcheses : actives) {
            Store store = storeJpaRepo.findById(activePurcheses.getStoreId()).orElse(null);
            if (store == null || !store.isActive())
                continue;
            for (Product product : matchProducts) {
                System.out
                        .println("searching for product: " + product.getName() + " in store: " + store.getStoreName());
                allBids.addAll(activePurcheses.getBidsForProduct(product.getProductId(), store.getStoreName(),
                        product.getName()));
            }
        }
        System.out.println("all bids size: " + allBids.size());
        return allBids.toArray(new BidDTO[0]);
    }

    @Transactional
    public boolean addUserBidToBid(String token, int bidId, int storeId, double price)
            throws UIException, DevException, Exception {
        synchronized (lockManager.getBidLock(bidId)) {
            logger.info("User trying to bid on bid for store: {}", storeId);
            int userId = checkUserAndStore(token, storeId, false);

            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElse(null);
            List<Integer> ownersIds = new ArrayList<>();
            suConnectionRepo.getOwnersInStore(storeId).forEach(user -> ownersIds.add(user.getMyId()));
            SingleBid bid = active.addUserBidToBid(bidId, userId, price);
            activePurchasesRepo.save(active);
            logger.info("bid saved to user!");
            UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bidId, bid.getId(),
                    SpecialType.BID, active.getBidById(bidId).getProductId());
            userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
            notifier.sendMessageForUsers(
                    "User " + userRepo.findById(userId).get().getUsername() + " placed an offer on bid: " + bidId
                            + ", on store: " + storeJpaRepo.findById(storeId).orElseThrow().getStoreName()
                            + ", on product : "
                            + stock.getProductById(active.getBidById(bidId).getProductId()).getName(),
                    ownersIds);
            activePurchasesRepo.flush();
            return true;
        }
    }

    @Transactional
    public SingleBid acceptBid(String token, int storeId, int bidId, int userToAcceptForId) throws Exception, DevException {

        synchronized (lockManager.getBidLock(bidId)) {
            logger.info("User trying to accept bid: {} for bidId: {} in store: {}", userToAcceptForId, bidId, storeId);
            int userId = checkUserAndStore(token, storeId, true);
            if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
                throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
            }
            Store store = storeJpaRepo.findById(storeId).orElseThrow();
            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();

            synchronized (lockManager.getStoreLock(storeId)) {
                List<Integer> ownersIds = new ArrayList<>();
                suConnectionRepo.getOwnersInStore(storeId).forEach(user -> ownersIds.add(user.getMyId()));
                SingleBid bidAccepted = active.acceptBid(bidId, userToAcceptForId, ownersIds, userId);

                if (!bidAccepted.isWinner()) {
                    notifier.sendDelayedMessageToUser(userRepo.findById(bidAccepted.getUserId()).get().getUsername(),
                            "Owner " + userRepo.findById(userId).get().getUsername() + " accepted your bid, on store: "
                                    + store.getStoreName() + ", on product : "
                                    + stock.getProductById(bidAccepted.getProductId()).getName());
                } else {
                    notifier.sendDelayedMessageToUser(userRepo.findById(bidAccepted.getUserId()).get().getUsername(),
                            "Owner " + userRepo.findById(userId).get().getUsername()
                                    + " accepted your bid on store : " + store.getStoreName() + ", on product : "
                                    + stock.getProductById(active.getBidById(bidId).getProductId()).getName()
                                    + " and you are the winner!");
                    for (int id : active.getBidById(bidId).getLosersIdsIfAccepted()) {

                        notifier.sendDelayedMessageToUser(userRepo.findById(id).get().getUsername(),
                                "the BID on strore: "
                                        + store.getStoreName() + ", on product : "
                                        + stock.getProductById(active.getBidById(bidId).getProductId()).getName()
                                        + " has ended and your bid was not accepted.");
                    }
                }
                logger.info("Bid accepted. User: {} is the winner.", bidAccepted.getUserId());
                activePurchasesRepo.saveAndFlush(active);
                return bidAccepted;
            }

        }
    }

    @Transactional
    public void rejectBid(String token, int storeId, int bidId, int userToRejectForId, int ownerOffer)
            throws Exception, DevException {
        synchronized (lockManager.getBidLock(bidId)) {
            logger.info("User trying to accept bid: {} for bidId: {} in store: {}", userToRejectForId, bidId, storeId);
            int userId = checkUserAndStore(token, storeId, true);
            if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
                throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
            }

            Store store = storeJpaRepo.findById(storeId).orElseThrow();
            ActivePurcheses active = activePurchasesRepo.findById(storeId).orElseThrow();
            boolean bidRejected = active.rejectBid(bidId, userToRejectForId);
            int userRejected = active.getBidById(bidId).getBid(userToRejectForId).getUserId();
            if (bidRejected) {
                notifier.sendDelayedMessageToUser(userRepo.findById(userRejected).get().getUsername(),
                        "Your bid on store: "
                                + store.getStoreName() + ", on product : "
                                + stock.getProductById(active.getBidById(bidId).getProductId()).getName()
                                + " has been rejected");
            }

            if( ownerOffer > 0 ) {
                String ownerName = userRepo.findById(userId)
                        .orElseThrow(() -> new UIException("Owner not found", ErrorCodes.USER_NOT_FOUND)).getUsername();
                String message = "Owner is Offering you to bid again with this price: " + ownerOffer
                        + " on store: " + store.getStoreName() + ", on product : "
                        + stock.getProductById(active.getBidById(bidId).getProductId()).getName() + ". Would you like to bid again?";
                notifier.sendDelayedMessageToUser(userRepo.findById(userRejected).get().getUsername(),
                        toJsonOffer(message, NotificationDTO.NotificationType.USER_OFFER, ownerName, storeId, bidId));
            }
            activePurchasesRepo.saveAndFlush(active);
        }
    }

    private String toJsonOffer(String message, NotificationDTO.NotificationType type, String senderName, int storeId, int bidId) {
        NotificationDTO dto = new NotificationDTO(message, NotificationDTO.NotificationType.USER_OFFER, senderName, storeId, bidId);
        JsonObject json = Json.createObject();
        json.put("message", dto.getMessage());
        json.put("bidId", dto.getBidId());
        json.put("type", dto.getType().name());
        json.put("senderName", dto.getSenderName());
        json.put("storeId", dto.getStoreId());
        return json.toJson();
    }

}
