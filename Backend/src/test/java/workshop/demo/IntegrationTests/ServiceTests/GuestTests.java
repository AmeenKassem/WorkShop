package workshop.demo.IntegrationTests.ServiceTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.DelayedNotificationDecorator;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GuestTests {
    PaymentServiceImp payment = new PaymentServiceImp();
    SupplyServiceImp serviceImp = new SupplyServiceImp();
    PurchaseRepository purchaseRepository = new PurchaseRepository();
    UserSuspensionRepo suspensionRepo = new UserSuspensionRepo();
    @Autowired
    NotificationRepository notificationRepository;
    OrderRepository orderRepository = new OrderRepository();
    StoreRepository storeRepository = new StoreRepository();
    StockRepository stockRepository = new StockRepository();
    SUConnectionRepository sIsuConnectionRepo = new SUConnectionRepository();
    AuthenticationRepo authRepo = new AuthenticationRepo();
    Encoder encoder = new Encoder();
    AdminInitilizer adminInitilizer = new AdminInitilizer("123321");
    UserRepository userRepo = new UserRepository(encoder, adminInitilizer);
    UserSuspensionService suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);
    UserService userService = new UserService(userRepo, authRepo, stockRepository, new AdminInitilizer("123321"),new AdminService(orderRepository, storeRepository, userRepo, authRepo));
    StockService stockService = new StockService(stockRepository, storeRepository, authRepo, userRepo,
            sIsuConnectionRepo, suspensionRepo);
    StoreService storeService = new StoreService(storeRepository, notificationRepository, authRepo, userRepo,
            orderRepository, sIsuConnectionRepo, stockRepository, suspensionRepo);
    PurchaseService purchaseService = new PurchaseService(authRepo, stockRepository, storeRepository, userRepo,
            purchaseRepository, orderRepository, payment, serviceImp, suspensionRepo);
    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;

    @BeforeEach
    void setup() throws Exception {
        System.out.println("===== SETUP RUNNING =====");

        purchaseRepository = new PurchaseRepository();
        suspensionRepo = new UserSuspensionRepo();
        orderRepository = new OrderRepository();
        storeRepository = new StoreRepository();
        stockRepository = new StockRepository();
        sIsuConnectionRepo = new SUConnectionRepository();
        authRepo = new AuthenticationRepo();
        encoder = new Encoder();
        adminInitilizer = new AdminInitilizer("123321");
        userRepo = new UserRepository(encoder, adminInitilizer);
        suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);
        // userService = new UserService(userRepo, authRepo, stockRepository, new AdminInitilizer("123321"));
        stockService = new StockService(stockRepository, storeRepository, authRepo, userRepo, sIsuConnectionRepo,
                suspensionRepo);
        storeService = new StoreService(storeRepository, notificationRepository, authRepo, userRepo, orderRepository,
                sIsuConnectionRepo, stockRepository, suspensionRepo);
        purchaseService = new PurchaseService(authRepo, stockRepository, storeRepository, userRepo, purchaseRepository,
                orderRepository, payment, serviceImp, suspensionRepo);

        GToken = userService.generateGuest();

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");
        assertEquals(createdStoreId, 1);

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        stockService.addProduct(NOToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);

        assertEquals(1, stockService.addItem(1, NOToken, 1, 2, 2000, Category.ELECTRONICS));
        itemStoreDTO = new ItemStoreDTO(1, 2, 2000, Category.ELECTRONICS, 0, 1);

        // ======================= SECOND GUEST SETUP =======================

    }

    @AfterEach

    void tearDown() {
        if (userRepo != null)
            userRepo.clear();
        if (storeRepository != null)
            storeRepository.clear();
        if (stockRepository != null)
            stockRepository.clear();
        if (orderRepository != null)
            orderRepository.clear();
        if (suspensionRepo != null)
            suspensionRepo.clear();
        // Add clear() for all other repos you wrote it for
    }

    // NOTE:ENTER+EXIT+REGISTER FINISH
    @Test
    void testGuestEnter_Success() throws Exception {

        String token = userService.generateGuest();

        assertTrue(authRepo.getUserId(token) == 4);
    }

    @Test
    void testGuestExit_Success() throws Exception {

        String token = userService.generateGuest();

        userService.destroyGuest(token);
        assertFalse(userRepo.guestExist(4));

    }

    @Test
    void testGuestExit_Failure_InvalidToken() throws Exception {

        assertThrows(UIException.class, () -> userService.destroyGuest("invalid token"));

    }

    @Test
    void testGuestRegister_Success() throws Exception {
        // enter
        String token = userService.generateGuest();

        userService.register(token, "Mohamad", "finish", 24);
        String tt = userService.login(token, "Mohamad", "finish");
        assertTrue(authRepo.getUserName(tt).equals("Mohamad"));

    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {

        String token = userService.generateGuest();

        userService.register(token, "Mohamad", "finish", 24);

        UIException exception = assertThrows(UIException.class, () -> {
            userService.register(token, "Mohamad", "finish", 24);
        });

        assertEquals(ErrorCodes.USERNAME_USED, exception.getNumber());
    }

    // NOTE:GET STORE PRODUCT FINISH
    @Test
    void testGuestGetStoreProducts() throws Exception {

        List<ItemStoreDTO> items = stockService.getProductsInStore(1);
        assertTrue(items.size() == 1);
        assertTrue(items.get(0).getId() == 1);
    }

    @Test
    void testGuestViewEmptyStore() throws Exception {
        storeService.addStoreToSystem(NOToken, "failure", "HOME");

        List<ItemStoreDTO> products = stockService.getProductsInStore(2);

        // ===== ASSERT =====
        assertTrue(products.isEmpty());
    }

    @Test
    void testGuestGetProductInfo() throws Exception {

        // ===== GUEST REQUESTS PRODUCT INFO =====
        ProductDTO info = stockService.getProductInfo(GToken, 1);

        // ===== ASSERTIONS =====

        assertNotNull(info);
        System.out.println(info.getName());
        assertTrue(info.getName().equals("Laptop"));
        assertTrue(info.getProductId() == 1);
        assertTrue(info.getCategory().equals(Category.ELECTRONICS));
        assertTrue(info.getDescription().equals("Gaming Laptop"));

    }

    @Test
    void testGuestGetProductInfo_ProductNotFound() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(GToken, 2);
        });

        assertEquals("Product not found.", exception.getMessage());
    }

    // NOTE:ADD TO CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestAddProductToCart_Success() throws Exception {

        assertDoesNotThrow(() -> {
            userService.addToUserCart(GToken, itemStoreDTO, 1);
        });
    }

    @Test
    void testGuestAddProductToCart_InvalidToken() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            userService.addToUserCart("invalid token", itemStoreDTO, 1);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());

        // Assert
    }

    // //NOTE :BUY CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestBuyCart_Success() throws Exception {

        userService.addToUserCart(GToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000.0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().getFirst().getPrice());

        // --- Step 8: Verify important calls happened ---

    }

    @Test
    void testGuestBuyCart_InvalidToken() throws Exception {
        String guestToken = "guest-token";

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testGuestBuyCart_ProductNotAvailable() throws Exception {

        userService.addToUserCart(GToken, new ItemStoreDTO(0, 0, 0, null, 0, 0), 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails));

        assertEquals("Not all items are available for guest purchase", ex.getMessage());
    }

    @Test
    void testGuestBuyCart_PaymentFails() throws Exception {

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.buyGuestCart(GToken, PaymentDetails.test_fail_Payment(), supplyDetails));

        assertEquals("Payment failed", ex.getMessage());
    }

    @Test
    void testGuestBuyCart_supplier_Fails() throws Exception {

        Exception ex = assertThrows(Exception.class, () -> purchaseService.buyGuestCart(GToken,
                PaymentDetails.testPayment(), SupplyDetails.test_fail_supply()));

        assertEquals("supplier failed", ex.getMessage());
    }

    @Test
    void testGuestSearchProductInStore_Success() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // product name filter
                null, // category filter
                null, // keyword filter
                1, // store ID to filter
                0, 10000, // no price range
                0, 5 // no rating range
        );

        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(2000, result[0].getPrice());
        assertEquals(1, result[0].getStoreId());
        assertEquals(Category.ELECTRONICS, result[0].getCategory());
    }

    @Test
    void testGuestSearchProducts_Success() throws Exception {

        // --- Step 2: Prepare search criteria ---
        String[] keywords = { "Laptop", "Lap", "top" };
        System.out.println(storeService.getFinalRateInStore(1));

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.ELECTRONICS, null, 1, 0, 2000, 0,
                2);

        // --- Step 5: Call the system under test ---
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        // --- Step 6: Assert results ---
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(1, result[0].getId());
        assertEquals(2000, result[0].getPrice());
        assertEquals(1, result[0].getStoreId());

        // --- Step 7: Verify mocks ---

    }

    @Test
    void testSearchProducts_InvalidToken() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, "Laptop", 100,
                0, 5000,
                0, 5);

        // 1. Throw on auth check

        // 3. Run the test
        UIException exception = assertThrows(UIException.class, () -> {
            stockService.searchProducts("invalid token", criteria);
        });

        // 4. Verify
        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testSearchProducts_NoMatches() throws Exception {

        String[] keywords = { "Laptop", "Lap", "top" };
        ProductSearchCriteria criteria = new ProductSearchCriteria("aa", Category.ELECTRONICS, keywords[0], 1, 0, 5000,
                0, 5);
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchProducts_ProductExists_NotInStore() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "toy", Category.ELECTRONICS, null, 1,
                0, 0, 0, 0);
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product not sold in this store
    }

    @Test
    void testSearchProducts_PriceOutOfRange() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, 1,
                5000, 10000, 0, 5 // Price filter too high
        );
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGuestModifyCartAddQToBuy() throws Exception {
        throw new Exception("not implmented");
    }

}
