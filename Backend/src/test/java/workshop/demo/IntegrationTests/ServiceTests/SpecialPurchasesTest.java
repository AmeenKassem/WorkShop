package workshop.demo.IntegrationTests.ServiceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.NodeJPARepository;
import workshop.demo.InfrastructureLayer.OfferJpaRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreTreeJPARepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) 
public class SpecialPurchasesTest {

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
    int PID;

    int createdStoreId;
    private Integer storeId;

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

        String GToken = userService.generateGuest();
        userService.register(GToken, "User", "User", 25);

        // --- Login ---
        NGToken = userService.login(GToken, "User", "User");

        assertTrue(authRepo.getUserName(NGToken).equals("User"));

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");
        System.out.println(createdStoreId + "aaaaaaaaaaaaaaaaa");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        PID = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        stockService.addItem(createdStoreId, NOToken, PID, 10, 2000, Category.Electronics);
        itemStoreDTO = new ItemStoreDTO(PID, 10, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");

    }


    // @AfterEach
    // public void hi(){

    //     node.deleteAll();
    //     orderRepository.deleteAll();
    //     tree.deleteAll();
    //     userRepo.deleteAll();

    //     guestRepo.deleteAll();

    //     stockRepositoryjpa.deleteAll();
    //     offerRepo.deleteAll();
    //     storeRepositoryjpa.deleteAll();
    //     storeStockRepo.deleteAll();
    //     activePurchasesRepo.deleteAll();

    //     orderRepository.deleteAll();
    // }

    

    @Test
    void test_searchActiveAuctions_shouldReturnProduct() throws Exception {
        long endTime = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hour from now
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();
        // Add product to auction
        activePurcheses.setProductToAuction(NOToken, x, PID, 10, endTime, 2000.0);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, createdStoreId, null, null, null, null);

        AuctionDTO[] result = activePurcheses.searchActiveAuctions(NGToken, criteria);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Laptop", result[0].productName);
    }

    @Test
    void test_searchActiveAuctions_shouldReturnProduct2() throws Exception {
        long endTime = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hour from now
        int x = storeRepositoryjpa.findAll().get(0).getstoreId();
        // Add product to auction
        activePurcheses.setProductToAuction(NOToken, x, PID, 10, endTime, 2000.0);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "Laptop", null, null, createdStoreId, null, null, null, null);

        AuctionDTO[] result = activePurcheses.searchActiveAuctions(NGToken, criteria);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Laptop", result[0].productName);
    }
}
