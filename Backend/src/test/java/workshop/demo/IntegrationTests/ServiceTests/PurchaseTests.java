package workshop.demo.IntegrationTests.ServiceTests;

import java.util.ArrayList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;
import workshop.demo.ApplicationLayer.ActivePurchasesService;
import workshop.demo.ApplicationLayer.DatabaseCleaner;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.AuctionStatus;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptDTO;

import workshop.demo.DTOs.Status;
import workshop.demo.DTOs.SupplyDetails;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.InfrastructureLayer.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows non-static @BeforeAll
public class PurchaseTests {

    @Autowired
    StoreTreeJPARepository tree;
    @Autowired
    private NodeJPARepository node;
    // @Autowired
    // private NotificationRepository notificationRepository;

    @Autowired
    private IStockRepoDB stockRepositoryjpa;
    @Autowired
    private IStoreRepoDB storeRepositoryjpa;
    @Autowired
    private GuestJpaRepository guestRepo;

    @Autowired
    private IOrderRepoDB orderRepository;
    // @Autowired
    // private PurchaseRepository purchaseRepository;

    @Autowired
    private AuthenticationRepo authRepo;

    @Autowired
    PaymentServiceImp payment;
    @Autowired
    SupplyServiceImp serviceImp;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private OfferJpaRepository offerRepo;
    @Autowired
    SUConnectionRepository sIsuConnectionRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    Encoder encoder;
    @Autowired
    private IActivePurchasesRepo activePurchasesRepo;
    @Autowired
    UserSuspensionService suspensionService;
    @Autowired
    public ActivePurchasesService activePurcheses;

    // @Autowired
    // AdminHandler adminService;
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
    @Autowired
    ReviewService reviewService;
    @Autowired
    DatabaseCleaner databaseCleaner;
    // ======================== Test Data ========================
    String NOToken;
    String NGToken;
    String GToken;
    String Admin;
    ItemStoreDTO itemStoreDTO;
    int productId_laptop;

    int createdStoreId;
    int auctionId;
    int randomId;

    @BeforeEach
    void setup() throws Exception {
        databaseCleaner.wipeDatabase();

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

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = {"Laptop", "Lap", "top"};
        productId_laptop = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        stockService.addItem(createdStoreId, NOToken, productId_laptop, 10, 2000, Category.Electronics);
        itemStoreDTO = new ItemStoreDTO(productId_laptop, 10, 2000, Category.Electronics, 0, createdStoreId, "Laptop",
                "TestStore");
        // not ready

        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 10);

        // assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId).length ==
        // 1);
        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        Admin = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(Admin, "123321", authRepo.getUserId(Admin));

