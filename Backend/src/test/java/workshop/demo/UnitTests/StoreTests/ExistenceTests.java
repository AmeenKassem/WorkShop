// package workshop.demo.UnitTests.StoreTests;

// import java.lang.reflect.Field;
// import java.util.Locale.Category;
// import java.util.concurrent.atomic.AtomicInteger;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;

// import workshop.demo.ApplicationLayer.OrderService;
// import workshop.demo.ApplicationLayer.PaymentServiceImp;
// import workshop.demo.ApplicationLayer.PurchaseService;
// import workshop.demo.ApplicationLayer.StockService;
// import workshop.demo.ApplicationLayer.StoreService;
// import workshop.demo.ApplicationLayer.SupplyServiceImp;
// import workshop.demo.ApplicationLayer.UserService;
// import workshop.demo.ApplicationLayer.UserSuspensionService;
// import workshop.demo.DTOs.ItemStoreDTO;
// import workshop.demo.DataAccessLayer.GuestJpaRepository;
// import workshop.demo.DataAccessLayer.NodeJPARepository;
// import workshop.demo.DataAccessLayer.OfferJpaRepository;
// import workshop.demo.DataAccessLayer.StoreTreeJPARepository;
// import workshop.demo.DataAccessLayer.UserJpaRepository;
// import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;
// import workshop.demo.DomainLayer.Stock.IStockRepoDB;
// import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
// import workshop.demo.DomainLayer.Store.IStoreRepo;
// import workshop.demo.DomainLayer.Store.IStoreRepoDB;
// import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
// import workshop.demo.InfrastructureLayer.AuthenticationRepo;
// import workshop.demo.InfrastructureLayer.Encoder;
// import workshop.demo.InfrastructureLayer.NotificationRepository;
// import workshop.demo.InfrastructureLayer.OrderRepository;
// import workshop.demo.InfrastructureLayer.PurchaseRepository;
// import workshop.demo.InfrastructureLayer.SUConnectionRepository;
// import workshop.demo.InfrastructureLayer.StockRepository;
// import workshop.demo.InfrastructureLayer.StoreRepository;

// @SpringBootTest
// @ActiveProfiles("test")
// public class ExistenceTests {

//  @Autowired
//     StoreTreeJPARepository tree;
//     @Autowired
//     private NodeJPARepository node;
//     @Autowired
//     private NotificationRepository notificationRepository;
//     @Autowired
//     private StoreRepository storeRepository;
//     @Autowired
//     private StockRepository stockRepository;
//     @Autowired
//     private IStockRepoDB stockRepositoryjpa;
//     @Autowired
//     private IStoreRepoDB storeRepositoryjpa;
//     @Autowired
//     private OrderRepository orderRepository;
//     @Autowired
//     private PurchaseRepository purchaseRepository;
//     @Autowired
//     private UserSuspensionJpaRepository suspensionRepo;
//     @Autowired
//     private AuthenticationRepo authRepo;
//     @Autowired
//     private UserJpaRepository userRepo;
//     @Autowired
//     private SUConnectionRepository sIsuConnectionRepo;
//     @Autowired
//     private GuestJpaRepository guestRepo;
//     @Autowired
//     private IStoreStockRepo storeStockRepo;
//     @Autowired
//     private OfferJpaRepository offerRepo;
//     // ======================== Services ========================
//     @Autowired
//     private UserService userService;
//     @Autowired
//     private StoreService storeService;
//     @Autowired
//     private StockService stockService;
//     @Autowired
//     private PurchaseService purchaseService;
//     @Autowired
//     private OrderService orderService;
//     @Autowired
//     private UserSuspensionService suspensionService;

//     // ======================== Payment / Supply ========================
//     @Autowired
//     private PaymentServiceImp payment;
//     @Autowired
//     private SupplyServiceImp serviceImp;

//     // ======================== Utility ========================
//     @Autowired
//     private Encoder encoder;

//     // ======================== Test Data ========================
//     String NOToken;
//     String NGToken;
//     String GToken;
//     String Admin;
//     ItemStoreDTO itemStoreDTO;
//     int PID;

//     int createdStoreId;
//     @BeforeEach
//     void setUp() throws Exception {
//           node.deleteAll();

//         tree.deleteAll();
//         userRepo.deleteAll();

//         guestRepo.deleteAll();

//         stockRepositoryjpa.deleteAll();
//         offerRepo.deleteAll();
//         storeRepositoryjpa.deleteAll();
//         storeStockRepo.deleteAll();

//         if (storeRepository != null) {
//             storeRepository.clear();
//         }
//         if (stockRepository != null) {
//             stockRepository.clear();
//         }
//         if (orderRepository != null) {
//             orderRepository.clear();
//         }
//         try {
//             resetIdGenerator();
//         } catch (Exception e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }

//          String OToken = userService.generateGuest();

//         userService.register(OToken, "owner", "owner", 25);

//         // --- Login ---
//         NOToken = userService.login(OToken, "owner", "owner");

//         storeService.addStoreToSystem(NOToken, "TechStore", "ELECTRONICS");
//        //sIsuConnectionRepo .addNewStoreOwner(storeId, storeId)

//     }
//     //for regenerating ID and get the same ID ->1

//     void resetIdGenerator() throws Exception {
//         Field counterField = StoreRepository.class.getDeclaredField("counterSId");
//         counterField.setAccessible(true);
//         AtomicInteger counter = (AtomicInteger) counterField.get(null);
//         counter.set(1);
//     }

//     @Test
//     void testDeactivateStoreByMainOwner() throws Exception {
//                 int x = storeRepositoryjpa.findAll().get(0).getstoreId();

//         // Assert that the store is active before deactivation
//         assertTrue(storeRepositoryjpa.findAll().get(0).isActive());
//         storeService.deactivateteStore(x, NOToken);
//         // Assert store is now inactive
//         assertFalse(storeRepositoryjpa.findAll().get(0).isActive());
//         //assertEquals(1, workerIds.size());
//         //after notfiing must check it I got notfied 
//     }

//     @Test
//     void testDeactivateStoreByAnotherOwner() throws Exception {
//                               int x = storeRepositoryjpa.findAll().get(0).getstoreId();

//          String OToken = userService.generateGuest();

//         userService.register(OToken, "owner1", "owner", 25);

//         // --- Login ---
//        String NOToken1 = userService.login(OToken, "owner1", "owner");
//         storeService.MakeofferToAddOwnershipToStore(x, NOToken, NOToken1); 
//         storeService.AddOwnershipToStore(x, authRepo.getUserId(NOToken), authRepo.getUserId(NOToken1), true);
//         // Assert that the store is active before deactivation
//         assertTrue(storeRepositoryjpa.findAll().get(0).isActive());

//         storeService.deactivateteStore(x, NOToken1);
//         // Assert that the store is still active
//         assertTrue(storeRepositoryjpa.findAll().get(0).isActive());
//     }

// }
