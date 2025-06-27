package workshop.demo.IntegrationTests.ServiceTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.method.P;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.ApplicationLayer.ActivePurchasesService;
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
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.Status;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.BID;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.InfrastructureLayer.*;


@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PurchaseTests {
    @Autowired
    StoreTreeJPARepository tree;
    @Autowired
    private NodeJPARepository node;
    // @Autowired
    // private NotificationRepository notificationRepository;

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private IStockRepoDB stockRepositoryjpa;
    @Autowired
    private IStoreRepoDB storeRepositoryjpa;
    @Autowired
    private GuestJpaRepository guestRepo;

    @Autowired
    private IOrderRepoDB orderRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

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

    // ======================== Test Data ========================
    String NOToken;
    String NGToken;
    String GToken;
    String Admin;
    ItemStoreDTO itemStoreDTO;
    int productId_laptop;

    int createdStoreId;
    int auctionId;

    @BeforeEach
    void setup() throws Exception {

        node.deleteAll();
        orderRepository.deleteAll();
        tree.deleteAll();
        userRepo.deleteAll();

        guestRepo.deleteAll();

        stockRepositoryjpa.deleteAll();
        offerRepo.deleteAll();
        storeRepositoryjpa.deleteAll();
        storeStockRepo.deleteAll();
        activePurchasesRepo.deleteAll();


        orderRepository.deleteAll();
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
        itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");
        // not ready
        //  activePurcheses.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);
        auctionId= activePurcheses.setProductToAuction(NOToken, createdStoreId, productId_laptop, 1, 1000, 2);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId).length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId).length == 1);
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

        // Act
        stockService.setProductToBid(NOToken, 1, 1, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, createdStoreId, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, 1)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
        stockService.getAllBidsStatus(NOToken, createdStoreId);

        assertFalse(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        stockService.acceptBid(NOToken, createdStoreId, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
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
            assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
            assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 7);
            assertTrue(userService.getRegularCart((NGToken)).length == 0);
            assertTrue(userService.getSpecialCart(NGToken).length==0);

        } catch (Exception exception) {
            System.out.println(exception);
        }

        // <<<<<<< HEAD
        // Assert
    }

    @Test
    void Add_BidProductToSpecialCart_Success_rejectBID() throws Exception {
        int x = stockService.setProductToBid(NOToken, createdStoreId, 1, 1);

        stockService.addRegularBid(NGToken, x, createdStoreId, 10);

        assertFalse(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        stockService.rejectBid(NOToken, 1, x, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        assertFalse(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        assertNotNull(receipts);
        assertEquals(0, receipts.length);

        assertTrue(userService.getRegularCart((NGToken)).length == 0);
        assertTrue(userService.getSpecialCart(NGToken).length==0);
    }

    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        // <<<<<<< HEAD
        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(token, 0, createdStoreId, 30.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_UserSuspended() throws Exception {
        // <<<<<<< HEAD

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(NGToken, 1, createdStoreId, 30.0));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_StoreNotFound() throws Exception {
        // <<<<<<< HEAD

        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(NGToken, 0, 2, 30.0));

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_BidNotFound() throws Exception {

        DevException ex = assertThrows(DevException.class, () -> stockService.addRegularBid(NGToken, 2, createdStoreId, 30.0));

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_won() throws Exception {
        activePurcheses.addBidOnAucction(NGToken, 1, createdStoreId, 10);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId).length == 1);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids.length == 1);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);

        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_WON));

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
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice() == 10);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
        assertTrue(stockService.getProductsInStore(createdStoreId).length == 1);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getProductId() == productId_laptop);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 9);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getPrice() == 2000);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 8);
        assertTrue(userService.getRegularCart((NGToken)).length == 0);
        assertTrue(userService.getSpecialCart(NGToken).length==0);

    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_lost() throws Exception {
        activePurcheses.addBidOnAucction(NGToken, 1, createdStoreId, 10);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId).length == 1);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids.length == 1);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_PENDING));
        activePurcheses.addBidOnAucction(NOToken, 1, createdStoreId, 20);
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids.length == 2);

        Thread.sleep(500);

        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[0].status.equals(Status.AUCTION_LOSED));
        assertTrue(activePurcheses.getAllActiveAuctions_user(NOToken, createdStoreId)[0].bids[1].status.equals(Status.AUCTION_WON));

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertTrue(userService.getSpecialCart(NGToken).length==0);

        purchaseService.finalizeSpecialCart(NOToken, paymentDetails, supplyDetails);

        assertTrue(userService.getSpecialCart(NGToken).length==0);

    }

    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        UIException ex = assertThrows(UIException.class, () -> activePurcheses.addBidOnAucction(token, 555, createdStoreId, 60.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        UIException ex = assertThrows(UIException.class, () -> activePurcheses.addBidOnAucction(NGToken, 1, createdStoreId, 60.0));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> activePurcheses.addBidOnAucction(NGToken, 1, 2, 60.0));

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
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
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertEquals(8, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 2000, paymentDetails);

        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations.length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations[0].won());

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

        assertTrue(orderService.getReceiptDTOsByUser(NGToken).size() == 1);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getFinalPrice() == 0);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().size() == 1);
        assertTrue(orderService.getReceiptDTOsByUser(NGToken).get(0).getProductsList().get(0).getProductId() == 1);
        assertTrue(userService.getRegularCart((NGToken)).length == 0);
        assertTrue(userService.getSpecialCart(NGToken).length==0);

    }

    @Test
    void Set_ProductToRandom_time() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertEquals(8, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 200, paymentDetails);
        Thread.sleep(1000);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations.length == 1);
        assertFalse(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations[0].won());

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertNotNull(receipts);
        assertEquals(0, receipts.length);

        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations[0].mustRefund);

        assertEquals(10, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
    }

    @Test
    void Set_ProductToRandom_time1() throws Exception {
        PaymentDetails paymentDetails1 = new PaymentDetails("111", "test", "12/25", "113");// fill if needed

        PaymentDetails paymentDetails = new PaymentDetails("111", "test", "12/25", null);// fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertEquals(8, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 200, paymentDetails1);
        Thread.sleep(1000);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations.length == 1);
        assertFalse(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations[0].won());

        assertThrows(Exception.class, () -> purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails));

    }

    @Test
    void Set_ProductToRandom_time2() throws Exception {
        PaymentDetails paymentDetails1 = new PaymentDetails("111", "test", "12/25", "113");// fill if needed

        PaymentDetails paymentDetails = new PaymentDetails(null, "test", "12/25", "113");// fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        assertEquals(8, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 200, paymentDetails1);
        Thread.sleep(1000);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations.length == 1);
        assertFalse(stockService.getAllRandomInStore(NOToken, createdStoreId)[0].participations[0].won());

        assertThrows(Exception.class, () -> purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails));

    }

    // Needs Fixing!
    @Test
    void Set_ProductToRandom_didntwin() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 1, paymentDetails);
        purchaseService.participateInRandom(NOToken, 1, createdStoreId, 1999, paymentDetails);

        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations.length == 2);
        ParticipationInRandomDTO player1 = stockService.getAllRandomInStore(NOToken,
                createdStoreId)[0].participations[0];
        ParticipationInRandomDTO player2 = stockService.getAllRandomInStore(NOToken,
                createdStoreId)[0].participations[1];
        xorAssert(player1.won(), player2.won());
        // assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].winner.userId==4);

        ReceiptDTO[] receipts1 = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        ReceiptDTO[] receipts2 = purchaseService.finalizeSpecialCart(NOToken, paymentDetails, supplyDetails);

        xorAssert(receipts1.length == 1, receipts2.length == 1);

        assertTrue(userService.getSpecialCart(NOToken).length==0);
        assertTrue(userService.getSpecialCart(NGToken).length==0);
    }

    private void xorAssert(boolean a, boolean b) {
        assertTrue(a || b);
        assertFalse(a && b);
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

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom(NGToken, 1, createdStoreId, 0, paymentDetails));

        assertEquals("Product price must be positive!", ex.getMessage());
    }

    @Test
    void testgetAllActiveAuctions_user_Fail_NoPermission() throws Exception {
        // Step 1: Register and login user who is NOT a store manager/owner
        String token = userService.generateGuest();
        userService.register(token, "noperm", "noperm", 30);
        String noPermToken = userService.login(token, "noperm", "noperm");

        // Step 2: Try to get auctions — should fail
        Exception ex = assertThrows(Exception.class, () -> {
            activePurcheses.getAllActiveAuctions_user(noPermToken, createdStoreId);
        });

    }

    @Test
    void testgetAllActiveAuctions_user_Fail_ManagerWithNoPermission() throws Exception {
        // Step 1: Register and login the manager
        String token = userService.generateGuest();
        userService.register(token, "noPermManager", "noPermManager", 30);
        String managerToken = userService.login(token, "noPermManager", "noPermManager");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Offer manager role with NO permissions
        List<Permission> emptyPerms = new ArrayList<>();
        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noPermManager", emptyPerms);
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noPermManager", true, false);

        // Step 3: Manager tries to get auctions (should fail due to missing SpecialType
        // permission)
        UIException ex = assertThrows(UIException.class, () -> {
            activePurcheses.getAllActiveAuctions_user(managerToken,createdStoreId);
        });

        // Step 4: Assert
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("you have no permession to see auctions info.", ex.getMessage());
    }

    @Test
    void testgetAllActiveAuctions_userUser_Success() throws Exception {
        AuctionDTO[] auctions = activePurcheses.getAllActiveAuctions_user(NGToken, createdStoreId); // NGToken is a registered user in setup
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
        UIException ex = assertThrows(UIException.class, () -> {
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
            activePurcheses.setProductToAuction(managerToken, createdStoreId, itemStoreDTO.getProductId(), 1, 1000, 100);
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
            stockService.setProductToBid(mgrToken, createdStoreId, itemStoreDTO.getProductId(), 1);
        });

        assertEquals("you have no permession to set product to bid.", ex.getMessage());
    }

    @Test
    void testGetAllBidsStatus_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noBidView", "noBidView", 30);
        String managerToken = userService.login(token, "noBidView", "noBidView");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noBidView", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noBidView", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus(managerToken, createdStoreId);
        });

        assertEquals("you have no permession to see auctions info.", ex.getMessage());
    }

    @Test
    void testGetAllBidsStatusUser_Success() throws Exception {
        BidDTO[] bids = stockService.getAllBidsStatus_user(NGToken, createdStoreId);
        assertNotNull(bids);
        // assuming at least one bid exists from setup or previous tests
        assertTrue(bids.length >= 0);
    }

    @Test
    void testGetAllBidsStatusUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus_user("invalid-token", createdStoreId);
        });
    }

    @Test
    void testGetAllBidsStatusUser_NotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // Not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus_user(guestToken, createdStoreId);
        });
    }

    @Test
    void testGetAllBidsStatusUser_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus_user(NGToken, 9999); // invalid store ID
        });
    }

    @Test
    void testAcceptBid_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noAcceptPerm", "noAcceptPerm", 30);
        String managerToken = userService.login(token, "noAcceptPerm", "noAcceptPerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(createdStoreId, NOToken, "noAcceptPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(createdStoreId, authRepo.getUserName(NOToken), "noAcceptPerm", true, false);

        int i=     stockService.setProductToBid(NOToken, createdStoreId, itemStoreDTO.getProductId(), 1);
        stockService.addRegularBid(NGToken, i, itemStoreDTO.getProductId(), 10);
        int bidToAcceptId = stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].id;

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.acceptBid(managerToken, createdStoreId, i, bidToAcceptId);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getErrorCode());
        assertEquals("you have no permession to accept bid", ex.getMessage());
    }

    @Test
    void testRejectBid_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noRejectPerm", "noRejectPerm", 30);
        String managerToken = userService.login(token, "noRejectPerm", "noRejectPerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noRejectPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noRejectPerm", true, false);

        stockService.setProductToBid(NOToken, 1, itemStoreDTO.getProductId(), 1);
        stockService.addRegularBid(NGToken, 1, itemStoreDTO.getProductId(), 10);
        int bidId = 1;
        int bidToRejectId = stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].id;

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.rejectBid(managerToken, createdStoreId, bidId, bidToRejectId);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getErrorCode());
        assertEquals("you have no permession to accept bid", ex.getMessage()); // Note: still says "accept bid"
    }
