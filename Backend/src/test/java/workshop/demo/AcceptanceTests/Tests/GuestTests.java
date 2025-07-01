package workshop.demo.AcceptanceTests.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.External.PaymentServiceImp;
import workshop.demo.External.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.PolicyManager;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.*;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class GuestTests extends AcceptanceTests {
    private static final Logger logger = LoggerFactory.getLogger(GuestTests.class);

    private String user1Token;
    private String guestToken;

    // Entities
    private Registered owner;
    //    private Registered user2;
    private Guest guest1;
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
   var paymentServiceImp = Mockito.mock(PaymentServiceImp.class);
     var   supplyServiceImp = Mockito.mock(SupplyServiceImp.class);

    when(paymentServiceImp.processPayment(any(PaymentDetails.class), anyDouble()))
    .thenReturn(42);
    when(supplyServiceImp.processSupply(any(SupplyDetails.class)))
    .thenReturn(55555);
    purchaseService.setPaymentService(paymentServiceImp);
        purchaseService.setSupplyService(supplyServiceImp);
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

        owner = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 20);
        saveUserRepo(owner);
        when(mockUserRepo.findRegisteredUsersByUsername(USER1_USERNAME)).thenReturn(List.of(owner));
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(owner));

        when(mockAuthRepo.generateUserToken(USER1_ID, USER1_USERNAME)).thenReturn("user1Token");
        when(mockAuthRepo.getUserId("user1Token")).thenReturn(USER1_ID);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user1Token", logger);
        user1Token = userService.login("guest-token", USER1_USERNAME, PASSWORD);

        // ===== USER2  =====
        guest1 = new Guest();
        saveGuestRepo(guest1);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token");
        when(mockAuthRepo.getUserId("guest-token")).thenReturn(0);
        guestToken=userService.generateGuest();
