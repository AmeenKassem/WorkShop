package workshop.demo.IntegrationTests.ServiceTests;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.transaction.Transactional;
// import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.*;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.SystemAnalyticsDTO;
import workshop.demo.DTOs.UserDTO;

import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows non-static @BeforeAll

public class UserTests {

    @Autowired
    private IStoreRepoDB storeRepositoryjpa;

    @Autowired
    private IOrderRepoDB orderRepository;
    // @Autowired
    // private PurchaseRepository purchaseRepository;

    @Autowired
    private AuthenticationRepo authRepo;

    @Autowired
    private UserJpaRepository userRepo;

    @Autowired
    public ActivePurchasesService activePurcheses;

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

    @Autowired
    DatabaseCleaner databaseCleaner;

    // ======================== Test Data ========================
    String NOToken;
    String NGToken;

    ItemStoreDTO itemStoreDTO;
    int PID;

    int createdStoreId;

    @BeforeAll
    void setup() throws Exception {
        long timer = System.currentTimeMillis();
        databaseCleaner.wipeDatabase();
        System.out.println((System.currentTimeMillis() - timer) + "'ms takes the setup time ");

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

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");
        System.out.println(createdStoreId + "aaaaaaaaaaaaaaaaa");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = {"Laptop", "Lap", "top"};
        PID = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);
        // timer = (System.currentTimeMillis()-timer);
        System.out.println((System.currentTimeMillis() - timer) + "'ms takes the setup time ");

        stockService.addItem(createdStoreId, NOToken, PID, 100, 2000, Category.Electronics);
        itemStoreDTO = new ItemStoreDTO(PID, 100, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");

    }

    @BeforeEach
    void setup1() throws Exception {
        stockService.updateQuantity(createdStoreId, NOToken, PID, 100);
        if (userService.getRegularCart(NGToken).length != 0) {

            databaseCleaner.wipeDatabase();

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

            createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");
            System.out.println(createdStoreId + "aaaaaaaaaaaaaaaaa");

            // ======================= PRODUCT & ITEM ADDITION =======================
            String[] keywords = {"Laptop", "Lap", "top"};
            PID = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);
            // timer = (System.currentTimeMillis()-timer);

            stockService.addItem(createdStoreId, NOToken, PID, 10, 2000, Category.Electronics);
            itemStoreDTO = new ItemStoreDTO(PID, 10, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");

        }
    }

