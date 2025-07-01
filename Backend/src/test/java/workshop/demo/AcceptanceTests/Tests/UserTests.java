package workshop.demo.AcceptanceTests.Tests;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.Order;
import workshop.demo.DomainLayer.Review.Review;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.PolicyManager;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.*;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

@SpringBootTest
@ActiveProfiles("test")
public class UserTests extends AcceptanceTests {
    private static final Logger logger = LoggerFactory.getLogger(UserTests.class);

    private String user1Token;
    private String user2Token;

    // Entities
    private Registered user1;
    private Registered user2;
    private Store store;
    private Product product;

    // Constants
    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String PASSWORD = "pass123";
    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final int USER1_ID = 1;
    private static final int USER2_ID = 2;

    private static final String STORE_NAME = "CoolStore";
    private static final String STORE_CATEGORY = "Electronics";

    private static final String PRODUCT_NAME = "Phone";
    private static final double PRODUCT_PRICE = 100.0;
    private static final String PRODUCT_DESC = "SMART PHONE";
    private static final String[] KEYWORD = {"Phone"};

    @BeforeEach
    void setup() throws Exception {
        mockGuestRepo.deleteAll();
        mockUserRepo.deleteAll();

        // ===== ENCODER =====
        Field encoderField = UserService.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        encoderField.set(userService, encoder);

        when(encoder.encodePassword(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(encoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // ===== USER1 guest =====
        Guest guest = new Guest();
        saveGuestRepo(guest);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token");
        when(mockAuthRepo.getUserId("guest-token")).thenReturn(0);
        userService.generateGuest();

        when(mockUserRepo.existsByUsername(USER1_USERNAME)).thenReturn(0);
        when(mockUserRepo.save(any(Registered.class))).thenAnswer(inv -> {
            Registered reg = inv.getArgument(0);
            if (USER1_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER1_ID);
            } else if (USER2_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER2_ID);
            } else {
                forceField(reg, "id", 999); // fallback
            }
            return reg;
        });

        userService.register("guest-token", USER1_USERNAME, PASSWORD, 20);

        user1 = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 20);
        saveUserRepo(user1);
        when(mockUserRepo.findRegisteredUsersByUsername(USER1_USERNAME)).thenReturn(List.of(user1));
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));

        when(mockAuthRepo.generateUserToken(USER1_ID, USER1_USERNAME)).thenReturn("user1Token");
        when(mockAuthRepo.getUserId("user1Token")).thenReturn(USER1_ID);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user1Token", logger);
        user1Token = userService.login("guest-token", USER1_USERNAME, PASSWORD);

        // ===== USER2  =====
        Guest guest1 = new Guest();
        saveGuestRepo(guest1);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token");
        when(mockAuthRepo.getUserId("guest-token")).thenReturn(0);
        userService.generateGuest();
        user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 22);
        saveUserRepo(user2);
        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockAuthRepo.generateUserToken(USER2_ID, USER2_USERNAME)).thenReturn("user2Token");
        when(mockAuthRepo.getUserId("user2Token")).thenReturn(USER2_ID);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user2Token", logger);
        user2Token = userService.login("guest-token", USER2_USERNAME, PASSWORD);

        // ===== STORE =====
        store = new Store(STORE_NAME, STORE_CATEGORY);
        forceStoreId(store, 0);
        when(mockStoreRepo.save(any(Store.class))).thenReturn(store);
        when(mockStoreRepo.findById(0)).thenReturn(Optional.of(store));
        when(suConnectionRepo.addNewStoreOwner(0, USER1_ID)).thenReturn(true);
        when(mockActivePurchases.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PolicyManager policyManager = new PolicyManager();
        policyManager.setStore(store);
        store.setPolicyManager(policyManager);
        when(policyManagerRepository.save(any())).thenReturn(policyManager);
        int storeId = storeService.addStoreToSystem(user1Token, STORE_NAME, STORE_CATEGORY);
        assertEquals(0, storeId);

        // ===== PRODUCT =====
        product = new Product(PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);
        forceField(product, "productId", 0);
        when(mockStockRepo1.save(any(Product.class))).thenReturn(product);
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));

        int productId = stockService.addProduct(user1Token, PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);
        assertEquals(0, productId);

        // ===== ITEM =====
        StoreStock stock = new StoreStock(storeId);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(0)).thenReturn(Optional.of(store));
        when(suConnectionRepo.manipulateItem(USER1_ID, 0, Permission.AddToStock)).thenReturn(true);
        when(mockStoreStock.findById(0)).thenReturn(Optional.of(stock));
        when(mockStoreStock.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int itemId = stockService.addItem(0, user1Token, 0, 10, 200, Category.Electronics);
        assertEquals(0, itemId);

        System.out.println();
        System.out.println();
    }


    @Test
    void testUser_LogIn_Success() throws Exception {
        // Act
        String token = userService.login("guest-token", USER1_USERNAME, PASSWORD);

        // Assert
        assertNotNull(token);
        assertEquals(user1Token, token);
    }


    @Test
    void testUser_LogOut_Success() throws Exception {
        // Arrange
        when(mockAuthRepo.getUserName(user1Token)).thenReturn(USER1_USERNAME);
        when(mockAuthRepo.generateGuestToken(USER1_ID)).thenReturn("guest-token");
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockUserRepo.findRegisteredUsersByUsername(USER1_USERNAME)).thenReturn(List.of(user1));
        when(mockUserRepo.save(any(Registered.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        String result = userService.logoutUser(user1Token);

        // Assert
        assertEquals("guest-token", result);
    }


    @Test
    void testUser_LogIn_Failure_Invalid_UserName() throws Exception {
        // Arrange
        String invalidUsername = "notfound";
        String token = "guest-token";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(mockUserRepo.findRegisteredUsersByUsername(invalidUsername)).thenReturn(List.of());

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.login(token, invalidUsername, "1234");
        });

        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
        assertEquals("User not found: " + invalidUsername, ex.getMessage());
    }

    //
    @Test
    void testUser_LogOut_Failure_InvalidToken() throws UIException {
        String token = "invalid-token";

        doThrow(new UIException("Invalid or expired token", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);

        UIException ex = assertThrows(UIException.class, () -> {
            userService.logoutUser(token);
        });

        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void testUser_LogOut_Failure_UserNotFound() throws Exception {
        String token = "user-token";
        String username = "ghost";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(mockAuthRepo.getUserName(token)).thenReturn(username);
        when(mockUserRepo.findRegisteredUsersByUsername(username)).thenReturn(List.of());

        UIException ex = assertThrows(UIException.class, () -> {
            userService.logoutUser(token);
        });

        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
        assertEquals("User not found: " + username, ex.getMessage());
    }

    @Test
    void testUser_LogOut_Failure_RepoSaveException() throws Exception {
        String token = "user-token";
        String username = "bashar";
        Registered user = new Registered(1, username, "encoded", 20);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(mockAuthRepo.getUserName(token)).thenReturn(username);
        when(mockUserRepo.findRegisteredUsersByUsername(username)).thenReturn(List.of(user));
        when(mockUserRepo.save(user)).thenThrow(new RuntimeException("Database save failed"));

        Exception ex = assertThrows(Exception.class, () -> {
            userService.logoutUser(token);
        });

        assertTrue(ex.getMessage().contains("Database save failed"));
    }


    @Test
    void testUserLogin_Failure_InvalidToken() throws Exception {
        String badToken = "bad-token";

        // Arrange: make the checkAuth_ThrowTimeOutException throw INVALID_TOKEN
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(badToken), any(Logger.class));

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.login(badToken, "bashar", "pass123");
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid"));
    }


    @Test
    void testUserLogin_Failure_UserNotFound() throws Exception {
        String token = "guest-token"; // מותר להשתמש בטוקן מה־setup
        String unknownUsername = "notExist";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);

        // Simulate DB returns no user
        when(mockUserRepo.findRegisteredUsersByUsername(unknownUsername))
                .thenReturn(List.of());

        UIException ex = assertThrows(UIException.class, () -> {
            userService.login(token, unknownUsername, "pass123");
        });

        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().contains("User not found"));
    }


    //
    //
    @Test
    void testUser_setAdmin_Success() throws Exception {
        int userId = USER1_ID;
        String adminKey = "123321";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockUserRepo.findById(userId)).thenReturn(Optional.of(user1));
        when(adminInitilizer.matchPassword(adminKey)).thenReturn(true);

        boolean result = userService.setAdmin(user1Token, adminKey, userId);

        assertTrue(result);
        assertTrue(user1.isAdmin());
    }


    @Test
    void testUser_setAdmin_Failure_InvalidToken() throws Exception {
        String adminKey = "123321";

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq("bad-token"), any(Logger.class));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.setAdmin("bad-token", adminKey, USER1_ID);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }


    @Test
    void testUser_setAdmin_Failure_UserNotFound() throws Exception {
        int userId = 999;
        String adminKey = "123321";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockUserRepo.findById(userId)).thenReturn(Optional.empty());

        boolean result = userService.setAdmin(user1Token, adminKey, userId);

        assertFalse(result);
    }


    @Test
    void testGetReceiptDTOsByUser_Success() throws Exception {
        // Arrange
        when(mockAuthRepo.validToken(user1Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));

        ReceiptProduct rp = new ReceiptProduct("Phone", "CoolStore", 2, 100, 0, Category.Electronics, 0);
        List<ReceiptProduct> products = List.of(rp);
        ReceiptDTO receiptDTO = new ReceiptDTO("CoolStore", null, products, 200);

        Order order = new Order(USER1_ID, receiptDTO, "CoolStore");
        when(mockOrderRepo.findOrdersByUserId(USER1_ID)).thenReturn(List.of(order));

        // Act
        List<ReceiptDTO> receipts = orderService.getReceiptDTOsByUser(user1Token);

        // Assert
        assertNotNull(receipts);
        assertEquals(1, receipts.size());
        ReceiptDTO r = receipts.getFirst();
        assertEquals("CoolStore", r.getStoreName());
        assertEquals(200, r.getFinalPrice());
        assertEquals(1, r.getProductsList().size());
        assertEquals("Phone", r.getProductsList().getFirst().getProductName());
        assertEquals(2, r.getProductsList().getFirst().getQuantity());
        assertEquals(100, r.getProductsList().getFirst().getPrice());
        assertEquals(0, r.getProductsList().getFirst().getStoreId());
    }


    @Test
    void testUser_CheckPurchaseHistory_Failure_InvalidToken() {
        // Arrange
        when(mockAuthRepo.validToken(user1Token)).thenReturn(false);

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser(user1Token);
        });

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }


    @Test
    void testUser_CheckPurchaseHistory_Failure_GetUserNameThrows() throws UIException {
        // Arrange
        when(mockAuthRepo.validToken(user1Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getReceiptDTOsByUser(user1Token);
        });

        assertTrue(ex.getMessage().contains("not registered to the system"));
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
    }


    @Test
    void testUserGetStoreProducts() throws Exception {

    }

    @Test
    void testUserViewEmptyStore() throws Exception {


    }


    @Test
    void testUserViewInvalidStore() throws Exception {

    }

    @Test
    void testUserGetProductInfo() throws Exception {
        int productId = 42;

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);

        Product product = new Product("Phone", Category.Electronics, "Smartphone", new String[]{"mobile"});
        forceField(product, "productId", productId);

        when(mockStockRepo1.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductDTO dto = stockService.getProductInfo(user1Token, productId);

        // Assert
        assertNotNull(dto);
        assertEquals("Phone", dto.getName());
        assertEquals(Category.Electronics, dto.getCategory());
        assertEquals("Smartphone", dto.getDescription());
    }


    @Test
    void testUserGetProductInfo_ProductNotFound() throws Exception {
        int productId = 42;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findById(productId)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(user1Token, productId);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("product"));
    }

    @Test
    void testUserGetProductInfo_Failure_DTO_Null() throws Exception {
        int productId = 42;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);

        Product product = new Product("Phone", Category.Electronics, "Smartphone", new String[]{"mobile"});
        forceField(product, "productId", productId);

        Product spyProduct = Mockito.spy(product);
        when(spyProduct.getDTO()).thenReturn(null);

        when(mockStockRepo1.findById(productId)).thenReturn(Optional.of(spyProduct));

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(user1Token, productId);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("product"));
    }


    @Test
    void testUserAddProductToCart_Success() throws Exception {
        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        // mock  Registered
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.save(any(Registered.class))).thenAnswer(inv -> inv.getArgument(0));

        // item
        ItemStoreDTO itemToAdd = new ItemStoreDTO(0, 300, 50, Category.Electronics, 3, 0, "Phone", "CoolStore");
        int quantity = 2;

        // Act
        boolean result = userService.addToUserCart(user1Token, itemToAdd, quantity);

        // Assert
        assertTrue(result);
        assertEquals(1, user1.getCart().size());

        CartItem cartItem = user1.getCart().getFirst();
        assertEquals("Phone", cartItem.name);
        assertEquals(50, cartItem.price);
        assertEquals(0, cartItem.storeId);
    }


    @Test
    void testUserAddProductToCart_InvalidToken() throws Exception {
        doThrow(new UIException("Token expired", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(user1Token), any());

        ItemStoreDTO itemToAdd = new ItemStoreDTO(
                0, 2, 50, Category.Electronics, 3, 0, "Phone", "CoolStore");

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user1Token, itemToAdd, 1);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Token expired"));
    }


    @Test
    void testUserAddProductToCart_UserNotFound() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(999);

        when(mockUserRepo.findById(999)).thenReturn(Optional.empty());
        when(mockGuestRepo.findById(999)).thenReturn(Optional.empty());

        ItemStoreDTO itemToAdd = new ItemStoreDTO(0, 2, 50, Category.Electronics, 3, 0, "Phone", "CoolStore");

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user1Token, itemToAdd, 1);
        });

        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("id is not registered or guest"));
    }


    @Test
    void testSearchProducts_Success_ByName() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);
        item.rankItem(4);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(1, result.length);
        assertEquals("Phone", result[0].getProductName());
        assertEquals(100, result[0].getPrice());
        assertEquals(4, result[0].getRank());
    }

    @Test
    void testSearchProducts_Success_PriceFilter() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 50, 150, 0, 5); // min=50, max=150

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(1, result.length);
        assertTrue(result[0].getPrice() >= 50 && result[0].getPrice() <= 150);
    }

    @Test
    void testSearchProducts_Success_ByNameAndRank() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 4, 5); // min rank=4, max rank=5

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);
        item.rankItem(5); // rank is 5

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(1, result.length);
        assertTrue(result[0].getRank() >= 4);
    }

    @Test
    void testSearchProducts_Success_ByCategory() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", Category.Electronics, null, 1, 0, 5000, 0, 5);

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(1, result.length);
        assertEquals(Category.Electronics, result[0].getCategory());
    }

    @Test
    void testSearchProducts_Success_MultipleItemsSameProduct() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item1 = new item(0, 10, 100, Category.Electronics);
        item item2 = new item(0, 15, 150, Category.Electronics);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item1, item2));
        when(mockStoreRepo.findById(anyInt())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(2, result.length);
        assertTrue(result[0].getPrice() == 100 || result[0].getPrice() == 150);
    }

    @Test
    void testSearchProducts_Success_StrictPrice() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 90, 110, 0, 5); // only 100 allowed

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.of(store));

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(1, result.length);
        assertTrue(result[0].getPrice() >= 90 && result[0].getPrice() <= 110);
    }

    @Test
    void testSearchProducts_Failure_NoMatchingProducts() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "NotExist", null, null, 1, 0, 5000, 0, 5);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("NotExist")).thenReturn(List.of());

        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(user1Token, criteria);

        assertEquals(0, result.length);
    }

    @Test
    void testSearchProducts_Failure_StoreNotFound() throws Exception {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        Product product = new Product("Phone", Category.Electronics, "Desc", new String[]{"Phone"});
        forceField(product, "productId", 0);

        item item = new item(0, 10, 100, Category.Electronics);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockStockRepo1.findByNameContainingIgnoreCase("Phone")).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(0)).thenReturn(List.of(item));
        when(mockStoreRepo.findById(item.getStoreId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            stockService.searchProductsOnAllSystem(user1Token, criteria);
        });
    }


    @Test
    void testRankProduct_Success() throws Exception {
        int storeId = 0;
        int productId = 0;
        int newRank = 5;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        StoreStock stock = Mockito.mock(StoreStock.class);
        when(mockStoreStock.findById(storeId)).thenReturn(Optional.of(stock));
        when(mockStoreStock.saveAndFlush(stock)).thenReturn(stock);

        int result = stockService.rankProduct(storeId, user1Token, productId, newRank);

        assertEquals(productId, result);
        verify(stock).rankProduct(productId, newRank);
        verify(mockStoreStock).saveAndFlush(stock);
    }

    @Test
    void testRankProduct_Failure_SuspendedUser() throws Exception {
        int storeId = 0;
        int productId = 0;
        int newRank = 3;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        UserSuspension suspension = new UserSuspension(USER1_ID, 100);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(suspension));

        UIException ex = assertThrows(UIException.class, () ->
                stockService.rankProduct(storeId, user1Token, productId, newRank));

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
    }

    @Test
    void testRankProduct_Failure_StoreStockNotFound() throws Exception {
        int storeId = 999;
        int productId = 1;
        int newRank = 4;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        when(mockStoreStock.findById(storeId)).thenReturn(Optional.empty());

        DevException ex = assertThrows(DevException.class, () ->
                stockService.rankProduct(storeId, user1Token, productId, newRank));

        assertTrue(ex.getMessage().toLowerCase().contains("store stock not found"));
    }

    @Test
    void testRankProduct_Failure_InvalidToken() throws Exception {
        int storeId = 0;
        int productId = 0;
        int newRank = 2;

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(eq("bad-token"), any());

        UIException ex = assertThrows(UIException.class, () ->
                stockService.rankProduct(storeId, "bad-token", productId, newRank));

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }

    @Test
    void testUserRankStore_Success() throws Exception {
        int storeId = 0;
        int newRank = 5;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        Store spyStore = Mockito.spy(store);
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(spyStore));

        // Act
        storeService.rankStore(user1Token, storeId, newRank);

        // Assert
        verify(spyStore).rankStore(newRank);
    }

    @Test
    void testUserRankStore_Failure_Suspended() throws Exception {
        int storeId = 0;
        int newRank = 4;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        UserSuspension suspension = new UserSuspension(USER1_ID, 100);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(suspension));

        UIException ex = assertThrows(UIException.class, () ->
                storeService.rankStore(user1Token, storeId, newRank)
        );

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
    }

    @Test
    void testUserRankStore_Failure_StoreNotFound() throws Exception {
        int storeId = 99;
        int newRank = 4;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () ->
                storeService.rankStore(user1Token, storeId, newRank)
        );
        logger.info("STORE NOT FOUND");
        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testUserAddReviewToProduct_Success() throws Exception {
        int storeId = store.getstoreId(); // מה-setup
        int productId = product.getProductId();
        String reviewText = "Excellent product!";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));
        when(mockStoreStock.findItemsByStoreId(storeId))
                .thenReturn(List.of(new item(productId, 10, 100, Category.Electronics)));

        when(mockAuthRepo.getUserName(user1Token)).thenReturn(USER1_USERNAME);

        boolean result = reviewService.AddReviewToProduct(user1Token, storeId, productId, reviewText);

        assertTrue(result);
        verify(mockReviewRepo).save(any(Review.class));
    }

    @Test
    void testUserAddReviewToProduct_Failure_Suspended() throws Exception {
        int storeId = store.getstoreId();
        int productId = product.getProductId();
        String reviewText = "Test Review";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(new UserSuspension(USER1_ID, 100)));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        UIException ex = assertThrows(UIException.class, () -> {
            reviewService.AddReviewToProduct(user1Token, storeId, productId, reviewText);
        });

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
    }

    @Test
    void testUserAddReviewToProduct_Failure_EmptyStore() throws Exception {
        int storeId = store.getstoreId();
        int productId = product.getProductId();
        String reviewText = "Nice one";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));
        when(mockStoreStock.findItemsByStoreId(storeId)).thenReturn(List.of()); // אין פריטים

        UIException ex = assertThrows(UIException.class, () -> {
            reviewService.AddReviewToProduct(user1Token, storeId, productId, reviewText);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("no products"));
    }

    @Test
    void testUserAddReviewToProduct_Failure_ProductNotFoundInStore() throws Exception {
        int storeId = store.getstoreId();
        int productId = product.getProductId();
        String reviewText = "Good";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // יש פריטים אבל עם productId אחר
        when(mockStoreStock.findItemsByStoreId(storeId))
                .thenReturn(List.of(new item(999, 5, 50, Category.Electronics)));

        UIException ex = assertThrows(UIException.class, () -> {
            reviewService.AddReviewToProduct(user1Token, storeId, productId, reviewText);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().contains("Product with ID"));
    }

    @Test
    void testUserAddReviewToStore_Success() throws Exception {
        int storeId = store.getstoreId();
        String reviewText = "Great store, will buy again!";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockAuthRepo.getUserName(user1Token)).thenReturn(USER1_USERNAME);
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        boolean result = reviewService.AddReviewToStore(user1Token, storeId, reviewText);

        assertTrue(result);
        verify(mockReviewRepo).save(any(Review.class));
    }


    @Test
    void testUserAddReviewToStore_Failure_StoreNotFound() throws Exception {
        int storeId = 999;
        String reviewText = "No such store";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> {
            reviewService.AddReviewToStore(user1Token, storeId, reviewText);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testGetReviewsForStore_Success() throws Exception {
        int storeId = store.getstoreId();

        // Arrange
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        Review r1 = new Review(USER1_ID, USER1_USERNAME, "Amazing store!", storeId, -1);
        Review r2 = new Review(USER2_ID, USER2_USERNAME, "Good selection.", storeId, -1);

        when(mockReviewRepo.findByStoreId(storeId)).thenReturn(List.of(r1, r2));

        // Act
        List<ReviewDTO> reviews = reviewService.getReviewsForStore(storeId);

        // Assert
        assertNotNull(reviews);
        assertEquals(2, reviews.size());
        assertEquals("Amazing store!", reviews.get(0).getReviewMsg());
        assertEquals("Good selection.", reviews.get(1).getReviewMsg());
    }

    @Test
    void testGetReviewsForStore_Failure_StoreNotFound() throws Exception {
        int storeId = 999;

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> {
            reviewService.getReviewsForStore(storeId);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testGetReviewsForProduct_Success() throws Exception {
        int storeId = store.getstoreId();
        int productId = product.getProductId();

        // Arrange
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        Review r1 = new Review(USER1_ID, USER1_USERNAME, "Awesome product!", storeId, productId);
        Review r2 = new Review(USER2_ID, USER2_USERNAME, "Worth the price.", storeId, productId);

        when(mockReviewRepo.findByStoreIdAndProductId(storeId, productId)).thenReturn(List.of(r1, r2));

        // Act
        List<ReviewDTO> reviews = reviewService.getReviewsForProduct(storeId, productId);

        // Assert
        assertNotNull(reviews);
        assertEquals(2, reviews.size());
        assertEquals("Awesome product!", reviews.get(0).getReviewMsg());
        assertEquals("Worth the price.", reviews.get(1).getReviewMsg());
    }

    @Test
    void testGetReviewsForProduct_Failure_StoreNotFound() throws Exception {
        int storeId = 999;
        int productId = 0;

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> {
            reviewService.getReviewsForProduct(storeId, productId);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testUserBuyCart_Success() throws Exception {
        when(mockAuthRepo.validToken(user2Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());

        //getUser

        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        user2.addToCart(new CartItem(new ItemCartDTO(store.getstoreId(), product.getProductId(), 1, 200, product.getName(), store.getStoreName(), Category.Electronics)));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        StoreStock stock = new StoreStock(store.getstoreId());
        stock.addItem(new item(product.getProductId(), 1, 200, Category.Electronics));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(stock));
        when(mockUserRepo.getReferenceById(user2.getId())).thenReturn(user2);
        // when(mockStoreStock.flush()).thenReturn()
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        ReceiptDTO[] re = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        System.out.println(re.length);
        assertNotNull(re, "Receipts should not be null");
        assertTrue(re.length > 0, "Should have at least one receipt");

        ReceiptDTO receipt = re[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertTrue(receipt.getFinalPrice() > 0);
        assertFalse(receipt.getProductsList().isEmpty(), "Receipt should have products");

        ReceiptProduct rp = receipt.getProductsList().get(0);
        assertEquals(product.getProductId(), rp.getProductId());
        assertEquals(product.getName(), rp.getProductName());
        assertEquals(200, rp.getPrice());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(mockOrderRepo, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertNotNull(savedOrder, "Order should be saved to repository");
        assertEquals(user2.getId(), savedOrder.getUserId(), "User ID should match");
        assertEquals(store.getStoreName(), savedOrder.getStoreName(), "Store name should match");
        assertTrue(savedOrder.getProductsList() != null && !savedOrder.getProductsList().isEmpty(), "Order should contain products");

        ReceiptProduct savedProduct = savedOrder.getProductsList().get(0);
        assertEquals(product.getProductId(), savedProduct.getProductId(), "Product ID should match in order");
        assertEquals(product.getName(), savedProduct.getProductName(), "Product name should match in order");
        assertEquals(200, savedProduct.getPrice(), "Price should match in order");
    }
    @Test
    void testBuyRegisteredCart_Failure_InvalidToken() {
        when(mockAuthRepo.validToken(user2Token)).thenReturn(false);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        });

    }

    @Test
    void testBuyRegisteredCart_Failure_SuspendedUser() throws UIException {
        when(mockAuthRepo.validToken(user2Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId()))
                .thenReturn(Optional.of(new UserSuspension(user2.getId(), System.currentTimeMillis() + 9999)));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        });

    }

    @Test
    void testBuyRegisteredCart_Failure_EmptyCart() throws UIException {
        when(mockAuthRepo.validToken(user2Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        });

    }

    @Test
    void testBuyRegisteredCart_Failure_StoreNotFound() throws UIException {
        // Setup as user with item in cart
        when(mockAuthRepo.validToken(user2Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        user2.addToCart(new CartItem(new ItemCartDTO(999, product.getProductId(), 1, 200, "Phone", "NotExistStore", Category.Electronics)));

        when(mockStoreRepo.findById(999)).thenReturn(Optional.empty());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        });

    }
    @Test
    void testUserBuyCart_Failure_PaymentFails() throws Exception {
        setupValidGuestCartScenario(5); // helper that sets everything up (you can ask me to generate)

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(user2Token, PaymentDetails.test_fail_Payment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.PAYMENT_ERROR, ex.getNumber());
        assertTrue(ex.getMessage().contains("Payment failed"));
    }
    @Test
    void testUserBuyCart_Failure_SupplyFails() throws Exception {
        setupValidGuestCartScenario(5);

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(user2Token, PaymentDetails.testPayment(), SupplyDetails.test_fail_supply());
        });

        assertEquals(ErrorCodes.SUPPLY_ERROR, ex.getNumber());
    }



    private void setupValidGuestCartScenario(int cartQuantity) throws Exception {
        when(mockAuthRepo.validToken(user2Token)).thenReturn(true);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.empty());

        // Guest with cart
        Guest guest = new Guest();
        forceField(guest, "id", 0);
        guest.addToCart(new CartItem(new ItemCartDTO(
                store.getstoreId(),
                product.getProductId(),
                cartQuantity,
                100,
                product.getName(),
                store.getStoreName(),
                Category.Electronics
        )));
        when(mockGuestRepo.findById(0)).thenReturn(Optional.of(guest));
        when(mockGuestRepo.getReferenceById(0)).thenReturn(guest);

        // Store exists
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        // StoreStock with enough quantity
        StoreStock stock = new StoreStock(store.getstoreId());
        stock.addItem(new item(product.getProductId(), cartQuantity + 5, 100, Category.Electronics));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(stock));
        when(mockStoreStock.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        // Policies and discount
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        store.setActive(true);

    }
}