//        user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 22);
//        saveUserRepo(user2);
//        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
//        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
//
//        when(mockAuthRepo.generateUserToken(USER2_ID, USER2_USERNAME)).thenReturn("user2Token");
//        when(mockAuthRepo.getUserId("user2Token")).thenReturn(USER2_ID);
//        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user2Token", logger);
//        g = userService.login("guest-token", USER2_USERNAME, PASSWORD);

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
    void testGuestEnter_Success() {
        // Arrange: create a guest and mock the save to return it
        Guest savedGuest = new Guest(); // Will have default id = 0
        saveGuestRepo(savedGuest);

        // Expect this token from the authRepo when guest ID = 0
        String expectedToken = "guest-token";
        when(mockAuthRepo.generateGuestToken(0)).thenReturn(expectedToken);

        // Act + Assert
        try {
            String token = userService.generateGuest();
            logger.info("guest generated successfully");

            assertNotNull(token);
            assertEquals(expectedToken, token);
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }


    @Test
    void testGuestEnter_Failure_InvalidToken() throws Exception {
        // Arrange
        Guest savedGuest = new Guest(); // Will have default id = 0

        saveGuestRepo(savedGuest); // this mocks guestJpaRepository.save(...) and sets ID
        when(mockAuthRepo.generateGuestToken(anyInt()))
                .thenThrow(new RuntimeException("Token generation failed"));

        // Act + Assert
        Exception ex = assertThrows(Exception.class, () -> {
            userService.generateGuest();
        });

        assertTrue(ex.getMessage().contains("Token generation failed"));
    }

    @Test
    void testGuestEnter_Failure_DataBaseError() {
        mockSaveGuestFailure();
        Exception ex = assertThrows(Exception.class, () -> {
            userService.generateGuest();
        });
        assertTrue(ex.getMessage().contains("DB error"));
    }

    @Test
    void testGuestExit_Success() throws Exception {
        // Arrange: create a guest and mock the save to return it
        Guest savedGuest = new Guest(); // Will have default id = 0
        saveGuestRepo(savedGuest);
        // Let the token be based on ID 0 (since that's what guest.getId() will return)
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token-0");

        // Act
        String token = userService.generateGuest();
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(mockAuthRepo.getUserId(token)).thenReturn(0);
        doNothing().when(mockGuestRepo).deleteById(0);
        assertTrue(userService.destroyGuest(token));

    }

    @Test
    void testGuestExit_Failure_DbError() throws Exception {
        // Arrange
        Guest savedGuest = new Guest(); // Will have default id = 0
        saveGuestRepo(savedGuest);
        // Let the token be based on ID 0 (since that's what guest.getId() will return)
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token-0");

        // Act
        String token = userService.generateGuest();
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(mockAuthRepo.getUserId(token)).thenReturn(0);
        doNothing().when(mockGuestRepo).deleteById(0);

        doThrow(new IllegalArgumentException("DB error"))
                .when(mockGuestRepo).deleteById(0);
        Exception ex = assertThrows(Exception.class, () -> {
            userService.destroyGuest(token);
        });
        assertTrue(ex.getMessage().contains("DB error"));
    }

    @Test
    void testGuestExit_Failure_InvalidToken() throws UIException {
        // Arrange
        String invalidToken = "invalid-token";

        doThrow(new UIException("InvalidToken", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(eq(invalidToken), any());

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.destroyGuest(invalidToken);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("InvalidToken"));
    }

    @Test
    void testGuestRegister_Success() throws Exception {
        // Use guest from setup (ID=0, token="guest-token")
        // and Registered user r from setup

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockUserRepo.existsByUsername(owner.getUsername())).thenReturn(0); // user does not exist
        when(encoder.encodePassword(owner.getEncodedPass())).thenReturn(owner.getEncodedPass()); // simulate password encoding
        saveUserRepo(owner);

        // Act + Assert
        assertTrue(userService.register(guestToken, owner.getUsername(), owner.getEncodedPass(), owner.getUserDTO().age));
    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {
        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockUserRepo.existsByUsername(owner.getUsername())).thenReturn(1); // Simulate that username is already taken

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.register(guestToken, owner.getUsername(), "password", 18);
        });

        assertEquals(ErrorCodes.USERNAME_USED, ex.getErrorCode());
