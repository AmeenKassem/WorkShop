package workshop.demo.AcceptanceTests.Tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.*;

import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class GuestTests extends AcceptanceTests {
    private static final Logger logger = LoggerFactory.getLogger(GuestTests.class);
    Guest g = new Guest();
    String GToken = "Gtoken";
    Registered r = new Registered(0, "bashar", "password", 24);

    String OToken = "Otoken";
    Guest go = new Guest();
    Registered or = new Registered(0, "bashar", "password", 24);

    Store s=new Store("TestStore","Home");

    @BeforeEach
    void setup() throws Exception {
        mockGuestRepo.deleteAll();
        mockUserRepo.deleteAll();
        mockStoreRepo.deleteAll();
        mockStockRepo1.deleteAll();
        mockStoreStock.deleteAll();
        mockNodeRepo.deleteAll();
        userService.generateGuest();
        //the owner of the store
        // guest ---> owner
        saveGuestRepo(go);
        when(mockAuthRepo.generateGuestToken(go.getId())).thenReturn(OToken);
        OToken = userService.generateGuest();


        //convert to user and login
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(OToken,logger);
        when(mockUserRepo.existsByUsername(or.getUsername())).thenReturn(0);
        saveUserRepo(or);
        userService.register(OToken,or.getUsername(), or.getEncodedPass(), 18);



        List<Registered> list_user_repo=new LinkedList<>();
        list_user_repo.add(or);
        when(mockUserRepo.findRegisteredUsersByUsername(or.getUsername())).thenReturn(list_user_repo);
        //when(encoder.matches(or.getEncodedPass(), or.getEncodedPass())).thenReturn(true);
        saveUserRepo(or);
        or.login();

        //add store to the owner
        when(mockAuthRepo.getUserId(OToken)).thenReturn(or.getId());
        //doNothing().when(userService).checkUserRegisterOnline_ThrowException(or.getId());
       // doNothing().when(mockSusRepo).(or.getId());


        when(mockStoreRepo.save(any())).thenReturn(s);

        //doNothing().when(mock).addNewStoreOwner(anyInt(), eq(or.getId()));
        when(mockStoreStock.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mockOrderRepo).addStoreTohistory(anyInt());

        int storeId = storeService.addStoreToSystem(OToken, "TestStore", "Home");















    }

    @Test
    void testGuestEnter_Success() throws Exception {
        // Arrange: create a guest and mock the save to return it
        Guest savedGuest = new Guest(); // Will have default id = 0
        saveGuestRepo(savedGuest);
        // Let the token be based on ID 0 (since that's what guest.getId() will return)
        when(mockAuthRepo.generateGuestToken(0)).thenReturn(GToken);

        // Act
        String token = userService.generateGuest();

        // Assert
        assertNotNull(token);
        assertEquals(GToken, token);

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
    void testGuestExit_Failure_InvalidToken() throws Exception {
        Guest savedGuest = new Guest();
        saveGuestRepo(savedGuest);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token-0");

        String token = userService.generateGuest();
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(token, logger);
        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(eq(token), any());
        UIException ex = assertThrows(UIException.class, () -> {
            userService.destroyGuest(token);
        });

        assertTrue(ex.getMessage().contains("Invalid token!"));

        verify(mockGuestRepo, never()).deleteById(any());
    }


    @Test
    void testGuestRegister_Success() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(GToken, logger);
        saveUserRepo(r);
        assertTrue(userService.register(GToken, r.getUsername(), r.getEncodedPass(), r.getUserDTO().age));


    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {
        Guest savedGuest = new Guest(); // Will have default id = 0
        saveGuestRepo(savedGuest);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn(GToken);
        String token = userService.generateGuest();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(GToken, logger);
        when(mockUserRepo.existsByUsername(r.getUsername())).thenReturn(1);
        Exception exception = assertThrows(Exception.class, () -> {
            userService.register(token, r.getUsername(), "password", 18);
        });
        assertTrue(exception.getMessage().contains("username"));
    }


    @Test
    void testGuestViewEmptyStore() throws Exception {
        int storeId = 100;  // use storeId from setup
        String guestToken = "guest-token";  // use guestToken from setup

        when(mockAuthRepo.validToken(guestToken)).thenReturn(true); // ensure guest token is valid
        Exception exception = assertThrows(Exception.class, () -> {
            stockService.getProductsInStore(storeId);
        });

    }
//
     @Test
     void testGuestGetProductInfo() throws Exception {

     }
//
//     @Test
//     void testGuestGetProductInfo_ProductNotFound() throws Exception {
//         int userIdB = 2;
//         int productId = 999; // non-existent product
//         String guestTokenB = "guest-token-B";
//
//         when(mockAuthRepo.validToken(guestTokenB)).thenReturn(true);
//
//         when(mockStockRepo.GetProductInfo(productId)).thenReturn(null);
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             testGuest_GetProductInfo(guestTokenB, productId);
//         });
//
//         assertEquals("Product not found.", exception.getMessage());
//     }
////
//     @Test
//     void testGuestAddProductToCart_Success() throws Exception {
//         int guestId = 1;
//         int storeId = 10;
//         int productId = 100;
//         String guestToken = "guest-token-1";
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//         ItemCartDTO itemCartDTO = new ItemCartDTO(storeId, productId, 5, 1500, "Laptop", "TestStore", Category.Electronics);
//
//         doNothing().when(mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);
//
//         assertDoesNotThrow(() -> {
//             userService.addToUserCart(guestToken, itemStoreDTO, 2);
//         });
//     }
//
//     @Test
//     void testGuestAddProductToCart_InvalidToken() throws Exception {
//         int storeId = 10;
//         int productId = 100;
//         String guestToken = "guest-token-1";
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             testGuest_AddProductToCart(guestToken, itemStoreDTO, 2);
//         });
//
//         assertEquals("Invalid token!", exception.getMessage());
//         assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
//
//     }
//
//     @Test
//     void testGuestAddProductToCart_GuestNotFound() throws Exception {
//         int guestId = 999; // non-existent ID
//         int productId = 100;
//         String guestToken = "guest-token-1";
//         int storeId = 10;
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//         ItemCartDTO itemCartDTO = new ItemCartDTO(storeId, productId, 5, 1500, "Laptop", "TestStore", Category.Electronics);
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         doThrow(new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND))
//                 .when(mockUserRepo).addItemToGeustCart(eq(guestId), any(ItemCartDTO.class));
//
//         UIException exception = assertThrows(UIException.class, ()
//                 -> userService.addToUserCart(guestToken, itemStoreDTO, 2)
//         );
//
//         assertEquals("Guest not found: " + guestId, exception.getMessage());
//         assertEquals(ErrorCodes.GUEST_NOT_FOUND, exception.getNumber());
//     }
//
//     @Test
//     void testGuestAddProductToCart_ZeroQuantity() throws Exception {
//         int guestId = 1;
//         int storeId = 10;
//         int productId = 100;
//         String guestToken = "guest-token-1";
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, storeId, "Laptop", "TestStore");
//
//         doThrow(new UIException("Cannot add product with zero quantity", 1025))
//                 .when(mockUserRepo).addItemToGeustCart(eq(guestId), any(ItemCartDTO.class));
//
//         Exception exception = assertThrows(UIException.class, () -> {
//             userService.addToUserCart(guestToken, itemStoreDTO, 3);
//         });
//
//         assertEquals("Cannot add product with zero quantity", exception.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_Success() throws Exception {
//         int guestId = 20;
//         int storeId = 1;
//         int productId = 200;
//         String guestToken = "guest-token";
//
//         when(mockUserRepo.generateGuest()).thenReturn(guestId);
//         when(mockAuthRepo.generateGuestToken(guestId)).thenReturn(guestToken);
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         String generatedGuestToken = userService.generateGuest();
//         assertEquals(guestToken, generatedGuestToken);
//
//         ItemStoreDTO itemStoreDTO = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");;
//         ItemCartDTO itemCartDTO = new ItemCartDTO(storeId, productId, 5, 1500, "Laptop", "TestStore", Category.Electronics);
//         CartItem CartItem = new CartItem(itemCartDTO);
//         doNothing().when(mockUserRepo).addItemToGeustCart(guestId, itemCartDTO);
//         userService.addToUserCart(guestToken, itemStoreDTO, 3);
//
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(CartItem));
//
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(CartItem));
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         List<ReceiptProduct> receiptProducts = List.of(new ReceiptProduct("ProductName", "TestStore", 2, 100, productId, Category.Electronics));
//         when(mockStockRepo.checkAvailability(any())).thenReturn(true);
//         when(mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("TestStore"))).thenReturn(receiptProducts);
//         when(mockStockRepo.calculateTotalPrice(receiptProducts)).thenReturn(200.0);
//         when(mockPay.processPayment(PaymentDetails.testPayment(), 200)).thenReturn(true);
//         when(mockSupply.processSupply(any())).thenReturn(true);
//         when(mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//
//         doNothing().when(mockOrderRepo).setOrderToStore(eq(storeId), eq(guestId), any(), eq("TestStore"));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//         //when(mockPurchaseRepo.)
//         when(mockStoreRepo.findStoreByID(1)).thenReturn(new Store(1, "TestStore", "ELECTRONICS"));
//         ReceiptDTO[] receipts = purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails);
//
//         assertNotNull(receipts);
//         assertEquals(1, receipts.length);
//         assertEquals("TestStore", receipts[0].getStoreName());
//         assertEquals(200.0, receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice() * 2);
//
//         verify(mockStockRepo).checkAvailability(any());
//         verify(mockPay).processPayment(any(), eq(200.0));
//         verify(mockSupply).processSupply(any());
//         verify(mockOrderRepo).setOrderToStore(eq(storeId), eq(guestId), any(), eq("TestStore"));
//     }
//
//     @Test
//     void testGuestBuyCart_InvalidToken() throws Exception {
//         String guestToken = "guest-token";
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(false);
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(UIException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Invalid token!", ex.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_EmptyCart() throws Exception {
//         int guestId = 20;
//         String guestToken = "guest-token";
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(Mockito.mock(ShoppingCart.class));
//         when(mockUserRepo.getUserCart(guestId).getAllCart()).thenReturn(List.of());
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(UIException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Shopping cart is empty or not found", ex.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_ProductNotAvailable() throws Exception {
//         int guestId = 20;
//         String guestToken = "guest-token";
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         when(mockStockRepo.checkAvailability(any())).thenReturn(false);
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(UIException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Not all items are available for guest purchase", ex.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_PaymentFails() throws Exception {
//         int guestId = 20;
//         int storeId = 100;
//         String guestToken = "guest-token";
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         when(mockStoreRepo.getStoreNameById(100)).thenReturn("TestStore");
//         when(mockStockRepo.checkAvailability(any())).thenReturn(true);
//         when(mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("TestStore")))
//                 .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
//         when(mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
//         when(mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));
//
//         doThrow(new RuntimeException("Payment failed")).when(mockPay)
//                 .processPayment(any(), eq(100.0));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(RuntimeException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Payment failed", ex.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_SupplyFails() throws Exception {
//         int guestId = 20;
//         int storeId = 100;
//         String guestToken = "guest-token";
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         when(mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//         when(mockStockRepo.checkAvailability(any())).thenReturn(true);
//         when(mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("TestStore")))
//                 .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
//         when(mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
//         when(mockStoreRepo.findStoreByID(storeId)).thenReturn(new Store(storeId, "TestStore", "ELECTRONICS"));
//
//         when(mockPay.processPayment(any(), eq(100.0))).thenReturn(true);
//         doThrow(new RuntimeException("Supply failed")).when(mockSupply).processSupply(any());
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(RuntimeException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Supply failed", ex.getMessage());
//     }
//
//     @Test
//     void testGuestBuyCart_StoreNotFound() throws Exception {
//         int guestId = 20;
//         int storeId = 123;
//         String guestToken = "guest-token";
//         ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
//         ShoppingBasket mockBasket = Mockito.mock(ShoppingBasket.class);
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//         when(mockUserRepo.getUserCart(guestId)).thenReturn(mockCart);
//         when(mockCart.getAllCart()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//
//         HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
//         baskets.put(storeId, mockBasket);
//         when(mockCart.getBaskets()).thenReturn(baskets);
//
//         when(mockBasket.getStoreId()).thenReturn(storeId);
//         when(mockBasket.getItems()).thenReturn(List.of(Mockito.mock(CartItem.class)));
//         when(mockStoreRepo.getStoreNameById(storeId)).thenReturn("UnknownStore");
//         when(mockStockRepo.checkAvailability(any())).thenReturn(true);
//         when(mockStockRepo.processCartItemsForStore(eq(storeId), anyList(), eq(true), eq("UnknownStore")))
//                 .thenReturn(List.of(Mockito.mock(ReceiptProduct.class)));
//         when(mockStockRepo.calculateTotalPrice(anyList())).thenReturn(100.0);
//
//         // Simulate exception for store not found
//         when(mockStoreRepo.findStoreByID(storeId)).thenThrow(new RuntimeException("Store not found"));
//
//         PaymentDetails paymentDetails = PaymentDetails.testPayment();
//         SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//
//         Exception ex = assertThrows(RuntimeException.class, ()
//                 -> purchaseService.buyGuestCart(guestToken, paymentDetails, supplyDetails));
//
//         assertEquals("Store not found", ex.getMessage());
//     }
//
//     @Test
//     void testGuestSearchProductInStore_Success() throws Exception {
//         int guestId = 1;
//         int storeId = 100;
//         int productId = 111; // Matches the setup
//         String guestToken = token_guest;
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 null, // product name filter
//                 null, // category filter
//                 null, // keyword filter
//                 storeId, // store ID to filter
//                 0, 0, // no price range
//                 0, 0 // no rating range
//         );
//
//         ProductDTO[] mockProducts = {
//             new ProductDTO(productId, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO mockItem = new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 100, "Laptop", "TestStore");
//
//         ItemStoreDTO[] mockItems = {mockItem};
//         when(mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//         when(mockStockRepo.search(criteria)).thenReturn(mockItems);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
//
//         assertEquals(1, result.length);
//
//         assertEquals(1500, result[0].getPrice());
//         assertEquals(storeId, result[0].getStoreId());
//         assertEquals(Category.Electronics, result[0].getCategory());
//     }
//
//     @Test
//     void testGuestSearchProducts_Success() throws Exception {
//         int guestId = 1;
//         int storeId = 100;
//         int productId = 200;
//         String guestToken = token_guest;
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         String[] keywords = {"Laptop", "Lap", "top"};
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.Electronics, keywords[0], 100, 0, 5000, 0, 5);
//
//         ProductDTO[] matchedProducts = new ProductDTO[]{
//             new ProductDTO(productId, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO[] matchedItems = new ItemStoreDTO[]{
//             new ItemStoreDTO(productId, 5, 1500, Category.Electronics, 0, 100, "Laptop", "TestStore")};
//         when(mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
//
//         when(mockStockRepo.search(criteria)).thenReturn(matchedItems);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
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
//     void testSearchProducts_InvalidToken() throws Exception {
//         String guestToken = token_guest;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, "Laptop", 100,
//                 0, 5000,
//                 0, 5
//         );
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(guestToken), any());
//
//         UIException exception = assertThrows(UIException.class, () -> {
//             stockService.searchProducts(guestToken, criteria);
//         });
//
//         assertEquals("Invalid token!", exception.getMessage());
//         assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
//     }
//
//     @Test
//     void testSearchProducts_NoMatches() throws Exception {
//         String guestToken = token_guest;
//
//         String[] keywords = {"Laptop", "Lap", "top"};
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop", Category.Electronics, keywords[0], 100, 0, 5000, 0, 5);
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testSearchProducts_ProductExists_NotInStore() throws Exception {
//         String guestToken = token_guest;
//         int storeId = 100;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, null, storeId,
//                 0, 0, 0, 0
//         );
//
//         ProductDTO[] foundProducts = {
//             new ProductDTO(111, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(1);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testSearchProducts_ProductInStore_QuantityZero() throws Exception {
//         String guestToken = token_guest;
//         int storeId = 100;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, null, storeId,
//                 0, 0, 0, 0
//         );
//
//         ProductDTO[] foundProducts = {
//             new ProductDTO(111, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO mockItem = new ItemStoreDTO(111, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");// quantity = 0
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(1);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//     void testSearchProducts_PriceOutOfRange() throws Exception {
//         String guestToken = token_guest;
//         int storeId = 100;
//
//         ProductSearchCriteria criteria = new ProductSearchCriteria(
//                 "Laptop", Category.Electronics, null, storeId,
//                 5000, 10000, 0, 0 // Price filter too high
//         );
//
//         ProductDTO[] foundProducts = {
//             new ProductDTO(111, "Laptop", Category.Electronics, "Gaming Laptop")
//         };
//
//         ItemStoreDTO mockItem = new ItemStoreDTO(111, 5, 1500, Category.Electronics, 0, 4, "Laptop", "TestStore");
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(1);
//
//         ItemStoreDTO[] result = stockService.searchProducts(guestToken, criteria);
//
//         assertNull(result);
//     }
//
//     @Test
//
//     void testGuestModifyCartAddQToBuy_Success() throws Exception {
//         int guestId = 1;
//         String guestToken = "guest-token";
//         int productId = 100;
//         int newQuantity = 3;
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         doNothing().when(mockUserRepo).ModifyCartAddQToBuy(guestId, productId, newQuantity);
//
//         assertDoesNotThrow(()
//                 -> userService.ModifyCartAddQToBuy(guestToken, productId, newQuantity)
//         );
//
//         verify(mockUserRepo).ModifyCartAddQToBuy(guestId, productId, newQuantity);
//     }
//
//     @Test
//     void testGuestModifyCartAddQToBuy_InvalidToken() throws Exception {
//         String guestToken = "invalid-token";
//         int productId = 100;
//
//         doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
//                 .when(mockAuthRepo)
//                 .checkAuth_ThrowTimeOutException(eq(guestToken), any(Logger.class));
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> userService.ModifyCartAddQToBuy(guestToken, productId, 2)
//         );
//
//         assertEquals("Invalid token!", ex.getMessage());
//         assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
//     }
//
//     @Test
//     void testGuestModifyCartAddQToBuy_GuestNotFound() throws Exception {
//         int guestId = 999;
//         String guestToken = "guest-token-999";
//         int productId = 100;
//         int newQuantity = 3;
//
//         when(mockAuthRepo.validToken(guestToken)).thenReturn(true);
//         when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
//
//         doThrow(new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND))
//                 .when(mockUserRepo).ModifyCartAddQToBuy(eq(guestId), eq(productId), eq(newQuantity));
//
//         UIException ex = assertThrows(UIException.class, ()
//                 -> userService.ModifyCartAddQToBuy(guestToken, productId, newQuantity)
//         );
//
//         assertEquals("Guest not found: " + guestId, ex.getMessage());
//         assertEquals(ErrorCodes.GUEST_NOT_FOUND, ex.getNumber());
//     }

}
