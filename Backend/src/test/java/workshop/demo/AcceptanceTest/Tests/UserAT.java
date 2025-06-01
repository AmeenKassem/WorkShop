package workshop.demo.AcceptanceTest.Tests;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
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
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;

public class UserAT extends AcceptanceTests {

    Real real = new Real();
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImp.class);

    public UserAT() throws Exception {
    }

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

        assertTrue(real.userService.register(ownerGuest, "owner", "owner", 18));
        String ownerToken = real.userService.login(ownerGuest, "owner", "owner");
        assertEquals(ownerUserToken, ownerToken);

        int storeId = 100;
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);

        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

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

        assertTrue(real.userService.register(guestToken, "user2", "pass2", 18));
        String loginToken = real.userService.login(guestToken, "user2", "pass2");
        assertEquals(userToken, loginToken);

        int productId = 111;
        String[] keywords = {"Laptop", "Gaming", "HighPerformance"};
        when(real.mockStockRepo.addProduct("Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords))
                .thenReturn(productId);

        int returnedProductId = real.stockService.addProduct(ownerUserToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);
        assertEquals(productId, returnedProductId);

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

        int userId = 21;
        String guestToken = "guest-token-21";
        String userToken = "user-token-21";

        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(guestToken);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);

        when(real.mockUserRepo.login("loginUser", "loginPass")).thenReturn(userId);
        when(real.mockAuthRepo.generateUserToken(userId, "loginUser")).thenReturn(userToken);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(userToken)).thenReturn("loginUser");

        real.userService.generateGuest();

        assertTrue(real.userService.register(guestToken, "loginUser", "loginPass", 18));
        String actualToken = real.userService.login(guestToken, "loginUser", "loginPass");

        assertEquals(userToken, actualToken);

        String invalidToken = "invalid-token";

        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        doCallRealMethod()
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(invalidToken), any());

        UIException ex = assertThrows(UIException.class, ()
                -> real.userService.logoutUser(invalidToken)
        );

        assertEquals("Invalid token!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testUserLogin_Failure_InvalidToken() throws UIException {
        String invalidToken = "invalid-token";

        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        doCallRealMethod()
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(invalidToken), any());

        UIException exception = assertThrows(UIException.class, ()
                -> real.userService.login(invalidToken, "userX", "passX")
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

        UIException exception = assertThrows(UIException.class, ()
                -> real.userService.login(generatedToken, "ghost", "nopass")
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testUserLogout_Failure_UserNotFound() throws Exception {
        String token = "bad-token";
        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenReturn("ghost");
        when(real.mockUserRepo.logoutUser("ghost")).thenThrow(new UIException("User not found: ghost", 101));

        UIException exception = assertThrows(UIException.class, ()
                -> real.userService.logoutUser(token)
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

        Exception ex = assertThrows(RuntimeException.class, ()
                -> real.userService.logoutUser(token)
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

        assertEquals(real.userService.setAdmin(token, adminKey, userIdToPromote), false);
    }

    @Test
    void testUser_CheckPurchaseHistory_Success() throws Exception {
        int userId = 20;
        String token = "user-token-2";
        int storeId = 100;
        String storeName = "TestStore";
        String date = "2025-05-10";
        List<ReceiptProduct> products = List.of(
                new ReceiptProduct("Keyboard", "TestStore", 1, 100,1,Category.ELECTRONICS),
                new ReceiptProduct("Mouse", "TestStore", 1, 100,2,Category.ELECTRONICS)
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

        assertTrue(ex.getMessage().contains("is not registered to the system!"));
    }

    @Test
    void testUser_CheckPurchaseHistory_Failure_NoReceipts() throws Exception {
        String token = "user-token-2";

        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
        when(real.mockAuthRepo.getUserName(token)).thenReturn("user2");
        when(real.mockAuthRepo.getUserId("user2")).thenReturn(20);
        when(real.mockUserRepo.isRegistered(20)).thenReturn(true);

        when(real.mockOrderRepo.getReceiptDTOsByUser(20))
                .thenThrow(new UIException("User has no receipts.", ErrorCodes.RECEIPT_NOT_FOUND));

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
        int productId = 111;

        ItemStoreDTO[] mockProducts = new ItemStoreDTO[]{
            new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore")};
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(mockProducts);

        ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);
        assertNotNull(products);

    }

    @Test
    void testUserViewEmptyStore() throws Exception {
        int storeId = 100;
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(new ItemStoreDTO[0]);

        ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);

        assertNotNull(products, "Products list should not be null");
    }

    @Test
    void testUserViewInvalidStore() throws Exception {
        int invalidStoreId = 999;
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        when(real.mockStockRepo.getProductsInStore(invalidStoreId))
                .thenThrow(new RuntimeException("Store not found"));

        ItemStoreDTO[] products = null;
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
                .thenReturn(new ItemStoreDTO[]{
            new ItemStoreDTO(1, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore")
        });

        ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);

        assertNotNull(products);
    }

    @Test
    void testUserGetProductInfo() throws Exception {
        int userId = 20;
        int storeId = 100;
        int productId = 111;
        String userToken = "user-token-2";

        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.addItem(storeId, productId, 5, 200, Category.ELECTRONICS))
                .thenReturn(new item(productId, 5, 200, Category.ELECTRONICS));

        int addItemResult = real.stockService.addItem(storeId, userToken, productId, 5, 200, Category.ELECTRONICS);
        assertEquals(addItemResult, productId);

        ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.ELECTRONICS, "Smart device");
        when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);

        String info = real.testGuest_GetProductInfo(userToken, productId);

        assertNotNull(info);
        assertTrue(info.contains("Phone"), "Expected product name in info");
        assertTrue(info.contains("Smart device"), "Expected product description in info");
        assertTrue(info.contains("ELECTRONICS"), "Expected product category in info");
    }

    @Test
    void testUserGetProductInfo_ProductNotFound() throws Exception {
        int productId = 999;
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

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");
        ItemCartDTO itemCartDTO = new ItemCartDTO(4,productId,5,1500,"Laptop","TestStore",Category.ELECTRONICS);
        //int storeId, int productId, int quantity, int price, String name, String storeName,Category category
        doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);

        assertDoesNotThrow(() -> {
            real.testGuest_AddProductToCart(userToken, itemStoreDTO, 3);
        });
    }

    @Test
    void testUserAddProductToCart_InvalidToken() throws Exception {
        String userToken = "user-token-2";
        int storeId = 10;
        int productId = 100;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);

        doCallRealMethod()
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq("invalidToken"), any());

        UIException ex = assertThrows(UIException.class, () -> {
            real.testGuest_AddProductToCart("invalidToken", itemStoreDTO, 3);
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

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");

        doThrow(new UIException("Cannot add product with zero quantity", 1025))
                .when(real.mockUserRepo).addItemToGeustCart(eq(userId), any(ItemCartDTO.class));

        UIException exception = assertThrows(UIException.class, () -> {
            real.userService.addToUserCart(userToken, itemStoreDTO, 3);
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

        ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 4, "Laptop","TestStore");
        ItemCartDTO itemCartDTO = new ItemCartDTO(4,productId,5,1500,"Laptop","TestStore",Category.ELECTRONICS);
        //int storeId, int productId, int quantity, int price, String name, String storeName,Category category
        CartItem CartItem=new CartItem(itemCartDTO);
        doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);
        real.userService.addToUserCart(userToken, itemStoreDTO, 3);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
        when(mockCart.getAllCart()).thenReturn(List.of(CartItem));

        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(CartItem));

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);

        List<ReceiptProduct> receiptProducts = List.of(
                new ReceiptProduct("ProductName", "TestStore", 2, 100,productId,Category.ELECTRONICS));
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(false), eq("TestStore")))
                .thenReturn(receiptProducts);
        when(real.mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
        when(real.mockPay.processPayment(PaymentDetails.testPayment(), 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(any())).thenReturn(true);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());

        verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), anyList(), eq(false), eq("TestStore"));
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

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

        UIException ex = assertThrows(UIException.class, ()
                -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));

        assertEquals("Shopping cart is empty or not found", ex.getMessage());
    }

    @Test
    void testUserBuyCart_ProductNotAvailable() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ItemCartDTO mockItem = Mockito.mock(ItemCartDTO.class);
        List<ItemCartDTO> items = List.of(mockItem);

        CartItem mockItem1 = Mockito.mock(CartItem.class);
        List<CartItem> items1 = List.of(mockItem1);


        ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(items1);

        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);

        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(mockCart.getAllCart()).thenReturn(items1);
        when(mockCart.getBaskets()).thenReturn(baskets);

        when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(false);

        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), eq(items1), eq(false), eq("TestStore")))
                .thenThrow(new UIException("Not all items are available for user purchase", 1006));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(Exception.class, ()
                -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));


        verify(real.mockUserRepo).getUserCart(userId);
        //verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), eq(items1), eq(false), eq("TestStore"));
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
        when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        baskets.put(storeId, mockBasket);
        when(mockCart.getBaskets()).thenReturn(baskets);
        when(mockBasket.getStoreId()).thenReturn(storeId);
        when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("TestStore")))
                .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
        when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);

        doThrow(new RuntimeException("Payment failed")).when(real.mockPay)
                .processPayment(any(), eq(100.0));
        when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        RuntimeException ex = assertThrows(RuntimeException.class, ()
                -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));


    }

    @Test
    void testUserSearchProductInStore_Success() throws Exception {
        int userId = 20;
        String userToken = "user-token-2";
        int storeId = 100;
        int productId = 111;

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.ELECTRONICS, "Lap", storeId,
                0, 0, 0, 0
        );

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        ProductDTO[] products = {
            new ProductDTO(productId, "Laptop", Category.ELECTRONICS, "Gaming Laptop")
        };

        ItemStoreDTO[] items = {
            new ItemStoreDTO(productId, 5, 1500, Category.ELECTRONICS, 0, 100, "Laptop","TestStore")};
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        when(real.mockStockRepo.search(criteria,"TestStore")).thenReturn(items);

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(productId, result[0].getProductId());
        assertEquals(1500, result[0].getPrice());
        assertEquals(storeId, result[0].getStoreId());

    }

    @Test
    void testUserSearchProducts_InvalidToken() throws Exception {
        String userToken = "user-token-2";

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, Category.ELECTRONICS, null,
                0, 0, 0, 0, 0
        );

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

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // productNameFilter
                Category.ELECTRONICS, // categoryFilter
                null, // keywordFilter
                0, // storeId (not restricted)
                0, // minPrice
                0, // maxPrice
                0, // minStoreRating
                0 // maxStoreRating
        );

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNull(result);
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

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNull(result);
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

        ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);

        assertNull(result);
    }

    @Test
    void testUserGetPurchasePolicy_Success() throws Exception {
//
    }

    @Test
    void testUserGetPurchasePolicy_Failure() throws Exception {
//
    }

    @Test
    void testRankStore_Success() throws Exception {
        String validToken = "user-token";
        String invalidToken = "guest-token";
        int userId = 10;
        int storeId = 100;
        int productId = 200;
        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);

        doNothing().when(real.mockStoreRepo).rankStore(storeId, 4);

        real.storeService.rankStore(validToken, storeId, 4);
        verify(real.mockStoreRepo).rankStore(storeId, 4);
    }

    @Test
    void testRankStore_InvalidToken() throws Exception {
        String invalidToken = "guest-token";
        int storeId = 100;

        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(invalidToken), any(Logger.class));

        UIException ex = assertThrows(UIException.class, ()
                -> real.storeService.rankStore(invalidToken, storeId, 4)
        );

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testRankStore_InvalidStore() throws Exception {
        String validToken = "user-token";
        String invalidToken = "guest-token";
        int userId = 10;
        int storeId = 100;
        int productId = 200;
        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        doThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND))
                .when(real.mockStoreRepo).checkStoreExistance(storeId);

        UIException ex = assertThrows(UIException.class, ()
                -> real.storeService.rankStore(validToken, storeId, 5)
        );
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testRankProduct_Success() throws Exception {
        String validToken = "user-token";
        int userId = 10;
        int storeId = 100;
        int productId = 200;

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        doNothing().when(real.mockStockRepo).rankProduct(storeId, productId, 3);

        real.stockService.rankProduct(storeId, validToken, productId, 3);

        verify(real.mockStockRepo).rankProduct(storeId, productId, 3);
    }

    @Test
    void testRankProduct_InvalidToken() throws Exception {
        String invalidToken = "guest-token";
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(invalidToken), any(Logger.class));

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.rankProduct(storeId, invalidToken, productId, 3)
        );

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
    }

    @Test
    void testRankProduct_InvalidStore() throws Exception {
        String validToken = "user-token";
        int userId = 10;
        int storeId = 100;
        int productId = 200;

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        doThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND))
                .when(real.mockStoreRepo).checkStoreExistance(storeId);

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.rankProduct(storeId, validToken, productId, 3)
        );

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testRankProduct_InvalidProduct() throws Exception {
        String validToken = "user-token";
        int userId = 10;
        int storeId = 100;
        int invalidProductId = 999;

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
        when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        doThrow(new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND))
                .when(real.mockStockRepo).rankProduct(storeId, invalidProductId, 3);

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.rankProduct(storeId, validToken, invalidProductId, 3)
        );

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testUser_AddReviewToStore_Success() throws Exception {
        String token = "user-token-2";
        int storeId = 100;
        int userId = 20;
        String username = "user2";
        String review = "Very fast delivery!";

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        doNothing().when(real.mockReviewRepo).AddReviewToStore(storeId, userId, username, review);

        boolean result = real.reviewService.AddReviewToStore(token, storeId, review);
        assertTrue(result);

        verify(real.mockReviewRepo).AddReviewToStore(storeId, userId, username, review);
    }

    @Test
    void testUser_AddReviewToProduct_Success() throws Exception {
        String token = "user-token-2";
        int storeId = 100;
        int productId = 111;
        int userId = 20;
        String username = "user2";
        String review = "Amazing laptop, very fast!";
        ItemStoreDTO[] itemsInStore = {
            new ItemStoreDTO(productId, 10, 1500, Category.ELECTRONICS, 4, storeId, "Laptop","TestStore")
        };

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(itemsInStore);
        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(token)).thenReturn(username);
        doNothing().when(real.mockReviewRepo).AddReviewToProduct(storeId, productId, userId, username, review);

        boolean result = real.reviewService.AddReviewToProduct(token, storeId, productId, review);
        assertTrue(result);

        verify(real.mockReviewRepo).AddReviewToProduct(storeId, productId, userId, username, review);
    }

    @Test
    void testUser_AddReviewToStore_Failure_StoreNotExist() throws Exception {
        String token = "user-token-2";
        int storeId = 999;
        String review = "Store not found!";

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(real.mockStoreRepo.checkStoreExistance(storeId))
                .thenThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));

        UIException ex = assertThrows(UIException.class, () -> {
            real.reviewService.AddReviewToStore(token, storeId, review);
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testUser_AddReviewToStore_Failure_InvalidToken() throws Exception {
        String token = "invalid-token";
        int storeId = 100;
        String review = "Should fail due to invalid token";

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(token), any());

        UIException ex = assertThrows(UIException.class, () -> {
            real.reviewService.AddReviewToStore(token, storeId, review);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }

    @Test
    void testUser_AddReviewToProduct_Failure_InvalidToken() throws Exception {
        String token = "invalid-token";
        int storeId = 100;
        int productId = 111;
        String review = "Should fail due to invalid token";

        // Force the mocked authRepo to throw when token is invalid
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(token), any());

        UIException ex = assertThrows(UIException.class, () -> {
            real.reviewService.AddReviewToProduct(token, storeId, productId, review);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }

    @Test
    void testUser_AddReviewToProduct_Failure_ProductNotFound() throws Exception {
        String token = "user-token-2";
        int storeId = 100;
        int nonExistentProductId = 999;
        int userId = 20;
        String username = "user2";
        String review = "This product does not exist!";

        ItemStoreDTO[] itemsInStore = {
            new ItemStoreDTO(111, 10, 1500, Category.ELECTRONICS, 4, storeId, "Laptop","TestStore")
        };

        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(itemsInStore);

        UIException ex = assertThrows(UIException.class, () -> {
            real.reviewService.AddReviewToProduct(token, storeId, nonExistentProductId, review);
        });

        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Product with ID " + nonExistentProductId));
    }

}
