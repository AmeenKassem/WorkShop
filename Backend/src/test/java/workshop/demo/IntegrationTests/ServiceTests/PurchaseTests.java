package workshop.demo.IntegrationTests.ServiceTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PurchaseTests {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserSuspensionRepo suspensionRepo;
    @Autowired
    private AuthenticationRepo authRepo;

    @Autowired
    PaymentServiceImp payment;
    @Autowired
    SupplyServiceImp serviceImp;

    @Autowired
    SUConnectionRepository sIsuConnectionRepo;

    @Autowired
    Encoder encoder;
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserSuspensionService suspensionService;
    @Autowired
    AdminService adminService;
    @Autowired
    UserService userService;
    @Autowired
    StockService stockService;
    @Autowired
    StoreService storeService;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    OrderService orderService;

    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;
    String Admin;

    @BeforeEach
    void setup() throws Exception {
        System.out.println("===== SETUP RUNNING =====");

        GToken = userService.generateGuest();
        userService.register(GToken, "user", "user", 25);
        NGToken = userService.login(GToken, "user", "user");

        String OToken = userService.generateGuest();
        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        int productId = stockService.addProduct(NOToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);

        assertEquals(1, stockService.addItem(createdStoreId, NOToken, productId, 5, 2000, Category.ELECTRONICS));
        itemStoreDTO = new ItemStoreDTO(1, 2, 2000, Category.ELECTRONICS, 0, createdStoreId, "Laptop");
        stockService.setProductToRandom(NOToken, productId, 1, 2000, createdStoreId, 5000);
        stockService.setProductToBid(NOToken, createdStoreId, productId, 1);
        stockService.setProductToAuction(NOToken, createdStoreId, productId, 1, 1000, 2);
        assertTrue(stockService.getAllAuctions(NOToken, createdStoreId).length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId).length == 1);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId).length == 1);

        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        Admin = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(Admin, "123321", 6);

        // ======================= SECOND GUEST SETUP =======================

    }

    @AfterEach

    void tearDown() {
        userRepo.clear();
        storeRepository.clear();
        stockRepository.clear();
        orderRepository.clear();
        suspensionRepo.clear();
        purchaseRepository.clear();
        sIsuConnectionRepo.clear();

    }

    // Needs Fixing!
    // AddBID
    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID() throws Exception {

        // Act
        int bidId =  stockService.setProductToBid(NOToken, 1, 1, 1);

         boolean a =stockService.addRegularBid(NGToken, bidId, 1, 10);
         assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].getStatus().equals(Status.BID_PENDING));
        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.acceptBid(NOToken, 1, bidId, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].getId());
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].getStatus().equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(10,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(10, r.getFinalPrice());

        // <<<<<<< HEAD

        // Assert
    }

    @Test
    void Add_BidProductToSpecialCart_Success_rejectBID() throws Exception {
        stockService.setProductToBid(NOToken, 1, 1, 1);

        // Act

        stockService.addRegularBid(NGToken, 1, 1, 10);
        stockService.addRegularBid(NGToken, 1, 1, 10);

        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.rejectBid(NOToken, 1, 1, 2);

        // <<<<<<< HEAD

        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);

        // Assert
    }

    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        // <<<<<<< HEAD

        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(token, 0, 100, 30.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    // @Test
    // void Add_BidProduct_Failure_UserSuspended() throws Exception {
    // // <<<<<<< HEAD

    // suspensionService.suspendRegisteredUser(2, 1, Admin);

    // UIException ex = assertThrows(UIException.class, () ->
    // stockService.addRegularBid(NGToken, 1, 1, 30.0)
    // );

    // assertEquals("Suspended user trying to perform an action", ex.getMessage());
    // }

    @Test
    void Add_BidProduct_Failure_StoreNotFound() throws Exception {
        // <<<<<<< HEAD

        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(NGToken, 0, 2, 30.0));

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_BidNotFound() throws Exception {
        // <<<<<<< HEAD

        // Bid ID not found

        DevException ex = assertThrows(DevException.class, () -> stockService.addRegularBid(NGToken, 2, 1, 30.0));

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    // Needs Fixing!
    // AddAUCTION
    @Test
    void Add_AuctionBidToSpecialCart_Success_won() throws Exception {
        stockService.addBidOnAucction(NGToken, 1, 1, 10);
        assertTrue(stockService.getAllAuctions(NOToken, 1).length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_PENDING));

        Thread.sleep(500);

        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_WON));

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(10,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(10, r.getFinalPrice());

    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_lost() throws Exception {
        stockService.addBidOnAucction(NGToken, 1, 1, 10);
        assertTrue(stockService.getAllAuctions(NOToken, 1).length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_PENDING));
        stockService.addBidOnAucction(NGToken, 1, 1, 20);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 2);

        Thread.sleep(500);

        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].getStatus().equals(Status.AUCTION_LOSED));

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails));
        assertEquals("Product not available", ex.getMessage());

    }

    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(token, 555, 100, 60.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    // @Test
    // void Add_AuctionBid_Failure_UserSuspended() throws Exception {

    // suspensionService.suspendRegisteredUser(2, 1, Admin);

    // UIException ex = assertThrows(UIException.class, () ->
    // stockService.addBidOnAucction(NGToken, 1, 1, 60.0)
    // );

    // assertEquals("Suspended user trying to perform an action", ex.getMessage());
    // }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(NGToken, 1, 2, 60.0));

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_AuctionNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(NGToken, 2, 1, 60.0));

        // assertEquals("Auction ID not found in active auctions!", ex.getMessage());
    }

    // Needs Fixing!
    // //AddRANDOM
    @Test
    void Set_ProductToRandom_Success() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.participateInRandom(NGToken, 1, 1, 2000, paymentDetails);

        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations.length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations[0].won());
        // assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].winner.userId==4);

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(0, r.getFinalPrice());

    }

    // Needs Fixing!
    // @Test
    void Set_ProductToRandom_didntwin() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.participateInRandom(NGToken, 1, 1, 1, paymentDetails);
        purchaseService.participateInRandom(NOToken, 1, 1, 1999, paymentDetails);

        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations.length == 2);
        assertFalse(stockService.getAllRandomInStore(NOToken, 1)[0].participations[0].won());
        // assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].winner.userId==4);

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(0, receipts.length);

    }

    @Test
    void Set_ProductToRandom_Failure_InvalidToken() throws UIException {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom("INvalid", 1, 1, 100, paymentDetails));

        assertEquals("Invalid token!", ex.getMessage());
    }

    // @Test
    // void Set_ProductToRandom_Failure_UserSuspended() throws Exception {
    // PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if
    // needed

    // suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1,
    // Admin);
    // UIException ex = assertThrows(UIException.class, () ->
    // purchaseService.participateInRandom(NGToken,1 , 1, 100, paymentDetails)
    // );

    // assertEquals("Suspended user trying to perform an action", ex.getMessage());
    // }

    // Needs Fixing!
    @Test
    void Set_ProductToRandom_Failure_InvalidPrice() throws Exception {

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom(NGToken, 1, 1, 0, paymentDetails));

        assertEquals("Product price must be positive!", ex.getMessage());
    }

}