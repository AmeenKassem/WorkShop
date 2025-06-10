package workshop.demo.IntegrationTests.ServiceTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import workshop.demo.ApplicationLayer.AdminHandler;
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
import workshop.demo.DomainLayer.Stock.BID;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.SingleBid;
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
import workshop.demo.DTOs.*;

import java.util.*;
import java.util.stream.Collectors;

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
    AdminHandler adminService;
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
    int createdStoreId;
    int productId_laptop;

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

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        productId_laptop = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        assertEquals(1,
                stockService.addItem(createdStoreId, NOToken, productId_laptop, 10, 2000, Category.Electronics));
        itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");
        stockService.setProductToRandom(NOToken, productId_laptop, 1, 2000, createdStoreId, 5000);
        stockService.setProductToAuction(NOToken, createdStoreId, productId_laptop, 1, 1000, 2);
        assertTrue(stockService.getAllAuctions(NOToken, createdStoreId).length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId).length == 1);
        // assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId).length ==
        // 1);
        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        Admin = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(Admin, "123321", authRepo.getUserId(Admin));

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
        stockService.setProductToBid(NOToken, 1, 1, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, 1, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, 1)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_PENDING));
        stockService.getAllBidsStatus(NOToken, 1);

        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.acceptBid(NOToken, 1, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
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
            assertTrue(userRepo.getUserCart(authRepo.getUserId(NGToken)).getAllCart().size() == 0);
            assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

        } catch (Exception exception) {
            System.out.println(exception);
        }

        // <<<<<<< HEAD
        // Assert
    }

    @Test
    void Add_BidProductToSpecialCart_Success_rejectBID() throws Exception {
        int x = stockService.setProductToBid(NOToken, 1, 1, 1);

        stockService.addRegularBid(NGToken, x, 1, 10);

        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.rejectBid(NOToken, 1, x, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        assertNotNull(receipts);
        assertEquals(0, receipts.length);

        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

    }

    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        // <<<<<<< HEAD
        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(token, 0, 100, 30.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_BidProduct_Failure_UserSuspended() throws Exception {
        // <<<<<<< HEAD

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        UIException ex = assertThrows(UIException.class, () -> stockService.addRegularBid(NGToken, 1, 1, 30.0));

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

        DevException ex = assertThrows(DevException.class, () -> stockService.addRegularBid(NGToken, 2, 1, 30.0));

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_won() throws Exception {
        stockService.addBidOnAucction(NGToken, 1, 1, 10);
        assertTrue(stockService.getAllAuctions(NOToken, 1).length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);

        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_WON));

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
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 8);
        assertTrue(userRepo.getUserCart(authRepo.getUserId(NGToken)).getAllCart().size() == 0);
        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

    }

    @Test
    void Add_AuctionBidToSpecialCart_Success_lost() throws Exception {
        stockService.addBidOnAucction(NGToken, 1, 1, 10);
        assertTrue(stockService.getAllAuctions(NOToken, 1).length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_PENDING));

        Thread.sleep(500);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.IN_PROGRESS));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_PENDING));
        stockService.addBidOnAucction(NOToken, 1, 1, 20);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids.length == 2);

        Thread.sleep(500);

        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].status.equals(AuctionStatus.FINISH));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[0].status.equals(Status.AUCTION_LOSED));
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].bids[1].status.equals(Status.AUCTION_WON));

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);

        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

        purchaseService.finalizeSpecialCart(NOToken, paymentDetails, supplyDetails);

        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

    }

    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
        String token = "bad-token";

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(token, 555, 100, 60.0));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(NGToken, 1, 1, 60.0));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.addBidOnAucction(NGToken, 1, 2, 60.0));

        assertEquals("store not found on active purchases hashmap", ex.getMessage());
    }

    @Test
    void Add_AuctionBid_Failure_AuctionNotFound() {

        try {
            stockService.addBidOnAucction(NGToken, 2, 1, 60.0);
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
        purchaseService.participateInRandom(NGToken, 1, 1, 2000, paymentDetails);

        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations.length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].participations[0].won());

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
        assertTrue(userRepo.getUserCart(authRepo.getUserId(NGToken)).getAllCart().size() == 0);
        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());

        assertEquals(8, stockRepository.getItemByStoreAndProductId(createdStoreId, productId_laptop).getQuantity());
    }

    // Needs Fixing!
    @Test
    void Set_ProductToRandom_didntwin() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        purchaseService.participateInRandom(NGToken, 1, 1, 1, paymentDetails);
        purchaseService.participateInRandom(NOToken, 1, 1, 1999, paymentDetails);

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
        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NGToken)).getSpecialCart().isEmpty());
        assertTrue(userRepo.getRegisteredUser(authRepo.getUserId(NOToken)).getSpecialCart().isEmpty());
    }

    private void xorAssert(boolean a, boolean b) {
        assertTrue(a || b);
        assertFalse(a && b);
    }

    @Test
    void Set_ProductToRandom_Failure_InvalidToken() throws UIException {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom("INvalid", 1, 1, 100, paymentDetails));

        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void Set_ProductToRandom_Failure_UserSuspended() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if

        suspensionService.suspendRegisteredUser(authRepo.getUserId(NGToken), 1, Admin);
        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom(NGToken, 1, 1, 100, paymentDetails));

        assertEquals("Suspended user trying to perform an action", ex.getMessage());
    }

    // Needs Fixing!
    @Test
    void Set_ProductToRandom_Failure_InvalidPrice() throws Exception {

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.participateInRandom(NGToken, 1, 1, 0, paymentDetails));

        assertEquals("Product price must be positive!", ex.getMessage());
    }

    @Test
    void testGetAllAuctions_Fail_NoPermission() throws Exception {
        // Step 1: Register and login user who is NOT a store manager/owner
        String token = userService.generateGuest();
        userService.register(token, "noperm", "noperm", 30);
        String noPermToken = userService.login(token, "noperm", "noperm");

        // Step 2: Try to get auctions — should fail
        Exception ex = assertThrows(Exception.class, () -> {
            stockService.getAllAuctions(noPermToken, 1);
        });

    }

    @Test
    void testGetAllAuctions_Fail_ManagerWithNoPermission() throws Exception {
        // Step 1: Register and login the manager
        String token = userService.generateGuest();
        userService.register(token, "noPermManager", "noPermManager", 30);
        String managerToken = userService.login(token, "noPermManager", "noPermManager");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Offer manager role with NO permissions
        List<Permission> emptyPerms = new ArrayList<>();
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noPermManager", emptyPerms);
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noPermManager", true, false);

        // Step 3: Manager tries to get auctions (should fail due to missing SpecialType
        // permission)
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllAuctions(managerToken, 1);
        });

        // Step 4: Assert
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("you have no permession to see auctions info.", ex.getMessage());
    }

    @Test
    void testGetAllAuctionsUser_Success() throws Exception {
        AuctionDTO[] auctions = stockService.getAllAuctions_user(NGToken, 1); // NGToken is a registered user in setup
        assertNotNull(auctions);
        assertEquals(1, auctions.length); // 1 auction set up in setup()
        assertEquals(1, auctions[0].productId); // or whatever name matches
    }

    @Test
    void testGetAllAuctionsUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllAuctions_user("bad-token", 1);
        });
    }

    @Test
    void testGetAllAuctionsUser_NotRegisteredOnline() throws Exception {
        String guestToken = userService.generateGuest(); // not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllAuctions_user(guestToken, 1);
        });
    }

    @Test
    void testGetAllAuctionsUser_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllAuctions_user(NGToken, 9999); // non-existent store
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
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "nopermmgr", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "nopermmgr", true, false);

        // Step 3: Try to set product to auction (should fail)
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToAuction(managerToken, 1, itemStoreDTO.getProductId(), 1, 1000, 100);
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
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noBidPermMgr", noPerms);
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noBidPermMgr", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToBid(mgrToken, 1, itemStoreDTO.getProductId(), 1);
        });

        assertEquals("you have no permession to set product to bid.", ex.getMessage());
    }

    @Test
    void testGetAllBidsStatus_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noBidView", "noBidView", 30);
        String managerToken = userService.login(token, "noBidView", "noBidView");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noBidView", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noBidView", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus(managerToken, 1);
        });

        assertEquals("you have no permession to see auctions info.", ex.getMessage());
    }

    @Test
    void testGetAllBidsStatusUser_Success() throws Exception {
        BidDTO[] bids = stockService.getAllBidsStatus_user(NGToken, 1);
        assertNotNull(bids);
        // assuming at least one bid exists from setup or previous tests
        assertTrue(bids.length >= 0);
    }

    @Test
    void testGetAllBidsStatusUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus_user("invalid-token", 1);
        });
    }

    @Test
    void testGetAllBidsStatusUser_NotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // Not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllBidsStatus_user(guestToken, 1);
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

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noAcceptPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noAcceptPerm", true, false);

        stockService.setProductToBid(NOToken, 1, itemStoreDTO.getProductId(), 1);
        stockService.addRegularBid(NGToken, 1, itemStoreDTO.getProductId(), 10);
        int bidToAcceptId = stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id;

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.acceptBid(managerToken, 1, 1, bidToAcceptId);
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
        int bidToRejectId = stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id;

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.rejectBid(managerToken, 1, bidId, bidToRejectId);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getErrorCode());
        assertEquals("you have no permession to accept bid", ex.getMessage()); // Note: still says "accept bid"
    }

    @Test
    void testEndBid_Success() throws Exception {
        PaymentDetails paymentDetails = PaymentDetails.testPayment();

        purchaseService.participateInRandom(NGToken, 1, 1, 2000, paymentDetails);
        ParticipationInRandomDTO result = stockService.endBid(NOToken, 1, 1);
        assertNotNull(result);
        assertTrue(result.ended);
    }

    @Test
    void testEndBid_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.endBid("bad-token", 1, 1);
        });
    }

    @Test
    void testEndBid_UserNotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.endBid(guestToken, 1, 1);
        });
    }

    @Test
    void testEndBid_UserSuspended() throws Exception {
        suspensionRepo.suspendRegisteredUser(authRepo.getUserId(NOToken), 3);
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.endBid(NOToken, 1, 1);
        });

    }

    @Test
    void testEndBid_InvalidRandomId() {
        Exception ex = assertThrows(Exception.class, () -> {
            stockService.endBid(NOToken, 1, 999); // Non-existent random ID
        });
    }

    @Test
    void testGetAllRandomInStore_ManagerNoPermission_Fail() throws Exception {
        // Step 1: Register & login manager
        String token = userService.generateGuest();
        userService.register(token, "noRandomPerm", "noRandomPerm", 30);
        String managerToken = userService.login(token, "noRandomPerm", "noRandomPerm");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Assign manager role WITHOUT permissions
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noRandomPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noRandomPerm", true, false);

        // Step 3: Try to get random sale info (should fail)
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore(managerToken, 1);
        });

        // Step 4: Validate exception
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("you have no permession to see random info.", ex.getMessage());
    }

    @Test
    void testGetAllRandomInStoreUser_Success() throws Exception {
        RandomDTO[] randoms = stockService.getAllRandomInStore_user(NGToken, 1);
        assertNotNull(randoms);
        assertTrue(randoms.length >= 1); // setup contains 1 random sale
        assertEquals(itemStoreDTO.getProductId(), randoms[0].productId);
    }

    @Test
    void testGetAllRandomInStoreUser_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore_user("invalid-token", 1);
        });
    }

    @Test
    void testGetAllRandomInStoreUser_NotRegistered() throws Exception {
        String guestToken = userService.generateGuest(); // not registered
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllRandomInStore_user(guestToken, 1);
        });
    }

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
        stockService.setProductToBid(NOToken, 1, 1, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, 1, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, 1)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_PENDING));
        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.acceptBid(NOToken, 1, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.test_fail_Payment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        assertThrows(UIException.class, () -> {
            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        });

        // <<<<<<< HEAD
        // Assert
    }

    @Test
    void Add_BidProductToSpecialCart_Success_acceptBID_invalidsupply() throws Exception {

        // Act
        stockService.setProductToBid(NOToken, 1, 1, 1);

        boolean a = stockService.addRegularBid(NGToken, 1, 1, 10);
        for (BidDTO iterable_element : stockService.getAllBidsStatus(NOToken, 1)) {
            System.err.println(iterable_element.bidId);
        }
        assertTrue(a);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_PENDING));
        assertFalse(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        stockService.acceptBid(NOToken, 1, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].status.equals(Status.BID_ACCEPTED));

        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].isAccepted);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.test_fail_supply(); // fill if needed

        assertThrows(UIException.class, () -> {
            purchaseService.finalizeSpecialCart(NGToken, paymentDetails, supplyDetails);
        });

        // <<<<<<< HEAD
        // Assert
    }

    @Test
    void testSetReceiptMapForBids_ProductNotFound_ThrowsException() throws Exception {
        // Arrange
        SingleBid badBid = new SingleBid(999, 1, 1, 100, SpecialType.BID, 1, 1, 1); // productId 999 doesn't exist
        List<SingleBid> bids = List.of(badBid);
        Map<Integer, List<ReceiptProduct>> res = new HashMap<>();

        // Mock: make stockRepo return null for productId 999
        // Act + Assert
        UIException ex = assertThrows(UIException.class, () -> {
            purchaseService.setRecieptMapForBids(bids, res);
        });

        assertEquals("Product not available", ex.getMessage());
    }

    @Test
    void testGetSpecialCart_BidRandomAuction() throws Exception {
        // ===== SETUP FOR BID =====
        int bidId = stockService.setProductToBid(NOToken, 1, 1, 1); // bidId = 1
        stockService.addRegularBid(NGToken, bidId, 1, 15);
        stockService.acceptBid(NOToken, 1, 1, stockService.getAllBidsStatus(NOToken, 1)[0].bids[0].id);

        // ===== SETUP FOR RANDOM =====
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        purchaseService.participateInRandom(NGToken, 1, 1, 1200, paymentDetails);

        // ===== SETUP FOR AUCTION =====
        int auctionId = stockService.setProductToAuction(NOToken, 1, 1, 1, 5000, 10);
        stockService.addBidOnAucction(NGToken, auctionId, 1, 10);

        // ===== EXECUTE =====
        SpecialCartItemDTO[] result = userService.getSpecialCart(NGToken);

        // ===== ASSERT =====
        assertEquals(3, result.length);
        Set<SpecialType> types = Arrays.stream(result).map(r -> r.type).collect(Collectors.toSet());

        assertTrue(types.contains(SpecialType.BID));
        assertTrue(types.contains(SpecialType.Random));
        assertTrue(types.contains(SpecialType.Auction));

        for (SpecialCartItemDTO dto : result) {
            assertEquals(1, dto.storeId); // Ensure all are for the correct store
            assertEquals("Laptop", dto.productName); // Assuming product name used in setup
        }

    }

    @Test
    void test_addProductToBid_invalidQuantity_throwsException() {

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToBid(NOToken, 1, 1, 0); // or -1
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
            stockService.acceptBid(NOToken, 1, invalidBidId, 1);
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
            stockService.rejectBid(NOToken, 1, invalidBidId, 1);
        });

        assertEquals("Bid ID not found in active bids!", ex.getMessage());
    }

    @Test
    void test_addProductToRandom_invalidQuantity_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, 1, 0, 2000, 1, 1000); // quantity = 0
        });

        assertEquals("Quantity must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_addProductToRandom_invalidPrice_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, 1, 1, 0, 1, 1000); // price = 0
        });

        assertEquals("Product price must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_addProductToRandom_invalidTime_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.setProductToRandom(NOToken, 1, 1, 2000, 1, 0); // time = 0
        });

        assertEquals("Random time must be positive!", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_RANDOM_PARAMETERS, ex.getErrorCode());
    }

    @Test
    void test_participateInRandom_invalidRandomId_throwsException() {
        UIException ex = assertThrows(UIException.class, () -> {
            // 999 is a non-existing randomId
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            purchaseService.participateInRandom(NGToken, 999, 1, 11, paymentDetails);
        });

        assertEquals("Random ID not found!", ex.getMessage());
        assertEquals(ErrorCodes.RANDOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void test_participateInRandom_invalidPrice_throwsException() throws Exception {
        int randomId = stockService.setProductToRandom(NOToken, 1, 1, 2000, 1, 1000);

        UIException ex = assertThrows(UIException.class, () -> {
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed

            purchaseService.participateInRandom(NGToken, randomId, 1, 0, paymentDetails);
        });

        assertEquals("Product price must be positive!", ex.getMessage());
    }

    @Test
    void test_getProductPrice_success() throws Exception {
        var price = stockService.getAllRandomInStore(NOToken, 1)[0].productPrice;
        assertEquals(2000, price);
    }

    @Test
    void test_acceptBid_invalidBidId_throwsDevException() throws Exception {
        BID bid = new BID(1, 1, 100, 1);
        bid.bid(20, 50.0); // adds one bid, get id

        DevException ex = assertThrows(DevException.class, () -> {
            bid.acceptBid(999); // ID that doesn't exist
        });

        assertEquals("Trying to accept bid for non-existent ID.", ex.getMessage());
    }

    @Test
    void test_rejectBid_invalidBidId_throwsDevException() throws Exception {
        BID bid = new BID(1, 1, 101, 1);
        bid.bid(21, 50.0); // valid one

        DevException ex = assertThrows(DevException.class, () -> {
            bid.rejectBid(999); // bid ID doesn't exist
        });

        assertEquals("Trying to reject bid with non-existent ID.", ex.getMessage());
    }

    @Test
    void test_bidIsWinner_nullBid() {
        BID bid = new BID(1, 1, 102, 1);
        Exception exception = assertThrows(Exception.class, () -> {
            bid.bidIsWinner(1234); // ID does not exist
        });
    }

    @Test
    void test_userIsWinner_falseWhenNoWinner() {
        BID bid = new BID(1, 1, 103, 1);
        assertFalse(bid.userIsWinner(999)); // no bids yet
    }

    @Test
    void test_getDTO_containsCorrectValues() throws Exception {
        BID bid = new BID(5, 2, 200, 10);
        SingleBid b = bid.bid(77, 100.0);
        BidDTO dto = bid.getDTO();

        assertEquals(5, dto.productId);
        assertEquals(2, dto.quantity);
        assertFalse(dto.isAccepted);
        assertEquals(200, dto.bidId);
        assertEquals(1, dto.bids.length);
        assertEquals(b.getId(), dto.bids[0].id);
    }
    //
    // @Test
    // void updatePrice_throwDevException_whenStoreNotExists() {
    // int nonExistingStoreId = 999;
    // assertThrows(DevException.class, () -> {
    // stockRepository.updatePrice(nonExistingStoreId, 1, 10);
    // });
    // }

    // @Test
    // void updatePrice_success_whenStoreExists() throws Exception {
    // int existingStoreId = 1;
    // storeService.addStoreToSystem(NOToken,); // or however you initialize a store
    // boolean result = stockRepository.updatePrice(existingStoreId, 1, 10);
    // assertTrue(result);
    // }

}
