package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.NotificationService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.ReviewRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

import static org.mockito.Mockito.when;

public class Real implements Bridge {

    public AuthenticationRepo mockAuthRepo = Mockito.mock(AuthenticationRepo.class);
    public UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
    public StoreRepository mockStoreRepo = Mockito.mock(StoreRepository.class);
    public NotificationRepository mockNotiRepo = Mockito.mock(NotificationRepository.class);
    public OrderRepository mockOrderRepo = Mockito.mock(OrderRepository.class);
    public PurchaseRepository mockPurchaseRepo = Mockito.mock(PurchaseRepository.class);
    public StockRepository mockStockRepo = Mockito.mock(StockRepository.class);

    public ISUConnectionRepo mockIOSrepo = Mockito.mock(ISUConnectionRepo.class);
    public UserSuspensionRepo mockSusRepo = Mockito.mock(UserSuspensionRepo.class);
    public IReviewRepo mockReviewRepo = Mockito.mock(ReviewRepository.class);

    //public SuperDataStructure mockserviceios=new SuperDataStructure();
    //    public SingleBid mockSingleBid = Mockito.mock(SingleBid.class);
//    public ShoppingCart mockShoppingCart = Mockito.mock(ShoppingCart.class);
    public PaymentServiceImp mockPay = Mockito.mock(PaymentServiceImp.class);
    public SupplyServiceImp mockSupply = Mockito.mock(SupplyServiceImp.class);
    // Service layer classes (built with mocks)
    public StockService stockService;
    public UserService userService;
    public StoreService storeService;
    public NotificationService notificationService;
    public PurchaseService purchaseService;
    public OrderService orderService;
    public ReviewService reviewService;
    public AdminHandler adminHandler;

    public Real() throws Exception {
        initServices();
        //setupDefaultMocks();
    }

    private void initServices() throws Exception {
        when(this.mockUserRepo.getRegisteredUserByName("admin")).thenReturn(new Registered(1,"admin","Admin123",23));        orderService = new OrderService(mockOrderRepo, mockStoreRepo, mockAuthRepo, mockUserRepo);
        AdminHandler admin = new AdminHandler(mockOrderRepo, mockStoreRepo, mockUserRepo, mockAuthRepo);
        stockService = new StockService(mockStockRepo, mockStoreRepo, mockAuthRepo, mockUserRepo, mockIOSrepo, mockSusRepo);
        adminHandler = new AdminHandler(mockOrderRepo, mockStoreRepo, mockUserRepo, mockAuthRepo);
        userService = new UserService(mockUserRepo, mockAuthRepo, mockStockRepo, new AdminInitilizer("123321"), adminHandler,mockStoreRepo);
        storeService = new StoreService(mockStoreRepo, mockNotiRepo, mockAuthRepo, mockUserRepo, mockOrderRepo, mockIOSrepo, mockStockRepo, mockSusRepo);
        notificationService = new NotificationService(mockNotiRepo, mockUserRepo);
        purchaseService = new PurchaseService(mockAuthRepo, mockStockRepo, mockStoreRepo, mockUserRepo,
                mockPurchaseRepo, mockOrderRepo, mockPay, mockSupply, mockSusRepo);
        reviewService = new ReviewService(mockReviewRepo, mockAuthRepo, mockUserRepo, mockStoreRepo, mockStockRepo);
    }

    /////////////////////// System /////////////////////////////
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {

        return "TODO";
    }