//        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    //
    @Test
    void testGuestViewEmptyStore() throws Exception {
        int storeId = 100; // use storeId from setup
        String guestToken = "guest-token"; // use guestToken from setup

        when(mockAuthRepo.validToken(guestToken)).thenReturn(true); // ensure guest token is valid
        Exception exception = assertThrows(Exception.class, () -> {
            stockService.getProductsInStore(storeId);
        });

    }


    @Test
    void testGuestAddProductToCart_Success() throws Exception {
        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);

        // Create a Guest with empty cart and force ID = 0
        Guest guest = new Guest();
        Field idField = Guest.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(guest, 0);

        // Mock JPA repository to return guest
        when(mockGuestRepo.findById(0)).thenReturn(Optional.of(guest));
        when(mockGuestRepo.save(any(Guest.class))).thenAnswer(inv -> inv.getArgument(0));

        // Prepare item to add
        ItemStoreDTO itemToAdd = new ItemStoreDTO(0, 300, 50, Category.Electronics, 3, 0, "Phone", "CoolStore");
        int quantity = 2;

        // Act
        boolean result = userService.addToUserCart(guestToken, itemToAdd, quantity);

        // Assert
        assertTrue(result);
        assertEquals(1, guest.getCart().size());

        CartItem cartItem = guest.getCart().get(0);
        assertEquals("Phone", cartItem.name);
        assertEquals(50, cartItem.price);
        assertEquals(0, cartItem.storeId);


    }

    @Test
    void testGuestAddProductToCart_InvalidToken() throws Exception {
        // Arrange
        // Use `anyString()` or `eq(...)` to match the token exactly
        doThrow(new UIException("Token expired", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any());

        // Create the item
        ItemStoreDTO itemToAdd = new ItemStoreDTO(
                0,                // storeId
                2,                // productId
                50,               // price
                Category.Electronics,
                3,                // product quantity
                0,                // item ID
                "Phone",          // productName
                "CoolStore"       // storeName
        );

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(guestToken, itemToAdd, 1);
        });

        // Verify error details
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Token expired"));
    }


    @Test
    void testGuestAddProductToCart_GuestNotFound() throws Exception {
        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(999); // user ID that doesn't exist

        when(mockUserRepo.findById(999)).thenReturn(Optional.empty()); // not registered
        when(mockGuestRepo.findById(999)).thenReturn(Optional.empty()); // not guest

        ItemStoreDTO itemToAdd = new ItemStoreDTO(0, 2, 50, Category.Electronics, 3, 0, "Phone", "CoolStore");

        // Act & Assert
        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(guestToken, itemToAdd, 1);
        });

        // Verify
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("id is not registered or guest"));

    }


    @Test
    void testGuestAddProductToCart_ZeroQuantity() throws Exception {

    }

    @Test
    void testGuestRemoveItem_Success() throws Exception {
        int userId = 0;
        int itemId = 7;

        CartItem item = new CartItem(new ItemCartDTO(0, 2, 1, 50, "Phone", "CoolStore", Category.Electronics));
        item.setId(itemId);

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(0, item);

        Guest guest = new Guest();
        guest.setCart(cart);

        when(mockAuthRepo.getUserId(guestToken)).thenReturn(userId);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockUserRepo.findById(userId)).thenReturn(Optional.empty());
        when(mockGuestRepo.findById(userId)).thenReturn(Optional.of(guest));

        boolean result = userService.removeItemFromCart(guestToken, itemId);

        assertTrue(result);
        assertTrue(guest.getCart().isEmpty());
        //verify(mockGuestRepo).saveAndFlush(guest);
    }

    @Test
    void testGuestModifyCartAddQToBuy_Success1() throws Exception {
        // Arrange
        int itemCartId = 5;
        int originalQty = 1;
        int quantityToAdd = 3;

        // Create the item with a known ID
        CartItem item = new CartItem(new ItemCartDTO(0, 2, originalQty, 50, PRODUCT_NAME, STORE_NAME, Category.Electronics));
        item.setId(itemCartId);

        guest1.addToCart(item); // Add it to the shared guest's cart

        // Ensure mockGuestRepo returns the shared guest
        when(mockGuestRepo.findById(guest1.getId())).thenReturn(Optional.of(guest1));

        // Act
        boolean result = userService.ModifyCartAddQToBuy(guestToken, itemCartId, quantityToAdd);

        // Assert
        assertTrue(result);

        CartItem updatedItem = guest1.getCart().stream()
                .filter(i -> i.getId() == itemCartId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        assertEquals(quantityToAdd, updatedItem.getQuantity());
    }


    @Test
    void testGuestModifyCartAddQToBuy_InvalidToken() throws Exception {
        String guestToken = "invalid-token";
        int productId = 100;

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));

        UIException ex = assertThrows(UIException.class, ()
                -> userService.ModifyCartAddQToBuy(guestToken, productId, 2)
        );

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testGuestModifyCartAddQToBuy_GuestNotFound() throws Exception {
        // Arrange
        int guestId = 999;
        String guestToken = "guest-token-999";
        int productId = 100;
        int newQuantity = 3;

        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        // Simulate both registered and guest user not found
        when(mockUserRepo.findById(guestId)).thenReturn(Optional.empty());
        when(mockGuestRepo.findById(guestId)).thenReturn(Optional.empty());

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () ->
                userService.ModifyCartAddQToBuy(guestToken, productId, newQuantity)
        );

        assertEquals("id is not registered or guest", ex.getMessage());
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
    }


    @Test
    void testGuestSearchProductInStore_Success() throws Exception {
        // Arrange
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        int storeId = store.getstoreId();
        int productId = product.getProductId();

        // Mock item in stock
        item item = new item(productId, 50, 100, Category.Electronics);

        item.rankItem(5);
        List<item> itemList = List.of(item);

        // Setup mock returns
        when(mockStockRepo1.findByNameContainingIgnoreCase(PRODUCT_NAME)).thenReturn(List.of(product));
        when(mockStoreStock.findItemsByProductId(productId)).thenReturn(itemList);
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        // Act
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(guestToken, criteria);

        // Assert
        assertEquals(1, result.length);

        ItemStoreDTO dto = result[0];
        assertEquals(productId, dto.getProductId());
        assertEquals(PRODUCT_NAME, dto.getProductName());
        assertEquals(100, dto.getPrice());
        assertEquals(storeId, dto.getStoreId());
        assertEquals(STORE_NAME, dto.getStoreName());
        assertEquals(5, dto.getRank());
        assertEquals(Category.Electronics, dto.getCategory());
    }

    @Test
    void testSearchProducts_InvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid-token";
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(invalidToken), any(Logger.class));

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () ->
                stockService.searchProductsOnAllSystem(invalidToken, criteria)
        );

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    //
    @Test
    void testSearchProducts_NoMatches() throws Exception {
        // Arrange
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "NonExistingProduct", null, null, 1, 0, 5000, 0, 5);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        when(mockStockRepo1.findByNameContainingIgnoreCase("NonExistingProduct"))
                .thenReturn(List.of()); // No matching product

        // Act
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(guestToken, criteria);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    //
    @Test
    void testSearchProducts_ProductExists_NotInStore() throws Exception {
        // Arrange
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        // Return the product but an item that does NOT match criteria (e.g., price out of range)
        when(mockStockRepo1.findByNameContainingIgnoreCase(PRODUCT_NAME)).thenReturn(List.of(product));

        item unmatchedItem = new item(product.getProductId(), 50, 10000, Category.Electronics); // Price too high
        List<item> items = List.of(unmatchedItem);
        when(mockStoreStock.findItemsByProductId(product.getProductId())).thenReturn(items);

        // Act
        ItemStoreDTO[] result = stockService.searchProductsOnAllSystem(guestToken, criteria);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchProducts_StoreMissing() throws Exception {
        // Arrange
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Phone", null, null, 1, 0, 5000, 0, 5);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        when(mockStockRepo1.findByNameContainingIgnoreCase(PRODUCT_NAME)).thenReturn(List.of(product));

        item validItem = new item(product.getProductId(), 50, 100, Category.Electronics);
        List<item> items = List.of(validItem);
        when(mockStoreStock.findItemsByProductId(product.getProductId())).thenReturn(items);

        // Store doesn't exist
        when(mockStoreRepo.findById(validItem.getStoreId())).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NoSuchElementException.class, () ->
                stockService.searchProductsOnAllSystem(guestToken, criteria)
        );
    }

    @Test
    void testGuestGetProductInfo() throws Exception {
        int productId = 42;

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        Product product = new Product("Phone", Category.Electronics, "Smartphone", new String[]{"mobile"});
        forceField(product, "productId", productId);

        when(mockStockRepo1.findById(productId)).thenReturn(Optional.of(product));

        // Act
        ProductDTO dto = stockService.getProductInfo(guestToken, productId);

        // Assert
        assertNotNull(dto);
        assertEquals("Phone", dto.getName());
        assertEquals(Category.Electronics, dto.getCategory());
        assertEquals("Smartphone", dto.getDescription());
    }


    @Test
    void testGuestGetProductInfo_ProductNotFound() throws Exception {
        int productId = 42;

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockStockRepo1.findById(productId)).thenReturn(Optional.empty());

        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(guestToken, productId);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("product"));
    }

    @Test
    void testGuestGetProductInfo_Failure_DTO_Null() throws Exception {
        int productId = 42;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);

        Product product = new Product("Phone", Category.Electronics, "Smartphone", new String[]{"mobile"});
        forceField(product, "productId", productId);

        Product spyProduct = Mockito.spy(product);
        when(spyProduct.getDTO()).thenReturn(null);

        when(mockStockRepo1.findById(productId)).thenReturn(Optional.of(spyProduct));

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(guestToken, productId);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("product"));
    }

    @Test
    void testGuestBuyCart_Success() throws Exception {
        //
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.empty());

        //getUser
        when(mockGuestRepo.findById(guest1.getId())).thenReturn(Optional.of(guest1));

        guest1.addToCart(new CartItem(new ItemCartDTO(store.getstoreId(),product.getProductId(),1,200,product.getName(),store.getStoreName(),Category.Electronics)));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        StoreStock stock=new StoreStock(store.getstoreId());
        stock.addItem(new item(product.getProductId(),1,200,Category.Electronics));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(stock));
        when(mockGuestRepo.getReferenceById(guest1.getId())).thenReturn(guest1);
        // when(mockStoreStock.flush()).thenReturn()
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        ReceiptDTO[] re=purchaseService.buyGuestCart(guestToken,paymentDetails,supplyDetails);

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
    }
    @Test
    void testGuestBuyCart_Failure_InvalidToken() {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(false);

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }
    @Test
    void testGuestBuyCart_Failure_Suspended() throws UIException {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.of(new UserSuspension(0, System.currentTimeMillis() + 100000)));

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().contains("Suspended"));
    }
    @Test
    void testGuestBuyCart_Failure_EmptyCart() throws Exception {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.empty());

        Guest guest = new Guest();
        forceField(guest, "id", 0);
        when(mockGuestRepo.findById(0)).thenReturn(Optional.of(guest));

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.CART_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().contains("cart"));
    }
    @Test
    void testGuestBuyCart_Failure_StoreNotFound() throws Exception {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.empty());

        Guest guest = new Guest();
        forceField(guest, "id", 0);
        guest.addToCart(new CartItem(new ItemCartDTO(999, 0, 200,100, "TestProduct", "FakeStore", Category.Electronics)));
        when(mockGuestRepo.findById(0)).thenReturn(Optional.of(guest));

        when(mockStoreRepo.findById(999)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }
    @Test
    void testGuestBuyCart_Failure_InsufficientStock() throws Exception {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
        when(mockSusRepo.findById(0)).thenReturn(Optional.empty());

        Guest guest = new Guest();
        forceField(guest, "id", 0);
        guest.addToCart(new CartItem(new ItemCartDTO(store.getstoreId(), product.getProductId(), 5, 100, product.getName(), store.getStoreName(), Category.Electronics)));
        when(mockGuestRepo.findById(0)).thenReturn(Optional.of(guest));

        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        StoreStock stock = new StoreStock(store.getstoreId());
        stock.addItem(new item(product.getProductId(), 1, 100, Category.Electronics)); // not enough quantity
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(stock));

        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        });

        assertEquals(ErrorCodes.INSUFFICIENT_STOCK, ex.getNumber());
    }
    @Test
    void testGuestBuyCart_Failure_PaymentFails() throws Exception {
        setupValidGuestCartScenario(5); // helper that sets everything up (you can ask me to generate)

        Exception ex = assertThrows(Exception.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.test_fail_Payment(), SupplyDetails.getTestDetails());
        });

        assertTrue(ex.getMessage().contains("Payment failed"));
    }
    @Test
    void testGuestBuyCart_Failure_SupplyFails_Refunds() throws Exception {
        setupValidGuestCartScenario(5);

        Exception ex = assertThrows(Exception.class, () -> {
            purchaseService.buyGuestCart(guestToken, PaymentDetails.testPayment(), SupplyDetails.test_fail_supply());
        });

    }
    private void setupValidGuestCartScenario(int cartQuantity) throws Exception {
        when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(0);
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