// not ready
//    @Test
//    void testEndBid_Success() throws Exception {
//        PaymentDetails paymentDetails = PaymentDetails.testPayment();
//
//        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 2000, paymentDetails);
//        ParticipationInRandomDTO result = activePurcheses.endBid(NOToken, createdStoreId, 1);
//        assertNotNull(result);
//        assertTrue(result.ended);
//    }

   
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
            stockService.getAllRandomInStore(managerToken, createdStoreId);
        });

        // Step 4: Validate exception
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("you have no permession to see random info.", ex.getMessage());
    }
    // not ready
    @Test
    void testGetAllRandomInStoreUser_Success() throws Exception {
        RandomDTO[] randoms = stockService.getAllRandomInStore_user(NGToken, createdStoreId);
        assertNotNull(randoms);
        assertTrue(randoms.length >= 1); // setup contains 1 random sale
        assertEquals(itemStoreDTO.getProductId(), randoms[0].productId);
    }
    // not ready
    @Test
    void testGetAllRandomInStoreUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore_user("invalid-token", createdStoreId);
        });
    }
    // Not ready
    @Test
    void testGetAllRandomInStoreUser_NotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore_user(guestToken, createdStoreId);
        });
    }
    // not ready
    @Test
    void testGetAllRandomInStoreUser_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore_user(NGToken, 9999);
        });
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID_invalidpayment() throws Exception {

        // Act
        stockService.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, createdStoreId, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, createdStoreId)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
        assertFalse(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        stockService.acceptBid(NOToken, createdStoreId, 1, stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].id);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.test_fail_Payment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        assertThrows(UIException.class, () -> {
            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        });

        // <<<<<<< HEAD
        // Assert
    }
    // not ready
    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID_invalidsupply() throws Exception {

        // Act
        stockService.setProductToBid(NOToken, createdStoreId, productId_laptop, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, createdStoreId, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, 1)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_PENDING));
        assertFalse(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        stockService.acceptBid(NOToken, createdStoreId, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);
        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.test_fail_supply(); // fill if needed

        assertThrows(UIException.class, () -> {
            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        });

        // <<<<<<< HEAD
        // Assert
    }



    //not ready
    @Test
    void testGetSpecialCart_BidRandomAuction() throws Exception {
        // ===== SETUP FOR BID =====
        int bidId = stockService.setProductToBid(NOToken, 1, 1, 1); // bidId = 1
        stockService.addRegularBid(NGToken, bidId, 1, 15);
        stockService.acceptBid(NOToken, 1, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        // ===== SETUP FOR RANDOM =====
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        purchaseService.participateInRandom(NGToken, 1, createdStoreId, 1200, paymentDetails);

        // ===== SETUP FOR AUCTION =====
        int auctionId = activePurcheses.setProductToAuction(NOToken, 1, 1, 1, 5000, 10);
        activePurcheses.addBidOnAucction(NGToken, auctionId, 1, 10);

        // ===== EXECUTE =====
        SpecialCartItemDTO[] result = userService.getSpecialCart(NGToken);

        // ===== ASSERT =====
        assertEquals(3, result.length);
        Set<SpecialType> types = Arrays.stream(result).map(r -> r.type).collect(Collectors.toSet());

        assertTrue(types.contains(SpecialType.BID));
        assertTrue(types.contains(SpecialType.Random));
        assertTrue(types.contains(SpecialType.Auction));

        for (SpecialCartItemDTO dto : result) {
            assertEquals(createdStoreId, dto.storeId); // Ensure all are for the correct store
            assertEquals("Laptop", dto.productName); // Assuming product name used in setup
        }

    }

    @Test
    void test_addProductToBid_invalidQuantity_throwsException() {

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToBid(NOToken, createdStoreId, productId_laptop, -1); // or -1
        });

        assertEquals("Quantity must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_BID_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_acceptBid_invalidBidId_throwsException() throws UIException {
        // Arrange
        int invalidBidId = 999; // assume not created
        int anyUserId = authRepo.getUserId(NGToken); // assuming valid logged-in user

        // Act + Assert
        DevException ex = assertThrows(DevException.class, () -> {
            stockService.acceptBid(NOToken, createdStoreId, invalidBidId, 1);
        });

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    @Test
    void test_rejectBid_invalidBidId_throwsException() throws UIException {
        // Arrange
        int invalidBidId = 999;
        int anyUserId = authRepo.getUserId(NGToken);

        // Act + Assert
        DevException ex = assertThrows(DevException.class, () -> {
            stockService.rejectBid(NOToken, createdStoreId, invalidBidId, 1);
        });

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    @Test
    void test_addProductToRandom_invalidQuantity_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, productId_laptop, 0, 2000, createdStoreId, 1000); // quantity = 0
        });

        assertEquals("Quantity must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_addProductToRandom_invalidPrice_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, productId_laptop, 1, 0, createdStoreId, 1000); // price = 0
        });

        assertEquals("Product price must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_addProductToRandom_invalidTime_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 0); // time = 0
        });

        assertEquals("Random time must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_participateInRandom_invalidRandomId_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            // 999 is a non-existing randomId
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            purchaseService.participateInRandom(NGToken, 999, createdStoreId, 11, paymentDetails);
        });

        assertEquals("Random ID not found!", ex.getMessage());
        assertEquals(ErrorCodes.RANDOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void test_participateInRandom_invalidPrice_throwsException() throws Exception {
        int randomId = stockService.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 1000);

        UIException ex = assertThrows(UIException.class, () -> {
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            purchaseService.participateInRandom(NGToken, randomId, createdStoreId, 0, paymentDetails);
        });

        assertEquals("Product price must be positive!", ex.getMessage());
    }

    @Test
    void test_getProductPrice_success() throws Exception {
        var price = stockService.getAllRandomInStore(NOToken, createdStoreId)[0].productPrice;
        assertEquals(2000, price);
    }





}