    @Override
    public String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        //notificationService.sendDMessageToAll(receiversIds, message, senderId);
        return "Done";
    }

    @Override
    public String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        //notificationService.sendRTMessageToAll(receiversIds, message, senderId);
        return "Done";
    }

    /////////////////////// Guest /////////////////////////////
    @Override
    public String testGuest_Enter() throws Exception {
        String guestToken = userService.generateGuest();
        return guestToken;
    }

    @Override
    public boolean testGuest_Exit(String token) throws Exception {
        return userService.destroyGuest(token);
    }

    @Override
    public boolean testGuest_Register(String token, String username, String password, int age) throws Exception {
        return userService.register(token, username, password, age);
    }

    @Override
    public String testGuest_GetStoreProducts(int storeID) throws Exception {
        ItemStoreDTO[] items = stockService.getProductsInStore(storeID);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(items);
    }

    // stock service
    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        ProductDTO a = stockService.getProductInfo(token, productID);
        return "" + a.getProductId() + " " + a.getName() + " " + a.getCategory() + " " + a.getDescription();
    }

    @Override
    public ItemStoreDTO[] testGuest_SearchProduct(String token, ProductSearchCriteria criteria) throws Exception {
        return stockService.searchProducts(token, criteria);

    }

    // search product
    // -1 in the system
    // with store id in the store id
    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        //return stockService.searchProducts(token,storeID, productID);
        return "Done";
    }

    // for the user is the same think
    @Override
    public boolean testGuest_AddProductToCart(String token, ItemStoreDTO a, int qunatity) throws Exception {
        return userService.addToUserCart(token, a, qunatity);
    }

    @Override
    public String testGuest_ModifyCartAddQToBuy(int storeId, String token, int productId) throws Exception {
//        // return stockService.modifyCart(token, cartID);
//        stockService.updateQuantity(storeId, token, productId);
        return "Done";

    }

    @Override
    public String testGuest_BuyCart(String token) throws Exception {
        ReceiptDTO[] a = purchaseService.buyGuestCart(token, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        return a[0].getStoreName() + a[0].getProductsList().toString();

    }

    @Override
    public String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception {
        // have to be in the usersirvice ?
        //return storeService.(token ,storeID);
        return "TODO";
    }

    //////////////////////////// User ////////////////////////////
    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return userService.login(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return userService.logoutUser(token);
    }

    @Override
    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        storeService.addStoreToSystem(token, storeName, category);
        return "Done";
    }

    @Override
    public String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception {
        // return storeService.addReview(token, storeId, review);
        return "TODO";

    }

    @Override
    public String testUser_AddReviewToProduct(String token, int storeId, int productId, String review)
            throws Exception {
        // return productService.addReview(token, storeId, review);
        return "TODO";

    }

    @Override
    public String testUser_RateProduct(int storeId, String token, int productId, int newRank) throws Exception {
        // return productService.rateProduct(token, storeID, productID, rate);
        //storeService.rankProduct(storeId, token, productId, newRank);
        return "Done";
    }

    @Override
    public String testUser_RateStore(String token, int storeId, int newRank) throws Exception {
        storeService.rankStore(token, storeId, newRank);
        return "Done";

    }

    @Override
    public String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception {
        //notificationService.sendDMessageToUser(userId, ownerId, msg);
        return "Done";
    }

    @Override
    public String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception {
        //notificationService.sendDMessageToUser(userId, adminId, msg);
        return "Done";
    }

    @Override
    public List<ReceiptDTO> testUser_CheckPurchaseHistory(String token) throws Exception {
        return orderService.getReceiptDTOsByUser(token);
    }

