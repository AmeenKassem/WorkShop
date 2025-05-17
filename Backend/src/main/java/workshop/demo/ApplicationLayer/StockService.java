package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private IStockRepo stockRepo;
    private IAuthRepo authRepo;
    private IStoreRepo storeRepo;
    private ISUConnectionRepo suConnectionRepo;
    private IUserRepo userRepo;
    private IUserSuspensionRepo susRepo;

    @Autowired
    public StockService(IStockRepo stockRepo, IStoreRepo storeRepo, IAuthRepo authRepo, IUserRepo userRepo,
                        ISUConnectionRepo cons, IUserSuspensionRepo susRepo) {
        this.stockRepo = stockRepo;
        this.authRepo = authRepo;
        this.storeRepo = storeRepo;
        this.userRepo = userRepo;
        this.suConnectionRepo = cons;
        this.susRepo = susRepo;
    }

    public ItemStoreDTO[] searchProducts(String token, ProductSearchCriteria criteria) throws Exception {
        logger.info("[searchProducts] Starting with criteria: {}", criteria);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        ProductDTO[] matchesProducts = stockRepo.getMatchesProducts(criteria);
        logger.debug("[searchProducts] Found {} matching products", matchesProducts.length);
        ItemStoreDTO[] matchedItems = stockRepo.getMatchesItems(criteria, matchesProducts);
        logger.info("[searchProducts] Returning {} matched items", matchedItems.length);
        return matchedItems;
    }


    public ProductDTO getProductInfo(String token, int productId) throws UIException {
        logger.info("[getProductInfo] Fetching product info for ID {}", productId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        ProductDTO dto = stockRepo.GetProductInfo(productId);
        if (dto == null) {
            logger.error("[getProductInfo] Product not found: ID={}", productId);
            throw new UIException("Product not found.", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        logger.info("[getProductInfo] Product retrieved: {}", dto.getName());
        return dto;
    }

    public boolean addBidOnAucction(String token, int auctionId, int storeId, double price)
            throws UIException, DevException {
        logger.info("[addBidOnAucction] User attempting to bid on auctionId={}, storeId={}, price={}", auctionId, storeId, price);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[addBidOnAucction] Token validated successfully.");

        int userId = authRepo.getUserId(token);
        logger.debug("[addBidOnAucction] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.debug("[addBidOnAucction] User {} is registered and not suspended.", userId);

        SingleBid bid = stockRepo.bidOnAuction(storeId, userId, auctionId, price);
        logger.debug("[addBidOnAucction] Bid created: specialId={}, bidId={}", bid.getSpecialId(), bid.getId());

        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(), SpecialType.Auction);
        userRepo.addSpecialItemToCart(specialItem, userId);
        logger.info("[addBidOnAucction] Bid successfully added to user {}'s cart for auction {}", userId, auctionId);

        return true;
    }


    public boolean addRegularBid(String token, int bitId, int storeId, double price) throws UIException, DevException {
        logger.info("[addRegularBid] User attempting to place regular bid on bitId={}, storeId={}, price={}", bitId, storeId, price);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[addRegularBid] Token validated successfully.");

        int userId = authRepo.getUserId(token);
        logger.debug("[addRegularBid] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.debug("[addRegularBid] User {} is registered and not suspended.", userId);

        SingleBid bid = stockRepo.bidOnBid(bitId, price, userId, storeId);
        logger.debug("[addRegularBid] Bid created: specialId={}, bidId={}", bid.getSpecialId(), bid.getId());

        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, bid.getSpecialId(), bid.getId(), SpecialType.BID);
        userRepo.addSpecialItemToCart(specialItem, userId);
        logger.info("[addRegularBid] Bid successfully added to user {}'s cart for bid {}", userId, bitId);

        return true;
    }


    public AuctionDTO[] getAllAuctions(String token, int storeId) throws Exception {
        logger.info("[getAllAuctions] User requesting all auctions in store: {}", storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[getAllAuctions] Token validated successfully.");

        int userId = authRepo.getUserId(token);
        logger.debug("[getAllAuctions] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[getAllAuctions] User {} lacks permission to view auctions in store {}", userId, storeId);
            throw new UIException("you have no permission to see auctions info.", ErrorCodes.NO_PERMISSION);
        }

        AuctionDTO[] auctions = stockRepo.getAuctionsOnStore(storeId);
        logger.info("[getAllAuctions] Retrieved {} auctions for store {} for user {}", auctions.length, storeId, userId);
        return auctions;
    }


    public int setProductToAuction(String token, int storeId, int productId, int quantity, long time, double startPrice)
            throws Exception, DevException {
        logger.info("[setProductToAuction] Setting product {} to auction in store {} (qty={}, time={}, price={})",
                productId, storeId, quantity, time, startPrice);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[setProductToAuction] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[setProductToAuction] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[setProductToAuction] User {} not authorized to set auction in store {}", userId, storeId);
            throw new UIException("you have no permission to set product to auction.", ErrorCodes.NO_PERMISSION);
        }

        int auctionId = stockRepo.addAuctionToStore(storeId, productId, quantity, time, startPrice);
        logger.info("[setProductToAuction] Product {} successfully added to auction with ID {} in store {}", productId, auctionId, storeId);
        return auctionId;
    }


    public int setProductToBid(String token, int storeId, int productId, int quantity) throws Exception {
        logger.info("[setProductToBid] User attempting to set product {} as bid in store {} with quantity {}", productId, storeId, quantity);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[setProductToBid] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[setProductToBid] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[setProductToBid] User {} not authorized to set product to bid in store {}", userId, storeId);
            throw new UIException("you have no permission to set product to bid.", ErrorCodes.NO_PERMISSION);
        }

        int bidId = stockRepo.addProductToBid(storeId, productId, quantity);
        logger.info("[setProductToBid] Product {} successfully set to bid with ID {} in store {}", productId, bidId, storeId);
        return bidId;
    }


    public BidDTO[] getAllBidsStatus(String token, int storeId) throws Exception, DevException {
        logger.info("[getAllBidsStatus] Fetching bid status for store: {}", storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[getAllBidsStatus] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[getAllBidsStatus] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[getAllBidsStatus] User {} lacks permission to view bid status in store {}", userId, storeId);
            throw new UIException("you have no permission to see auctions info.", ErrorCodes.NO_PERMISSION);
        }

        storeRepo.checkStoreExistance(storeId);
        BidDTO[] bids = stockRepo.getAllBids(storeId);

        logger.info("[getAllBidsStatus] Retrieved {} bids for store {}.", bids.length, storeId);
        return bids;
    }


    public SingleBid acceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception, DevException {
        logger.info("[acceptBid] User attempting to accept bid {} for bidId {} in store {}", bidToAcceptId, bidId, storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[acceptBid] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[acceptBid] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[acceptBid] User {} lacks permission to accept bid in store {}", userId, storeId);
            throw new UIException("you have no permission to accept bid", ErrorCodes.USER_NOT_LOGGED_IN);
        }

        SingleBid winner = stockRepo.acceptBid(storeId, bidId, bidToAcceptId);
        logger.info("[acceptBid] Bid accepted. Winning user: {}, bidId: {}", winner.getUserId(), winner.getId());

        return winner;
    }

    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId, long RandomTime)
            throws UIException, DevException {
        logger.info("[setProductToRandom] User attempting to set product {} to random in store {} (qty={}, price={}, duration={})",
                productId, storeId, quantity, productPrice, RandomTime);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[setProductToRandom] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[setProductToRandom] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        int randomId = stockRepo.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
        logger.info("[setProductToRandom] Product {} successfully added to random sale with ID {} in store {}",
                productId, randomId, storeId);

        return randomId;
    }


    public ParticipationInRandomDTO endBid(String token, int storeId, int randomId) throws Exception, DevException {
        logger.info("[endBid] Attempting to end random event {} in store {}", randomId, storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[endBid] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[endBid] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.debug("[endBid] User {} is registered and not suspended.", userId);

        ParticipationInRandomDTO result = stockRepo.endRandom(storeId, randomId);
        logger.info("[endBid] Random event {} in store {} ended successfully. Winner: {}", randomId, storeId, result.getUserId());

        return result;
    }


    public RandomDTO[] getAllRandomInStore(String token, int storeId) throws Exception, DevException {
        logger.info("[getAllRandomInStore] Fetching all random sales in store {}", storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[getAllRandomInStore] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[getAllRandomInStore] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);

        if (!this.suConnectionRepo.manipulateItem(userId, storeId, Permission.SpecialType)) {
            logger.warn("[getAllRandomInStore] User {} not authorized to view random events in store {}", userId, storeId);
            throw new UIException("you have no permission to see random info.", ErrorCodes.NO_PERMISSION);
        }

        RandomDTO[] randoms = stockRepo.getRandomsInStore(storeId);
        logger.info("[getAllRandomInStore] Retrieved {} random events from store {}", randoms.length, storeId);

        return randoms;
    }



    //stock managment:
    public List<ItemStoreDTO> getProductsInStore(int storeId) throws UIException, DevException {
        logger.info("[getProductsInStore] Fetching all products in store: {}", storeId);

        storeRepo.checkStoreExistance(storeId);
        List<ItemStoreDTO> products = stockRepo.getProductsInStore(storeId);

        logger.info("[getProductsInStore] Retrieved {} products from store {}", products.size(), storeId);
        return products;
    }


    public int addItem(int storeId, String token, int productId, int quantity, int price, Category category)
            throws Exception, DevException {
        logger.info("[addItem] User attempting to add item {} (qty={}, price={}) to store {}", productId, quantity, price, storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[addItem] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[addItem] User ID resolved: {}", userId);

        logger.debug("[addItem] Checking if user {} is registered...", userId);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        logger.debug("[addItem] User {} is registered.", userId);

        logger.debug("[addItem] Checking if user {} is suspended...", userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        logger.debug("[addItem] User {} is not suspended.", userId);

        logger.debug("[addItem] Checking if store {} exists...", storeId);
        storeRepo.checkStoreExistance(storeId);
        logger.debug("[addItem] Store {} exists.", storeId);

        if (!suConnectionRepo.manipulateItem(userId, storeId, Permission.AddToStock)) {
            logger.warn("[addItem] User {} lacks AddToStock permission in store {}", userId, storeId);
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }

        stockRepo.checkProductExists_ThrowException(productId);

        if (price <= 0 || quantity <= 0) {
            logger.warn("[addItem] Invalid quantity or price: quantity={}, price={}", quantity, price);
            throw new UIException("price or quantity not in range", 404);
        }

        item toAdd = stockRepo.addItem(storeId, productId, quantity, price, category);
        logger.info("[addItem] Item {} added successfully to store {} with quantity={} and price={}", productId, storeId, quantity, price);

        return toAdd.getProductId();
    }


    public int addProduct(String token, String name, Category category, String description, String[] keywords)
            throws Exception {
        logger.info("[addProduct] User attempting to add a new product: name={}, category={}", name, category);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[addProduct] Token validated.");

        int userId = authRepo.getUserId(token);
        logger.debug("[addProduct] User ID resolved: {}", userId);

        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        int productId = stockRepo.addProduct(name, category, description, keywords);
        logger.info("[addProduct] Product '{}' added successfully with ID {} by user {}", name, productId, userId);

        return productId;
    }


    public int removeItem(int storeId, String token, int productId) throws Exception, DevException {
        logger.info("[removeItem] User attempting to remove item {} from store {}", productId, storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[removeItem] Token validated.");

        int removerId = authRepo.getUserId(token);
        logger.debug("[removeItem] User ID resolved: {}", removerId);

        logger.debug("[removeItem] Checking if user {} is registered...", removerId);
        userRepo.checkUserRegisterOnline_ThrowException(removerId);
        logger.debug("[removeItem] User {} is registered.", removerId);

        logger.debug("[removeItem] Checking if user {} is suspended...", removerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(removerId);
        logger.debug("[removeItem] User {} is not suspended.", removerId);

        logger.debug("[removeItem] Checking if store {} exists...", storeId);
        storeRepo.checkStoreExistance(storeId);
        logger.debug("[removeItem] Store {} exists.", storeId);

        if (!this.suConnectionRepo.manipulateItem(removerId, storeId, Permission.DeleteFromStock)) {
            logger.warn("[removeItem] User {} lacks DeleteFromStock permission in store {}", removerId, storeId);
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }

        stockRepo.removeItem(storeId, productId);
        logger.info("[removeItem] Item {} successfully removed from store {}", productId, storeId);

        return productId;
    }


    public int updateQuantity(int storeId, String token, int productId, int newQuantity) throws Exception, DevException {
        logger.info("[updateQuantity] User attempting to update quantity of product {} in store {} to {}", productId, storeId, newQuantity);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[updateQuantity] Token validated.");

        int changerUserId = authRepo.getUserId(token);
        logger.debug("[updateQuantity] User ID resolved: {}", changerUserId);

        logger.debug("[updateQuantity] Checking if user {} is registered...", changerUserId);
        userRepo.checkUserRegisterOnline_ThrowException(changerUserId);
        logger.debug("[updateQuantity] User {} is registered.", changerUserId);

        logger.debug("[updateQuantity] Checking if user {} is suspended...", changerUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(changerUserId);
        logger.debug("[updateQuantity] User {} is not suspended.", changerUserId);

        logger.debug("[updateQuantity] Checking if store {} exists...", storeId);
        storeRepo.checkStoreExistance(storeId);
        logger.debug("[updateQuantity] Store {} exists.", storeId);

        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdateQuantity)) {
            logger.warn("[updateQuantity] User {} lacks UpdateQuantity permission in store {}", changerUserId, storeId);
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }

        if (newQuantity <= 0) {
            logger.warn("[updateQuantity] Invalid quantity: {}", newQuantity);
            throw new UIException("the quantity not in range", 404);
        }

        stockRepo.updateQuantity(storeId, productId, newQuantity);
        logger.info("[updateQuantity] Quantity updated successfully for product {} in store {} to {}", productId, storeId, newQuantity);

        return productId;
    }


    public int updatePrice(int storeId, String token, int productId, int newPrice) throws Exception, DevException {
        logger.info("[updatePrice] User attempting to update price of product {} in store {} to {}", productId, storeId, newPrice);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[updatePrice] Token validated.");

        int changerUserId = authRepo.getUserId(token);
        logger.debug("[updatePrice] User ID resolved: {}", changerUserId);

        logger.debug("[updatePrice] Checking if user {} is registered...", changerUserId);
        userRepo.checkUserRegisterOnline_ThrowException(changerUserId);
        logger.debug("[updatePrice] User {} is registered.", changerUserId);

        logger.debug("[updatePrice] Checking if user {} is suspended...", changerUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(changerUserId);
        logger.debug("[updatePrice] User {} is not suspended.", changerUserId);

        logger.debug("[updatePrice] Checking if store {} exists...", storeId);
        storeRepo.checkStoreExistance(storeId);
        logger.debug("[updatePrice] Store {} exists.", storeId);

        if (!suConnectionRepo.manipulateItem(changerUserId, storeId, Permission.UpdatePrice)) {
            logger.warn("[updatePrice] User {} lacks UpdatePrice permission in store {}", changerUserId, storeId);
            throw new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION);
        }

        if (newPrice <= 0) {
            logger.warn("[updatePrice] Invalid price: {}", newPrice);
            throw new UIException("the price not in range", 404);
        }

        stockRepo.updatePrice(storeId, productId, newPrice);
        logger.info("[updatePrice] Price updated successfully for product {} in store {} to {}", productId, storeId, newPrice);

        return productId;
    }


    public int rankProduct(int storeId, String token, int productId, int newRank) throws Exception {
        logger.info("[rankProduct] User attempting to update rank of product {} in store {} to {}", productId, storeId, newRank);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.debug("[rankProduct] Token validated.");

        int updaterUserId = authRepo.getUserId(token);
        logger.debug("[rankProduct] User ID resolved: {}", updaterUserId);

        logger.debug("[rankProduct] Checking if user {} is registered...", updaterUserId);
        userRepo.checkUserRegisterOnline_ThrowException(updaterUserId);
        logger.debug("[rankProduct] User {} is registered.", updaterUserId);

        logger.debug("[rankProduct] Checking if user {} is suspended...", updaterUserId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(updaterUserId);
        logger.debug("[rankProduct] User {} is not suspended.", updaterUserId);

        logger.debug("[rankProduct] Checking if store {} exists...", storeId);
        storeRepo.checkStoreExistance(storeId);
        logger.debug("[rankProduct] Store {} exists.", storeId);

        stockRepo.rankProduct(storeId, productId, newRank);
        logger.info("[rankProduct] Rank updated successfully for product {} in store {} to {}", productId, storeId, newRank);

        return productId;
    }

}


