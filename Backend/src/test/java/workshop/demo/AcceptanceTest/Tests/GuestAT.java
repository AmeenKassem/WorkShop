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
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GuestAT extends AcceptanceTests {
    Real real = new Real();
    String token_guest = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwiYWRtaW5Vc2VyMlwiLFwiaWRcIjoyfSIsImlhdCI6MTc0NjgyNDkyNiwiZXhwIjoxNzQ2ODI4NTI2fQ.zSFqaapc2ANZpwbewtxqk2hedPH50VrdFYS8Dj58eTw";
    Logger logger;

    @BeforeEach
    void setup() throws Exception {
        // ======================= ADMIN USER SETUP =======================
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";

        // --- Guest creation ---
        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);

        // --- Registration ---
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);

        // --- Login ---
        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        // --- Register admin and login ---
        String guestToken = real.userService.generateGuest();
        assertEquals(adminGuestToken, guestToken);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        boolean registered = real.userService.register(guestToken, "admin", "adminPass", 18);
        assertTrue(registered);

        String userToken = real.userService.login(guestToken, "admin", "adminPass");
        assertEquals(adminUserToken, userToken);

        // --- Initialize market with admin ---
        testSystem_InitMarket(adminUserToken);

        // ======================= OWNER USER SETUP =======================
        int ownerId = 10;
        String GuestToken = "guest-token";
        String UserToken = "user-token";

        // --- Guest creation ---
        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(GuestToken);
        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);

        // --- Registration ---
        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);

        // --- Login ---
        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(UserToken);
        when(real.mockAuthRepo.validToken(UserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(UserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(UserToken)).thenReturn("owner");

        // --- Register owner and login ---
        String guestToken1 = real.userService.generateGuest();
        assertEquals(GuestToken, guestToken1);

        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        boolean registered1 = real.userService.register(guestToken1, "owner", "owner", 18);
        assertTrue(registered1);

        String userToken1 = real.userService.login(guestToken1, "owner", "owner");
        assertEquals(UserToken, userToken1);

        // ======================= STORE CREATION =======================
        int storeId = 100;

        when(real.mockAuthRepo.validToken(userToken1)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken1)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);

        int createdStoreId = real.storeService.addStoreToSystem(userToken1, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

        // ======================= PRODUCT & ITEM ADDITION =======================
        int productid = 111;
        String[] keywords = {"Laptop", "Lap", "top"};

        when(real.mockStockRepo.addProduct("Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords)).thenReturn(productid);
        assertTrue(real.stockService.addProduct(userToken1, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords) == productid);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productid)).thenReturn(new Product("Laptop", productid, Category.ELECTRONICS, "Gaming Laptop", keywords));
        when(real.mockStockRepo.addItem(storeId, productid, 2, 2000, Category.ELECTRONICS)).thenReturn(new item(productid, 2, 2000, Category.ELECTRONICS));

        assertEquals(productid, real.stockService.addItem(storeId, userToken1, productid, 2, 2000, Category.ELECTRONICS));

        // ======================= SECOND GUEST SETUP =======================
        when(real.mockUserRepo.generateGuest()).thenReturn(1);
        when(real.mockAuthRepo.generateGuestToken(1)).thenReturn(token_guest);
        String token = real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");

    }


    //NOTE:ENTER+EXIT+REGISTER FINISH
    @Test
    void testGuestEnter_Success() throws Exception {
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("token_guest");
        String token = real.userService.generateGuest();

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

        String token = real.userService.generateGuest();

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.destroyGuest(token));


    }

    @Test
    void testGuestExit_Failure_InvalidToken() throws Exception {
        // Arrange
        int guestId = 2;
        String guestToken = "token_guest";

        // Guest was generated normally
        when(real.mockUserRepo.generateGuest()).thenReturn(guestId);
        when(real.mockAuthRepo.generateGuestToken(guestId)).thenReturn(guestToken);

        // Token is valid
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        // Simulate failure: user is not a guest

        // Act: generate the guest
        String token = real.userService.generateGuest();
        assertEquals(guestToken, token);

        // Assert: destroyGuest should throw

        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN


        assertEquals(true, real.userService.destroyGuest(token));
    }


    @Test
    void testGuestRegister_Success() throws Exception {
        //enter
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn(token_guest);
        String token = real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");
        //check
        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        assertTrue(real.userService.register(token, "bashar", "finish", 18));

    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {
        String token = "guest-token-1";
        //real.userService.register(token, "bashar", "finish", 18);

        // Mock: username already exists
        //CHANGE THE FUNCTION FROM VOID TO BOOLEAN

        when(real.userService.register(token, "bashar", "finish", 18))
                .thenThrow(new UIException("Username already exists", ErrorCodes.USERNAME_USED));

        // Act + Assert
        UIException exception = assertThrows(UIException.class, () -> {
            real.userService.register(token, "bashar", "finish", 18);
        });

        // Validate exception message and error code
        //assertEquals("Username already exists", exception.getMessage());
        assertEquals(ErrorCodes.USERNAME_USED, exception.getNumber());
    }


//    //NOTE:GET STORE PRODUCT FINISH
//    @Test
//    void testGuestGetStoreProducts() throws Exception {
//        real.storeService.
//    }

    @Test
    void testGuestViewEmptyStore() throws Exception {
        int storeId = 100;  // use storeId from setup
        String guestToken = "guest-token";  // use guestToken from setup

        // ===== MOCK: store has no products =====
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(List.of());

        // ===== GUEST requests products =====
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true); // ensure guest token is valid

        List<ItemStoreDTO> products = real.stockService.getProductsInStore(storeId);

        // ===== ASSERT =====
        assertNotNull(products, "Products list should not be null");
        assertTrue(products.isEmpty());
    }

    @Test
    void testGuestGetProductInfo() throws Exception {
        int storeId = 100;
        int productId = 111; // Based on setup
        String guestToken = token_guest; // From setup

        // ===== MOCK PRODUCT INFO RESPONSE =====
        ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.ELECTRONICS, "Smart device");

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);

        // ===== GUEST REQUESTS PRODUCT INFO =====
        String info = real.testGuest_GetProductInfo(guestToken, productId);

        // ===== ASSERTIONS =====
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
        // Arrange
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(
                productId,
                10, // quantity
                100, // price
                Category.ELECTRONICS,
                5, // discount
                storeId
        );

        // Simulate invalid token
        //when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));

        // Act
        UIException exception = assertThrows(UIException.class, () -> {
            real.testGuest_AddProductToCart(guestToken, itemStoreDTO);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());

        // Assert

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
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true))).thenReturn(receiptProducts);
        when(real.mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
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
        assertEquals(200.0, receipts[0].getProductsList().size() * receipts[0].getProductsList().getFirst().getPrice() * 2);

        // --- Step 8: Verify important calls happened ---
        verify(real.mockUserRepo).getUserCart(guestId);
        verify(real.mockStockRepo).checkAvailability(any());
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
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(false);

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

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true)))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);

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
        int productId = 111; // Matches the setup
        String guestToken = token_guest;

        // --- Step 1: Mock valid guest token and user ID ---
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        // --- Step 2: Prepare search criteria for the store ---
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null,             // product name filter
                null,             // category filter
                null,             // keyword filter
                storeId,          // store ID to filter
                0, 0,             // no price range
                0, 0              // no rating range
        );

        // --- Step 3: Mock matching products and store items ---
        ProductDTO[] mockProducts = {
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO mockItem = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 4, storeId);
//        mockItem.setProductName("Laptop");
//        mockItem.setStoreName("TestStore");

        ItemStoreDTO[] mockItems = {mockItem};

        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(mockProducts);
        when(real.mockStockRepo.getMatchesItems(criteria, mockProducts)).thenReturn(mockItems);

        // --- Step 4: Call the real service method ---
        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        // --- Step 5: Assertions ---
        assertNotNull(result);
        assertEquals(1, result.length);
