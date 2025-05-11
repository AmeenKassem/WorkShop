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
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GuestAT extends AcceptanceTests {
    Real real = new Real();
    String token_guest="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwiYWRtaW5Vc2VyMlwiLFwiaWRcIjoyfSIsImlhdCI6MTc0NjgyNDkyNiwiZXhwIjoxNzQ2ODI4NTI2fQ.zSFqaapc2ANZpwbewtxqk2hedPH50VrdFYS8Dj58eTw";

    @BeforeEach
    void setup() throws Exception {
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";
        // Guest creation
        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);
        // Registration
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);
        // Login
        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        // Step 1: generate guest
        String guestToken = real.userService.generateGuest();
        assertEquals(adminGuestToken, guestToken);
        // Step 2: register admin user
        boolean registered = real.userService.register(guestToken, "admin", "adminPass");
        assertTrue(registered);
        // Step 3: login admin user
        String userToken = real.userService.login(guestToken, "admin", "adminPass");
        assertEquals(adminUserToken, userToken);
        testSystem_InitMarket(adminUserToken);


        int ownerId = 10;
        String GuestToken = "guest-token";
        String UserToken = "user-token";
        // Guest creation
        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(GuestToken);
        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);
        // Registration
        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);
        // Login
        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(UserToken);
        when(real.mockAuthRepo.validToken(UserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(UserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(UserToken)).thenReturn("owner");


        // Step 1: generate guest
        String guestToken1 = real.userService.generateGuest();
        assertEquals(GuestToken, guestToken1);
        // Step 2: register admin user
        boolean registered1 = real.userService.register(guestToken1, "owner", "owner");
        assertTrue(registered1);
        // Step 3: login admin user
        String userToken1 = real.userService.login(guestToken1, "owner", "owner");
        assertEquals(UserToken, userToken1);
        int storeId = 100;
        when(real.mockAuthRepo.validToken(userToken1)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken1)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(userToken1, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);
        when(real.mockUserRepo.generateGuest()).thenReturn(1);
        when(real.mockAuthRepo.generateGuestToken(1)).thenReturn(token_guest);
        String token=real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");
    }


    //NOTE:ENTER+EXIT+REGISTER FINISH
    @Test
    void testGuestEnter_Success() throws Exception {
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("token_guest");
        String token=real.userService.generateGuest();

        assertTrue(token.equals("token_guest"));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");
    }
    @Test
    void testGuestEnter_Failure() throws Exception {
        // Arrange: first call works
        when(real.mockUserRepo.isOnline(2)).thenReturn(true);
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");

        String token = real.userService.generateGuest();
        assertNotNull(token);

        // Arrange: second call throws unchecked exception
        when(real.mockAuthRepo.generateGuestToken(2)).thenThrow(new RuntimeException("already entered"));
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            real.userService.generateGuest();
        });

        assertEquals("already entered", ex.getMessage());
    }
    @Test
    void testGuestExit_Success() throws Exception {
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn(token_guest);

        String token=real.userService.generateGuest();

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        assertTrue(real.userService.destroyGuest(token));


    }
    @Test
    void testGuestExit_Failure() throws Exception {
        // --- Arrange: Guest enters the system ---
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("token_guest");
        String token = real.userService.generateGuest();
        // --- Arrange: simulate invalid token ---
        when(real.mockAuthRepo.validToken("invalid_token")).thenReturn(false);

        // --- Act & Assert: try to destroy with invalid token and expect exception ---
        Exception ex = assertThrows(Exception.class, () -> real.userService.destroyGuest("invalid_token"));
        assertEquals("Invalid token!", ex.getMessage());
    }
    @Test
    void testGuestRegister_Success() throws Exception {
        //enter
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn(token_guest);
        String token=real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");
        //check
        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        assertTrue(real.userService.register(token,"bashar","finish"));

    }
    @Test
    void testGuestRegister_Failure_InvalidToken() throws Exception {
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn(token_guest);
        String token=real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> real.userService.register("Invalid token!","bashar","finish"));

    }


    //NOTE:GET STORE PRODUCT FINISH
    @Test
    void testGuestGetStoreProducts() throws Exception {
        // ===== SETUP IDS & TOKENS =====
        int userId = 10;
        int storeId = 100;
        int productId = 100;
        String UserToken1 = "user-token";
        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));

        boolean addItemResult = real.storeService.addItem(storeId, UserToken1, productId, 10, 100, Category.ELECTRONICS);
        assertTrue(addItemResult);
        // ===== GUEST B REQUESTS PRODUCTS =====
        List<ItemStoreDTO> mockProducts = List.of(new ItemStoreDTO(
                productId, 10, 100, Category.ELECTRONICS, 5, storeId));
        when(real.mockStoreRepo.getProductsInStore(storeId)).thenReturn(mockProducts);

        List<ItemStoreDTO> products = real.storeService.getProductsInStore(storeId);
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(productId, products.get(0).getId());
    }
    @Test
    void testGuestViewEmptyStore() throws Exception {
        int storeId = 100;  // use storeId from setup
        String guestToken = "guest-token";  // use guestToken from setup

        // ===== MOCK: store has no products =====
        when(real.mockStoreRepo.getProductsInStore(storeId)).thenReturn(List.of());

        // ===== GUEST requests products =====
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true); // ensure guest token is valid

        List<ItemStoreDTO> products = real.storeService.getProductsInStore(storeId);

        // ===== ASSERT =====
        assertNotNull(products, "Products list should not be null");
        assertTrue(products.isEmpty());
    }
    @Test
    void testGuestViewInvalidStore() throws Exception {
        int userIdA = 1;
        int invalidStoreId = 999;
        String guestTokenA = "guest-token-A";

        // ===== MOCK GUEST A =====
        when(real.mockUserRepo.generateGuest()).thenReturn(userIdA);
        when(real.mockAuthRepo.generateGuestToken(userIdA)).thenReturn(guestTokenA);

        String guestAToken = real.userService.generateGuest();
        assertEquals(guestTokenA, guestAToken);

        // ===== MOCK STORE REPO TO THROW EXCEPTION =====
        when(real.mockStoreRepo.getProductsInStore(invalidStoreId))
                .thenThrow(new RuntimeException("Store not found"));

        List<ItemStoreDTO> products = null;
        try {
            products = real.storeService.getProductsInStore(invalidStoreId);
        } catch (Exception e) {
            assertNull(products);
        }
    }
    @Test
    void testGuestViewStoreWithoutToken() throws Exception {
        int storeId = 10;

        // ===== MOCK STORE WITH PRODUCTS =====
        when(real.mockStoreRepo.getProductsInStore(storeId))
                .thenReturn(List.of(new ItemStoreDTO(100, 10, 100, Category.ELECTRONICS, 5, storeId)));

        // No guest token involved in this test, but you could extend it if your system requires a valid token.
        List<ItemStoreDTO> products = real.storeService.getProductsInStore(storeId);

        assertNotNull(products);
        assertFalse(products.isEmpty());
    }


    //TODO:pick them to store and manger test

