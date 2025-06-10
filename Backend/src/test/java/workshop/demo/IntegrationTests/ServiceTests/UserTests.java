package workshop.demo.IntegrationTests.ServiceTests;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import workshop.demo.ApplicationLayer.*;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.User.ShoppingCart;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserTests {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserSuspensionRepo suspensionRepo;
    @Autowired
    private AuthenticationRepo authRepo;

    @Autowired
    PaymentServiceImp payment;
    @Autowired
    SupplyServiceImp serviceImp;

    @Autowired
    SUConnectionRepository sIsuConnectionRepo;

    @Autowired
    Encoder encoder;
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserSuspensionService suspensionService;
    @Autowired
    AdminHandler adminService;
    @Autowired
    UserService userService;
    @Autowired
    StockService stockService;
    @Autowired
    StoreService storeService;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    OrderService orderService;

    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;
    String Admin;

    @BeforeEach
    void setup() throws Exception {
        // ====== ADMIN SETUP ======

        String GToken = userService.generateGuest();
        userService.register(GToken, "User", "User", 25);

        // --- Login ---
        NGToken = userService.login(GToken, "User", "User");

        assertTrue(authRepo.getUserName(NGToken).equals("User"));

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");
        assertEquals(createdStoreId, 1);

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = {"Laptop", "Lap", "top"};
        stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        assertEquals(1, stockService.addItem(1, NOToken, 1, 10, 2000, Category.Electronics));
        itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.Electronics, 0, 1, "Laptop", "TestStore");

    }

    @AfterEach
    void tearDown() {
        if (userRepo != null) {
            userRepo.clear();
        }
        if (storeRepository != null) {
            storeRepository.clear();
        }
        if (stockRepository != null) {
            stockRepository.clear();
        }
        if (orderRepository != null) {
            orderRepository.clear();
        }
        if (suspensionRepo != null) {
            suspensionRepo.clear();
        }
        // Add clear() for all other repos you wrote it for
    }

    @Test
    void testUser_Add_Admin() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User1", "User");

        assertTrue(authRepo.getUserName(token1).equals("User1"));
        userService.setAdmin(Token, "123321", 5);
        assertTrue(userRepo.isAdmin(5));

    }

    @Test
    void testUser_Add_Admin_wrongkey() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User1", "User");

        assertTrue(authRepo.getUserName(token1).equals("User1"));
        userService.setAdmin(Token, "13321", 6);
        assertFalse(userRepo.isAdmin(6));

    }

    @Test
    void testUser_LogIn_Success() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User1", "User");

        assertTrue(authRepo.getUserName(token1).equals("User1"));

    }

    @Test
    void testUser_LogOut_Success() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User1", "User");

        assertTrue(authRepo.getUserName(token1).equals("User1"));

        userService.logoutUser(token1);
        assertFalse(userRepo.isOnline(authRepo.getUserId(token1)));
    }

    @Test
    void testUser_LogIn_Failure() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // --- Login ---
        UIException ex = assertThrows(UIException.class, () -> {
            userService.login(Token, "User1", "invalid");
        });
        assertEquals("Incorrect username or password.", ex.getMessage());
    }

    @Test
    void testUser_LogOut_Failure() throws Exception {

        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);
        userService.login(Token, "User1", "User");

        // Expect the logout to fail due to invalid token
        UIException ex = assertThrows(UIException.class, () -> userService.logoutUser("invalidToken"));

        // Verify exception details
        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testUserLogin_Failure_InvalidToken() throws Exception {

        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        // Expect the logout to fail due to invalid token
        UIException ex = assertThrows(UIException.class, () -> userService.login("invalidToken", "user1", "User"));
        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testUserLogin_Failure_UserNotFound() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1", "User", 25);

        UIException exception = assertThrows(UIException.class, () -> userService.login(Token, "ghost", "nopass"));

        assertEquals("User not found: ghost", exception.getMessage());
    }

    @Test
    void testUserLogout_Failure_UserNotFound() throws Exception {
        String token = "bad-token";

        UIException exception = assertThrows(UIException.class, () -> userService.logoutUser(token));

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testUser_setAdmin_Success() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser3", "adminPass3", 22);
        String token1 = userService.login(token, "adminUser3", "adminPass3");
        userService.setAdmin(token1, "123321", authRepo.getUserId(token1));

        assertTrue(userRepo.isAdmin(authRepo.getUserId(token1)));

    }

    @Test
    void testUser_setAdmin_Failure_InvalidToken() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser3", "adminPass3", 22);
        String token1 = userService.login(token, "adminUser3", "adminPass3");
        userService.setAdmin(token1, "123321", authRepo.getUserId(token1));

        assertTrue(userRepo.isAdmin(authRepo.getUserId(token1)));
        UIException ex = assertThrows(UIException.class, () -> {
            userService.setAdmin("invalid token", "123321", 1);
        });
        assertFalse(userService.setAdmin(token, "121", 7));
    }

    @Test
    void testUser_setAdmin_Failure_LogoutUserThrows() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser3", "adminPass3", 22);

        assertFalse(userService.setAdmin(token, "adminUser3", 5));
    }

    @Test
    void testUser_CheckPurchaseHistory_Success() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000.0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(2000, r.getFinalPrice());
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 9);

    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_InvalidToken() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000.0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser("invalid token");
        });
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertEquals("Invalid token!", ex.getMessage());
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 9);

    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_NoReceipts() throws Exception {

        // Act & Assert
        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser(NGToken);
        });

        assertEquals(ErrorCodes.RECEIPT_NOT_FOUND, ex.getNumber());
        assertEquals("User has no receipts.", ex.getMessage());
    }

    @Test
    void testUserGetStoreProducts() throws Exception {
        ItemStoreDTO[] items = stockService.getProductsInStore(1);
        assertTrue(items.length == 1);
        assertTrue(items[0].getProductId() == 1);

    }

    @Test
    void testUserViewEmptyStore() throws Exception {
        storeService.addStoreToSystem(NOToken, "failure", "HOME");

        ItemStoreDTO[] products = stockService.getProductsInStore(2);
// ask bhaa i dont know what is happening , how are we getting an item please help help help

        // ===== ASSERT =====
        assertTrue(products.length == 0);
    }

    @Test
    void testUserViewInvalidStore() throws Exception {

        ItemStoreDTO[] products = null;
        try {
            products = stockService.getProductsInStore(2);
        } catch (Exception e) {
            assertNull(products);
            assertEquals(" store does not exist.", e.getMessage());
        }
    }

    @Test
    void testUserGetProductInfo() throws Exception {
        ProductDTO info = stockService.getProductInfo(NGToken, 1);

        // ===== ASSERTIONS =====
        assertNotNull(info);
        System.out.println(info.getName());
        assertTrue(info.getName().equals("Laptop"));
        assertTrue(info.getProductId() == 1);
        assertTrue(info.getCategory().equals(Category.Electronics));
        assertTrue(info.getDescription().equals("Gaming Laptop"));
    }

    @Test
    void testUserGetProductInfo_ProductNotFound() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(NGToken, 2);
        });

        assertEquals("Product not found.", exception.getMessage());
    }

    @Test
    void testUserAddProductToCart_Success() throws Exception {
        assertDoesNotThrow(() -> {
            userService.addToUserCart(NGToken, itemStoreDTO, 1);
        });
        assertTrue(userService.getRegularCart(NGToken).length == 1);

    }

    @Test
    void testUserAddProductToCart_InvalidToken() throws Exception {
        UIException exception = assertThrows(UIException.class, () -> {
            userService.addToUserCart("invalid token", itemStoreDTO, 1);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());

        assertTrue(userService.getRegularCart(NGToken).length == 0);

        // Assert
    }

    @Test
    void testUserBuyCart_Success() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000.0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        assertTrue(orderService.getReceiptDTOsByUser(NGToken).size() == 1);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice() == 2000);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 9);
        assertTrue(userRepo.getUserCart(authRepo.getUserId(NGToken)).getAllCart().size() == 0);
    }

    @Test
    void testUserBuyCart_ProductNotAvailable() throws Exception {

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        assertThrows(Exception.class,
                () -> userService.addToUserCart(NGToken, new ItemStoreDTO(0, 0, 0, null, 0, 2, "", "TestStore"), 1));

    }

    @Test
    void testUserBuyCart_EmptyCart() throws Exception {

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 10);

    }

    @Test
    void testUserBuyCart_PaymentFails() throws Exception {
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.buyRegisteredCart(NGToken, PaymentDetails.test_fail_Payment(), supplyDetails));

        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 10);

    }

    @Test
    void testUserSearchProductInStore_Success() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // product name filter
                null, // category filter
                null, // keyword filter
                1, // store ID to filter
                0, 10000, // no price range
                0, 5 // no rating range
        );

        ItemStoreDTO[] result = stockService.searchProducts(NGToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(2000, result[0].getPrice());
        assertEquals(1, result[0].getStoreId());
        assertEquals(Category.Electronics, result[0].getCategory());
    }

    @Test
    void testUserSearchProducts_InvalidToken() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.Electronics, "Laptop", 100,
                0, 5000,
                0, 5);

        // 1. Throw on auth check
        // 3. Run the test
        UIException exception = assertThrows(UIException.class, () -> {
            stockService.searchProducts("invalid token", criteria);
        });
        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testUserSearchProducts_NoMatches() throws Exception {

        String[] keywords = {"Laptop", "Lap", "top"};
        ProductSearchCriteria criteria = new ProductSearchCriteria("aa", Category.Electronics, keywords[0], 1, 0, 5000,
                0, 5);
        ItemStoreDTO[] result = stockService.searchProducts(NGToken, criteria);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testUserSearchProductInStore_InvalidToken() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.Electronics, "Laptop", 100,
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
    void testUserSearchProductInStore_ProductNotInStore() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "toy", Category.Electronics, null, 1,
                0, 0, 0, 0);
        ItemStoreDTO[] result = stockService.searchProducts(NGToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product not sold in this store
    }

    @Test
    void testUserGetPurchasePolicy_Success() throws Exception {
        // throw new Exception("not implmented");

    }

    @Test
    void testUserGetPurchasePolicy_Failure() throws Exception {
        // throw new Exception("not implmented");
    }

    @Test

    void testGuestModifyCartAddQToBuy_sucess() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        userService.ModifyCartAddQToBuy(NGToken, 1, 3);
        ReceiptDTO[] re = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
        assertTrue(re[0].getFinalPrice() == 6000);
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 7);

    }

    @Test
    void testGuestModifyCartAddQToBuy_failure() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        UIException ex = assertThrows(UIException.class, ()
                -> userService.ModifyCartAddQToBuy("INVALID", 1, 2)
        );
        ReceiptDTO[] re = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 9);

    }

    @Test
    void testGetReceiptDTOsByUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser("bad-token");
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testGetReceiptDTOsByUser_UserNotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // valid token, but not registered

        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser(guestToken);
        });

        int userId = authRepo.getUserId(guestToken);
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getErrorCode());
        assertEquals("The user:" + userId + " is not registered to the system!", ex.getMessage());
    }

    @Test
    void testRemoveItemFromCart_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            userService.removeItemFromCart("bad-token", 1); // productId can be any value
        });

    }

    @Test
    void testRemoveItemFromCart_Success() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Add item first
        boolean result = userService.removeItemFromCart(NGToken, itemStoreDTO.getProductId());
        assertTrue(result);
        assertTrue(userService.getRegularCart(NGToken).length == 0);
    }

    @Test
    void testGetUserDTO_Success() throws Exception {
        // Arrange
        UserDTO dto = userService.getUserDTO(NGToken);

        // Assert
        assertNotNull(dto);
        assertEquals("User", dto.username);
    }

    @Test
    void testGetUserDTO_InvalidToken_ThrowsException() {
        // Arrange + Act + Assert
        Exception ex = assertThrows(Exception.class, () -> {
            userService.getUserDTO("invalid-token");
        });
    }

    @Test
    void testGetAllUsers_AsAdmin_Success() throws Exception {
        // Act
        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        String token1 = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(token1, "123321", authRepo.getUserId(token1));

        List<UserDTO> allUsers = userService.getAllUsers(token1);

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("User")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("owner")));
    }

    @Test
    void testGetAllUsers_NotAdmin_ThrowsException() {
        // Act + Assert
        Exception ex = assertThrows(Exception.class, () -> {
            userService.getAllUsers(NGToken); // regular user token
        });
    }

    @Test
    void testProcessPayment_NegativeAmount_ThrowsUIException() throws UIException {
        userService.addToUserCart(NGToken, new ItemStoreDTO(1, 1, -50, Category.Electronics, 3, 1, "Test", "TestStore"), 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
        });
        assertEquals("Invalid payment amount.", ex.getMessage());
    }
    //admin

    @Test
    public void testViewPurchaseHistory_NoOrders_ReturnsEmpty() throws Exception {
        adminService.recordLoginEvent();
        adminService.recordLogoutEvent();
        adminService.recordRegisterEvent();
        String token = userService.generateGuest();
        userService.register(token, "admin2", "admin2", 22);
        String adminToken = userService.login(token, "admin2", "admin2");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
        List<PurchaseHistoryDTO> history = adminService.viewPurchaseHistory(adminToken);
        assertNotNull(history);
        assertEquals(1, history.size());
    }

    @Test
    public void testGetSystemAnalytics_EventsTrackedProperly() throws Exception {
        // Simulate some system activity
        adminService.recordLoginEvent();
        adminService.recordLogoutEvent();
        adminService.recordRegisterEvent();
        String token = userService.generateGuest();
        userService.register(token, "admin2", "admin2", 22);
        String adminToken = userService.login(token, "admin2", "admin2");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);

        SystemAnalyticsDTO dto = adminService.getSystemAnalytics(adminToken);
        assertNotNull(dto);

        Map<LocalDate, Integer> logins = dto.getLoginsPerDay();
        Map<LocalDate, Integer> logouts = dto.getLogoutsPerDay();
        Map<LocalDate, Integer> registers = dto.getRegisterPerDay();

        LocalDate today = LocalDate.now();

        assertTrue(logins.getOrDefault(today, 0) >= 1);
        assertTrue(logouts.getOrDefault(today, 0) >= 1);
        assertTrue(registers.getOrDefault(today, 0) >= 1);
    }

    @Test
    public void testViewPurchaseHistory_InvalidToken_ThrowsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            adminService.viewPurchaseHistory("invalid-token");
        });
        assertEquals("Invalid Token!", ex.getMessage());
    }

    @Test
    public void testGetSystemAnalytics_InvalidToken_ThrowsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            adminService.getSystemAnalytics("invalid-token");
        });
        assertEquals("Invalid Token!", ex.getMessage());
    }

    @Test
    void test_getUserCart_registeredUser_returnsCart() throws Exception {
        String guestToken = userService.generateGuest();
        userService.register(guestToken, "user1", "pass1", 25);
        String token = userService.login(guestToken, "user1", "pass1");
        int userId = authRepo.getUserId(token);

        ShoppingCart cart = userRepo.getUserCart(userId);

        assertNotNull(cart);
        assertTrue(cart.getAllCart().isEmpty()); // assuming empty cart
    }

    @Test
    void test_sendRTMessageToUser_success() throws Exception {
        notificationService.sendRTMessageToUser(authRepo.getUserName(NOToken), "Hello there!");
    }

    @Test
    void test_sendDMMessageToUser_success() throws Exception {

        notificationService.sendDMessageToUser(authRepo.getUserName(NGToken), "You missed this!");

    }

    @Test
    void test_getDelayedMessages_returnsMessages() throws UIException {
        notificationService.sendDMessageToUser(authRepo.getUserName(NOToken), "You missed this!");

        String[] messages = {"You missed this!"};

        String[] result = notificationService.getDelayedMessages(authRepo.getUserName(NOToken));

        assertArrayEquals(messages, result);

    }

}