//        assertEquals("Laptop", result[0].getProductName());
//        assertEquals("Gaming Laptop", result[0].getDescription()); // If set
//        assertEquals("TestStore", result[0].getStoreName());
        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());
        //assertEquals(5, result[0].getQuantity());
        assertEquals(Category.ELECTRONICS, result[0].getCategory());
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
        String[] keywords = {"Laptop", "Lap", "top"};

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.ELECTRONICS, keywords[0], 100, 0, 5000, 0, 5);


        // --- Step 3: Mock stockRepo response ---
        ProductDTO[] matchedProducts = new ProductDTO[]{
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(matchedProducts);

        // --- Step 4: Mock storeRepo response ---
        ItemStoreDTO[] matchedItems = new ItemStoreDTO[]{
                new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 10, storeId)
        };
        when(real.mockStockRepo.getMatchesItems(criteria, matchedProducts)).thenReturn(matchedItems);

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
        verify(real.mockStockRepo).getMatchesItems(criteria, matchedProducts);
    }

    @Test
    void testSearchProducts_InvalidToken() throws Exception {
        String guestToken = token_guest;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, "Laptop", 100,
                0, 5000,
                0, 5
        );

        // 1. Throw on auth check
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any());

        // 2. Prevent accidental NPE if auth is bypassed
        when(real.mockStockRepo.getMatchesProducts(any())).thenReturn(new ProductDTO[0]);
        when(real.mockStockRepo.getMatchesItems(any(), any())).thenReturn(new ItemStoreDTO[0]);

        // 3. Run the test
        UIException exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProducts(guestToken, criteria);
        });

        // 4. Verify
        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testSearchProducts_NoMatches() throws Exception {
        String guestToken = token_guest;

        String[] keywords = {"Laptop", "Lap", "top"};

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.ELECTRONICS, keywords[0], 100, 0, 5000, 0, 5);


        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(new ProductDTO[0]);
        when(real.mockStockRepo.getMatchesItems(criteria, new ProductDTO[0])).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchProducts_ProductExists_NotInStore() throws Exception {
        String guestToken = token_guest;
        int storeId = 100;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, storeId,
                0, 0, 0, 0
        );

        ProductDTO[] foundProducts = {
                new ProductDTO(111, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(1);
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(foundProducts);
        when(real.mockStockRepo.getMatchesItems(criteria, foundProducts)).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product not sold in this store
    }

    @Test
    void testSearchProducts_ProductInStore_QuantityZero() throws Exception {
        String guestToken = token_guest;
        int storeId = 100;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, storeId,
                0, 0, 0, 0
        );

        ProductDTO[] foundProducts = {
                new ProductDTO(111, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO mockItem = new ItemStoreDTO(111, 0, 2000, Category.ELECTRONICS, 3, storeId); // quantity = 0
//        mockItem.setProductName("Laptop");
//        mockItem.setStoreName("TestStore");

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(1);
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(foundProducts);
        when(real.mockStockRepo.getMatchesItems(criteria, foundProducts))
                .thenReturn(new ItemStoreDTO[0]);  // Simulates filtering out by logic in repo or domain

        // Simulate that the filtering logic discards items with 0 quantity
        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product exists, but unavailable
    }

    @Test
    void testSearchProducts_PriceOutOfRange() throws Exception {
        String guestToken = token_guest;
        int storeId = 100;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, null, storeId,
                5000, 10000, 0, 0 // Price filter too high
        );

        ProductDTO[] foundProducts = {
                new ProductDTO(111, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO mockItem = new ItemStoreDTO(111, 5, 2000, Category.ELECTRONICS, 4, storeId);
//        mockItem.setProductName("Laptop");
//        mockItem.setStoreName("TestStore");

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(1);
        when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(foundProducts);
        when(real.mockStockRepo.getMatchesItems(criteria, foundProducts)).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product doesn't match price filter
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
        when(real.mockUserRepo.registerUser("owner1", "pass", 18)).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass", 18);

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
        when(real.mockUserRepo.registerUser("owner1", "pass", 18)).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass", 18);

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