package workshop.demo.AcceptanceTests.Tests;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;


import org.slf4j.LoggerFactory;
import workshop.demo.AcceptanceTests.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class PurchaseTests extends AcceptanceTests {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseTests.class);

    Real real = new Real();

    public PurchaseTests() throws Exception {
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
        real.testSystem_InitMarket(adminUserToken);

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
    void Add_BidProductToSpecialCart_Success_And_Buy() throws Exception {
        int storeId = 100;
        int userId = 20;
        String userToken = "user-token-2";
        int bidId = 1;
        int specialId = 1;
        double price = 30.0;
        int productId = 200;

        // ===== MOCK: User Validations =====
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());

        // ===== MOCK: Bidding Phase =====
        SingleBid bid = new SingleBid(productId, 1, userId, price, SpecialType.BID, storeId, bidId, specialId);
        when(real.mockStockRepo.bidOnBid(bidId, price, userId, storeId)).thenReturn(bid);
        doNothing().when(real.mockUserRepo).addSpecialItemToCart(any(), eq(userId));

        boolean added = real.stockService.addRegularBid(userToken, bidId, storeId, price);
        assertTrue(added);

        // ===== MOCK: Accept Bid Phase =====
        when(real.mockIOSrepo.manipulateItem(userId, storeId, Permission.SpecialType)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStockRepo.acceptBid(storeId, bidId, bidId)).thenReturn(bid);  // acceptBid returns SingleBid

        SingleBid accepted = real.stockService.acceptBid(userToken, storeId, bidId, bidId);
        assertNotNull(accepted);
        assertEquals(userId, accepted.getUserId());

        // ===== MOCK: Cart Preparation Phase =====
        List<UserSpecialItemCart> specialItems = List.of(new UserSpecialItemCart(storeId, specialId, bidId, SpecialType.BID));
        UserSpecialItemCart a=new UserSpecialItemCart(8,9,9,SpecialType.Auction);
        a.equals(specialItems.get(0));



        when(real.mockUserRepo.getAllSpecialItems(userId)).thenReturn(specialItems);

        // Ensure bid is returned as winner + ended
        SingleBid wonBid = Mockito.spy(bid);
        when(wonBid.isWinner()).thenReturn(true);
        when(wonBid.isEnded()).thenReturn(true);
        when(real.mockStockRepo.getBidIfWinner(storeId, specialId, bidId, SpecialType.BID)).thenReturn(wonBid);

        // ===== MOCK: Product & Store Info =====
        Product product = new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", null);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(product);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        // ===== MOCK: Payment & Supply =====
        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = SupplyDetails.getTestDetails();
        when(real.mockPay.processPayment(payment, price)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        // ===== MOCK: Cleanup and Save Receipts =====
        doNothing().when(real.mockUserRepo).removeBoughtSpecialItems(eq(userId), anyList(), anyList());
        doNothing().when(real.mockUserRepo).removeSpecialItem(eq(userId), any());

        ReceiptDTO expectedReceipt = new ReceiptDTO("TestStore", "2025-06-01", List.of(
                new ReceiptProduct("Phone", "TestStore", 1, (int) price, productId, Category.ELECTRONICS)
        ), price);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        // ===== ACTUAL FINALIZATION =====
        ReceiptDTO[] result = real.purchaseService.finalizeSpecialCart(userToken, payment, supply);

        // ===== ASSERTIONS =====
        assertNotNull(result);
        assertEquals(1, result.length);

        ReceiptDTO receipt = result[0];
        assertEquals("TestStore", receipt.getStoreName());
        assertEquals(price, receipt.getFinalPrice());

        List<ReceiptProduct> products = receipt.getProductsList();
        assertEquals(1, products.size());

        ReceiptProduct productResult = products.get(0);
        assertEquals("Phone", productResult.getProductName());
        assertEquals(1, productResult.getQuantity());
        assertEquals((int) price, productResult.getPrice());
        assertEquals(productId, productResult.getProductId());
        assertEquals("TestStore", productResult.getStorename());
        assertEquals(Category.ELECTRONICS, productResult.getCategory());
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
    void Add_AuctionBidToSpecialCart_Success_And_Buy() throws Exception {
        int storeId = 100;
        int userId = 20;
        String userToken = "user-token-2";
        int auctionId = 555;
        int specialId = 6000;
        double price = 60.0;
        int productId = 200;

        // ===== MOCK: Auth & User Validation =====
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(userToken), any());
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        // ===== MOCK: Auction Bidding =====
        SingleBid auctionBid = new SingleBid(productId, 1, userId, price, SpecialType.Auction, storeId, auctionId, specialId);
        when(real.mockStockRepo.bidOnAuction(storeId, userId, auctionId, price)).thenReturn(auctionBid);
        doNothing().when(real.mockUserRepo).addSpecialItemToCart(any(), eq(userId));

        boolean added = real.stockService.addBidOnAucction(userToken, auctionId, storeId, price);
        assertTrue(added);

        // ===== MOCK: Special Cart Contains Auction Item =====
        List<UserSpecialItemCart> specialItems = List.of(new UserSpecialItemCart(storeId, specialId, auctionId, SpecialType.Auction));
        when(real.mockUserRepo.getAllSpecialItems(userId)).thenReturn(specialItems);

        // ===== MOCK: Auction Result = Winner & Ended =====
        SingleBid wonAuction = Mockito.spy(auctionBid);
        when(wonAuction.isWinner()).thenReturn(true);
        when(wonAuction.isEnded()).thenReturn(true);
        when(real.mockStockRepo.getBidIfWinner(storeId, specialId, auctionId, SpecialType.Auction)).thenReturn(wonAuction);

        // ===== MOCK: Product Info & Store Name =====
        Product product = new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", null);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(product);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        // ===== MOCK: Payment & Supply Services =====
        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = SupplyDetails.getTestDetails();
        when(real.mockPay.processPayment(payment, price)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        // ===== MOCK: Post-purchase Cleanup =====
        doNothing().when(real.mockUserRepo).removeBoughtSpecialItems(eq(userId), anyList(), anyList());
        doNothing().when(real.mockUserRepo).removeSpecialItem(eq(userId), any());

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        // ===== ACT: Finalize Auction Cart =====
        ReceiptDTO[] result = real.purchaseService.finalizeSpecialCart(userToken, payment, supply);

        // ===== ASSERTIONS =====
        assertNotNull(result);
        assertEquals(1, result.length);

        ReceiptDTO receipt = result[0];
        assertEquals("TestStore", receipt.getStoreName());
        assertEquals(price, receipt.getFinalPrice());

        List<ReceiptProduct> products = receipt.getProductsList();
        assertEquals(1, products.size());

        ReceiptProduct productResult = products.get(0);
        assertEquals("Phone", productResult.getProductName());
        assertEquals(1, productResult.getQuantity());
        assertEquals((int) price, productResult.getPrice());
        assertEquals(productId, productResult.getProductId());
        assertEquals("TestStore", productResult.getStorename());
        assertEquals(Category.ELECTRONICS, productResult.getCategory());
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
    void ParticipateInRandom_Success_And_Buy() throws Exception {
        int storeId = 100;
        int userId = 20;
        String token = "user-token-2";
        int randomId = 777;
        int productId = 200;
        double price = 100.0;

        // ===== MOCK: Auth & User Validations =====
        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
        doNothing().when(real.mockAuthRepo).checkAuth_ThrowTimeOutException(eq(token), any());
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(userId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        // ===== MOCK: Participation (User wins + event ended) =====
        ParticipationInRandomDTO card = new ParticipationInRandomDTO(
                productId, storeId, userId, randomId, price
        );
        card.isWinner = true;
        card.ended = true;
        when(real.mockStockRepo.validatedParticipation(userId, randomId, storeId, price)).thenReturn(card);
        doNothing().when(real.mockUserRepo).addSpecialItemToCart(any(), eq(userId));

        // ===== ACT: participateInRandom =====
        PaymentDetails payment = PaymentDetails.testPayment();
        ParticipationInRandomDTO participationResult = real.purchaseService.participateInRandom(
                token, randomId, storeId, price, payment
        );
        assertNotNull(participationResult);
        assertEquals(randomId, participationResult.randomId);

        // ===== MOCK: Special cart contains random item =====
        UserSpecialItemCart specialItem = new UserSpecialItemCart(storeId, randomId, -1, SpecialType.Random);
        when(real.mockUserRepo.getAllSpecialItems(userId)).thenReturn(List.of(specialItem));

        // ===== MOCK: Returning the winning card during finalizeSpecialCart =====
        when(real.mockStockRepo.getRandomCardIfWinner(storeId, randomId, userId)).thenReturn(card);

        // ===== MOCK: Product Info & Store Name =====
        Product product = new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", null);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(product);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        doNothing().when(real.mockStockRepo).validateAndDecreaseStock(storeId, productId, 1);

        // ===== MOCK: Payment & Supply (success) =====
        SupplyDetails supply = SupplyDetails.getTestDetails();
        when(real.mockPay.processPayment(payment, price)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        // ===== MOCK: Cleanup after purchase =====
        doNothing().when(real.mockUserRepo).removeBoughtSpecialItems(eq(userId), anyList(), anyList());
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        when(real.mockPay.processPayment(payment, 0.0)).thenReturn(true);

        // ===== ACT: Finalize Special Cart =====
        ReceiptDTO[] resultReceipts = real.purchaseService.finalizeSpecialCart(token, payment, supply);

        // ===== ASSERTIONS =====
        assertNotNull(resultReceipts);
        assertEquals(1, resultReceipts.length);

        ReceiptDTO receipt = resultReceipts[0];
        assertEquals("TestStore", receipt.getStoreName());
        assertEquals(0.0, receipt.getFinalPrice()); // random is already paid during participation

        List<ReceiptProduct> products = receipt.getProductsList();
        assertEquals(1, products.size());

        ReceiptProduct productResult = products.get(0);
        assertEquals("Phone", productResult.getProductName());
        assertEquals(1, productResult.getQuantity());
        assertEquals(productId, productResult.getProductId());
        assertEquals("TestStore", productResult.getStorename());
        assertEquals(Category.ELECTRONICS, productResult.getCategory());
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
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.ELECTRONICS);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 2, 200, productId, Category.ELECTRONICS);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
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


}