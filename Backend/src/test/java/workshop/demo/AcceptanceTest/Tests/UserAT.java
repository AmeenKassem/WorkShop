package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserAT extends AcceptanceTests {
    Real real = new Real();

    @BeforeEach
    void setup() throws Exception {
        // ====== ADMIN SETUP ======
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);

        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        String adminGuest = real.userService.generateGuest();
        assertEquals(adminGuestToken, adminGuest);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN
        assertTrue(real.userService.register(adminGuest, "admin", "adminPass", 18));
        String adminToken = real.userService.login(adminGuest, "admin", "adminPass");
        assertEquals(adminUserToken, adminToken);
        testSystem_InitMarket(adminUserToken);

        // ====== STORE OWNER SETUP ======
        int ownerId = 10;
        String ownerGuestToken = "guest-token";
        String ownerUserToken = "user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(ownerGuestToken);
        when(real.mockAuthRepo.validToken(ownerGuestToken)).thenReturn(true);

        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(ownerUserToken);
        when(real.mockAuthRepo.validToken(ownerUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerUserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(ownerUserToken)).thenReturn("owner");

        String ownerGuest = real.userService.generateGuest();
        assertEquals(ownerGuestToken, ownerGuest);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.register(ownerGuest, "owner", "owner", 18));
        String ownerToken = real.userService.login(ownerGuest, "owner", "owner");
        assertEquals(ownerUserToken, ownerToken);

        // ====== STORE CREATION ======
        int storeId = 100;
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

        // ====== REGISTERED USER SETUP ======
        int userId = 20;
        String userGuestToken = "guest-token-2";
        String userToken = "user-token-2";

        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(userGuestToken);
        when(real.mockAuthRepo.validToken(userGuestToken)).thenReturn(true);

        when(real.mockUserRepo.login("user2", "pass2")).thenReturn(userId);
        when(real.mockAuthRepo.generateUserToken(userId, "user2")).thenReturn(userToken);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(userToken)).thenReturn("user2");
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);
        when(real.mockAuthRepo.isRegistered(userToken)).thenReturn(true);

        String guestToken = real.userService.generateGuest();
        assertEquals(userGuestToken, guestToken);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.register(guestToken, "user2", "pass2", 18));
        String loginToken = real.userService.login(guestToken, "user2", "pass2");
        assertEquals(userToken, loginToken);

        // ====== ADD PRODUCT TO SYSTEM ======
        int productId = 111;
        String[] keywords = {"Laptop", "Gaming", "HighPerformance"};
        when(real.mockStockRepo.addProduct("Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords))
                .thenReturn(productId);

        int returnedProductId = real.stockService.addProduct(ownerUserToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);
        assertEquals(productId, returnedProductId);

        // ====== ADD ITEM TO STORE ======
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(new Product("Laptop", productId, Category.ELECTRONICS, "Gaming Laptop", new String[]{"Laptop", "Gaming"}));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 1500, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 1500, Category.ELECTRONICS));

        int itemAdded = real.stockService.addItem(storeId, ownerUserToken, productId, 10, 1500, Category.ELECTRONICS);
        assertTrue(itemAdded == productId);
    }


    @Test
    void testUser_LogIn_Success() throws Exception {
        int newId = 21;
        String guestToken = "guest-token-21";
        String expectedToken = "user-token-21";

        when(real.mockUserRepo.generateGuest()).thenReturn(newId);
        when(real.mockAuthRepo.generateGuestToken(newId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        when(real.mockUserRepo.login("loginUser", "loginPass")).thenReturn(newId);
        when(real.mockAuthRepo.generateUserToken(newId, "loginUser")).thenReturn(expectedToken);
        when(real.mockAuthRepo.validToken(expectedToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(expectedToken)).thenReturn(newId);
        when(real.mockAuthRepo.getUserName(expectedToken)).thenReturn("loginUser");

        String guest = real.userService.generateGuest();
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.register(guest, "loginUser", "loginPass", 18));
        String actualToken = real.userService.login(guest, "loginUser", "loginPass");
        assertEquals(expectedToken, actualToken);
    }

    @Test
    void testUser_LogOut_Success() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        String guestAfterLogout = "guest-token-logout";

        when(real.mockUserRepo.logoutUser("user2")).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(guestAfterLogout);

        String result = real.userService.logoutUser(userToken);
        assertEquals(guestAfterLogout, result);
    }

    @Test
    void testUser_LogIn_Failure() throws Exception {
        int userId = 30;
        String guestToken = "guest-token-30";

        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        when(real.mockUserRepo.registerUser("baduser", "badpass", 18)).thenReturn(userId);
        when(real.mockUserRepo.login("baduser", "badpass")).thenThrow(new UIException("Incorrect username or password", 1012));

        String guest = real.userService.generateGuest();
        assertTrue(real.userService.register(guest, "baduser", "badpass", 18));

        UIException ex = assertThrows(UIException.class, () -> {
            real.userService.login(guest, "baduser", "badpass");
        });
        assertEquals("Incorrect username or password", ex.getMessage());
    }

    @Test
    void testUser_LogOut_Failure() throws Exception {
        // -------------------------
        // PART 1: Perform a successful login
        // -------------------------
        int userId = 21;
        String guestToken = "guest-token-21";
        String userToken = "user-token-21";

        // Guest registration mocks
        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        // Registration and login mocks
        when(real.mockUserRepo.login("loginUser", "loginPass")).thenReturn(userId);
        when(real.mockAuthRepo.generateUserToken(userId, "loginUser")).thenReturn(userToken);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(userToken)).thenReturn("loginUser");

        // Call the actual flow
        real.userService.generateGuest();
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.register(guestToken, "loginUser", "loginPass", 18));
        String actualToken = real.userService.login(guestToken, "loginUser", "loginPass");

        // Confirm login returned the expected token
        assertEquals(userToken, actualToken);

        // -------------------------
        // PART 2: Attempt logout with an invalid token (force failure)
        // -------------------------
        String invalidToken = "invalid-token";

        // Mock validToken to return false for the invalid token
        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        // Let the mock call the real method, which will throw UIException
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN


        when(real.mockAuthRepo.checkAuth_ThrowTimeOutException(eq(invalidToken), any()))
                .thenCallRealMethod();

        // Expect the logout to fail due to invalid token
        UIException ex = assertThrows(UIException.class, () ->
                real.userService.logoutUser(invalidToken)
        );

        // Verify exception details
        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }


    @Test
    void testUserLogin_Failure_InvalidToken() throws UIException {
        String invalidToken = "invalid-token";

        // Step 1: Mock the token check to return false
        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        // Step 2: Allow the method to call the real implementation (which will throw)
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        when(real.mockAuthRepo.checkAuth_ThrowTimeOutException(eq(invalidToken), any()))
                .thenCallRealMethod();

        // Step 3: Assert exception is thrown
        UIException exception = assertThrows(UIException.class, () ->
                real.userService.login(invalidToken, "userX", "passX")
        );

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }


    @Test
    void testUserLogin_Failure_UserNotFound() throws Exception {
        String guestToken = "guest-token-new";
        int newGuestId = 500;
        when(real.mockUserRepo.generateGuest()).thenReturn(newGuestId);
        when(real.mockAuthRepo.generateGuestToken(newGuestId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockUserRepo.login("ghost", "nopass")).thenThrow(new UIException("User not found", 101));

        String generatedToken = real.userService.generateGuest();
        assertEquals(guestToken, generatedToken);

        UIException exception = assertThrows(UIException.class, () ->
                real.userService.login(generatedToken, "ghost", "nopass")
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUserLogout_Failure_UserNotFound() throws Exception {
        String token = "bad-token";
        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenReturn("ghost");
        when(real.mockUserRepo.logoutUser("ghost")).thenThrow(new UIException("User not found: ghost", 101));

        UIException exception = assertThrows(UIException.class, () ->
                real.userService.logoutUser(token)
        );

        assertEquals("User not found: ghost", exception.getMessage());
    }

    @Test
    void testUserLogout_Failure_GuestTokenGenerationError() throws Exception {
        String token = "ghost-token";
        int userId = 20;

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenReturn("guestUser");
        when(real.mockUserRepo.logoutUser("guestUser")).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenThrow(new RuntimeException("Token generation failed"));

        Exception ex = assertThrows(RuntimeException.class, () ->
                real.userService.logoutUser(token)
        );
        assertEquals("Token generation failed", ex.getMessage());
    }

    @Test
    void testUser_setAdmin_Success() throws Exception {
        int adminId = 999;
        String adminToken = "admin-user-token";
        int userIdToPromote = 20;
        String usernameToPromote = "user-token-2";
        String adminKey = "123321";

        when(real.mockAuthRepo.validToken(usernameToPromote)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(usernameToPromote)).thenReturn("user-token-2");
        when(real.mockUserRepo.logoutUser("user-token-2")).thenReturn(userIdToPromote);
        when(real.mockUserRepo.setUserAsAdmin(userIdToPromote, adminKey)).thenReturn(true);
        boolean result = real.userService.setAdmin(usernameToPromote, adminKey, userIdToPromote);
        assertTrue(result);

    }

    @Test
    void testUser_setAdmin_Failure_InvalidToken() throws UIException {
        String token = "user-token-2";
        String adminKey = "123321";
        int userIdToPromote = 20;

        when(real.mockAuthRepo.validToken(token)).thenReturn(false);

//        UIException ex = assertThrows(UIException.class, () -> {
//            real.userService.setAdmin(token, adminKey, userIdToPromote);
//        });
        assertEquals(real.userService.setAdmin(token, adminKey, userIdToPromote), false);
    }


    //    @Test
//    void testUser_setAdmin_Failure_LogoutUserThrows() throws UIException {
//        String token = "user-token-2";
//        String adminKey = "123321";
//        int userIdToPromote = 20;
//
//        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
//        when(real.mockAuthRepo.getUserName(token)).thenReturn("user2");
//        when(real.mockUserRepo.logoutUser("user2")).thenThrow(new RuntimeException("logoutUser failed"));
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
//            real.userService.setAdmin(token, adminKey,userIdToPromote);
//        });
//        assertEquals("logoutUser failed", ex.getMessage());
//    }
    @Test
    void testUser_CheckPurchaseHistory_Success() throws Exception {
        int userId = 20;
        String token = "user-token-2";
        int storeId = 100;
        String storeName = "TestStore";
        String date = "2025-05-10";
        List<ReceiptProduct> products = List.of(
                new ReceiptProduct("Keyboard", Category.ELECTRONICS, "Standard keyboard", "TestStore", 1, 100),
                new ReceiptProduct("Mouse", Category.ELECTRONICS, "Wireless mouse", "TestStore", 1, 100)
        );
        int totalPrice = 200;

        ReceiptDTO receipt = new ReceiptDTO(storeName, date, products, totalPrice);
        List<ReceiptDTO> receipts = new LinkedList<>();
        receipts.add(receipt);
        real.mockOrderRepo.setOrderToStore(storeId, userId, receipt, storeName);
        when(real.mockOrderRepo.getReceiptDTOsByUser(userId)).thenReturn(receipts);
        List<ReceiptDTO> result = real.testUser_CheckPurchaseHistory(token);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals(storeName, r.getStoreName());
        assertEquals(date, r.getDate());
        assertEquals(products, r.getProductsList());
        assertEquals(totalPrice, r.getFinalPrice());
    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_InvalidToken() {
        String token = "guest-registered-token";

        when(real.mockAuthRepo.validToken(token)).thenReturn(false);

        UIException ex = assertThrows(UIException.class, () -> {
            real.testUser_CheckPurchaseHistory(token);
        });
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_GetUserNameThrows() throws UIException {
        String token = "guest-registered-token";

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenThrow(new RuntimeException("getUserName failed"));

        UIException ex = assertThrows(UIException.class, () -> {
            real.testUser_CheckPurchaseHistory(token);
        });
        //System.out.println("Actual exception message: " + ex.getMessage());

        assertTrue(ex.getMessage().contains("is not registered to the system!"));
    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_NoReceipts() throws Exception {
        String token = "user-token-2";

        // All of these are already established in @BeforeEach
        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenReturn("user2");
        when(real.mockAuthRepo.getUserId("user2")).thenReturn(20);
        when(real.mockUserRepo.isRegistered(20)).thenReturn(true);

        // Simulate no receipts
        when(real.mockOrderRepo.getReceiptDTOsByUser(20))
                .thenThrow(new UIException("User has no receipts.", ErrorCodes.RECEIPT_NOT_FOUND));

        // Act & Assert
        UIException ex = assertThrows(UIException.class, () -> {
            real.testUser_CheckPurchaseHistory(token);
        });

        assertEquals(ErrorCodes.RECEIPT_NOT_FOUND, ex.getNumber());
        assertEquals("User has no receipts.", ex.getMessage());
    }

    @Test
    void testUserGetStoreProducts() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;
        int productId = 111; // use the one from @BeforeEach

        // Already mocked in setup:
        // findByIdInSystem(111)
        // addProduct
        // addItem

        List<ItemStoreDTO> mockProducts = List.of(
                new ItemStoreDTO(productId, 10, 1500, Category.ELECTRONICS, 5, storeId)
        );
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(mockProducts);

        List<ItemStoreDTO> products = real.stockService.getProductsInStore(storeId);
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(productId, products.get(0).getId());
    }

    @Test
    void testUserViewEmptyStore() throws Exception {
        int storeId = 100;
        String userToken = "user-token-2";

        // Ensure user is valid (should already be mocked in setup)
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        // Mock: store has no products
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(List.of());

        List<ItemStoreDTO> products = real.stockService.getProductsInStore(storeId);

        assertNotNull(products, "Products list should not be null");
        assertTrue(products.isEmpty());
    }

    @Test
    void testUserViewInvalidStore() throws Exception {
        int invalidStoreId = 999;
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        // Mock: store retrieval throws exception
        when(real.mockStockRepo.getProductsInStore(invalidStoreId))
                .thenThrow(new RuntimeException("Store not found"));

        List<ItemStoreDTO> products = null;
        try {
            products = real.stockService.getProductsInStore(invalidStoreId);
        } catch (Exception e) {
            assertNull(products);
            assertEquals("Store not found", e.getMessage());
        }
    }

    @Test
    void testUserViewStoreWithoutToken() throws Exception {
        int storeId = 10;

        when(real.mockStockRepo.getProductsInStore(storeId))
                .thenReturn(List.of(new ItemStoreDTO(100, 10, 100, Category.ELECTRONICS, 5, storeId)));

        List<ItemStoreDTO> products = real.stockService.getProductsInStore(storeId);

        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void testUserGetProductInfo() throws Exception {
        int userId = 20; // generic user
        int storeId = 100;
        int productId = 111;
        String userToken = "user-token-2";

        // === USER adds product ===
        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.addItem(storeId, productId, 5, 200, Category.ELECTRONICS))
                .thenReturn(new item(productId, 5, 200, Category.ELECTRONICS));

        int addItemResult = real.stockService.addItem(storeId, userToken, productId, 5, 200, Category.ELECTRONICS);
        assertEquals(addItemResult, productId);

        // === MOCK: product info ===
        ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.ELECTRONICS, "Smart device");
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        // === USER requests product info ===
        String info = real.testGuest_GetProductInfo(userToken, productId);

        assertNotNull(info);
        assertTrue(info.contains("Phone"), "Expected product name in info");
        assertTrue(info.contains("Smart device"), "Expected product description in info");
        assertTrue(info.contains("ELECTRONICS"), "Expected product category in info");
    }

    @Test
    void testUserGetProductInfo_ProductNotFound() throws Exception {
        int productId = 999; // non-existent product
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(null);

        UIException exception = assertThrows(UIException.class, () -> {
            real.testGuest_GetProductInfo(userToken, productId);
        });

        assertEquals("Product not found.", exception.getMessage());
    }

    @Test
    void testUserAddProductToCart_Success() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 10;
        int productId = 100;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 5, storeId);
        ItemCartDTO itemCartDTO = new ItemCartDTO(itemStoreDTO);

        doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);

        assertDoesNotThrow(() -> {
            real.testGuest_AddProductToCart(userToken, itemStoreDTO); // method name unchanged
        });
    }

    @Test
    void testUserAddProductToCart_InvalidToken() throws Exception {
        String userToken = "user-token-2";
        int storeId = 10;
        int productId = 100;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(false); // token is invalid

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 5, storeId);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        when(real.mockAuthRepo.checkAuth_ThrowTimeOutException(eq(userToken), any())).thenCallRealMethod();

        UIException ex = assertThrows(UIException.class, () -> {
            real.testGuest_AddProductToCart(userToken, itemStoreDTO);
        });

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testUserAddProductToCart_ZeroQuantity() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 10;
        int productId = 100;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 0, 100, Category.ELECTRONICS, 5, storeId);

        doThrow(new UIException("Cannot add product with zero quantity", 1025))
                .when(real.mockUserRepo).addItemToGeustCart(eq(userId), any(ItemCartDTO.class));

        UIException exception = assertThrows(UIException.class, () -> {
            real.userService.addToUserCart(userToken, itemStoreDTO);
        });

        assertEquals("Cannot add product with zero quantity", exception.getMessage());
    }

    @Test
    void testUserBuyCart_Success() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        // Add item to user cart
        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 2, 100, Category.ELECTRONICS, 5, storeId);
        ItemCartDTO itemCartDTO = new ItemCartDTO(itemStoreDTO);
        doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);
        real.userService.addToUserCart(userToken, itemStoreDTO);

        // Mock user cart and basket
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(itemCartDTO));

        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(itemCartDTO));

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        // StoreRepo + Payment + Supply
        List<ReceiptProduct> receiptProducts = List.of(
                new ReceiptProduct("ProductName", Category.ELECTRONICS, "desc", "TestStore", 2, 100));
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(false))).thenReturn(receiptProducts);
        when(real.mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
        when(real.mockPay.processPayment(PaymentDetails.testPayment(), 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(any())).thenReturn(true);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());

        verify(real.mockUserRepo).getUserCart(userId);
        verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), anyList(), eq(false));
        verify(real.mockStockRepo).calculateTotalPrice(anyList());
        verify(real.mockPay).processPayment(any(), eq(200.0));
        verify(real.mockSupply).processSupply(any());
        verify(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
    }

    @Test
    void testUserBuyCart_InvalidToken() throws Exception {
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
        assertEquals(1009, ex.getNumber());

    }

    @Test
    void testUserBuyCart_EmptyCart() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
    }

    @Test
    void testUserBuyCart_ProductNotAvailable() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;

        // === Basic Auth Mocking ===
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        // === Mock item inside basket ===
        ItemCartDTO mockItem = Mockito.mock(ItemCartDTO.class);
        List<ItemCartDTO> items = List.of(mockItem);

        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(items);

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(mockCart.getAllCart()).thenReturn(items);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);

        // === Force checkAvailability to fail ===
        // ✅ This is now pointless — user flow does not call this
        // But we keep it for verification
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(false);

        // === Add real failure: simulate failure inside processCartItemsForStore ===
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), eq(items), eq(false)))
                .thenThrow(new UIException("Not all items are available for user purchase", 1006));

        // === Prepare payment/supply ===
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        // === Actual test ===
        UIException ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

        assertEquals("Not all items are available for user purchase", ex.getMessage());

        // Optional: confirm other mocks
        verify(real.mockUserRepo).getUserCart(userId);
        verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), eq(items), eq(false));
    }


    @Test
    void testUserBuyCart_PaymentFails() throws Exception {
        int userId = 20;
        int storeId = 100;
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(ItemCartDTO.class)));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(ItemCartDTO.class)));

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true)))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);

        doThrow(new RuntimeException("Payment failed")).when(real.mockPay)
                .processPayment(any(), eq(100.0));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

        assertEquals("Payment failed", ex.getMessage());
    }

    @Test
    void testUserSearchProductInStore_Success() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;
        int productId = 111; // From setup

        // === Criteria that filters by store ===
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, "Lap", storeId,
                0, 0, 0, 0
        );

        // === Auth validation ===
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        // === Return product and item ===
        ProductDTO[] products = {
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(products);

        ItemStoreDTO[] items = {
                new ItemStoreDTO(productId, 10, 1500, Category.ELECTRONICS, 4, storeId)
        };
        when(real.mockStockRepo.getMatchesItems(criteria, products)).thenReturn(items);

        // === Perform search ===
        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        // === Assert ===
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(productId, result[0].getId());
        //assertEquals("Laptop", result[0].getProductName());
        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());

        verify(real.mockStockRepo).getMatchesProducts(criteria);
        verify(real.mockStockRepo).getMatchesItems(criteria, products);
    }


    ProductSearchCriteria criteria = new ProductSearchCriteria(
            null,                       // productNameFilter
            Category.ELECTRONICS,       // categoryFilter
            null,                       // keywordFilter
            0,                          // storeId (0 = not restricted)
            0,                          // minPrice
            0,                          // maxPrice
            0,                          // minStoreRating
            0                           // maxStoreRating
    );


    @Test
    void testUserSearchProducts_InvalidToken() throws Exception {
        String userToken = "user-token-2";

        // Build criteria to search across all stores
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, Category.ELECTRONICS, null,
                0, 0, 0, 0, 0
        );

        // Simulate checkAuth failure
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());

        UIException exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProducts(userToken, criteria);
        });

        assertEquals("Invalid token!", exception.getMessage());
    }


    @Test
    void testUserSearchProducts_NoMatches() throws Exception {
        String userToken = "user-token-2";

        // Use the full constructor
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,                     // productNameFilter
                Category.ELECTRONICS,     // categoryFilter
                null,                     // keywordFilter
                0,                        // storeId (not restricted)
                0,                        // minPrice
                0,                        // maxPrice
                0,                        // minStoreRating
                0                         // maxStoreRating
        );

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20); // Optional, for completeness

        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(new ProductDTO[0]);
        when(real.mockStockRepo.getMatchesItems(criteria, new ProductDTO[0])).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testUserSearchProductInStore_InvalidToken() throws Exception {
        String userToken = "user-token-2";
        int storeId = 100;
        int productId = 200;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Product", null, null, storeId,
                0, 0, 0, 0
        );

        // Instead of validToken() → you must mock the checkAuth method itself
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());

        UIException exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProducts(userToken, criteria);
        });

        assertEquals("Invalid token!", exception.getMessage());
    }


    @Test
    void testUserSearchProductInStore_ProductNotFound() throws Exception {
        int storeId = 100;
        int productId = 200;
        String userToken = "user-token-2";

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, storeId,
                0, 0, 0, 0
        );

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);

        ProductDTO[] emptyProducts = new ProductDTO[0];
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(emptyProducts);

        // If matchesProducts is empty, the service still calls getMatchesItems, so we must mock it too
        when(real.mockStockRepo.getMatchesItems(criteria, emptyProducts)).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // No matches expected
    }


    @Test
    void testUserSearchProductInStore_ProductNotInStore() throws Exception {
        int storeId = 100;
        int productId = 200;
        String userToken = "user-token-2";

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, storeId,
                0, 0, 0, 0
        );

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);

        ProductDTO[] products = {
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(products);

        when(real.mockStockRepo.getMatchesItems(criteria, products)).thenReturn(new ItemStoreDTO[0]);

        // No exception expected anymore
        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }


    @Test
    void testUserGetPurchasePolicy_Success() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        String ownerToken = "user-token"; // from setup