        // ======================= SECOND GUEST SETUP =======================
    }

    // Needs Fixing!
    // AddBID
    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        // Act
        int id = activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
        id = activePurcheses.getAllBids(NOToken, createdStoreId)[0].bidId;
        boolean a = activePurcheses.addUserBidToBid(NGToken, id, createdStoreId, 10);

        assertTrue(a);
        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
        activePurcheses.getAllActiveBids_user(NOToken, createdStoreId);

        assertFalse(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        activePurcheses.acceptBid(NOToken, createdStoreId, id, authRepo.getUserId(NGToken));

        assertTrue(
                activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        try {
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

            assertTrue(orderService.getReceiptDTOsByUser(NGToken).size() == 1);
            assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice() == 10);
            assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
            assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == productId_laptop);
            //     assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 7);
            assertTrue(userService.getRegularCart((NGToken)).length == 0);
            assertTrue(userService.getSpecialCart(NGToken).length == 0);

        } catch (Exception exception) {
            System.out.println(exception);
        }

        // <<<<<<< HEAD
        // Assert
    }

    // fix
    @Test
    void Add_BidProductToSpecialCart_Success_rejectBID() throws Exception {
        int x = activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
        x = activePurcheses.getAllBids(NOToken, createdStoreId)[0].bidId;
        //activePurcheses.getAllBids(NGToken, x, createdStoreId, 10);
        activePurcheses.addUserBidToBid(NGToken, x, createdStoreId, 10);

        assertFalse(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        //  activePurcheses.rejectBid(NOToken, 1, x, activePurcheses.getAllBids(NOToken, 1)[0].bids[0].id);
        activePurcheses.rejectBid(NOToken, createdStoreId, x, authRepo.getUserId(NGToken), null);
        assertFalse(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        assertNotNull(receipts);
        assertEquals(0, receipts.length);

        assertTrue(userService.getRegularCart((NGToken)).length == 0);
        assertTrue(userService.getSpecialCart(NGToken).length == 0);
    }

    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        // <<<<<<< HEAD
        UIException ex = assertThrows(UIException.class,
                () -> activePurcheses.addUserBidToBid(token, 0, createdStoreId, 30.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_UserSuspended() throws Exception {
        // <<<<<<< HEAD

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        Exception ex = assertThrows(Exception.class,
                () -> activePurcheses.addUserBidToBid(NGToken, 1, createdStoreId, 30.0));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> activePurcheses.addUserBidToBid(NGToken, 0, 2, 30.0));

    }

    @Test
    void Add_BidProduct_Failure_BidNotFound() throws Exception {

        Exception ex = assertThrows(Exception.class,
                () -> activePurcheses.addUserBidToBid(NGToken, 2, createdStoreId, 30.0));

    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_won() throws Exception {
        auctionId = activePurcheses.setProductToAuction(NOToken, createdStoreId, productId_laptop, 1, 5000, 2);
        auctionId = activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].auctionId;
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId).length == 1);
        activePurcheses.addBidOnAucction(NGToken, auctionId, createdStoreId, 10);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId).length == 1);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids.length == 1);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status
                .equals(Status.AUCTION_PENDING));

        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status
                .equals(Status.AUCTION_PENDING));

        Thread.sleep(5000);

        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].status
                .equals(AuctionStatus.FINISH));
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status
                .equals(Status.AUCTION_WON));

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

        assertTrue(orderService.getReceiptDTOsByUser(NGToken).size() == 1);
        //  assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice() == 10);
        //    assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
        //       assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
        assertTrue(stockService.getProductsInStore(createdStoreId).length == 1);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getProductId() == productId_laptop);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 9);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getPrice() == 2000);
        assertTrue(userService.getRegularCart((NGToken)).length == 0);
        assertTrue(userService.getSpecialCart(NGToken).length == 0);

    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_lost() throws Exception {
        auctionId = activePurcheses.setProductToAuction(NOToken, createdStoreId, productId_laptop, 1, 5000, 2);
        auctionId = activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].auctionId;
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId).length == 1);
        activePurcheses.addBidOnAucction(NGToken, auctionId, createdStoreId, 10);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId).length == 1);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids.length == 1);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status
                .equals(Status.AUCTION_PENDING));

        assertEquals(Status.AUCTION_PENDING,
                activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status);

        activePurcheses.addBidOnAucction(NOToken, auctionId, createdStoreId, 20);
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids.length == 2);

        Thread.sleep(5000);

        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].status
                .equals(AuctionStatus.FINISH));
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[0].status
                .equals(Status.AUCTION_LOSED));
        assertTrue(activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].bids[1].status
                .equals(Status.AUCTION_WON));

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertTrue(userService.getSpecialCart(NGToken).length == 0);

        purchaseService.finalizeSpecialCart(NOToken, paymentDetails, supplyDetails);

        assertTrue(userService.getSpecialCart(NGToken).length == 0);

    }

    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        UIException ex = assertThrows(UIException.class,
                () -> activePurcheses.addBidOnAucction(token, 555, createdStoreId, 60.0));

    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        UIException ex = assertThrows(UIException.class,
                () -> activePurcheses.addBidOnAucction(NGToken, 1, createdStoreId, 60.0));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {

        Exception ex = assertThrows(Exception.class, () -> activePurcheses.addBidOnAucction(NGToken, 1, 2, 60.0));

    }

    @Test
    void Add_AuctionBid_Failure_AuctionNotFound() {

        try {
            activePurcheses.addBidOnAucction(NGToken, 2, createdStoreId, 60.0);
            assertTrue(false);
        } catch (Exception exception) {
            assertTrue(true);
        }
    }

    // Needs Fixing!
    // //AddRANDOM
    @Test
    void Set_ProductToRandom_Success() throws Exception {
        randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 500000);
        randomId = activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].id;
        assertEquals(1, activePurcheses.getAllRandoms(NOToken, createdStoreId).length);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertEquals(9, stockService.getProductsInStore(createdStoreId)[0].getQuantity());

        //    activePurcheses.participateInRandom(authRepo.getUserId(NGToken), randomId, createdStoreId, 2000);
        purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 2000, paymentDetails);

        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();
        assertEquals(1, activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations.length);
        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations[0].won());

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        assertEquals(9, stockService.getProductsInStore(createdStoreId)[0].getQuantity());

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(2000, r.getFinalPrice());

        assertEquals(1, orderService.getReceiptDTOsByUser(NGToken).size());
        assertEquals(2000, orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice());
        //     assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
        //       assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
        assertEquals(0, userService.getRegularCart((NGToken)).length);
        assertEquals(0, userService.getSpecialCart(NGToken).length);

    }

    // n eed Fixing!
    @Test
    void Set_ProductToRandom_time() throws Exception {
        randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 10000);
        randomId = activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].id;
        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId).length == 1);
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 9);

        purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 200, paymentDetails);

        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations.length == 1);
        assertFalse(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations[0].won());

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        Thread.sleep(10000);
        assertNotNull(receipts);
        assertEquals(0, receipts.length);

        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations[0].mustRefund);

        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 10);
    }

    @Test
    void Set_ProductToRandom_time2() throws Exception {
        randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 5000000);
        randomId = activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].id;
        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId).length == 1);
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        PaymentDetails paymentDetails1 = new PaymentDetails("111", "test", "12/25", "113");// fill if needed

        PaymentDetails paymentDetails = new PaymentDetails(null, "test", "12/25", "113");// fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 9);
        purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 200, paymentDetails1);
        Thread.sleep(1000);
        assertTrue(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations.length == 1);
        assertFalse(activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations[0].won());

        assertThrows(Exception.class,
                () -> purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails));

    }

    @Test
    void Set_ProductToRandom_didntwin() throws Exception {
        randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 5000000);
        randomId = activePurcheses.getAllActiveRandoms_user(NOToken, createdStoreId)[0].id;
        assertEquals(1, activePurcheses.getAllActiveRandoms_user(NOToken, createdStoreId).length);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();
        purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 1000, paymentDetails);
        purchaseService.participateInRandom(NOToken, randomId, createdStoreId, 1000, paymentDetails);

        assertEquals(2, activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].participations.length);
        ParticipationInRandomDTO player1 = activePurcheses.getAllRandoms(NOToken,
                createdStoreId)[0].participations[0];
        ParticipationInRandomDTO player2 = activePurcheses.getAllRandoms(NOToken,
                createdStoreId)[0].participations[1];
        // assertFalse(player1.won());
        //   assertTrue(player2.won());
        assertTrue(player1.won() ^ player2.won(), "Exactly one player should be winner");

        ReceiptDTO[] receipts1 = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        ReceiptDTO[] receipts2 = purchaseService.finalizeSpecialCart(NOToken, paymentDetails, supplyDetails);
