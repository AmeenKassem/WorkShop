package workshop.demo.AcceptanceTest.Tests;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;

import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.AndDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountConditions;
import workshop.demo.DomainLayer.Store.DiscountScope;
import workshop.demo.DomainLayer.Store.OrDiscount;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.VisibleDiscount;
import workshop.demo.DomainLayer.Store.XorDiscount;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

public class PurchaseAT extends AcceptanceTests {

    Real real = new Real();

    public PurchaseAT() throws Exception {
    }

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
//        System.out.println(randomid + "" + bidid + "" + auctionid);
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addRegularBid(token, 0, 100, 30.0)
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addRegularBid(token, 0, 100, 30.0)
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addRegularBid(token, 0, storeId, 30.0)
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

        DevException ex = assertThrows(DevException.class, ()
                -> real.stockService.addRegularBid(token, bidId, storeId, 30.0)
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addBidOnAucction(token, 555, 100, 60.0)
        );

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {
        String token = "user-token-2";
        int userId = 20;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        NullPointerException ex = assertThrows(NullPointerException.class, ()
                -> real.stockService.addBidOnAucction(token, 555, 100, 60.0)
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addBidOnAucction(token, auctionId, storeId, 60.0)
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

        DevException ex = assertThrows(DevException.class, ()
                -> real.stockService.addBidOnAucction(token, auctionId, storeId, 60.0)
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
        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.SpecialType)).thenReturn(true);
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.setProductToRandom(token, 200, 1, 100.0, 100, 5000L)
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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.setProductToRandom(token, 200, 1, 100.0, 100, 5000L)
        );

    }

    @Test
    void testBuyRegisteredCart_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId,  productId, 1, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone",  "TestStore", 2, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),"TestStore"))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        when(real.mockStoreRepo.findStoreByID(100)).thenReturn(new Store(100, "TestStore", "ELECTRONICS"));

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 3, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");

        List<ItemStoreDTO> scopeItems = List.of(
                new ItemStoreDTO(productId, 3, 200, Category.ELECTRONICS, 0, storeId, "Phone")
        );
        Discount discount = new VisibleDiscount("50percent", 50,
                scope -> scope != null && scope.getItems().stream()
                        .anyMatch(scopeItem -> scopeItem.getStoreId() == storeId && scopeItem.getProductId() == productId)
        );

        store.addDiscount(discount);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 100);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),"TestStore"))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice()); // Confirm discount was applied
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount_OneConditionApplies_OnlyHighestUsed() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");

        List<ItemStoreDTO> scopeItems = List.of(
                new ItemStoreDTO(productId, 1, 200, Category.ELECTRONICS, 0, storeId, "Phone")
        );
        DiscountScope scope = new DiscountScope(scopeItems);

        Predicate<DiscountScope> cond1 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");

        Predicate<DiscountScope> cond2 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");

        Discount d1 = new VisibleDiscount("d1", 0.3, cond1);

        Discount d2 = new VisibleDiscount("d2", 0.5, cond2);

        OrDiscount orDiscount = new OrDiscount("OrDiscount");
        orDiscount.addDiscount(d1);
        orDiscount.addDiscount(d2);
        store.setDiscount(orDiscount);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),"TestStore"))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // total before discount

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithXorDiscount_OnlyFirstApplied() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");

        List<ItemStoreDTO> scopeItems = List.of(
                new ItemStoreDTO(productId, 1, 200, Category.ELECTRONICS, 0, storeId, "Phone")
        );
        DiscountScope scope = new DiscountScope(scopeItems);

        Predicate<DiscountScope> cond1 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");
        Predicate<DiscountScope> cond2 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");

        Discount d1 = new VisibleDiscount("d1", 0.3, cond1);
        Discount d2 = new VisibleDiscount("d2", 0.5, cond2);

        XorDiscount xorDiscount = new XorDiscount("XorTest");
        xorDiscount.addDiscount(d1);
        xorDiscount.addDiscount(d2);
        store.setDiscount(xorDiscount);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone","TestStore", 1, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),"TestStore"))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // before discount

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true); // 200 - 30% = 140
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_BothConditionsApply() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");

        List<ItemStoreDTO> scopeItems = List.of(
                new ItemStoreDTO(productId, 1, 200, Category.ELECTRONICS, 0, storeId, "Phone")
        );
        DiscountScope scope = new DiscountScope(scopeItems);

        Predicate<DiscountScope> cond1 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");
        Predicate<DiscountScope> cond2 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");

        Discount d1 = new VisibleDiscount("d1", 0.3, cond1); // 30%
        Discount d2 = new VisibleDiscount("d2", 0.1, cond2); // 10%

        AndDiscount andDiscount = new AndDiscount("AndTest");
        andDiscount.addDiscount(d1);
        andDiscount.addDiscount(d2);
        store.setDiscount(andDiscount);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),"TestStore"))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 120.0)).thenReturn(true); // pay 120
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(120, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_OneConditionFails() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore");
        CartItem item1=new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");

        List<ItemStoreDTO> scopeItems = List.of(
                new ItemStoreDTO(productId, 1, 200, Category.ELECTRONICS, 0, storeId, "Phone")
        );
        DiscountScope scope = new DiscountScope(scopeItems);

        Predicate<DiscountScope> cond1 = DiscountConditions.fromString("CATEGORY:ELECTRONICS");
        Predicate<DiscountScope> cond2 = DiscountConditions.fromString("TOTAL>1000");//no match

        Discount d1 = new VisibleDiscount("d1", 30, cond1);
        Discount d2 = new VisibleDiscount("d2", 20, cond2);

        AndDiscount andDiscount = new AndDiscount("AndFailTest");
        andDiscount.addDiscount(d1);
        andDiscount.addDiscount(d2);
        store.setDiscount(andDiscount);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone",  "TestStore", 1, 200);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false),eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

}