//        String userToken = "user-token-2"; // generic user
//        int userId = 20;
//
//        // --- Owner creates store ---
//        when(real.mockStoreRepo.addStoreToSystem(ownerId, "store1", "ELECTRONICS")).thenReturn(storeId);
//        real.testUser_OpenStore(ownerToken, "store1", "ELECTRONICS");
//
//        // --- Generic user is valid ---
//        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//        // --- Mock policy description ---
//       // when(real.mockStoreRepo.(storeId)).thenReturn("Minimum quantity policy: 2");
//
//        String result = real.testGuest_GetPurchasePolicy(userToken, storeId);
//
//        assertNotNull(result);
//        assertTrue(result.contains("Minimum quantity policy"));
    }

    @Test
    void testUserGetPurchasePolicy_Failure() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        String ownerToken = "user-token"; // from setup
//        String userToken = "user-token-2"; // generic user
//        int userId = 20;
//
//        // --- Owner creates store ---
//        when(real.mockStoreRepo.addStoreToSystem(ownerId, "store1", "ELECTRONICS")).thenReturn(storeId);
//        real.testUser_OpenStore(ownerToken, "store1", "ELECTRONICS");
//
//        // --- Generic user is valid ---
//        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//        // --- Simulate failure (e.g., no policy found) ---
//        when(real.mockStoreRepo.getPurchasePolicyString(storeId))
//                .thenThrow(new UIException("Store has no purchase policy", ErrorCodes.POLICY_NOT_FOUND));
//
//        UIException ex = assertThrows(UIException.class, () ->
//                real.testGuest_GetPurchasePolicy(userToken, storeId));
//
//        assertEquals("Store has no purchase policy", ex.getMessage());
    }

    void testGuestModifyCartAddQToBuy() throws Exception {
        //TODO:NOT IMPLEMENTED
    }


}