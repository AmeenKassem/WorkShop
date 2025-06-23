package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.AISearch;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    @Autowired
    private IStockRepo stockRepo;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private ISUConnectionRepo suConnectionRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    @Autowired
    private IStockRepoDB stockJpaRepo;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private NotificationService notifier;
    private AISearch aiSearch = new AISearch();

    private UIException storeNotFound() {
        return new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND);
    }

    @Transactional
    public ItemStoreDTO[] searchProductsOnAllSystem(String token, ProductSearchCriteria criteria) throws Exception {

        logger.info("Starting searchProducts with criteria: {}", criteria);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        List<Product> products = null;
        if (criteria.keywordSearch() && aiSearch.isActive()) {
            List<Integer> ids = aiSearch.getSameProduct(criteria.getKeyword(), criteria.specificCategory() ? criteria.getCategory().hashCode() : -1, 0.35);
            products = stockJpaRepo.findAllById(ids);
        } else if (criteria.nameSearch()) {
            products = stockJpaRepo.findByNameContainingIgnoreCase(criteria.getName());
        } else {
            throw new UIException("the ai search api is not running !!", ErrorCodes.AI_NOT_WORK);
        }
        logger.info("Returning matched items to client ");
        List<ItemStoreDTO> res = new ArrayList<>();
        for (Product product : products) {
            logger.info("product match found: " + product.getName());
            List<item> items = storeStockRepo.findItemsByProductId(product.getProductId());
            for (item item : items) {
                if (criteria.matchesForStore(item)) {
                    logger.info("found item in store " + item.getStoreId());
                    ItemStoreDTO toAdd = new ItemStoreDTO();
                    toAdd.setProductId(product.getProductId());
                    toAdd.setName(product.getName());
                    toAdd.setPrice(item.getPrice());
                    toAdd.setCategory(item.getCategory());
                    toAdd.setRank(item.getFinalRank());
                    toAdd.setStoreId(item.getStoreId());
                    toAdd.setStoreName(storeJpaRepo.findById(item.getStoreId()).orElseThrow().getStoreName());
                    res.add(toAdd);
                }
            }
        }
        return res.toArray(new ItemStoreDTO[0]);
    }

    public RandomDTO[] searchActiveRandoms(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchRandoms with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        RandomDTO[] randoms = stockRepo.searchActiveRandoms(criteria);
        //storeRepo.fillWithStoreName(randoms);
        //-> must be jpa
        return randoms;
    }

    public BidDTO[] searchActiveBids(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchBids with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        BidDTO[] bids = stockRepo.searchActiveBids(criteria);
        //storeRepo.fillWithStoreName(bids);
        //-> must be jpa
        return bids;
    }

    public AuctionDTO[] searchActiveAuctions(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchAuctions with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        // String storeName = this.storeRepo.getStoreNameById(criteria.getStoreId());
        AuctionDTO[] auctions = stockRepo.searchActiveAuctions(criteria);
        //storeRepo.fillWithStoreName(auctions);
        //-> must be jpa
        return auctions;
    }

    public ProductDTO getProductInfo(String token, int productId) throws UIException {
        logger.info("Fetching product info for ID {}", productId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);

        ProductDTO dto = stockJpaRepo.findById(productId)
                .orElseThrow(() -> new UIException("product not found!", ErrorCodes.PRODUCT_NOT_FOUND)).getDTO();
        if (dto == null) {
            logger.error("Product not found for ID {}", productId);
            throw new UIException("Product not found.", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        logger.info("Successfully retrieved product info: {}", dto.getName());
        return dto;
    }

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

        SingleBid bid = stockRepo.bidOnAuction(storeId, userId, auctionId, price);
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.Auction);
        userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
        logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
        return true;

    }

    private void checkUserRegisterOnline_ThrowException(int userId) throws UIException {
        Optional<Registered> user = userRepo.findById(userId);
        if (!user.isPresent()) {
            throw new UIException("stock service:user not found!", ErrorCodes.USER_NOT_FOUND);
        }
    }

    public boolean addRegularBid(String token, int bitId, int storeId, double price) throws UIException, DevException {
        logger.info("User attempting regular bid on bidId: {}, storeId: {}", bitId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        SingleBid bid = stockRepo.bidOnBid(bitId, price, userId, storeId);
        bid.ownersNum = suConnectionRepo.getOwnersInStore(storeId).size();
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.BID);
        userRepo.findById(userId).get().addSpecialItemToCart(specialItem);
        for (Node worker : suConnectionRepo.getOwnersInStore(storeId)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName,
                    "User " + userRepo.findById(userId).get().getUsername() + " placed a bid on your product");
        }
        logger.info("Regular bid successful by user: {}", userId);
        return true;

    }

    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        logger.info("Returning auction list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        return stockRepo.getAuctionsOnStore(storeId);
    }

    public AuctionDTO[] getAllAuctions_user(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        logger.info("Returning auction list to user: {}", userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        return stockRepo.getAuctionsOnStore(storeId);
    }

    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        // must add the exceptions here:
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        for (Node worker : suConnectionRepo.getOwnersInStore(storeId)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName, "Owner "
                    + userRepo.findById(userId).get().getUsername() + " set a product to auction in your store");
        }
        return stockRepo.addAuctionToStore(storeId, productId, quantity, time, startPrice);
    }

    public int setProductToBid(String token, int storeid, int productId, int quantity) throws Exception {
        logger.info("User attempting to set product {} as bid in store {}", productId, storeid);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeid).orElseThrow(() -> storeNotFound());
        // Node Worker= this.
        if (!this.suConnectionRepo.manipulateItem(userId, storeid, Permission.SpecialType)) {
            throw new UIException("you have no permession to set product to bid.", ErrorCodes.NO_PERMISSION);
        }
        for (Node worker : suConnectionRepo.getOwnersInStore(storeid)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName,
                    "Owner " + userRepo.findById(userId).get().getUsername() + " set a product to bid in your store");
        }

        return stockRepo.addProductToBid(storeid, productId, quantity);
    }

    public BidDTO[] getAllBidsStatus(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching bid status for store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        return stockRepo.getAllBids(storeId);
    }

    public BidDTO[] getAllBidsInStore(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching bid for store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        return stockRepo.getAllBids(storeId);
    }

    public SingleBid acceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidToAcceptId, bidId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        SingleBid bidAccepted = stockRepo.acceptBid(storeId, bidId, bidToAcceptId);
        if (!bidAccepted.isWinner()) {
            notifier.sendDelayedMessageToUser(userRepo.findById(bidAccepted.getUserId()).get().getUsername(),
                    "Owner " + userRepo.findById(userId).get().getUsername() + " accepted your bid");
        } else {
            notifier.sendDelayedMessageToUser(userRepo.findById(bidAccepted.getUserId()).get().getUsername(),
                    "Owner " + userRepo.findById(userId).get().getUsername()
                    + " accepted your bid and you are the winner!");
        }
        logger.info("Bid accepted. User: {} is the winner.", bidAccepted.getUserId());
        return bidAccepted;
    }

    public void rejectBid(String token, int storeId, int bidId, int bidTorejectId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidTorejectId, bidId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        stockRepo.rejectBid(storeId, bidId, bidTorejectId);
    }

    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        for (Node worker : suConnectionRepo.getOwnersInStore(storeId)) {
            String ownerName = userRepo.findById(worker.getMyId()).get().getUsername();
            notifier.sendDelayedMessageToUser(ownerName, "Owner "
                    + userRepo.findById(userId).get().getUsername() + " set a product to random in your store");
        }
        return stockRepo.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    }

    public ParticipationInRandomDTO endBid(String token, int storeId, int randomId) throws Exception, DevException {
        logger.info("Ending random bid {} in store {}", randomId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);

        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        return stockRepo.endRandom(storeId, randomId);
    }

    public RandomDTO[] getAllRandomInStore(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see random info.", ErrorCodes.NO_PERMISSION);
        }
        return stockRepo.getRandomsInStore(storeId);
    }

    public RandomDTO[] getAllRandomInStoreToUser(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        return stockRepo.getRandomsInStore(storeId);
    }

    public RandomDTO[] getAllRandomInStore_user(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        return stockRepo.getRandomsInStore(storeId);
    }

    // stock managment:
    public ItemStoreDTO[] getProductsInStore(int storeId) throws UIException, DevException {
        logger.info("Fetching all products in store: {}", storeId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        // ItemStoreDTO[] products = stockRepo.getProductsInStore(storeId);
        StoreStock store4sstock = storeStockRepo.findById(store.getstoreId())
                .orElseThrow(() -> new UIException("store stock not found!", ErrorCodes.STOCK_NOT_FOUND));

        List<item> itemsOnStore = store4sstock.getAllItemsInStock();
        ItemStoreDTO[] res = new ItemStoreDTO[itemsOnStore.size()];
        for (int i = 0; i < res.length; i++) {
            item item = itemsOnStore.get(i);
            String productName = stockJpaRepo.findById(item.getProductId())
                    .orElseThrow(() -> new DevException("Db has no product!")).getName();
            res[i] = new ItemStoreDTO(item.getProductId(), item.getQuantity(), item.getPrice(), item.getCategory(),
                    item.getFinalRank(), store.getstoreId(), productName, store.getStoreName());
        }
        logger.info("fetched {} products from store: {}", res.length, storeId);
        return res;
    }

    public int addItem(int storeId, String token, int productId, int quantity, int price, Category category)
            throws Exception, DevException {
        logger.info("User attempting to add item {} to store {}", productId, storeId);
        if (quantity <= 0) {
            throw new UIException("Quantity must be greater than zero.", ErrorCodes.INVALID_QUANTITY);
        }
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        logger.info("HmodeID is:{}", storeId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!suConnectionRepo.manipulateItem(userId, storeId, Permission.AddToStock)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        // stockRepo.checkProductExists_ThrowException(productId);
        stockJpaRepo.findById(productId)
                .orElseThrow(() -> new UIException("product not found!", ErrorCodes.PRODUCT_NOT_FOUND));

        StoreStock stock4store = storeStockRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("store stock not found on db!"));
        item toAdd = new item(productId, quantity, price, category);
        stock4store.addItem(toAdd);
        storeStockRepo.save(stock4store);
        logger.info("Item added successfully to store {}: {}", storeId, toAdd);
        return toAdd.getProductId();
    }

    // DONE
    public int addProduct(String token, String name, Category category, String description, String[] keywords)
            throws Exception {
        logger.info("User attempting to add a new product to the system: name={}, category={}", name, category);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Product product = new Product(name, category, description, keywords);
        product = stockJpaRepo.save(product);
        aiSearch.trainProduct(name, keywords);
        logger.info("Product added successfully: {} with id ={}", name, product.getProductId());
        return product.getProductId();
    }

    public int removeItem(int storeId, String token, int productId) throws Exception, DevException {
        logger.info("User attempting to remove item {} from store {}", productId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int removerId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(removerId);
        UserSuspension suspension = suspensionJpaRepo.findById(removerId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        if (!this.suConnectionRepo.manipulateItem(removerId, storeId, Permission.DeleteFromStock)) {
            throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }

        StoreStock stock = storeStockRepo.findById(storeId).orElse(null);

        stock.removeItem(productId);

        storeStockRepo.saveAndFlush(stock);
        logger.info("Item {} successfully removed from store {}", productId, storeId);
        return productId;
    }

    public int updateQuantity(int storeId, String token, int productId, int newQuantity)
            throws Exception, DevException {
        logger.info("User attempting to update quantity of product {} in store {} to {}", productId, storeId,
                newQuantity);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int changerUserId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(changerUserId);
        UserSuspension suspension = suspensionJpaRepo.findById(changerUserId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdateQuantity)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        // stockRepo.updateQuantity(storeId, productId, newQuantity);
        StoreStock stock = storeStockRepo.findById(storeId)
                .orElseThrow(() -> new DevException("store stock not found on db!!"));
        stock.changeQuantity(productId, newQuantity);
        storeStockRepo.saveAndFlush(stock);
        logger.info("Quantity updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public int updatePrice(int storeId, String token, int productId, int newPrice) throws Exception, DevException {
        logger.info("User attempting to update price of product {} in store {} to {}", productId, storeId, newPrice);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int changerUserId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(changerUserId);
        UserSuspension suspension = suspensionJpaRepo.findById(changerUserId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdatePrice)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        // stockRepo.updatePrice(storeId, productId, newPrice);
        StoreStock stock = storeStockRepo.findById(storeId)
                .orElseThrow(() -> new DevException("store stock not found on db!!"));
        stock.updatePrice(productId, newPrice);
        storeStockRepo.saveAndFlush(stock);
        logger.info("Price updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public int rankProduct(int storeId, String token, int productId, int newRank) throws Exception {
        logger.info("user attempting to update rank of product {} in store {} to {}", productId, storeId, newRank);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int updaterUserId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(updaterUserId);
        UserSuspension suspension = suspensionJpaRepo.findById(updaterUserId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        //this.stockRepo.rankProduct(storeId, productId, newRank);
        StoreStock stock = storeStockRepo.findById(storeId)
                .orElseThrow(() -> new DevException("store stock not found on db!!"));
        stock.rankProduct(productId, newRank);
        storeStockRepo.saveAndFlush(stock);
        logger.info("the rank updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public ProductDTO[] getAllProducts(String token) throws Exception {
        logger.info("fetching all the products in the system");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        List<ProductDTO> products = new ArrayList<>();
        stockJpaRepo.findAll().forEach((product) -> products.add(product.getDTO()));
        return products.toArray(new ProductDTO[0]);
    }

    // public ParticipationInRandomDTO participateInRandom(String token, int
    // storeId, int randomId, double price) throws Exception {
    // logger.info("user participating in randomId: {} in store: {} with price: {}",
    // randomId, storeId, price);
    // authRepo.checkAuth_ThrowTimeOutException(token, logger);
    // int userId = authRepo.getUserId(token);
    // checkUserRegisterOnline_ThrowException(userId);
    // susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
    // return stockRepo.participateInRandom(userId, randomId, storeId, price);
    // }
    public BidDTO[] getAllBidsStatus_user(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching bid status for store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());

        return stockRepo.getAllBids(storeId);
    }
}
