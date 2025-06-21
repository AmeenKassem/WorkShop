package workshop.demo.IntegrationTests.ServiceTests;

import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.NodeJPARepository;
import workshop.demo.DataAccessLayer.OfferJpaRepository;
import workshop.demo.DataAccessLayer.StoreTreeJPARepository;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepoDB;
import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GuestTests {

    // ======================== Repositories ========================
    @Autowired
    StoreTreeJPARepository tree;
    @Autowired
    private NodeJPARepository node;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private IStockRepoDB stockRepositoryjpa;
    @Autowired
    private IStoreRepoDB storeRepositoryjpa;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserSuspensionJpaRepository suspensionRepo;
    @Autowired
    private AuthenticationRepo authRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private SUConnectionRepository sIsuConnectionRepo;
    @Autowired
    private GuestJpaRepository guestRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private OfferJpaRepository offerRepo;
    // ======================== Services ========================
    @Autowired
    private UserService userService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StockService stockService;
    @Autowired
    private PurchaseService purchaseService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserSuspensionService suspensionService;

    // ======================== Payment / Supply ========================
    @Autowired
    private PaymentServiceImp payment;
    @Autowired
    private SupplyServiceImp serviceImp;

    // ======================== Utility ========================
    @Autowired
    private Encoder encoder;

    // ======================== Test Data ========================
    String NOToken;
    String NGToken;
    String GToken;
    String Admin;
    ItemStoreDTO itemStoreDTO;
    int PID;

    int createdStoreId;

    @BeforeEach
    void setup() throws Exception {
        node.deleteAll();

        tree.deleteAll();
        userRepo.deleteAll();

        guestRepo.deleteAll();

        stockRepositoryjpa.deleteAll();
        offerRepo.deleteAll();
        storeRepositoryjpa.deleteAll();
        storeStockRepo.deleteAll();

        if (storeRepository != null) {
            storeRepository.clear();
        }
        if (stockRepository != null) {
            stockRepository.clear();
        }
        if (orderRepository != null) {
            orderRepository.clear();
        }

        GToken = userService.generateGuest();

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore",
                "ELECTRONICS");
        // assertEquals( 1,createdStoreId);
        System.out.println(createdStoreId + "aaaaaaaaaaaaaaaaa");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        PID = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        stockService.addItem(createdStoreId, NOToken, PID, 10, 2000,
                Category.Electronics);
        itemStoreDTO = new ItemStoreDTO(PID, 10, 2000, Category.Electronics, 0,
                createdStoreId, "Laptop", "TestStore");

    }

    // @AfterEach

    // void tearDown() {
    // if (userRepo != null) {
    // userRepo.clear();
    // }
    // if (storeRepository != null) {
    // storeRepository.clear();
    // }
    // if (stockRepository != null) {
    // stockRepository.clear();
    // }
    // if (orderRepository != null) {
    // orderRepository.clear();
    // }
    // if (suspensionRepo != null) {
    // suspensionRepo.clear();
    // }
    // // Add clear() for all other repos you wrote it for
    // }
    // @AfterEach
    // void tearDown() {
    // userRepo.deleteAll();

    // guestRepo.deleteAll();

    // stockRepositoryjpa.deleteAll();
    // storeRepositoryjpa.deleteAll();

    // if (storeRepository != null) {
    // storeRepository.clear();
    // }
    // if (stockRepository != null) {
    // stockRepository.clear();
    // }
    // if (orderRepository != null) {
    // orderRepository.clear();
    // }
    // }

    // NOTE:ENTER+EXIT+REGISTER FINISH
    @Test
    void testGuestEnter_Success() throws Exception {

        String token = userService.generateGuest();
        int id = authRepo.getUserId(token);
        assertTrue(guestRepo.guestExists(id));
    }

    @Test
    void testGuestExit_Success() throws Exception {

        String token = userService.generateGuest();
        int id = authRepo.getUserId(token);
        userService.destroyGuest(token);
        assertFalse(guestRepo.guestExists(id));

    }

    @Test
    void testGuestExit_Failure_InvalidToken() throws Exception {
        assertThrows(UIException.class, () -> userService.destroyGuest("invalid token"));
    }

    @Test
    void testGuestRegister_Success() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "Mohamad", "finish", 24);
        // String tt = userService.login(token, "Mohamad", "finish");
        assertEquals(1, userRepo.existsByUsername("Mohamad"));
        assertFalse(userRepo.findByUsername("Mohamad").get().isOnline());
    }

    @Test
    void testGuestRegister_Failure_UsernameExists() throws Exception {

        String token = userService.generateGuest();

        userService.register(token, "Mohamad", "finish", 24);

        UIException exception = assertThrows(UIException.class, () -> {
            userService.register(token, "Mohamad", "finish", 24);
        });

        assertEquals(ErrorCodes.USERNAME_USED, exception.getNumber());
    }

    // NOTE:GET STORE PRODUCT FINISH
    @Test
    void testGuestGetStoreProducts() throws Exception {

        ItemStoreDTO[] items = stockService.getProductsInStore(createdStoreId);
        assertTrue(items.length == 1);
        assertTrue(items[0].getProductId() == PID);
    }

    @Test
    void testGuestViewEmptyStore() throws Exception {
        int id = storeService.addStoreToSystem(NOToken, "failure", "HOME");

        ItemStoreDTO[] products = stockService.getProductsInStore(id);

        assertTrue(products.length == 0);
    }

    @Test
    void testGuestGetProductInfo() {
        try {
            // ===== GUEST REQUESTS PRODUCT INFO =====
            ProductDTO info = stockService.getProductInfo(GToken, PID);

            // ===== ASSERTIONS =====
            assertNotNull(info);
            System.out.println(info.getCategory());
            assertTrue(info.getName().equals("Laptop"));
            assertTrue(info.getProductId() == PID);
            assertTrue(info.getCategory().equals(Category.Electronics));
            assertTrue(info.getDescription().equals("Gaming Laptop"));

        } catch (Exception e) {
            assertTrue(false);
        }

    }

    @Test
    void testGuestGetProductInfo_ProductNotFound() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            stockService.getProductInfo(GToken, 2);
        });

        assertEquals("product not found!", exception.getMessage());
    }

    // NOTE:ADD TO CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestAddProductToCart_Success() throws Exception {

        assertDoesNotThrow(() -> {
            userService.addToUserCart(GToken, itemStoreDTO, 1);
        });
        assertTrue(userService.getRegularCart(GToken).length == 1);
    }

    @Test
    void testGuestAddProductToCart_InvalidToken() throws Exception {

        UIException exception = assertThrows(UIException.class, () -> {
            userService.addToUserCart("invalid token", itemStoreDTO, 1);
        });

        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
        assertTrue(userService.getRegularCart(GToken).length == 0);

        // Assert
    }

    // //NOTE :BUY CART FINISH +ASK FOR MORE FAILURE
    @Test
    void testGuestBuyCart_Success()  {

        try{
            userService.addToUserCart(GToken, itemStoreDTO, 1);
            PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if
            SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if
    
            ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails,
                    supplyDetails);
    
            assertNotNull(receipts);
            assertEquals(1, receipts.length);
            assertEquals("TestStore", receipts[0].getStoreName());
            assertEquals(2000.0,
                    receipts[0].getProductsList().size() *
                            receipts[0].getProductsList().get(0).getPrice());
    
            int guestId = authRepo.getUserId(GToken);
            assertTrue(userService.getRegularCart(GToken).length == 0);
    
            assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 9);

        }catch(Exception ex ){
            assertEquals("", ex.getMessage());
        }

    }

    @Test
    void testGuestBuyCart_InvalidToken() throws Exception {
        String guestToken = "guest-token";

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyGuestCart(guestToken, paymentDetails,
                        supplyDetails));

        assertEquals("Invalid token!", ex.getMessage());
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 10);

    }

    @Test
    void testGuestBuyCart_ProductNotAvailable() throws Exception {

        userService.addToUserCart(GToken, new ItemStoreDTO(0, 0, 0, null, 0, 1, "",
                "TestStore"), 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        Exception ex = assertThrows(UIException.class,
                () -> purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails));

        assertEquals("store not found on db!",
                ex.getMessage());
    }

    @Test
    void testGuestBuyCart_PaymentFails() throws Exception {

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        userService.addToUserCart(GToken, itemStoreDTO, 1);

        UIException ex = assertThrows(UIException.class,
                () -> purchaseService.buyGuestCart(GToken,
                        PaymentDetails.test_fail_Payment(), supplyDetails));
        System.out.println(ex.getErrorCode());
        System.out.println(ex.getMessage());
        System.out.println(PID);
        assertEquals(10, stockService.getProductsInStore(createdStoreId)[0].getQuantity());
    }

    @Test
    void testGuestBuyCart_supplier_Fails() throws Exception {
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        userService.addToUserCart(GToken, itemStoreDTO, 1);

        Exception ex = assertThrows(Exception.class, () -> purchaseService.buyGuestCart(GToken,
                PaymentDetails.testPayment(), SupplyDetails.test_fail_supply()));

        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 10);

    }

    @Test
    void test_guest_cart_given_many_items_one_not_avaliable() {
        // TODO abu el3asi make one with many items .
        // cart must not change + quantity for all stores must not change
    }

    @Test
    void testGuestSearchProductInStore_Success() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, // product name filter
                null, // category filter
                null, // keyword filter
                createdStoreId, // store ID to filter
                0, 10000, // no price range
                0, 5 // no rating range
        );

        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(2000, result[0].getPrice());
        assertEquals(createdStoreId, result[0].getStoreId());
        assertEquals(Category.Electronics, result[0].getCategory());
    }

    @Test
    void testGuestSearchProducts_Success() throws Exception {

        // --- Step 2: Prepare search criteria ---
        String[] keywords = { "Laptop", "Lap", "top" };
        System.out.println(storeService.getFinalRateInStore(createdStoreId));

        ProductSearchCriteria criteria = new ProductSearchCriteria("Laptop",
                Category.Electronics, null, createdStoreId, 0, 3000, 0,
                5);
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(PID, result[0].getProductId());
        assertEquals(2000, result[0].getPrice());
        assertEquals(createdStoreId, result[0].getStoreId());

        // --- Step 7: Verify mocks ---
    }

    @Test
    void testSearchProducts_InvalidToken() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.Electronics, "Laptop", 100,
                0, 5000,
                0, 5);

        // 1. Throw on auth check
        // 3. Run the test
        UIException exception = assertThrows(UIException.class, () -> {
            stockService.searchProducts("invalid token", criteria);
        });

        // 4. Verify
        assertEquals("Invalid token!", exception.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, exception.getNumber());
    }

    @Test
    void testSearchProducts_NoMatches() throws Exception {

        String[] keywords = { "Laptop", "Lap", "top" };
        ProductSearchCriteria criteria = new ProductSearchCriteria("aa",
                Category.Electronics, keywords[0], createdStoreId, 0, 5000,
                0, 5);
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testSearchProducts_ProductExists_NotInStore() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "toy", Category.Electronics, null, createdStoreId,
                0, 0, 0, 0);
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length); // Product not sold in this store
    }

    @Test
    void testSearchProducts_PriceOutOfRange() throws Exception {

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", Category.Electronics, null, createdStoreId,
                5000, 10000, 0, 5 // Price filter too high
        );
        ItemStoreDTO[] result = stockService.searchProducts(GToken, criteria);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testGuestModifyCartAddQToBuy() throws Exception {
        userService.addToUserCart(GToken, itemStoreDTO, 1);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        int id = userService.getRegularCart(GToken)[0].itemCartId;

        userService.ModifyCartAddQToBuy(GToken, id, 3);
        ReceiptDTO[] re = purchaseService.buyGuestCart(GToken, paymentDetails,
                supplyDetails);

        assertTrue(re[0].getFinalPrice() == 6000);
        assertTrue(stockService.getProductsInStore(createdStoreId)[0].getQuantity() == 7);

    }

    @Test
    void testRemoveItemFromCart_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            userService.removeItemFromCart("bad-token", 1); // productId can be any value
        });

    }

    @Test
    void testRemoveItemFromCart_Success() throws Exception {
        userService.addToUserCart(GToken, itemStoreDTO, 1);
        // Add item first
        int id = userService.getRegularCart(GToken)[0].itemCartId;
        boolean result = userService.removeItemFromCart(GToken,
                id);
        assertTrue(result);
        assertTrue(userService.getRegularCart(GToken).length == 0);
    }

}
