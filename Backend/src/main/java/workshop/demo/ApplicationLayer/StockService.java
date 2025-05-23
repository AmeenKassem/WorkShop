package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

@Service
@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private IStockRepo stockRepo;
    private IAuthRepo authRepo;
    private IStoreRepo storeRepo;
    private ISUConnectionRepo suConnectionRepo;
    private IUserRepo userRepo;
    private IUserSuspensionRepo susRepo;

    @Autowired
    @Autowired
    public StockService(IStockRepo stockRepo, IStoreRepo storeRepo, IAuthRepo authRepo, IUserRepo userRepo,
            ISUConnectionRepo cons, IUserSuspensionRepo susRepo) {
            ISUConnectionRepo cons, IUserSuspensionRepo susRepo) {
        this.stockRepo = stockRepo;
        this.authRepo = authRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.suConnectionRepo = cons;
        this.susRepo = susRepo;
    }

    public ItemStoreDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        int x = storeRepo.getFinalRateInStore(1);
        logger.info("gfnedsm," + 0);
        logger.info("tarting searchProducts with criteria: {}", criteria);
        int x = storeRepo.getFinalRateInStore(1);
        logger.info("gfnedsm," + 0);
        logger.info("tarting searchProducts with criteria: {}", criteria);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        
        logger.info("Returning matched items to client " );
        ItemStoreDTO[] items =  stockRepo.search(criteria);
        
        storeRepo.fillWithStoreName(items);
        return items;
    }


    public ProductDTO getProductInfo(String token, int productId) throws UIException {
        logger.info("Fetching product info for ID {}", productId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);

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
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        SingleBid bid = stockRepo.bidOnAuction(storeId, userId, auctionId, price);
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.Auction);
        userRepo.addSpecialItemToCart(specialItem, userId);
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.Auction);
        userRepo.addSpecialItemToCart(specialItem, userId);
        logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
        return true;

    }

    public boolean addRegularBid(String token, int bitId, int storeId, double price) throws UIException, DevException {
        logger.info("User attempting regular bid on bidId: {}, storeId: {}", bitId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        SingleBid bid = stockRepo.bidOnBid(bitId, price, userId, storeId);
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.BID);
        userRepo.addSpecialItemToCart(specialItem, userId);
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(),
                SpecialType.BID);
        userRepo.addSpecialItemToCart(specialItem, userId);
        logger.info("Regular bid successful by user: {}", userId);
        return true;

    }

    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        logger.info("User requesting all auctions in store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        logger.info("Returning auction list to user: {}", userId);
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        return stockRepo.getAuctionsOnStore(storeId);
    }

    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("Setting product {} to auction in store {}", productId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        // must add the exceptions here:
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to set produt to auction.", ErrorCodes.NO_PERMISSION);
        }
        return stockRepo.addAuctionToStore(storeId, productId, quantity, time, startPrice);
    }

    public int setProductToBid(String token, int storeid, int productId, int quantity) throws Exception {
        logger.info("User attempting to set product {} as bid in store {}", productId, storeid);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        storeRepo.checkStoreExistance(storeid);
        // Node Worker= this.
        if (!this.suConnectionRepo.manipulateItem(userId, storeid, Permission.SpecialType)) {
            throw new UIException("you have no permession to set product to bid.", ErrorCodes.NO_PERMISSION);
        }

        return stockRepo.addProductToBid(storeid, productId, quantity);
    }

    public BidDTO[] getAllBidsStatus(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching bid status for store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see auctions info.", ErrorCodes.NO_PERMISSION);
        }
        storeRepo.checkStoreExistance(storeId);

        return stockRepo.getAllBids(storeId);
    }

    public SingleBid acceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidToAcceptId, bidId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        SingleBid winner = stockRepo.acceptBid(storeId, bidId, bidToAcceptId);
        logger.info("Bid accepted. User: {} is the winner.", winner.getUserId());
        return winner;
    }
    public void rejectBid(String token, int storeId, int bidId, int bidTorejectId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidTorejectId, bidId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        stockRepo.rejectBid(storeId, bidId, bidTorejectId);
    }

    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
    public void rejectBid(String token, int storeId, int bidId, int bidTorejectId) throws Exception, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidTorejectId, bidId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        stockRepo.rejectBid(storeId, bidId, bidTorejectId);
    }

    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return stockRepo.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
        return stockRepo.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    }

    public ParticipationInRandomDTO endBid(String token, int storeId, int randomId) throws Exception, DevException {
        logger.info("Ending random bid {} in store {}", randomId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return stockRepo.endRandom(storeId, randomId);
        return stockRepo.endRandom(storeId, randomId);
    }

    public RandomDTO[] getAllRandomInStore(String token, int storeId) throws Exception, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            throw new UIException("you have no permession to see random info.", ErrorCodes.NO_PERMISSION);
        }
        return stockRepo.getRandomsInStore(storeId);
    }

    // stock managment:
    public ItemStoreDTO[] getProductsInStore(int storeId) throws UIException, DevException {
    // stock managment:
    public ItemStoreDTO[] getProductsInStore(int storeId) throws UIException, DevException {
        logger.info("Fetching all products in store: {}", storeId);
        storeRepo.checkStoreExistance(storeId);
        ItemStoreDTO[] products = stockRepo.getProductsInStore(storeId);
        logger.info("fetched {} products from store: {}", products.length, storeId);
        ItemStoreDTO[] products = stockRepo.getProductsInStore(storeId);
        logger.info("fetched {} products from store: {}", products.length, storeId);
        return products;
    }

    public int addItem(int storeId, String token, int productId, int quantity, int price, Category category)
            throws Exception, DevException {
    public int addItem(int storeId, String token, int productId, int quantity, int price, Category category)
            throws Exception, DevException {
        logger.info("User attempting to add item {} to store {}", productId, storeId);
         if (quantity <= 0) {
        throw new UIException("Quantity must be greater than zero.", ErrorCodes.INVALID_QUANTITY); 
    }
         if (quantity <= 0) {
        throw new UIException("Quantity must be greater than zero.", ErrorCodes.INVALID_QUANTITY); 
    }
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.info("HmodeID is:{}",storeId);
        logger.info("HmodeID is:{}",storeId);
        storeRepo.checkStoreExistance(storeId);
        if (!suConnectionRepo.manipulateItem(userId, storeId, Permission.AddToStock)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        stockRepo.checkProductExists_ThrowException(productId);
        item toAdd = stockRepo.addItem(storeId, productId, quantity, price, category);
        logger.info("Item added successfully to store {}: {}", storeId, toAdd);
        return toAdd.getProductId();
    }

    public int addProduct(String token, String name, Category category, String description, String[] keywords)
            throws Exception {
    public int addProduct(String token, String name, Category category, String description, String[] keywords)
            throws Exception {
        logger.info("User attempting to add a new product to the system: name={}, category={}", name, category);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);


        int productId = stockRepo.addProduct(name, category, description, keywords);
        logger.info("Product added successfully: {} with id ={}", name, productId);
        return productId;
    }

    public int removeItem(int storeId, String token, int productId) throws Exception, DevException {
    public int removeItem(int storeId, String token, int productId) throws Exception, DevException {
        logger.info("User attempting to remove item {} from store {}", productId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int removerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(removerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(removerId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(removerId, storeId, Permission.DeleteFromStock)) {
            throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        stockRepo.removeItem(storeId, productId);
        logger.info("Item {} successfully removed from store {}", productId, storeId);
        return productId;
    }

    public int updateQuantity(int storeId, String token, int productId, int newQuantity)
            throws Exception, DevException {
        logger.info("User attempting to update quantity of product {} in store {} to {}", productId, storeId,
                newQuantity);

    public int updateQuantity(int storeId, String token, int productId, int newQuantity)
            throws Exception, DevException {
        logger.info("User attempting to update quantity of product {} in store {} to {}", productId, storeId,
                newQuantity);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int changerUserId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(changerUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(changerUserId);
        storeRepo.checkStoreExistance(storeId);
        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdateQuantity)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        stockRepo.updateQuantity(storeId, productId, newQuantity);
        logger.info("Quantity updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public int updatePrice(int storeId, String token, int productId, int newPrice) throws Exception, DevException {
        logger.info("User attempting to update price of product {} in store {} to {}", productId, storeId, newPrice);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int changerUserId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(changerUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(changerUserId);
        storeRepo.checkStoreExistance(storeId);
        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdatePrice)) {
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }
        stockRepo.updatePrice(storeId, productId, newPrice);
        logger.info("Price updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public int rankProduct(int storeId, String token, int productId, int newRank) throws Exception {
        logger.info("user attempting to update rank of product {} in store {} to {}", productId, storeId, newRank);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int updaterUserId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(updaterUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(updaterUserId);
        storeRepo.checkStoreExistance(storeId);
        this.stockRepo.rankProduct(storeId, productId, newRank);
        logger.info("the rank updated successfully for product {} in store {}", productId, storeId);
        return productId;
    }

    public ProductDTO[] getAllProducts(String token) throws Exception {
        logger.info("fetching all the products in the system");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return stockRepo.getAllProducts();
    }
}

    public ProductDTO[] getAllProducts(String token) throws Exception {
        logger.info("fetching all the products in the system");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        return stockRepo.getAllProducts();
    }
}
