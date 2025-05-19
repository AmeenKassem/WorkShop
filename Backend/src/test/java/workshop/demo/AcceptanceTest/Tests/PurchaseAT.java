package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class PurchaseAT extends AcceptanceTests {
    Real real = new Real();

    @BeforeEach
    void setup() throws Exception {
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
        assertTrue(real.userService.register(adminGuest, "admin", "adminPass", 18));
        String adminToken = real.userService.login(adminGuest, "admin", "adminPass");
        assertEquals(adminUserToken, adminToken);
        testSystem_InitMarket(adminUserToken);

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
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);

        String ownerGuest = real.userService.generateGuest();
        assertEquals(ownerGuestToken, ownerGuest);
        assertTrue(real.userService.register(ownerGuest, "owner", "owner", 18));
        String ownerToken = real.userService.login(ownerGuest, "owner", "owner");
        assertEquals(ownerUserToken, ownerToken);

        int storeId = 100;
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

        int productId = 200;
        String[] keywords = {"Phone", "Smartphone"};
        when(real.mockStockRepo.addProduct("Phone", Category.ELECTRONICS, "Smartphone", keywords)).thenReturn(productId);
        int returnedProductId = real.stockService.addProduct(ownerToken, "Phone", Category.ELECTRONICS, "Smartphone", keywords);
        assertEquals(productId, returnedProductId);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", keywords));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        int itemAdded = real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);
        assertEquals(itemAdded, productId);


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
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(true);
        int randomid = real.stockService.setProductToRandom(ownerToken, productId, 1, 100, storeId, 5000);
        int bidid = real.stockService.setProductToBid(ownerToken, storeId, productId, 1);
        int auctionid = real.stockService.setProductToAuction(ownerToken, storeId, productId, 1, 5000, 2);
        System.out.println(randomid + "" + bidid + "" + auctionid);
    }

    @Test
    void Add_BidProductToSpecialCart_Success() throws UIException, DevException {
        int storeId = 100;
        int userId = 20;
        String userToken = "user-token-2";
        int bidId = 999;
        double price = 30.0;

        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(userId);
doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);

                when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);

        int specialId = 5000;
        SingleBid mockBid = Mockito.mock(SingleBid.class);
        when(mockBid.getSpecialId()).thenReturn(specialId);
        when(mockBid.getId()).thenReturn(bidId);
        when(real.mockStockRepo.bidOnBid(bidId, price, userId, storeId)).thenReturn(mockBid);

        doNothing().when(real.mockUserRepo).addSpecialItemToCart(any(), eq(userId));

        boolean result = real.stockService.addRegularBid(userToken, bidId, storeId, price);

        assertTrue(result);
    }

    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        doCallRealMethod()
    .when(real.mockAuthRepo)
    .checkAuth_ThrowTimeOutException(eq(token), any()); 


        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addRegularBid(token, 0, 100, 30.0)
        );

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_UserSuspended() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        doThrow(new UIException("suspended user trying to make something", ErrorCodes.USER_SUSPENDED))
    .when(real.mockSusRepo)
    .checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);


        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addRegularBid(token, 0, 100, 30.0)
        );

        assertEquals("suspended user trying to make something", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_StoreNotFound() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;


                when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.bidOnBid(0, 30.0, userId, storeId))
                .thenThrow(new UIException("store not found on active purchases hashmap", ErrorCodes.STORE_NOT_FOUND));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addRegularBid(token, 0, storeId, 30.0)
        );

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_BidNotFound() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int bidId = 123;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.bidOnBid(bidId, 30.0, userId, storeId))
                .thenThrow(new DevException("Bid ID not found in active bids!"));

        DevException ex = assertThrows(DevException.class, () ->
                real.stockService.addRegularBid(token, bidId, storeId, 30.0)
        );

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }


    @Test
    void Add_AuctionBidToSpecialCart_Success() throws UIException, DevException {
        int storeId = 100;
        int userId = 20;
        String userToken = "user-token-2";
        int auctionId = 555;
        double price = 60.0;

    
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);


        int specialId = 6000;
        SingleBid mockBid = Mockito.mock(SingleBid.class);
        when(mockBid.getSpecialId()).thenReturn(specialId);
        when(mockBid.getId()).thenReturn(auctionId);
        when(real.mockStockRepo.bidOnAuction(storeId, userId, auctionId, price)).thenReturn(mockBid);

        doNothing().when(real.mockUserRepo).addSpecialItemToCart(any(), eq(userId));

        boolean result = real.stockService.addBidOnAucction(userToken, auctionId, storeId, price);

        assertTrue(result);
    }

    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

     doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
    .when(real.mockAuthRepo)
    .checkAuth_ThrowTimeOutException(eq(token), any(Logger.class));


        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addBidOnAucction(token, 555, 100, 60.0)
        );

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);


        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                real.stockService.addBidOnAucction(token, 555, 100, 60.0)
        );

    }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int auctionId = 555;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.bidOnAuction(storeId, userId, auctionId, 60.0))
                .thenThrow(new UIException("store not found on active purchases hashmap", ErrorCodes.STORE_NOT_FOUND));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addBidOnAucction(token, auctionId, storeId, 60.0)
        );

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_AuctionNotFound() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int auctionId = 999;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.bidOnAuction(storeId, userId, auctionId, 60.0))
                .thenThrow(new DevException("Auction ID not found in active auctions!"));

        DevException ex = assertThrows(DevException.class, () ->
                real.stockService.addBidOnAucction(token, auctionId, storeId, 60.0)
        );

        assertEquals("Auction ID not found in active auctions!", ex.getMessage());
    }


    @Test
    void Set_ProductToRandom_Success() throws Exception {
        int storeId = 100;
        int userId = 20;
        String token = "user-token-2";
        int productId = 200;
        int quantity = 1;
        double price = 100.0;
        long time = 5000L;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.addProductToRandom(productId, quantity, price, storeId, time)).thenReturn(777); 

        int randomId = real.stockService.setProductToRandom(token, productId, quantity, price, storeId, time);
        assertEquals(777, randomId);
    }

    @Test
    void Set_ProductToRandom_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
    .when(real.mockAuthRepo)
    .checkAuth_ThrowTimeOutException(eq(token), any(Logger.class));


        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToRandom(token, 200, 1, 100.0, 100, 5000L)
        );

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Set_ProductToRandom_Failure_UserSuspended() throws Exception {
        String token = "user-token-2";
        int userId = 20;

doThrow(new UIException("Suspended user trying to perform action", ErrorCodes.USER_SUSPENDED))
    .when(real.mockSusRepo)
    .checkUserSuspensoin_ThrowExceptionIfSuspeneded(eq(userId));


        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToRandom(token, 200, 1, 100.0, 100, 5000L)
        );

    }

    @Test
    void Set_ProductToRandom_Failure_InvalidQuantity() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.addProductToRandom(200, 0, 100.0, 100, 5000L))
                .thenThrow(new UIException("Quantity must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToRandom(token, 200, 0, 100.0, 100, 5000L)
        );

        assertEquals("Quantity must be positive!", ex.getMessage());
    }

    @Test
    void Set_ProductToRandom_Failure_InvalidPrice() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.addProductToRandom(200, 1, 0.0, 100, 5000L))
                .thenThrow(new UIException("Product price must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToRandom(token, 200, 1, 0.0, 100, 5000L)
        );

        assertEquals("Product price must be positive!", ex.getMessage());
    }

    @Test
    void Set_ProductToRandom_Failure_InvalidTime() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        when(real.mockStockRepo.addProductToRandom(200, 1, 100.0, 100, 0L))
                .thenThrow(new UIException("Random time must be positive!", ErrorCodes.INVALID_RANDOM_PARAMETERS));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToRandom(token, 200, 1, 100.0, 100, 0L)
        );

        assertEquals("Random time must be positive!", ex.getMessage());
    }



    @Test
    void testBuyRegisteredCart_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, Category.ELECTRONICS, productId, 1, 200, "Phone", "Smartphone", "TestStore");
        cart.addItem(storeId, item);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", Category.ELECTRONICS, "Smartphone", "TestStore", 2, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false)))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }


}