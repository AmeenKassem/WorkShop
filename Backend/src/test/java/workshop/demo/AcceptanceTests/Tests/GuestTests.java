package workshop.demo.AcceptanceTests.Tests;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.AcceptanceTests.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;

@SpringBootTest
public class GuestTests extends AcceptanceTests {

    Real real = new Real();
    String token_guest = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ7XCJ1c2VyTmFtZVwiOlwiYWRtaW5Vc2VyMlwiLFwiaWRcIjoyfSIsImlhdCI6MTc0NjgyNDkyNiwiZXhwIjoxNzQ2ODI4NTI2fQ.zSFqaapc2ANZpwbewtxqk2hedPH50VrdFYS8Dj58eTw";
    Logger logger;

    public GuestTests() throws Exception {
    }

    @BeforeEach
    void setup() throws Exception {


        // when (real.mockUserRepo.getRegisteredUserByName("admin")).thenReturn(new Registered(1,"admin","Admin123",23));
       // when (real.mockUserRepo.getRegisteredUserByName("admin")).thenReturn(new Registered(1,"admin","Admin123",23));
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);

        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        String guestToken = real.userService.generateGuest();
        assertEquals(adminGuestToken, guestToken);

        boolean registered = real.userService.register(guestToken, "admin", "adminPass", 18);
        assertTrue(registered);

        String userToken = real.userService.login(guestToken, "admin", "adminPass");
        assertEquals(adminUserToken, userToken);

        testSystem_InitMarket(adminUserToken);

