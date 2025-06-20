package workshop.demo.AcceptanceTests.Tests;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.LinkedList;

import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.UserService;
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
import workshop.demo.DomainLayer.User.*;
import workshop.demo.InfrastructureLayer.Encoder;

@SpringBootTest
@ActiveProfiles("test")
public class UserTests extends AcceptanceTests {

    private static final Logger logger = LoggerFactory.getLogger(UserTests.class);

    private Registered user;
    private List<Registered> repo_results;

    public UserTests() {
        // Logical ID consistent with AtomicInteger usage
        this.user = new Registered("bashar", "encoded", 20);
        setId(user, 1); // ID 1 is realistic for first registered user
        this.repo_results = List.of(user);
    }

    @BeforeEach
    void setup() throws Exception {
        mockGuestRepo.deleteAll();
        mockUserRepo.deleteAll();

        // Inject mocked encoder using reflection
        Field encoderField = UserService.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        encoderField.set(userService, encoder);

        // Logical behavior for password validation
        when(encoder.matches("pass123", "encoded")).thenReturn(true);
    }

    @Test
    void testUser_LogIn_Success() throws Exception {
        mockSaveGuestSuccess();
        mockExistsByUsernameFailure();


        saveUserRepo(user);
        when(mockUserRepo.findRegisteredUsersByUsername("bashar")).thenReturn(repo_results);
        String guestToken = userService.generateGuest();
        assertTrue(userService.register(guestToken, "bashar", "pass123", 20));

        String userToken = userService.login(guestToken, "bashar", "pass123");

        assertNotNull(userToken);
        assertFalse(userToken.isEmpty());
    }