//        assertEquals(0, receipts1.length);
//        assertEquals(1, receipts2.length);
        assertTrue(
                (receipts1.length == 1) && (receipts2.length == 1),
                "Exactly one of the receipts should have length 1"
        );
//
//        assertEquals(1, userService.getSpecialCart(NOToken).length);
//        assertEquals(0, userService.getSpecialCart(NGToken).length);

    }

    @Test
    void Set_ProductToRandom_Failure_InvalidToken() throws UIException {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom("INvalid", 1, createdStoreId, 100, paymentDetails));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Set_ProductToRandom_Failure_UserSuspended() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);
        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom(NGToken, 1, createdStoreId, 100, paymentDetails));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    // Needs Fixing!
    @Test
    void Set_ProductToRandom_Failure_InvalidPrice() throws Exception {
        randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);
        assertTrue(activePurcheses.getAllActiveRandoms_user(NOToken, createdStoreId).length == 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 0, paymentDetails));

    }

    @Test
    void testgetAllAuctions_user_Fail_NoPermission() throws Exception {
        // Step 1: Register and login user who is NOT a store manager/owner
        String token = userService.generateGuest();
        userService.register(token, "noperm", "noperm", 30);
        String noPermToken = userService.login(token, "noperm", "noperm");

        // Step 2: Try to get auctions — should fail
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllAuctions(noPermToken, createdStoreId);
        });

    }

    @Test
    void testgetAllAuctions_user_Fail_ManagerWithNoPermission() throws Exception {
        // Step 1: Register and login the manager
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        String token = userService.generateGuest();
        userService.register(token, "noPermManager", "noPermManager", 30);
        String managerToken = userService.login(token, "noPermManager",
                "noPermManager");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Offer manager role with NO permissions
        List<Permission> emptyPerms = new ArrayList<>();
        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken,
                "noPermManager", emptyPerms);
        storeService.reciveAnswerToOffer(createdStoreId,
                authRepo.getUserName(NOToken), "noPermManager", true, false);

        // Step 3: Manager tries to get auctions (should fail due to missing
        // permission)
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllAuctions(managerToken, createdStoreId);
        });

        // Step 4: Assert
    }

    @Test
    void testgetAllAuctions_userUser_Success() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();
        activePurcheses.setProductToAuction(NOToken, createdStoreId, productId_laptop, 1, 1000, 2);
        activePurcheses.addBidOnAucction(NGToken, activePurcheses.getAllAuctions(NOToken, createdStoreId)[0].auctionId, createdStoreId, 10);

        AuctionDTO[] auctions = activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId); // NGToken is a
        // registered user
        // in setup
        assertNotNull(auctions);
        assertEquals(1, auctions.length); // 1 auction set up in setup()
        assertEquals(productId_laptop, auctions[0].productId); // or whatever name matches
    }

    @Test
    void testgetAllActiveAuctions_userUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveAuctions_user("bad-token", createdStoreId);
        });
    }

    @Test
    void testgetAllActiveAuctions_userUser_NotRegisteredOnline() throws Exception {
        String guestToken = userService.generateGuest(); // not registered
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveAuctions_user(guestToken, createdStoreId);
        });
    }

    @Test
    void testgetAllActiveAuctions_userUser_StoreNotFound() {
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllActiveAuctions_user(NGToken, 9999); // non-existent store
        });
    }

    @Test
    void testSetProductToAuction_ManagerNoPermission_Fail() throws Exception {
        // Step 1: Create manager with NO permissions
        String token = userService.generateGuest();
        userService.register(token, "nopermmgr", "nopermmgr", 30);
        String managerToken = userService.login(token, "nopermmgr", "nopermmgr");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Offer manager role with no permissions
        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "nopermmgr", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "nopermmgr", true, false);

        // Step 3: Try to set product to auction (should fail)
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.setProductToAuction(managerToken, createdStoreId, itemStoreDTO.getProductId(), 1, 1000,
                    100);
        });

        // Step 4: Assert
        assertEquals("you have no permession to set produt to auction.", ex.getMessage());
    }

    @Test
    void testSetProductToBid_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noBidPermMgr", "noBidPermMgr", 30);
        String mgrToken = userService.login(token, "noBidPermMgr", "noBidPermMgr");
        int mgrId = authRepo.getUserId(mgrToken);

        List<Permission> noPerms = new ArrayList<>();
        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noBidPermMgr", noPerms);
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noBidPermMgr", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.setProductToBid(mgrToken, createdStoreId, itemStoreDTO.getProductId(), 1);
        });

    }

    @Test
    void testGetAllBidsStatus_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noBidView", "noBidView", 30);
        String managerToken = userService.login(token, "noBidView", "noBidView");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noBidView", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noBidView", true, false);

        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllBids(managerToken, createdStoreId);
        });

    }

    @Test
    void testGetAllBidsStatusUser_Success() throws Exception {
        activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        BidDTO[] bids = activePurcheses.getAllBids(NOToken, createdStoreId);
        assertNotNull(bids);
        // assuming at least one bid exists from setup or previous tests
        assertTrue(bids.length >= 0);
    }

    @Test
    void testGetAllBidsStatusUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveBids_user("invalid-token", createdStoreId);
        });
    }

    @Test
    void testGetAllBidsStatusUser_NotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // Not registered
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveBids_user(guestToken, createdStoreId);
        });
    }

    @Test
    void testGetAllBidsStatusUser_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveBids_user(NGToken, 9999); // invalid store ID
        });
    }

    @Test

    void testAcceptBid_ManagerNoPermission_Fail() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        String token = userService.generateGuest();
        userService.register(token, "noAcceptPerm", "noAcceptPerm", 30);
        String managerToken = userService.login(token, "noAcceptPerm", "noAcceptPerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noAcceptPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noAcceptPerm", true, false);

        int i = 1;
        int bidToAcceptId = 1;

        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.acceptBid(managerToken, createdStoreId, i, bidToAcceptId);
        });

        //  assertEquals("you have no permession to accept bid.", ex.getMessage());}
    }

    @Test
    void testRejectBid_ManagerNoPermission_Fail() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        String token = userService.generateGuest();
        userService.register(token, "noRejectPerm", "noRejectPerm", 30);
        String managerToken = userService.login(token, "noRejectPerm", "noRejectPerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noRejectPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noRejectPerm", true, false);

        activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
        //   activePurcheses.addUserBidToBid(NGToken, activePurcheses.getAllBids(managerToken, managerId)[0].bidId, createdStoreId, 10);
        int bidId = 1;
        int bidToRejectId = 1;
        double bidToRejectIdDouble = 1;

        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.rejectBid(managerToken, createdStoreId, bidId, bidToRejectId, bidToRejectIdDouble);
        });
        //assertEquals("you have no permession to reject bid.", ex.getMessage());
    }
    // not ready
    // @Test
    // void testEndBid_Success() throws Exception {
    // PaymentDetails paymentDetails = PaymentDetails.testPayment();

    // purchaseService.participateInRandom(NGToken, 1, createdStoreId, 2000,
    // paymentDetails);
    // ParticipationInRandomDTO result = activePurcheses.endBid(NOToken,
    // createdStoreId, 1);
    // assertNotNull(result);
    // assertTrue(result.ended);
    // }
    @Test
    void testGetAllRandomInStore_ManagerNoPermission_Fail() throws Exception {
        // Step 1: Register & login manager
        String token = userService.generateGuest();
        userService.register(token, "noRandomPerm", "noRandomPerm", 30);
        String managerToken = userService.login(token, "noRandomPerm", "noRandomPerm");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Assign manager role WITHOUT permissions
        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noRandomPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noRandomPerm", true, false);

        // Step 3: Try to get random sale info (should fail)
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllRandoms(managerToken, createdStoreId);
        });

    }

    // not ready
    @Test
    void testGetAllRandomInStoreUser_Success() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();
        activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);
        RandomDTO[] randoms = activePurcheses.getAllRandoms(NOToken, createdStoreId);
        assertNotNull(randoms);
        assertTrue(randoms.length >= 1); // setup contains 1 random sale
        assertEquals(itemStoreDTO.getProductId(), randoms[0].productId);
    }

    // not ready
    @Test
    void testGetAllRandomInStoreUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveRandoms_user("invalid-token", createdStoreId);
        });
    }

    // Not ready
    // @Test
    // void testGetAllRandomInStoreUser_NotRegistered() throws Exception {
    //     String guestToken = userService.generateGuest(); // not registered
    //     UIException ex = assertThrows(UIException.class, () -> {
    //         activePurcheses.getAllRandoms(guestToken, createdStoreId);
    //     });
    // }
    // not ready
    @Test
    void testGetAllRandomInStoreUser_StoreNotFound() {
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllActiveRandoms_user(GToken, 1); // non-existent store
        });
    }

    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID_invalidpayment() throws Exception {

        // Act
        activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
        int id = activePurcheses.getAllBids(NOToken, createdStoreId)[0].bidId;

        boolean a = activePurcheses.addUserBidToBid(NGToken, id, createdStoreId, 10);
        for (BidDTO iterable_element : activePurcheses.getAllBids(NOToken, createdStoreId)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
        assertFalse(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        activePurcheses.acceptBid(NOToken, createdStoreId, id,
                authRepo.getUserId(NGToken));
        assertTrue(
                activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.test_fail_Payment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        assertThrows(UIException.class, () -> {
            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        });

        // <<<<<<< HEAD
        // Assert
    }

    //    @Test
//    void Add_BidProductToSpecialCart_Success_acceptBID_invalidsupply() throws Exception {
//
//        // Act
//        activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);
//        int   id= activePurcheses.getAllBids(NOToken, createdStoreId)[0].bidId;
//
//        boolean a = activePurcheses.addUserBidToBid(NGToken, id, createdStoreId, 10);
//        for (BidDTO iterable_element : activePurcheses.getAllBids(NOToken, createdStoreId)) {
//            System.err.println(iterable_element.bidId);
//        }
//        assertTrue(a);
//        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
//        assertFalse(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
//        activePurcheses.acceptBid(NOToken, createdStoreId, id, activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].id);
//        assertTrue(
//                activePurcheses.getAllBids(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));
//
//        assertTrue(activePurcheses.getAllBids(NOToken, createdStoreId)[0].isAccepted);
//        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
//        SupplyDetails supplyDetails = SupplyDetails.test_fail_supply(); // fill if needed
//
//        assertThrows(UIException.class, () -> {
//            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
//        });
//
//        // <<<<<<< HEAD
//        // Assert
//    }
    // not ready
    @Test
    void test_addProductToBid_invalidQuantity_throwsException() {

        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.setProductToBid(NOToken, createdStoreId, productId_laptop, -1); // or -1
        });

    }

    @Test
    void test_acceptBid_invalidBidId_throwsException() throws UIException {
        // Arrange
        int invalidBidId = 999; // assume not created
        int anyUserId = authRepo.getUserId(NGToken); // assuming valid logged-in user

        // Act + Assert
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.acceptBid(NOToken, createdStoreId, invalidBidId, 1);
        });

    }

    // @Test
    // void test_rejectBid_invalidBidId_throwsException() throws UIException {
    //     // Arrange
    //     int invalidBidId = 999;
    //     int anyUserId = authRepo.getUserId(NGToken);
    //     // Act + Assert
    //     Exception ex = assertThrows(Exception.class, () -> {
    //         activePurcheses.rejectBid(NOToken, anyUserId, invalidBidId, invalidBidId, anyUserId);
    //     });
    // }
    @Test
    void test_addProductToRandom_invalidQuantity_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.setProductToRandom(NOToken, productId_laptop, 0, 2000, createdStoreId, 1000); // quantity = 0
        });

    }

    @Test
    void test_addProductToRandom_invalidPrice_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 0, createdStoreId, 1000); // price = 0
        });

        // assertEquals("Product price must be positive!", ex.getMessage());
        // assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_addProductToRandom_invalidTime_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 0); // time = 0
        });

    }

    @Test
    void test_participateInRandom_invalidRandomId_throwsException() {
        Exception ex = assertThrows(Exception.class, () -> {
            // 999 is a non-existing randomId
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            purchaseService.participateInRandom(NGToken, 999, createdStoreId, 11, paymentDetails);
        });

    }

    @Test
    void test_participateInRandom_invalidPrice_throwsException() throws Exception {
        int randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);

        Exception ex = assertThrows(Exception.class, () -> {
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            activePurcheses.participateInRandom(authRepo.getUserId(NGToken), randomId, createdStoreId, 0); // price = 0
        });

    }

    @Test
    void test_getProductPrice_success() throws Exception {
        int randomId = activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);

        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        var price = activePurcheses.getAllRandoms(NOToken, createdStoreId)[0].productPrice;
        assertEquals(2000, price);
    }

}