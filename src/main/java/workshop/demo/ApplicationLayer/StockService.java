package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;

public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private IStockRepo stockRepo;
    private IAuthRepo authRepo;
    private IStoreRepo storeRepo;

    private ISUConnectionRepo suConnectionRepo;
    private IUserRepo userRepo;

    public StockService(IStockRepo stockRepo, IStoreRepo storeRepo, IAuthRepo authRepo, IUserRepo userRepo,
            ISUConnectionRepo cons) {
        this.stockRepo = stockRepo;
        this.authRepo = authRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.suConnectionRepo = cons;
    }

    public ItemStoreDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("Starting searchProducts with criteria: {}", criteria);

        authRepo.checkAuth(token, logger);
        ProductDTO[] matchesProducts = stockRepo.getMatchesProducts(criteria);
        logger.debug("Found {} matching products in stock", matchesProducts.length);

        ItemStoreDTO[] matchedItems = storeRepo.getMatchesItems(criteria, matchesProducts);
        logger.info("Returning {} matched items to client", matchedItems.length);
        return matchedItems;
    }

    // public String searchProductInStore(String token, int storeId, int productId)
    // throws Exception {
    // logger.info("Searching for productId {} in storeId {}", productId, storeId);
    // if (!authRepo.validToken(token)) {
    // logger.error("Unauthorized access to searchProductInStore with token: {}",
    // token);
    // throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
    // }
    // Product product = stockRepo.findById(productId);
    // if (product == null) {
    // logger.error("Product not found with ID: {}", productId);
    // throw new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND);
    // }
    // item itemInStore = storeRepo.getItemByStoreAndProductId(storeId, productId);
    // if (itemInStore == null) {
    // logger.warn("Product {} not sold in store {}", productId, storeId);
    // throw new UIException("Product not sold in this store",
    // ErrorCodes.PRODUCT_NOT_FOUND);
    // }
    // String storeName = storeRepo.getStoreNameById(storeId);
    // logger.info("Product {} found in store {} (ID {})", product.getName(),
    // storeName, storeId);

    // return "Product: " + product.getName() + ", Price: " + itemInStore.getPrice()
    // + ", Store: " + storeName;
    // }

    public ProductDTO getProductInfo(String token, int productId) throws UIException {
        logger.info("Fetching product info for ID {}", productId);

        authRepo.checkAuth(token, logger);

        ProductDTO dto = stockRepo.GetProductInfo(productId);
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
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        // if (userRepo.isRegistered(userId) && userRepo.isOnline(userId)) {
        userRepo.checkUserRegisterOnline(userId);
        SingleBid bid = storeRepo.bidOnAuction(storeId, userId, auctionId, price);
        userRepo.addBidToAuctionCart(bid);
        logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
        return true;

    }

    public boolean addRegularBid(String token, int bitId, int storeId, double price) throws UIException, DevException {
        logger.info("User attempting regular bid on bidId: {}, storeId: {}", bitId, storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        // if (userRepo.isRegistered(userId) && userRepo.isOnline(userId)) {
        SingleBid bid = storeRepo.bidOnBid(bitId, price, userId, storeId);
        userRepo.addBidToRegularCart(bid);
        logger.info("Regular bid successful by user: {}", userId);
        return true;

    }

    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        logger.info("Returning auction list to user: {}", userId);
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        return storeRepo.getAuctionsOnStore(userId, storeId);
    }

    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        // must add the exceptions here:
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        return storeRepo.addAuctionToStore(storeId, userId, productId, quantity, time, startPrice);
    }

    public int setProductToBid(String token, int storeid, int productId, int quantity) throws Exception {
        logger.info("User attempting to set product {} as bid in store {}", productId, storeid);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);

        storeRepo.checkStoreExistance(storeid);
        // Node Worker= this.
        if (!this.suConnectionRepo.manipulateItem(userId, storeid, Permission.SpecialType)) {
            throw new UIException("you have no permession to set product to bid.", ErrorCodes.NO_PERMISSION);
        }

        return storeRepo.addProductToBid(storeid, userId, productId, quantity);
    }

    public BidDTO[] getAllBidsStatus(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching bid status for store: {}", storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        storeRepo.checkStoreExistance(storeId);

        return storeRepo.getAllBids(userId, storeId);
    }

    public SingleBid acceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidToAcceptId, bidId, storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        SingleBid winner = storeRepo.acceptBid(storeId, bidId, userId, bidToAcceptId);
        logger.info("Bid accepted. User: {} is the winner.", winner.getUserId());
        return winner;
    }

    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        return storeRepo.addProductToRandom(userId, productId, quantity, productPrice, storeId, RandomTime);
    }

    public ParticipationInRandomDTO endBid(String token, int storeId, int randomId) throws Exception, DevException {
        logger.info("Ending random bid {} in store {}", randomId, storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        return storeRepo.endRandom(storeId, userId, randomId);
    }

    public RandomDTO[] getAllRandomInStore(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        authRepo.checkAuth(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline(userId);
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see random info.", ErrorCodes.NO_PERMISSION);
        }
        return storeRepo.getRandomsInStore(storeId, userId);
    }

}