        int ownerId = 10;
        String GuestToken = "guest-token";
        String UserToken = "user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(GuestToken);
        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);

        when(real.mockAuthRepo.validToken(GuestToken)).thenReturn(true);

        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(UserToken);
        when(real.mockAuthRepo.validToken(UserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(UserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(UserToken)).thenReturn("owner");

        String guestToken1 = real.userService.generateGuest();
        assertEquals(GuestToken, guestToken1);

        boolean registered1 = real.userService.register(guestToken1, "owner", "owner", 18);
        assertTrue(registered1);

        String userToken1 = real.userService.login(guestToken1, "owner", "owner");
        assertEquals(UserToken, userToken1);

        int storeId = 1;

        when(real.mockAuthRepo.validToken(userToken1)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken1)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);

        int createdStoreId = real.storeService.addStoreToSystem(userToken1, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

        int productid = 111;
        String[] keywords = {"Laptop", "Lap", "top"};

        when(real.mockStockRepo.addProduct("Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords)).thenReturn(productid);
        assertTrue(real.stockService.addProduct(userToken1, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords) == productid);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productid)).thenReturn(new Product("Laptop", productid, Category.ELECTRONICS, "Gaming Laptop", keywords));
        when(real.mockStockRepo.addItem(storeId, productid, 2, 2000, Category.ELECTRONICS)).thenReturn(new item(productid, 2, 2000, Category.ELECTRONICS));

        assertEquals(productid, real.stockService.addItem(storeId, userToken1, productid, 2, 2000, Category.ELECTRONICS));

        when(real.mockUserRepo.generateGuest()).thenReturn(1);
        when(real.mockAuthRepo.generateGuestToken(1)).thenReturn(token_guest);
        String token = real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");

    }

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
        when(real.mockUserRepo.isOnline(2)).thenReturn(true);
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");

        String token = real.userService.generateGuest();
        assertNotNull(token);

        when(real.mockAuthRepo.generateGuestToken(2)).thenThrow(new RuntimeException("already entered"));
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

        assertTrue(real.userService.destroyGuest(token));

    }

    @Test
    void testGuestExit_Failure_InvalidToken() throws Exception {
        int guestId = 2;
        String guestToken = "token_guest";

        when(real.mockUserRepo.generateGuest()).thenReturn(guestId);
        when(real.mockAuthRepo.generateGuestToken(guestId)).thenReturn(guestToken);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(false);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        String token = real.userService.generateGuest();
        assertEquals(guestToken, token);

        assertEquals(true, real.userService.destroyGuest(token));
    }

    @Test
    void testGuestRegister_Success() throws Exception {
        when(real.mockUserRepo.generateGuest()).thenReturn(2);
        when(real.mockAuthRepo.generateGuestToken(2)).thenReturn(token_guest);
        String token = real.userService.generateGuest();
        assertTrue(token.equals(token_guest));
        assertFalse(token.isEmpty(), "Expected a non-empty guest token");

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);

        assertTrue(real.userService.register(token, "bashar", "finish", 18));

    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {
        String token = "guest-token-1";

        when(real.userService.register(token, "bashar", "finish", 18))
                .thenThrow(new UIException("Username already exists", ErrorCodes.USERNAME_USED));

        // Act + Assert
        UIException exception = assertThrows(UIException.class, () -> {
            real.userService.register(token, "bashar", "finish", 18);
        });

        assertEquals(ErrorCodes.USERNAME_USED, exception.getNumber());
    }

    @Test
    void testGuestViewEmptyStore() throws Exception {
        int storeId = 100;  // use storeId from setup
        String guestToken = "guest-token";  // use guestToken from setup

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true); // ensure guest token is valid
        Exception exception = assertThrows(Exception.class, () -> {
            real.stockService.getProductsInStore(storeId); // Example
        });

    }

    @Test
    void testGuestGetProductInfo() throws Exception {
        int storeId = 100;
        int productId = 111;
        String guestToken = token_guest;

        ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.ELECTRONICS, "Smart device");

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);

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

        when(real.mockAuthRepo.validToken(guestTokenB)).thenReturn(true);

        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(null);

        UIException exception = assertThrows(UIException.class, () -> {
            real.testGuest_GetProductInfo(guestTokenB, productId);
        });

        assertEquals("Product not found.", exception.getMessage());
    }

    @Test
    void testGuestAddProductToCart_Success() throws Exception {
        int guestId = 1;
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");
        ItemCartDTO itemCartDTO = new ItemCartDTO(storeId,productId,5,1500,"Laptop", "TestStore",Category.ELECTRONICS);

        doNothing().when(real.mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);

        assertDoesNotThrow(() -> {
            real.userService.addToUserCart(guestToken, itemStoreDTO, 2);
        });
    }

    @Test
    void testGuestAddProductToCart_InvalidToken() throws Exception {
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));

        UIException exception = assertThrows(UIException.class, () -> {
            real.testGuest_AddProductToCart(guestToken, itemStoreDTO, 2);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());

    }

    @Test
    void testGuestAddProductToCart_GuestNotFound() throws Exception {
        int guestId = 999; // non-existent ID
        int productId = 100;
        String guestToken = "guest-token-1";
        int storeId = 10;
        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");
        ItemCartDTO itemCartDTO = new ItemCartDTO(storeId,productId,5,1500,"Laptop", "TestStore",Category.ELECTRONICS);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        doThrow(new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND))
                .when(real.mockUserRepo).addItemToGeustCart(eq(guestId), any(ItemCartDTO.class));

        UIException exception = assertThrows(UIException.class, ()
                -> real.userService.addToUserCart(guestToken, itemStoreDTO, 2)
        );

        assertEquals("Guest not found: " + guestId, exception.getMessage());
        assertEquals(ErrorCodes.GUEST_NOT_FOUND, exception.getNumber());
    }

    @Test
    void testGuestAddProductToCart_ZeroQuantity() throws Exception {
        int guestId = 1;
        int storeId = 10;
        int productId = 100;
        String guestToken = "guest-token-1";

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, storeId, "Laptop","TestStore");

        doThrow(new UIException("Cannot add product with zero quantity", 1025))
                .when(real.mockUserRepo).addItemToGeustCart(eq(guestId), any(ItemCartDTO.class));

        Exception exception = assertThrows(UIException.class, () -> {
            real.userService.addToUserCart(guestToken, itemStoreDTO, 3);
        });

        assertEquals("Cannot add product with zero quantity", exception.getMessage());
    }

    @Test
    void testGuestBuyCart_Success() throws Exception {
        int guestId = 20;
        int storeId = 1;
        int productId = 200;
        String guestToken = "guest-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(guestId);
        when(real.mockAuthRepo.generateGuestToken(guestId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        String generatedGuestToken = real.userService.generateGuest();
        assertEquals(guestToken, generatedGuestToken);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");;
        ItemCartDTO itemCartDTO = new ItemCartDTO(storeId,productId,5,1500,"Laptop", "TestStore",Category.ELECTRONICS);
        CartItem CartItem=new CartItem(itemCartDTO);
        doNothing().when(real.mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);
        real.userService.addToUserCart(guestToken, itemStoreDTO, 3);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(CartItem));

        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(CartItem));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        List<ReceiptProduct> receiptProducts = List.of(new ReceiptProduct("ProductName", "TestStore", 2, 100,productId,Category.ELECTRONICS));
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true),eq("TestStore"))).thenReturn(receiptProducts);
        when(real.mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
        when(real.mockPay.processPayment(PaymentDetails.testPayment(), 200)).thenReturn(true);
        when(real.mockSupply.processSupply(any())).thenReturn(true);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(guestId), any(), eq("TestStore"));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        //when(real.mockPurchaseRepo.)
        when(real.mockStoreRepo.findStoreByID(1)).thenReturn(new Store(1, "TestStore", "ELECTRONICS"));
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice() * 2);

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

        Exception ex = assertThrows(UIException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

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

        Exception ex = assertThrows(UIException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

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
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(false);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

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
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        when(real.mockStoreRepo.getStoreNameById(100)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true),eq("TestStore")))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
        when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));

        doThrow(new RuntimeException("Payment failed")).when(real.mockPay)
                .processPayment(any(), eq(100.0));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(RuntimeException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Payment failed", ex.getMessage());
    }

    @Test
    void testGuestBuyCart_SupplyFails() throws Exception {
        int guestId = 20;
        int storeId = 100;
        String guestToken = "guest-token";
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true),eq("TestStore")))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(new Store(storeId, "TestStore", "ELECTRONICS"));

        when(real.mockPay.processPayment(any(), eq(100.0))).thenReturn(true);
        doThrow(new RuntimeException("Supply failed")).when(real.mockSupply).processSupply(any());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(RuntimeException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Supply failed", ex.getMessage());
    }

    @Test
    void testGuestBuyCart_StoreNotFound() throws Exception {
        int guestId = 20;
        int storeId = 123;
        String guestToken = "guest-token";
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("UnknownStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true),eq("UnknownStore")))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);

        // Simulate exception for store not found
        when(real.mockStoreRepo.findStoreByID(storeId)).thenThrow(new RuntimeException("Store not found"));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(RuntimeException.class, ()
                -> real.purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));

        assertEquals("Store not found", ex.getMessage());
    }

    @Test
    void testGuestSearchProductInStore_Success() throws Exception {
        int guestId = 1;
        int storeId = 100;
        int productId = 111; // Matches the setup
        String guestToken = token_guest;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // product name filter
                null, // category filter
                null, // keyword filter
                storeId, // store ID to filter
                0, 0, // no price range
                0, 0 // no rating range
        );

        ProductDTO[] mockProducts = {
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO mockItem = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 100, "Laptop","TestStore");

        ItemStoreDTO[] mockItems = {mockItem};
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.search(criteria)).thenReturn(mockItems);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);


        assertEquals(1, result.length);

        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());
        assertEquals(Category.ELECTRONICS, result[0].getCategory());
    }

    @Test
    void testGuestSearchProducts_Success() throws Exception {
        int guestId = 1;
        int storeId = 100;
        int productId = 200;
        String guestToken = token_guest;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        String[] keywords = {"Laptop", "Lap", "top"};

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.ELECTRONICS, keywords[0], 100, 0, 5000, 0, 5);

        ProductDTO[] matchedProducts = new ProductDTO[]{
                new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO[] matchedItems = new ItemStoreDTO[]{
                new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 100, "Laptop","TestStore")};
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        when(real.mockStockRepo.search(criteria)).thenReturn(matchedItems);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(productId, result[0].getProductId());
        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());

    }

    @Test
    void testSearchProducts_InvalidToken() throws Exception {
        String guestToken = token_guest;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, "Laptop", 100,
                0, 5000,
                0, 5
        );

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any());

        UIException exception = assertThrows(UIException.class, () -> {
            real.stockService.searchProducts(guestToken, criteria);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testSearchProducts_NoMatches() throws Exception {
        String guestToken = token_guest;

        String[] keywords = {"Laptop", "Lap", "top"};

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.ELECTRONICS, keywords[0], 100, 0, 5000, 0, 5);

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNull(result);
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

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNull(result);
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

        ItemStoreDTO mockItem = new ItemStoreDTO(111, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");// quantity = 0

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(1);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNull(result);
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

        ItemStoreDTO mockItem = new ItemStoreDTO(111, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(1);

        ItemStoreDTO[] result = real.stockService.searchProducts(guestToken, criteria);

        assertNull(result);
    }

    @Test

    void testGuestModifyCartAddQToBuy_Success() throws Exception {
        int guestId = 1;
        String guestToken = "guest-token";
        int productId = 100;
        int newQuantity = 3;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        doNothing().when(real.mockUserRepo).ModifyCartAddQToBuy(guestId, productId, newQuantity);

        assertDoesNotThrow(()
                -> real.userService.ModifyCartAddQToBuy(guestToken, productId, newQuantity)
        );

        verify(real.mockUserRepo).ModifyCartAddQToBuy(guestId, productId, newQuantity);
    }

    @Test
    void testGuestModifyCartAddQToBuy_InvalidToken() throws Exception {
        String guestToken = "invalid-token";
        int productId = 100;

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));

        UIException ex = assertThrows(UIException.class, ()
                -> real.userService.ModifyCartAddQToBuy(guestToken, productId, 2)
        );

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testGuestModifyCartAddQToBuy_GuestNotFound() throws Exception {
        int guestId = 999;
        String guestToken = "guest-token-999";
        int productId = 100;
        int newQuantity = 3;

        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);

        doThrow(new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND))
                .when(real.mockUserRepo).ModifyCartAddQToBuy(eq(guestId), eq(productId), eq(newQuantity));

        UIException ex = assertThrows(UIException.class, ()
                -> real.userService.ModifyCartAddQToBuy(guestToken, productId, newQuantity)
        );

        assertEquals("Guest not found: " + guestId, ex.getMessage());
        assertEquals(ErrorCodes.GUEST_NOT_FOUND, ex.getNumber());
    }

}