//    @Override
//    public String testUser_updateProfile(String token) throws Exception {
//        userService.updateProfile(token);
//        return "TODO";
//
//    }
    @Override
    public String testUser_AddBid(String token, int bitId, int storeId, double price) throws Exception {
        // return purchaseService.addBid(token, storeID, productID, bid);
        //storeService.addRegularBid(token, bitId, storeId, price);
        return "Done";

    }

    @Override
    public String testUser_JoinAuction(String token, int auctionId, int storeId, double price) throws Exception {
        // return purchaseService.joinAuction(token, storeID, auctionID);
        //storeService.addBidOnAucction(token, auctionId, storeId, price);
        return "Done";

    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        // return purchaseService.joinRandom(token, storeID, raffleID, num);
        return "TODO";

    }

    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        //if (userService.setAdmin(token, newAdminUsername))
        return "Done";
        //else
        //return "false";
    }

    @Override
    public String testUser_getAllAucationInStore(String token, int storeId) throws Exception {
        //storeService.getAllAuctions(token, storeId);
        return "Done";
    }

    @Override

    public String testUser_getAllRandomInStore(String token, int storeId) throws Exception {
        //storeService.getAllRandomInStore(token, storeId);
        return "Done";
    }

    // the function that named here is for the guest
    @Override
    public String testUser_BuyCart(String token) throws Exception {
        purchaseService.buyRegisteredCart(token, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        return "Done";

    }

    //////////////////////////// Owner ////////////////////////////
    @Override
    public String testOwner_ManageInventory_AddProduct(int storeId, String token, int productId, int quantity,
            int price, Category category)
            throws Exception {
        stockService.addItem(storeId, token, productId, quantity, price, category);
        return "Done";
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(int storeId, String token, int productId) throws Exception {
        stockService.removeItem(storeId, token, productId);
        return "Done";

    }

    @Override
    public String testOwner_ManageInventory_UpdateProductPrice(int storeId, String token, int productId, int newPrice)
            throws Exception {
        stockService.updatePrice(storeId, token, productId, newPrice);
        return "Done";

    }

    @Override
    public String testOwner_SetPurchasePolicies(int storeId, String token, int productId) throws Exception {
        //storeService.updateQuantity(storeId, token, productId);
        return "Done";

    }

    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return "TODO";

    }

    @Override
    public String testOwner_AssignNewOwner(int token, int storeID, int NewOwner, boolean decide) throws Exception {
        storeService.AddOwnershipToStore(storeID, token, NewOwner, decide);
        return "Done";
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        storeService.DeleteOwnershipFromStore(storeID, token, ownerToRemoveId);
        return "Done";
    }

//    @Override
//    public String testOwner_AssignManager(String token, int storeID, int mangerId) throws Exception {
//        storeService.AddManagerToStore(storeID, token, mangerId);
//        return "Done";
//    }
    @Override
    public String testOwner_EditManagerPermissions(String token, int managerId, int storeID,
            List<Permission> autorization) throws Exception {
        storeService.changePermissions(token, managerId, storeID, autorization);
        return "Done";
    }

    @Override
    public String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception {
        storeService.deleteManager(storeID, token, managerId);
        return "Done";
    }

    @Override
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        return "TODO";
    }

    @Override
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return "TODO";
    }

    @Override
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return "TODO";

    }

    @Override
    public String testOwner_ReceiveNotifications(int userId) throws Exception {
        //notificationService.getDelayedMessages(userId);
        return "Done";
    }

    @Override
    public String testOwner_ReplyToMessages(String msg, int ownerId, int UserId) throws Exception {
        //notificationService.sendRTMessageToUser(msg, ownerId, UserId);
        return "Done";
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory(int storeId) throws Exception {
        storeService.veiwStoreHistory(storeId);
        return "Done";
    }

    @Override
    public String testOwner_ViewStoreRanks(int storeId) throws Exception {
        storeService.getFinalRateInStore(storeId);
        return "Done";
    }

    @Override
    public String testOwner_addProductToAucation(String token, int id, int productId, int quantity, long time,
            double startPrice)
            throws Exception {
        //storeService.setProductToAuction(token, id, productId, quantity, time, startPrice);
        return "Done";
    }

    @Override
    public String testOwner_addProductToBid(String token, int storeid, int productId, int quantity)
            throws Exception {
        //storeService.setProductToBid(token, storeid, productId, quantity);
        return "Done";
    }

    @Override
    public String testOwner_EndBid(String token, int storeId, int randomId)
            throws Exception {
        //storeService.endBid(token, storeId, randomId);
        return "Done";
    }

    @Override
    public String testOwner_AcceptBid(String token, int storeId, int bidId, int bidToAcceptId)
            throws Exception {
        //storeService.acceptBid(token, storeId, bidId, bidToAcceptId);
        return "Done";
    }

    @Override
    public String testOwner_BidStatus(String token, int storeId)
            throws Exception {
        //storeService.getAllBidsStatus(token, storeId);
        return "Done";
    }

    @Override
    public String testOwner_addProductToRandom(String token, int storeId, int quantity, int productId,
            int numberOfCards, double priceForCard)
            throws Exception {
        //storeService.setProductToRandom(token, storeId, quantity, productId, numberOfCards, (long)priceForCard);
        return "Done";
    }

    //////////////////////////// Manager ////////////////////////////
    @Override
    public String testManager_PerformPermittedActions(String token, int storeID)
            throws Exception {
        return "TODO";
    }

    //////////////////////////// Admin ////////////////////////////

    @Override
    public String testAdmin_CloseStore(int storeID, String token) throws Exception {
        return "TODO";
    }

    @Override
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return "TODO";

    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return "TODO";

    }

    @Override
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        return "TODO";
    }

}