    @Test
    void testUser_LogOut_Success() throws Exception {
    }

//    @Test
//    void testUser_LogIn_Failure() {
//        when(mockUserRepo.findRegisteredUsersByUsername("notfound"))
//                .thenReturn(List.of());
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            userService.login("guest-token", "notfound", "1234");
//        });
//
//        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getNumber());
//    }



//
//    @Test
//    void testUser_LogOut_Failure() {
//        doThrow(new RuntimeException("logout error"))
//                .when(mockAuthRepo).logout("token_user");
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            userService.logout("token_user");
//        });
//
//        assertTrue(ex.getMessage().contains("logout error"));
//    }
//
//    @Test
//    void testUserLogin_Failure_InvalidToken() throws UIException {
//        mockValidTokenFailure("bad-token");
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            userService.getUser("bad-token");
//        });
//
//        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//    }
//
//    @Test
//    void testUserLogin_Failure_UserNotFound() throws Exception {
//        when(mockAuthRepo.validToken("some-token")).thenReturn(true);
//        when(mockAuthRepo.getUserId("some-token")).thenReturn(100);
//        when(mockUserRepo.findById(100)).thenReturn(null);
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            userService.getUser("some-token");
//        });
//
//        assertTrue(ex.getMessage().contains("User not found"));
//    }
//
//    @Test
//    void testUserLogout_Failure_UserNotFound() throws Exception {
//        doThrow(new RuntimeException("user not found"))
//                .when(mockAuthRepo).logout("unknown-token");
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            userService.logout("unknown-token");
//        });
//
//        assertTrue(ex.getMessage().contains("user not found"));
//    }
//
//    @Test
//    void testUserLogout_Failure_GuestTokenGenerationError() throws Exception {
//        doThrow(new RuntimeException("token error"))
//                .when(mockAuthRepo).logout("guest-token");
//
//        Exception ex = assertThrows(Exception.class, () -> {
//            userService.logout("guest-token");
//        });
//
//        assertTrue(ex.getMessage().contains("token error"));
//    }
//
//    @Test
//    void testUser_setAdmin_Success() throws Exception {
//        mockValidToken("admin-token", true);
//        mockGetUserIdFromToken("admin-token", 1);
//        when(mockUserRepo.existsById(7)).thenReturn(true);
//
//        boolean result = userService.setAdmin("admin-token", 7);
//
//        assertTrue(result);
//    }
//
//    @Test
//    void testUser_setAdmin_Failure_InvalidToken() throws UIException {
//        mockValidTokenFailure("bad-admin-token");
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            userService.setAdmin("bad-admin-token", 7);
//        });
//
//        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//    }

//     @Test
//     void testUser_CheckPurchaseHistory_Success() throws Exception {
//         int userId = 20;
//         String token = "user-token-2";
//         int storeId = 100;
//         String storeName = "TestStore";
//         String date = "2025-05-10";
//         List<ReceiptProduct> products = List.of(
//                 new ReceiptProduct("Keyboard", "TestStore", 1, 100, 1, Category.Electronics),
//                 new ReceiptProduct("Mouse", "TestStore", 1, 100, 2, Category.Electronics)
//         );
//         int totalPrice = 200;
//
//         ReceiptDTO receipt = new ReceiptDTO(storeName, date, products, totalPrice);
//         List<ReceiptDTO> receipts = new LinkedList<>();
//         receipts.add(receipt);
//         real.mockOrderRepo.setOrderToStore(storeId, userId, receipt, storeName);
//         when(real.mockOrderRepo.getReceiptDTOsByUser(userId)).thenReturn(receipts);
//         List<ReceiptDTO> result = real.testUser_CheckPurchaseHistory(token);
//
//         assertEquals(1, result.size());
//         ReceiptDTO r = result.get(0);
//         assertEquals(storeName, r.getStoreName());
//         assertEquals(date, r.getDate());
//         assertEquals(products, r.getProductsList());
//         assertEquals(totalPrice, r.getFinalPrice());
//     }
//
//     @Test
//     void testUser_CheckPurchaseHistory_Failure_InvalidToken() {
//         String token = "guest-registered-token";
//
//         when(real.mockAuthRepo.validToken(token)).thenReturn(false);
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.testUser_CheckPurchaseHistory(token);
//         });
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//         assertEquals("Invalid token!", ex.getMessage());
//     }
//
//     @Test
//     void testUser_CheckPurchaseHistory_Failure_GetUserNameThrows() throws UIException {
//         String token = "guest-registered-token";
//
//         when(real.mockAuthRepo.validToken(token)).thenReturn(true);
//         when(real.mockAuthRepo.getUserName(token)).thenThrow(new RuntimeException("getUserName failed"));
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.testUser_CheckPurchaseHistory(token);
//         });
//
//         assertTrue(ex.getMessage().contains("is not registered to the system!"));
//     }
//
//     @Test
//     void testUser_CheckPurchaseHistory_Failure_NoReceipts() throws Exception {
//         String token = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(token)).thenReturn(true);
//         when(real.mockAuthRepo.getUserName(token)).thenReturn("user2");
//         when(real.mockAuthRepo.getUserId("user2")).thenReturn(20);
//         when(real.mockUserRepo.isRegistered(20)).thenReturn(true);
//
//         when(real.mockOrderRepo.getReceiptDTOsByUser(20))
//                 .thenThrow(new UIException("User has no receipts.", ErrorCodes.RECEIPT_NOT_FOUND));
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.testUser_CheckPurchaseHistory(token);
//         });
//
//         assertEquals(ErrorCodes.RECEIPT_NOT_FOUND, ex.getNumber());
//         assertEquals("User has no receipts.", ex.getMessage());
//     }
//
//     @Test
//     void testUserGetStoreProducts() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 100;
//         int productId = 111;
//
//         ItemStoreDTO[] mockProducts = new ItemStoreDTO[]{
//             new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore")};
//         when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(mockProducts);
//
//         ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);
//         assertNotNull(products);
//
//     }
//
//     @Test
//     void testUserViewEmptyStore() throws Exception {
//         int storeId = 100;
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//
//         when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(new ItemStoreDTO[0]);
//
//         ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);
//
//         assertNotNull(products, "Products list should not be null");
//     }
//
//     @Test
//     void testUserViewInvalidStore() throws Exception {
//         int invalidStoreId = 999;
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//
//         when(real.mockStockRepo.getProductsInStore(invalidStoreId))
//                 .thenThrow(new RuntimeException("Store not found"));
//
//         ItemStoreDTO[] products = null;
//         try {
//             products = real.stockService.getProductsInStore(invalidStoreId);
//         } catch (Exception e) {
//             assertNull(products);
//             assertEquals("Store not found", e.getMessage());
//         }
//     }
//
//     @Test
//     void testUserViewStoreWithoutToken() throws Exception {
//         int storeId = 10;
//
//         when(real.mockStockRepo.getProductsInStore(storeId))
//                 .thenReturn(new ItemStoreDTO[]{
//             new ItemStoreDTO(1, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore")
//         });
//
//         ItemStoreDTO[] products = real.stockService.getProductsInStore(storeId);
//
//         assertNotNull(products);
//     }
//
//     @Test
//     void testUserGetProductInfo() throws Exception {
//         int userId = 20;
//         int storeId = 100;
//         int productId = 111;
//         String userToken = "user-token-2";
//
//         when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.AddToStock)).thenReturn(true);
//         when(real.mockStockRepo.addItem(storeId, productId, 5, 200, Category.Electronics))
//                 .thenReturn(new item(productId, 5, 200, Category.Electronics));
//
//         int addItemResult = real.stockService.addItem(storeId, userToken, productId, 5, 200, Category.Electronics);
//         assertEquals(addItemResult, productId);
//
//         ProductDTO mockProduct = new ProductDTO(productId, "Phone", Category.Electronics, "Smart device");
//         when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(mockProduct);
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//
//         String info = real.testGuest_GetProductInfo(userToken, productId);
//
//         assertNotNull(info);
//         assertTrue(info.contains("Phone"), "Expected product name in info");
//         assertTrue(info.contains("Smart device"), "Expected product description in info");
//         assertTrue(info.contains("Electronics"), "Expected product category in info");
//     }
//
//     @Test
//     void testUserGetProductInfo_ProductNotFound() throws Exception {
//         int productId = 999;
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockStockRepo.GetProductInfo(productId)).thenReturn(null);
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             real.testGuest_GetProductInfo(userToken, productId);
//         });
//
//         assertEquals("Product not found.", exception.getMessage());
//     }
//
//     @Test
//     void testUserAddProductToCart_Success() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 10;
//         int productId = 100;
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//         ItemCartDTO itemCartDTO = new ItemCartDTO(4, productId, 5, 1500, "Laptop", "TestStore", Category.Electronics);
//         //int storeId, int productId, int quantity, int price, String name, String storeName,Category category
//         doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);
//
//         assertDoesNotThrow(() -> {
//             real.testGuest_AddProductToCart(userToken, itemStoreDTO, 3);
//         });
//     }
//
//     @Test
//     void testUserAddProductToCart_InvalidToken() throws Exception {
//         String userToken = "user-token-2";
//         int storeId = 10;
//         int productId = 100;
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);
//
//         doCallRealMethod()
//                 .when(real.mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq("invalidToken"), any());
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.testGuest_AddProductToCart("invalidToken", itemStoreDTO, 3);
//         });
//
//         assertEquals("Invalid token!", ex.getMessage());
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//     }
//
//     @Test
//     void testUserAddProductToCart_ZeroQuantity() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 10;
//         int productId = 100;
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//
//         doThrow(new UIException("Cannot add product with zero quantity", 1025))
//                 .when(real.mockUserRepo).addItemToGeustCart(eq(userId), any(ItemCartDTO.class));
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             real.userService.addToUserCart(userToken, itemStoreDTO, 3);
//         });
//
//         assertEquals("Cannot add product with zero quantity", exception.getMessage());
//     }
//
//     @Test
//     void testUserBuyCart_Success() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 100;
//         int productId = 200;
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//         when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//         ItemCartDTO itemCartDTO = new ItemCartDTO(4, productId, 5, 1500, "Laptop", "TestStore", Category.Electronics);
//         //int storeId, int productId, int quantity, int price, String name, String storeName,Category category
//         CartItem CartItem = new CartItem(itemCartDTO);
//         doNothing().when(real.mockUserRepo).addItemToGeustCart(userId, itemCartDTO);
//         real.userService.addToUserCart(userToken, itemStoreDTO, 3);
//
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(CartItem));
//
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(CartItem));
//
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         List<ReceiptProduct> receiptProducts = List.of(
//                 new ReceiptProduct("ProductName", "TestStore", 2, 100, productId, Category.Electronics));
//         when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(false), eq("TestStore")))
//                 .thenReturn(receiptProducts);
//         when(real.mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
//         when(real.mockPay.processPayment(PaymentDetails.testPayment(), 200.0)).thenReturn(true);
//         when(real.mockSupply.processSupply(any())).thenReturn(true);
//         when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//
//         doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
//         when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//         ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails);
//
//         assertNotNull(receipts);
//         assertEquals(1, receipts.length);
//         assertEquals("TestStore", receipts[0].getStoreName());
//
//         verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), anyList(), eq(false), eq("TestStore"));
//         verify(real.mockStockRepo).calculateTotalPrice(anyList());
//         verify(real.mockPay).processPayment(any(), eq(200.0));
//         verify(real.mockSupply).processSupply(any());
//         verify(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
//     }
//
//     @Test
//     void testUserBuyCart_InvalidToken() throws Exception {
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(false);
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));
//
//         assertEquals("Shopping cart is empty or not found", ex.getMessage());
//         assertEquals(1009, ex.getNumber());
//
//     }
//
//     @Test
//     void testUserBuyCart_EmptyCart() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of());
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));
//
//         assertEquals("Shopping cart is empty or not found", ex.getMessage());
//     }
//
//     @Test
//     void testUserBuyCart_ProductNotAvailable() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 100;
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ItemCartDTO mockItem = Mockito.mock(ItemCartDTO.class);
//         List<ItemCartDTO> items = List.of(mockItem);
//
//         CartItem mockItem1 = Mockito.mock(CartItem.class);
//         List<CartItem> items1 = List.of(mockItem1);
//
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(items1);
//
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         when(mockCart.getAllCart()).thenReturn(items1);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
//
//         when(real.mockStockRepo.checkAvailability(any())).thenReturn(false);
//
//         when(real.mockStockRepo.processCartItemsForStore(eq(storeId), eq(items1), eq(false), eq("TestStore")))
//                 .thenThrow(new UIException("Not all items are available for user purchase", 1006));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
// //        Exception ex = assertThrows(Exception.class, ()
//         //              -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));
//         //  verify(real.mockUserRepo).getUserCart(userId);
//         //verify(real.mockStockRepo).processCartItemsForStore(eq(storeId), eq(items1), eq(false), eq("TestStore"));
//     }
//
//     @Test
//     void testUserBuyCart_PaymentFails() throws Exception {
//         int userId = 20;
//         int storeId = 100;
//         String userToken = "user-token-2";
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//         when(real.mockUserRepo.getUserCart(userId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//
//         when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
//         when(real.mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("TestStore")))
//                 .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
//         when(real.mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
//
//         doThrow(new RuntimeException("Payment failed")).when(real.mockPay)
//                 .processPayment(any(), eq(100.0));
//         when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         RuntimeException ex = assertThrows(RuntimeException.class, ()
//                 -> real.purchaseService.buyRegisteredCart(userToken, paymentDetails, supplyDetails));
//
//     }
//
//     @Test
//     void testUserSearchProductInStore_Success() throws Exception {
//         int userId = 20;
//         String userToken = "user-token-2";
//         int storeId = 100;
//         int productId = 111;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, "Lap", storeId,
//                 0, 0, 0, 0
//         );
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
//
//         ProductDTO[] products = {
//             new ProductDTO(productId, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO[] items = {
//             new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 100, "Laptop", "TestStore")};
//         when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//
//         when(real.mockStockRepo.search(criteria)).thenReturn(items);
//
//         ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);
//
//         assertNotNull(result);
//         assertEquals(1, result.length);
//         assertEquals(productId, result[0].getProductId());
//         assertEquals(1500, result[0].getPrice());
//         assertEquals(storeId, result[0].getStoreId());
//
//     }
//
//     @Test
//     void testUserSearchProducts_InvalidToken() throws Exception {
//         String userToken = "user-token-2";
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 null, Category.Electronics, null,
//                 0, 0, 0, 0, 0
//         );
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             real.stockService.searchProducts(userToken, criteria);
//         });
//
//         assertEquals("Invalid token!", exception.getMessage());
//     }
//
//     @Test
//     void testUserSearchProducts_NoMatches() throws Exception {
//         String userToken = "user-token-2";
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 null, // productNameFilter
//                 Category.Electronics, // categoryFilter
//                 null, // keywordFilter
//                 0, // storeId (not restricted)
//                 0, // minPrice
//                 0, // maxPrice
//                 0, // minStoreRating
//                 0 // maxStoreRating
//         );
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);
//
//         ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testUserSearchProductInStore_InvalidToken() throws Exception {
//         String userToken = "user-token-2";
//         int storeId = 100;
//         int productId = 200;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Product", null, null, storeId,
//                 0, 0, 0, 0
//         );
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             real.stockService.searchProducts(userToken, criteria);
//         });
//
//         assertEquals("Invalid token!", exception.getMessage());
//     }
//
//     @Test
//     void testUserSearchProductInStore_ProductNotFound() throws Exception {
//         int storeId = 100;
//         int productId = 200;
//         String userToken = "user-token-2";
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, null, storeId,
//                 0, 0, 0, 0
//         );
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);
//
//         ProductDTO[] emptyProducts = new ProductDTO[0];
//
//         ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testUserSearchProductInStore_ProductNotInStore() throws Exception {
//         int storeId = 100;
//         int productId = 200;
//         String userToken = "user-token-2";
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, null, storeId,
//                 0, 0, 0, 0
//         );
//
//         when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//         when(real.mockAuthRepo.getUserId(userToken)).thenReturn(20);
//
//         ProductDTO[] products = {
//             new ProductDTO(productId, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO[] result = real.stockService.searchProducts(userToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testUserGetPurchasePolicy_Success() throws Exception {
// //
//     }
//
//     @Test
//     void testUserGetPurchasePolicy_Failure() throws Exception {
// //
//     }
//
//     @Test
//     void testRankStore_Success() throws Exception {
//         String validToken = "user-token";
//         String invalidToken = "guest-token";
//         int userId = 10;
//         int storeId = 100;
//         int productId = 200;
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
//         when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
//         doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
//         doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//
//         doNothing().when(real.mockStoreRepo).rankStore(storeId, 4);
//
//         real.storeService.rankStore(validToken, storeId, 4);
//         verify(real.mockStoreRepo).rankStore(storeId, 4);
//     }
//
//     @Test
//     void testRankStore_InvalidToken() throws Exception {
//         String invalidToken = "guest-token";
//         int storeId = 100;
//
//         when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(invalidToken), any(Logger.class));
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.storeService.rankStore(invalidToken, storeId, 4)
//         );
//
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//     }
//
//     @Test
//     void testRankStore_InvalidStore() throws Exception {
//         System.out.println("sgfmnklfgjklrgwhjrwjklhtrwgjkl");
//         String validToken = "user-token";
//         String invalidToken = "guest-token";
//         int userId = 10;
//         int storeId = 100;
//         int productId = 200;
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
//         when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
//         doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(userId);
//         doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
//         doThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND))
//                 .when(real.mockStoreRepo).checkStoreExistance(storeId);
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.storeService.rankStore(validToken, storeId, 5)
//         );
//         assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
//     }
//
//     @Test
//     void testRankProduct_Success() throws Exception {
//         String validToken = "user-token";
//         int userId = 10;
//         int storeId = 100;
//         int productId = 200;
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
//         when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
//         doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
//         doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//         doNothing().when(real.mockStockRepo).rankProduct(storeId, productId, 3);
//
//         real.stockService.rankProduct(storeId, validToken, productId, 3);
//
//         verify(real.mockStockRepo).rankProduct(storeId, productId, 3);
//     }
//
//     @Test
//     void testRankProduct_InvalidToken() throws Exception {
//         String invalidToken = "guest-token";
//         int storeId = 100;
//         int productId = 200;
//
//         when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(invalidToken), any(Logger.class));
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.stockService.rankProduct(storeId, invalidToken, productId, 3)
//         );
//
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//     }
//
//     @Test
//     void testRankProduct_InvalidStore() throws Exception {
//         String validToken = "user-token";
//         int userId = 10;
//         int storeId = 100;
//         int productId = 200;
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
//         when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
//         doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
//         doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
//         doThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND))
//                 .when(real.mockStoreRepo).checkStoreExistance(storeId);
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.stockService.rankProduct(storeId, validToken, productId, 3)
//         );
//
//         assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
//     }
//
//     @Test
//     void testRankProduct_InvalidProduct() throws Exception {
//         String validToken = "user-token";
//         int userId = 10;
//         int storeId = 100;
//         int invalidProductId = 999;
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(validToken, logger);
//         when(real.mockAuthRepo.getUserId(validToken)).thenReturn(userId);
//         doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
//         doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//         doThrow(new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND))
//                 .when(real.mockStockRepo).rankProduct(storeId, invalidProductId, 3);
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> real.stockService.rankProduct(storeId, validToken, invalidProductId, 3)
//         );
//
//         assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
//     }
//
//     @Test
//     void testUser_AddReviewToStore_Success() throws Exception {
//         String token = "user-token-2";
//         int storeId = 100;
//         int userId = 20;
//         String username = "user2";
//         String review = "Very fast delivery!";
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//         doNothing().when(real.mockReviewRepo).AddReviewToStore(storeId, userId, username, review);
//
//         boolean result = real.reviewService.AddReviewToStore(token, storeId, review);
//         assertTrue(result);
//
//         verify(real.mockReviewRepo).AddReviewToStore(storeId, userId, username, review);
//     }
//
//     @Test
//     void testUser_AddReviewToProduct_Success() throws Exception {
//         String token = "user-token-2";
//         int storeId = 100;
//         int productId = 111;
//         int userId = 20;
//         String username = "user2";
//         String review = "Amazing laptop, very fast!";
//         ItemStoreDTO[] itemsInStore = {
//             new ItemStoreDTO(productId, 10, 1500, Category.Electronics, 4, storeId, "Laptop", "TestStore")
//         };
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//         when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(itemsInStore);
//         when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
//         when(real.mockAuthRepo.getUserName(token)).thenReturn(username);
//         doNothing().when(real.mockReviewRepo).AddReviewToProduct(storeId, productId, userId, username, review);
//
//         boolean result = real.reviewService.AddReviewToProduct(token, storeId, productId, review);
//         assertTrue(result);
//
//         verify(real.mockReviewRepo).AddReviewToProduct(storeId, productId, userId, username, review);
//     }
//
//     @Test
//     void testUser_AddReviewToStore_Failure_StoreNotExist() throws Exception {
//         String token = "user-token-2";
//         int storeId = 999;
//         String review = "Store not found!";
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
//         when(real.mockStoreRepo.checkStoreExistance(storeId))
//                 .thenThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.reviewService.AddReviewToStore(token, storeId, review);
//         });
//
//         assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getErrorCode());
//     }
//
//     @Test
//     void testUser_AddReviewToStore_Failure_InvalidToken() throws Exception {
//         String token = "invalid-token";
//         int storeId = 100;
//         String review = "Should fail due to invalid token";
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(token), any());
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.reviewService.AddReviewToStore(token, storeId, review);
//         });
//
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
//         assertTrue(ex.getMessage().contains("Invalid token"));
//     }
//
//     @Test
//     void testUser_AddReviewToProduct_Failure_InvalidToken() throws Exception {
//         String token = "invalid-token";
//         int storeId = 100;
//         int productId = 111;
//         String review = "Should fail due to invalid token";
//
//         // Force the mocked authRepo to throw when token is invalid
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(real.mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(token), any());
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.reviewService.AddReviewToProduct(token, storeId, productId, review);
//         });
//
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
//         assertTrue(ex.getMessage().contains("Invalid token"));
//     }
//
//     @Test
//     void testUser_AddReviewToProduct_Failure_ProductNotFound() throws Exception {
//         String token = "user-token-2";
//         int storeId = 100;
//         int nonExistentProductId = 999;
//         int userId = 20;
//         String username = "user2";
//         String review = "This product does not exist!";
//
//         ItemStoreDTO[] itemsInStore = {
//             new ItemStoreDTO(111, 10, 1500, Category.Electronics, 4, storeId, "Laptop", "TestStore")
//         };
//
//         doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
//         when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
//         when(real.mockStockRepo.getProductsInStore(storeId)).thenReturn(itemsInStore);
//
//         UIException ex = assertThrows(UIException.class, () -> {
//             real.reviewService.AddReviewToProduct(token, storeId, nonExistentProductId, review);
//         });
//
//         assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getErrorCode());
//         assertTrue(ex.getMessage().contains("Product with ID " + nonExistentProductId));
//     }

}