//    @Test
//    void testAddItem_InvalidToken_Acceptance() throws Exception {
//        int userIdA = 1;
//        int storeId = 10;
//        String validToken = "valid-token";
//        String invalidToken = "invalid-token";
//        int productId = 100;
//
//        // Guest → Register → Login → Open Store
//        when(real.mockUserRepo.generateGuest()).thenReturn(userIdA);
//        when(real.mockAuthRepo.generateGuestToken(userIdA)).thenReturn("guest-token");
//        String guestToken = real.userService.generateGuest();
//
//        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
//        when(real.mockUserRepo.registerUser("userA","passA")).thenReturn(userIdA);
//        when(real.mockUserRepo.login("userA", "passA")).thenReturn(userIdA);
//        when(real.mockAuthRepo.generateUserToken(userIdA, "userA")).thenReturn(validToken);
//        when(real.mockAuthRepo.validToken(validToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userIdA);
//        when(real.mockUserRepo.isRegistered(userIdA)).thenReturn(true);
//        when(real.mockStoreRepo.addStoreToSystem(userIdA, "TestStore", "ELECTRONICS")).thenReturn(storeId);
//        when(real.mockIOSrepo.addNewStoreOwner(storeId, userIdA)).thenReturn(true);
//
//        real.userService.register(guestToken, "userA", "passA");
//        String userAToken = real.userService.login(guestToken, "userA", "passA");
//        real.storeService.addStoreToSystem(userAToken, "TestStore", "ELECTRONICS");
//
//        // Now simulate invalid token usage
//        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);
//
//        boolean result = real.storeService.addItem(storeId, invalidToken, productId, 10, 100, Category.ELECTRONICS);
//        assertFalse(result);
//    }
//
//    @Test
//    void testAddItem_UserNotRegistered() throws Exception {
//        int userIdA = 1;
//        int storeId = 10;
//        int productId = 100;
//        String userAToken = "user-token-A";
//
//        when(real.mockAuthRepo.validToken(userAToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(userAToken)).thenReturn(userIdA);
//        when(real.mockUserRepo.isRegistered(userIdA)).thenReturn(false);
//
//        boolean result = real.storeService.addItem(storeId, userAToken, productId, 10, 100, Category.ELECTRONICS);
//        assertFalse(result);
//    }
////    @Test
////    void testAddItem_UserNotRegistered_Acceptance() throws Exception {
////        int userIdA = 1;
////        int storeId = 10;
////        int productId = 100;
////        String token = "user-token-A";
////
////        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(token)).thenReturn(userIdA);
////        when(real.mockUserRepo.isRegistered(userIdA)).thenReturn(false);
////
////        boolean result = real.storeService.addItem(storeId, token, productId, 10, 100, Category.ELECTRONICS);
////        assertFalse(result);
////    }
//    @Test
//    void testAddItem_NoPermission_Acceptance() throws Exception {
//        int userIdA = 1;
//        int storeId = 10;
//        String userToken = "user-token-A";
//        int productId = 100;
//
//        // Setup as valid owner
//        when(real.mockUserRepo.generateGuest()).thenReturn(userIdA);
//        when(real.mockAuthRepo.generateGuestToken(userIdA)).thenReturn("guest-token");
//        String guestToken = real.userService.generateGuest();
//
//        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
//        when(real.mockUserRepo.registerUser("userA","passA")).thenReturn(userIdA);
//        when(real.mockUserRepo.login("userA", "passA")).thenReturn(userIdA);
//        when(real.mockAuthRepo.generateUserToken(userIdA, "userA")).thenReturn(userToken);
//        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userIdA);
//        when(real.mockUserRepo.isRegistered(userIdA)).thenReturn(true);
//        when(real.mockStoreRepo.addStoreToSystem(userIdA, "TestStore", "ELECTRONICS")).thenReturn(storeId);
//        when(real.mockIOSrepo.addNewStoreOwner(storeId, userIdA)).thenReturn(true);
//
//        real.userService.register(guestToken, "userA", "passA");
//        String userAToken = real.userService.login(guestToken, "userA", "passA");
//        real.storeService.addStoreToSystem(userAToken, "TestStore", "ELECTRONICS");
//
//        // Simulate missing permission
//        when(real.mockIOSrepo.manipulateItem(userIdA, storeId, Permission.AddToStock)).thenReturn(false);
//
//        boolean result = real.storeService.addItem(storeId, userAToken, productId, 10, 100, Category.ELECTRONICS);
//        assertFalse(result);
//    }

    //NOTE:INFO PRODUCT FINISH ASK ABOUT MORE FAILURE
    @Test
    void testGuestGetProductInfo() throws Exception {
        int userId = 10;
        int storeId = 100;
        int productId = 100;
        String userToken = "user-token";
        String guestToken = "guest-token-1";

        // ===== USER (OWNER) ADDS PRODUCT =====
        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStoreRepo.addItem(storeId, productId, 5, 200, Category.ELECTRONICS))
                .thenReturn(new item(productId, 5, 200, Category.ELECTRONICS));

        boolean addItemResult = real.storeService.addItem(storeId, userToken, productId, 5, 200, Category.ELECTRONICS);
        assertTrue(addItemResult);

        // ===== MOCK PRODUCT INFO =====
        ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.ELECTRONICS, "Smart device");
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        // ===== GUEST REQUESTS PRODUCT INFO =====
        String info = real.testGuest_GetProductInfo(guestToken, productId);

        assertNotNull(info);
        assertTrue(info.contains("Phone"), "Expected product name in info");
        assertTrue(info.contains("Smart device"), "Expected product description in info");
        assertTrue(info.contains("ELECTRONICS"), "Expected product category in info");
    }
    @Test
    void testGuestGetProductInfo_ProductNotFound() throws Exception {
        int userIdB = 2;
        int productId = 999; // non-existent product
        String guestTokenB = "guest-token-B";

        // Mock valid guest token
        when(real.mockAuthRepo.validToken(guestTokenB)).thenReturn(true);

        // Important: return null from repo, service will throw
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(null);

        UIException exception = assertThrows(UIException.class, () -> {
            real.testGuest_GetProductInfo(guestTokenB, productId);
        });

        assertEquals("Product not found.", exception.getMessage());
    }

    //NOTE:ADD TO CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestAddProductToCart_Success() throws Exception {
        int guestId = 1;
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 5, storeId);
        ItemCartDTO itemCartDTO = new ItemCartDTO(itemStoreDTO);

        doNothing().when(real.mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);

        assertDoesNotThrow(() -> {
            real.testGuest_AddProductToCart(guestToken, itemStoreDTO);
        });
    }
    @Test
    void testGuestAddProductToCart_InvalidToken() throws Exception {
        int guestId = 1;
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        // Mock token invalid
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 5, storeId);
        ItemCartDTO itemCartDTO = new ItemCartDTO(itemStoreDTO);

        Exception exception = assertThrows(UIException.class, () -> {
            real.testGuest_AddProductToCart(guestToken, itemStoreDTO);
        });

        assertEquals("Invalid token!", exception.getMessage());
    }
    @Test
    void testGuestAddProductToCart_ZeroQuantity() throws Exception {
        int guestId = 1;
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 0, 100, Category.ELECTRONICS, 5, storeId);

        doThrow(new UIException("Cannot add product with zero quantity", 1025))
                .when(real.mockUserRepo).addItemToGeustCart(eq(guestId), any(ItemCartDTO.class));

        Exception exception = assertThrows(UIException.class, () -> {
            real.userService.addToUserCart(guestToken, itemStoreDTO);
        });

        assertEquals("Cannot add product with zero quantity", exception.getMessage());
    }


    //NOTE :BUY CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestBuyCart_Success() throws Exception {
        int guestId = 20;
        int storeId = 100;
        int productId = 200;
        String guestToken = "guest-token";

        // --- Step 1: Mock guest creation ---
        when(real.mockUserRepo.generateGuest()).thenReturn(guestId);
        when(real.mockAuthRepo.generateGuestToken(guestId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        String generatedGuestToken = real.userService.generateGuest();
        assertEquals(guestToken, generatedGuestToken);

        // --- Step 2: Add product to guest cart ---
        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 2, 100, Category.ELECTRONICS, 5, storeId);
        ItemCartDTO itemCartDTO = new ItemCartDTO(itemStoreDTO);

        // Simulate adding item to cart
        doNothing().when(real.mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);
        real.userService.addToUserCart(guestToken, itemStoreDTO);

        // --- Step 3: Mock the guest cart with item inside ---
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(itemCartDTO));

        // Create fake basket with the storeId
        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(itemCartDTO));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        // --- Step 4: Mock storeRepo and services behavior ---
        List<ReceiptProduct> receiptProducts = List.of(new ReceiptProduct("ProductName", Category.ELECTRONICS, "desc", "TestStore", 2, 100));
        when(real.mockStoreRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStoreRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true))).thenReturn(receiptProducts);
        when(real.mockStoreRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
        when(real.mockPay.processPayment(PaymentDetails.testPayment(), 200)).thenReturn(true);
        when(real.mockSupply.processSupply(any())).thenReturn(true);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        // --- Step 5: Mock orderRepo to accept orders ---
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(guestId), any(), eq("TestStore"));

        // --- Step 6: Execute buyGuestCart ---
        PaymentDetails paymentDetails = PaymentDetails.testPayment();  // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();    // fill if needed
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails);

        // --- Step 7: Assert results ---
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getProductsList().size()*receipts[0].getProductsList().getFirst().getPrice()*2);

        // --- Step 8: Verify important calls happened ---
        verify(real.mockUserRepo).getUserCart(guestId);
        verify(real.mockStoreRepo).checkAvailability(any());
        verify(real.mockPay).processPayment(any(), eq(200.0));
        verify(real.mockSupply).processSupply(any());
        verify(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(guestId), any(), eq("TestStore"));
    }
    @Test
    void testGuestBuyCart_InvalidToken() throws Exception {
        String guestToken = "guest-token";
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Invalid token!", ex.getMessage());
    }
    @Test
    void testGuestBuyCart_EmptyCart() throws Exception {
        int guestId = 20;
        String guestToken = "guest-token";
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(Mockito.mock(ShoppingCart.class));
        when(real.mockUserRepo.getUserCart(guestId).getAllCart()).thenReturn(List.of());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
    }
    @Test
    void testGuestBuyCart_ProductNotAvailable() throws Exception {
        int guestId = 20;
        String guestToken = "guest-token";
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(ItemCartDTO.class)));
        when(real.mockStoreRepo.checkAvailability(any())).thenReturn(false);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class, () ->
                real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Not all items are available for guest purchase", ex.getMessage());
    }
    @Test
    void testGuestBuyCart_PaymentFails() throws Exception {
        int guestId = 20;
        int storeId = 100;
        String guestToken = "guest-token";
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(ItemCartDTO.class)));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(ItemCartDTO.class)));

        when(real.mockStoreRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStoreRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true)))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStoreRepo.calculateTotalPrice(anyList())).thenReturn(100.0);

        doThrow(new RuntimeException("Payment failed")).when(real.mockPay)
                .processPayment(any(), eq(100.0));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(RuntimeException.class, () ->
                real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Payment failed", ex.getMessage());
    }


    //NOTE :SEARCH FINISHED +ASK FOR MORE FAILURE
    @Test
    void testGuestSearchProductInStore_Success() throws Exception {
        int guestId = 1;
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        // --- Step 1: Mock valid token and guest ID ---
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        // --- Step 2: Mock product and item in store ---
        Product mockProduct = new Product("Laptop",productId,  Category.ELECTRONICS, "Gaming Laptop",null);
        when(real.mockStockRepo.findById(productId)).thenReturn(mockProduct);

        item mockItem = new item(productId, 5, 1500, Category.ELECTRONICS);
        when(real.mockStoreRepo.getItemByStoreAndProductId(storeId, productId)).thenReturn(mockItem);

        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        // --- Step 3: Call StockService.searchProductInStore ---
        String result = real.testGuest_SearchProductInStore(guestToken, storeId, productId);

        // --- Step 4: Assert the returned result string ---
        assertNotNull(result);
        assertTrue(result.contains("Laptop"));
        assertTrue(result.contains("1500"));
        assertTrue(result.contains("TestStore"));

        // --- Step 5: Verify that the mocks were called ---
        verify(real.mockStockRepo).findById(productId);
        verify(real.mockStoreRepo).getItemByStoreAndProductId(storeId, productId);
        verify(real.mockStoreRepo).getStoreNameById(storeId);
    }
    @Test
    void testGuestSearchProducts_Success() throws Exception {
        int guestId = 1;
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        // --- Step 1: Mock valid guest token ---
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        // --- Step 2: Prepare search criteria ---
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategory(Category.ELECTRONICS);

        // --- Step 3: Mock stockRepo response ---
        ProductDTO[] matchedProducts = new ProductDTO[] {
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(matchedProducts);

        // --- Step 4: Mock storeRepo response ---
        ItemStoreDTO[] matchedItems = new ItemStoreDTO[] {
                new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 10, storeId)
        };
        when(real.mockStoreRepo.getMatchesItems(criteria, matchedProducts)).thenReturn(matchedItems);

        // --- Step 5: Call the system under test ---
        ItemStoreDTO[] result = real.testGuest_SearchProduct(guestToken, criteria);

        // --- Step 6: Assert results ---
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(productId, result[0].getId());
        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());

        // --- Step 7: Verify mocks ---
        verify(real.mockStockRepo).getMatchesProducts(criteria);
        verify(real.mockStoreRepo).getMatchesItems(criteria, matchedProducts);
    }
    @Test
    void testSearchProducts_InvalidToken() throws Exception {
        String guestToken = token_guest;

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategory(Category.ELECTRONICS);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);

        Exception exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProducts(guestToken, criteria);
        });

        assertEquals("Invalid token!", exception.getMessage());
    }
    @Test
    void testSearchProducts_NoMatches() throws Exception {
        String guestToken = token_guest;

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategory(Category.ELECTRONICS);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(new ProductDTO[0]);
        when(real.mockStoreRepo.getMatchesItems(criteria, new ProductDTO[0])).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
    @Test
    void testSearchProductInStore_InvalidToken() throws Exception {
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);

        Exception exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProductInStore(guestToken, storeId, productId);
        });

        assertEquals("Invalid token!", exception.getMessage());
    }
    @Test
    void testSearchProductInStore_ProductNotFound() throws Exception {
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.findById(productId)).thenReturn(null);

        Exception exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProductInStore(guestToken, storeId, productId);
        });

        assertEquals("Product not found", exception.getMessage());
    }
    @Test
    void testSearchProductInStore_ProductNotInStore() throws Exception {
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        Product product = new Product( "Laptop",productId, Category.ELECTRONICS, "Gaming Laptop",null);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.findById(productId)).thenReturn(product);
        when(real.mockStoreRepo.getItemByStoreAndProductId(storeId, productId)).thenReturn(null);

        Exception exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProductInStore(guestToken, storeId, productId);
        });

        assertEquals("Product not sold in this store", exception.getMessage());
    }



    //TODO: NEED TO CHECK THE NEW DEVELOPMENT BRANCH
    @Test
    void testGuestModifyCartAddQToBuy() throws Exception {
        //TODO:NOT IMPLEMENTED
    }


    //TODO :I THINK THESE IS NOT RELATED TO GUEST BUT TO STORE OWNER
    @Test
    void testGuestGetPurchasePolicy() throws Exception {
        // --- Step 1: Owner enters and registers ---
        when(real.mockUserRepo.generateGuest()).thenReturn(1);
        when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store ---
        int storeId = 100;
        when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");

        // --- Step 3: Guest enters (no registration) ---
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // TODO: need to implement this

        String result = real.testGuest_GetPurchasePolicy("guest-token", storeId);


    }
    @Test
    void testGuestGetPurchasePolicy_Failure() throws Exception {
        // --- Step 1: Owner enters and registers ---
        when(real.mockUserRepo.generateGuest()).thenReturn(1);
        when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store ---
        int storeId = 100;
        when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");

        // --- Step 3: Guest enters (no registration) ---
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // TODO: need to implement this

        String result = real.testGuest_GetPurchasePolicy("guest-token", storeId);


    }
}