    @Test
    void testUser_Add_Admin() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User12", "User12", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User12", "User12");

        assertTrue(authRepo.getUserName(token1).equals("User12"));

        userService.setAdmin(Token, "123321", authRepo.getUserId(token1));
        Registered user = userRepo.findById(authRepo.getUserId(token1))
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));

        assertTrue(user.isAdmin());

    }

    @Test
    void testUser_Add_Admin_wrongkey() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User1888", "User1888", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User1888", "User1888");

        assertTrue(authRepo.getUserName(token1).equals("User1888"));
        userService.setAdmin(Token, "13321", 6);
        Registered user = userRepo.findById(authRepo.getUserId(token1))
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));

        assertFalse(user.isAdmin());

    }

    @Test
    void testUser_LogIn_Success() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User112", "User112", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User112", "User112");

        assertTrue(authRepo.getUserName(token1).equals("User112"));

    }

    @Test
    void testUser_LogOut_Success() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User166", "User166", 25);

        // --- Login ---
        String token1 = userService.login(Token, "User166", "User166");

        assertTrue(authRepo.getUserName(token1).equals("User166"));

        userService.logoutUser(token1);
        Registered user = userRepo.findById(authRepo.getUserId(token1))
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));
        assertFalse(user.isOnline());
    }

    @Test
    void testUser_LogIn_Failure() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User11", "User11", 25);

        // --- Login ---
        UIException ex = assertThrows(UIException.class, () -> {
            userService.login(Token, "User11", "invalid");
        });
        assertEquals("wrong password!!", ex.getMessage());
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
        userService.register(Token, "User18", "User18", 25);

        // Expect the logout to fail due to invalid token
        UIException ex = assertThrows(UIException.class, () -> userService.login("invalidToken", "user18", "User18"));
        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testUserLogin_Failure_UserNotFound() throws Exception {
        String Token = userService.generateGuest();
        userService.register(Token, "User100", "User100", 25);

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
        Registered user = userRepo.findById(authRepo.getUserId(token1))
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));
        assertTrue(user.isAdmin());

    }

    @Test
    void testUser_setAdmin_Failure_InvalidToken() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser3", "adminPass3", 22);
        String token1 = userService.login(token, "adminUser3", "adminPass3");
        userService.setAdmin(token1, "123321", authRepo.getUserId(token1));
        Registered user = userRepo.findById(authRepo.getUserId(token1))
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));
        assertTrue(user.isAdmin());
        UIException ex = assertThrows(UIException.class, () -> {
            userService.setAdmin("invalid token", "123321", 1);
        });
        assertFalse(userService.setAdmin(token, "121", 7));
    }

    @Test
    void testUser_setAdmin_Failure_LogoutUserThrows() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser33", "adminPass33", 22);

        assertFalse(userService.setAdmin(token, "adminUser33", 5));
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
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertTrue(stockService.getProductsInStore(x)[0].getQuantity() == 99);

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
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertEquals(99, stockService.getProductsInStore(x)[0].getQuantity());

    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_NoReceipts() throws Exception {

        // Act & Assert
        // UIException ex = assertThrows(UIException.class, () -> {
        List<ReceiptDTO> a = orderService.getReceiptDTOsByUser(NOToken);
        // });
        assertEquals(0, a.size());
        // assertEquals(ErrorCodes.RECEIPT_NOT_FOUND, ex.getNumber());
        // assertEquals("User has no receipts.", ex.getMessage());
    }

    @Test
    void testUserGetStoreProducts() throws Exception {
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        ItemStoreDTO[] items = stockService.getProductsInStore(x);
        assertTrue(items.length == 1);
        assertTrue(items[0].getProductId() == PID);

    }

    @Test
    void testUserViewEmptyStore() throws Exception {
        var id = storeService.addStoreToSystem(NOToken, "failure", "HOME");

        ItemStoreDTO[] products = stockService.getProductsInStore(id);
        // ask bhaa i dont know what is happening , how are we getting an item please
        // help help help

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
        ProductDTO info = stockService.getProductInfo(NGToken, PID);

        // ===== ASSERTIONS =====
        assertNotNull(info);
        System.out.println(info.getName());
        assertTrue(info.getName().equals("Laptop"));
        assertTrue(info.getProductId() == PID);
        assertTrue(info.getCategory().equals(Category.Electronics));
        assertTrue(info.getDescription().equals("Gaming Laptop"));
    }

    @Test
    void testUserGetProductInfo_ProductNotFound() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(NGToken, 2);
        });

        assertEquals("product not found!", exception.getMessage());
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

        // assertEquals("Invalid token!", exception.getMessage());
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
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertTrue(stockService.getProductsInStore(x)[0].getQuantity() == 99);
        assertTrue(userService.getRegularCart(NGToken).length == 0);
    }

    // @Test
    // void testUserBuyCart_ProductNotAvailable() throws Exception {
    // PaymentDetails paymentDetails = PaymentDetails.testPayment();
    // SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
    // userService.addToUserCart(NGToken, new ItemStoreDTO(0, 0, 0, null, 0, 2, "",
    // "TestStore"), 1);
    // }
    @Test
    void testUserBuyCart_EmptyCart() throws Exception {

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertEquals(100, stockService.getProductsInStore(x)[0].getQuantity());

    }

    @Test
    void testUserBuyCart_PaymentFails() throws Exception {
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.buyRegisteredCart(NGToken, PaymentDetails.test_fail_Payment(), supplyDetails));

        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertTrue(stockService.getProductsInStore(x)[0].getQuantity() == 100);

    }

    @Test
    void testUserSearchProductInStore_Success() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", // product name filter
                null, // category filter
                null, // keyword filter
                1, // store ID to filter
                0, 10000, // no price range
                0, 5 // no rating range
        );

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(2000, result[0].getPrice());
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
            stockService.searchProductsOnAllSystem("invalid token", criteria);
        });
        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testUserSearchProducts_NoMatches() throws Exception {

        String[] keywords = {"Laptop", "Lap", "top"};
        ProductSearchCriteria criteria = new ProductSearchCriteria("aa", Category.Electronics, keywords[0], 1, 0, 5000,
                0, 5);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
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
            stockService.searchProductsOnAllSystem("invalid token", criteria);
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
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product not sold in this store
    }

    @Test

    void testUserModifyCartAddQToBuy_sucess() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        int id = userService.getRegularCart(NGToken)[0].itemCartId;

        userService.ModifyCartAddQToBuy(NGToken, id, 3);
        ReceiptDTO[] re = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
        assertTrue(re[0].getFinalPrice() == 6000);
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertEquals(97, stockService.getProductsInStore(x)[0].getQuantity());

    }

    @Test
    void testUserModifyCartAddQToBuy_failure() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        int id = userService.getRegularCart(NGToken)[0].itemCartId;

        UIException ex = assertThrows(UIException.class, () -> userService.ModifyCartAddQToBuy("INVALID", id, 2));
        ReceiptDTO[] re = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        assertEquals(99, stockService.getProductsInStore(x)[0].getQuantity());

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
        int id = userService.getRegularCart(NGToken)[0].itemCartId;

        boolean result = userService.removeItemFromCart(NGToken, id);
        assertTrue(result);
        assertTrue(userService.getRegularCart(NGToken).length == 0);
    }

    @Test
    void test_user_cart_given_many_items_one_not_avaliable() throws Exception {
        // TODO abu el3asi make one with many items .
        // cart must not change + quantity for all stores must not change
        // Product 2 - Smartphone
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        String[] keywords2 = {"Phone", "Smartphone", "Mobile"};
        int PID2 = stockService.addProduct(NOToken, "Smartphone", Category.Electronics, "Latest smartphone model",
                keywords2);

        stockService.addItem(x, NOToken, PID2, 15, 1000, Category.Electronics);
        ItemStoreDTO itemStoreDTO2 = new ItemStoreDTO(PID2, 15, 1000, Category.Electronics, 0,
                x, "Smartphone", "TestStore");

        // Product 3 - Headphones
        String[] keywords3 = {"Headphones", "Audio", "Music"};
        int PID3 = stockService.addProduct(NOToken, "Headphones", Category.Electronics, "Wireless over-ear headphones",
                keywords3);

        stockService.addItem(x, NOToken, PID3, 20, 300, Category.Electronics);
        ItemStoreDTO itemStoreDTO3 = new ItemStoreDTO(PID3, 20, 300, Category.Electronics, 0,
                x, "Headphones", "TestStore");

        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        userService.addToUserCart(NGToken, itemStoreDTO3, 100);
        userService.addToUserCart(NGToken, itemStoreDTO2, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        ReceiptDTO[] re = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);

        var items = stockService.getProductsInStore(x);

        // You know the product IDs and expected quantities:
        Map<Integer, Integer> expectedQuantities = Map.of(
                PID, 9,
                PID2, 14,
                PID3, 20);

        // Verify each product quantity hasn't changed
        for (ItemStoreDTO item : items) {
            int expectedQty = expectedQuantities.getOrDefault(item.getProductId(), -1);
            assertNotEquals(-1, expectedQty, "Unexpected product in store: " + item.getProductId());
            assertEquals(expectedQty, item.getQuantity(), "Product ID " + item.getProductId() + " has wrong quantity");
        }

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

    // @Test
    // void testGetAllUsers_NotAdmin_ThrowsException() {
    // // Act + Assert
    // Exception ex = assertThrows(Exception.class, () -> {
    // userService.getAllUsers(NGToken); // regular user token
    // });
    // }
//    @Test
//    void testProcessPayment_NegativeAmount_ThrowsUIException() throws UIException {
//        int x = storeRepositoryjpa.findAll().get(0).getstoreId();
//
//        userService.addToUserCart(NGToken,
//                new ItemStoreDTO(PID, 1, -50, Category.Electronics, 3, x, "Test", "TestStore"), 1);
//        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
//        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
//        });
//        assertEquals("Payment failed", ex.getMessage());
//    }
    // admin
    // @Test
    // public void testViewPurchaseHistory_NoOrders_ReturnsEmpty() throws Exception
    // {
    // adminService.recordLoginEvent();
    // adminService.recordLogoutEvent();
    // adminService.recordRegisterEvent();
    // String token = userService.generateGuest();
    // userService.register(token, "admin2", "admin2", 22);
    // String adminToken = userService.login(token, "admin2", "admin2");
    // userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));
    // userService.addToUserCart(NGToken, itemStoreDTO, 1);
    // PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if
    // needed
    // SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if
    // needed
    // purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
    // List<PurchaseHistoryDTO> history =
    // adminService.viewPurchaseHistory(adminToken);
    // assertNotNull(history);
    // assertEquals(1, history.size());
    // }
    // @Test
    // public void testGetSystemAnalytics_EventsTrackedProperly() throws Exception {
    // // Simulate some system activity
    // adminService.recordLoginEvent();
    // adminService.recordLogoutEvent();
    // adminService.recordRegisterEvent();
    // String token = userService.generateGuest();
    // userService.register(token, "admin2", "admin2", 22);
    // String adminToken = userService.login(token, "admin2", "admin2");
    // userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));
    // userService.addToUserCart(NGToken, itemStoreDTO, 1);
    // PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if
    // needed
    // SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if
    // needed
    // purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
    // SystemAnalyticsDTO dto = adminService.getSystemAnalytics(adminToken);
    // assertNotNull(dto);
    // Map<LocalDate, Integer> logins = dto.getLoginsPerDay();
    // Map<LocalDate, Integer> logouts = dto.getLogoutsPerDay();
    // Map<LocalDate, Integer> registers = dto.getRegisterPerDay();
    // LocalDate today = LocalDate.now();
    // assertTrue(logins.getOrDefault(today, 0) >= 1);
    // assertTrue(logouts.getOrDefault(today, 0) >= 1);
    // assertTrue(registers.getOrDefault(today, 0) >= 1);
    // }
    // @Test
    // public void testViewPurchaseHistory_InvalidToken_ThrowsException() {
    // UIException ex = assertThrows(UIException.class, () -> {
    // adminService.viewPurchaseHistory("invalid-token");
    // });
    // assertEquals("Invalid Token!", ex.getMessage());
    // }
    // @Test
    // public void testGetSystemAnalytics_InvalidToken_ThrowsException() {
    // UIException ex = assertThrows(UIException.class, () -> {
    // adminService.getSystemAnalytics("invalid-token");
    // });
    // assertEquals("Invalid Token!", ex.getMessage());
    // }
    @Test
    void test_getUserCart_registeredUser_returnsCart() throws Exception {
        String guestToken = userService.generateGuest();
        userService.register(guestToken, "user1", "pass1", 25);
        String token = userService.login(guestToken, "user1", "pass1");
        int userId = authRepo.getUserId(token);

        var cart = userService.getRegularCart(NGToken);

        assertNotNull(cart);
        assertTrue(cart.length == 0); // assuming empty cart
    }

    // @Test
    // void test_sendRTMessageToUser_success() throws Exception {
    // notificationService.sendRTMessageToUser(authRepo.getUserName(NOToken), "Hello
    // there!");
    // }
    // @Test
    // void test_sendDMMessageToUser_success() throws Exception {
    // notificationService.sendDMessageToUser(authRepo.getUserName(NGToken), "You
    // missed this!");
    // }
    // @Test
    // void test_getDelayedMessages_returnsMessages() throws UIException {
    // notificationService.sendDMessageToUser(authRepo.getUserName(NOToken), "You
    // missed this!");
    // String[] messages = { "You missed this!" };
    // String[] result =
    // notificationService.getDelayedMessages(authRepo.getUserName(NOToken));
    // assertArrayEquals(messages, result);
    // }
    @Test
    void test_searchActiveRandoms_shouldReturnProduct() throws Exception {
        int productId = PID;
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        //   activePurcheses.setProductToRandom(NOToken, productId, 10, 2000, x, 2000);
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, x, 0, 5000, 0, 5);

        RandomDTO[] result = activePurcheses.searchActiveRandoms(NGToken, criteria);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Laptop", result[0].productName);
    }

    @Test
    void test_searchActiveBids_shouldReturnProduct() throws Exception {
        long endTime = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hour from

        // Add product to bid
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();

        // stockService.setProductToBid(NOToken, x, PID, 10);
        activePurcheses.setProductToRandom(NOToken, PID, 3, 1, x, endTime);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, createdStoreId, 0, 100000, 1, 5);

        // BidDTO[] result = stockService.searchActiveBids(NGToken, criteria);
        RandomDTO[] result = activePurcheses.searchActiveRandoms(NGToken, criteria);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Laptop", result[0].productName);
    }

    @Test
    void test_searchActiveAuctions_shouldReturnProduct() throws Exception {
        long endTime = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hour from
        // now
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();
        // Add product to auction
        activePurcheses.setProductToAuction(NOToken, x, PID, 10, endTime, 2000.0);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, createdStoreId, null, null, null, null);

        AuctionDTO[] result = activePurcheses.searchActiveAuctions(NGToken,
                criteria);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Laptop", result[0].productName);
    }

    @Test
    void testSearchByProductName_Match() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "laptop", null, null, null,
                -1.0, -1.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByProductName_NoMatch() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "phone", null, null, null,
                -1.0, -1.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByCategory_Match() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.Electronics, null, null,
                -1.0, -1.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByCategory_NoMatch() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, Category.Books, null, null,
                -1.0, -1.0, -1.0, -1.0);
        Exception ex = assertThrows(Exception.class, () -> stockService.searchProductsOnAllSystem(NGToken, criteria));
    }

    // @Test
    // void testSearchByKeyword_Match() throws Exception {
    // ProductSearchCriteria criteria = new ProductSearchCriteria(
    // "Laptop", null, "top", null,
    // -1.0, -1.0, -1.0, -1.0);
    // ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken,
    // criteria);
    // assertEquals(1, result.length);
    // }
    @Test
    void testSearchByKeyword_NoMatch() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, "banana", null,
                -1.0, -1.0, -1.0, 1);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByMinPrice_Pass() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "laptop", null, null, null,
                1500.0, -1.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByMinPrice_TooHigh() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                3000.0, -1.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByMaxPrice_Pass() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                -1.0, 3000.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByMaxPrice_TooLow() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                -1.0, 1000.0, -1.0, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByRatingRange_Pass() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                -1.0, -1.0, 0.0, 5.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByMinRating_TooHigh() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                -1.0, -1.0, 4.9, -1.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByMaxRating_TooLow() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, null,
                -1.0, -1.0, -1.0, 0.0);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchByStoreId_Match() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, 1,
                -1.0, -1.0, -1.0, -1);
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(NGToken, criteria);
        assertEquals(1, result.length);
    }

    @Test
    void testSearchByStoreId_NoMatch() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, 99,
                -1.0, -1.0, -1.0, -1);
        assertThrows(Exception.class, () -> stockService.searchProductsOnAllSystem(NGToken, criteria));
    }

}
