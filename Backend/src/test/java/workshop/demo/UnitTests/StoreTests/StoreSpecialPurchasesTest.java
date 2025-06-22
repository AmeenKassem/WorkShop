// package workshop.demo.UnitTests.StoreTests;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;

// import workshop.demo.DTOs.Category;

// import workshop.demo.DomainLayer.Stock.SingleBid;
// import workshop.demo.DomainLayer.Store.IStoreRepo;
// import workshop.demo.DomainLayer.Store.Store;
// import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
// import workshop.demo.InfrastructureLayer.IStoreRepoDB;
// import workshop.demo.InfrastructureLayer.SUConnectionRepository;
// import workshop.demo.InfrastructureLayer.StockRepository;
// import workshop.demo.InfrastructureLayer.StoreRepository;
// import workshop.demo.InfrastructureLayer.UserJpaRepository;
// import workshop.demo.ApplicationLayer.StoreService;
// import workshop.demo.ApplicationLayer.UserService;

// @SpringBootTest
// @ActiveProfiles("test")
// public class StoreSpecialPurchasesTest {

//     @Autowired
//     private IStoreRepoDB storeRepo;
//     private ISUConnectionRepo sUConnectionRepo = new SUConnectionRepository();
//     private IStockRepo stockRepo = new StockRepository();
//     @Autowired
//     private StoreService store;
//     @Autowired
//     private UserService userService;

//     @Autowired
//     private UserJpaRepository userRepo;
//     // @BeforeEach
//     // public void setup() {
//     // // Create a mock of the storeRepo interface
//     // storeRepo = new StoreRepository();
//     // }
//     int storeId1;

//     @Test
//     public void initTestStoreWithProducts1() throws Exception {
//         // 1. Add a new store
//         userRepo.deleteAll();

//         int bossId = 10;
//         String storeName = "Test Store";
//         String storeCategory = "Electronics";

//         String OToken = userService.generateGuest();

//         userService.register(OToken, "owner11111", "owner", 25);

//         // --- Login ---
//         String NOToken = userService.login(OToken, "owner11111", "owner");

//         int id = store.addStoreToSystem(NOToken, storeName, storeCategory);

//         // 2. Find the store ID (assuming the last one added is the new one)
//         List<Store> stores = storeRepo.findAll();
//         Store newStore = stores.get(stores.size() - 1);
//         storeId1 = newStore.getstoreId();

//         // 3. Add a product to the store
//         int productId = 101; // You should ensure this product exists in your product catalog
//         int quantity = 3;
//         int price = 150;
//         Category category = Category.Electronics; // Adjust this to match your enum or object
//         stockRepo.addStore(id);
//         stockRepo.addItem(storeId1, productId, quantity, price, category);

//     }

//     @Test
//     public void testAddAuctionWithInsufficientQuantity_Failure() throws Exception {

//         int bossId = 10; // owner
//         int productId = 101;
//         int quantityTooHigh = 9999;
//         long time = 1000;
//         double startPrice = 100.0;

//         assertThrows(Exception.class, () -> {
//             stockRepo.addAuctionToStore(storeId1, productId, quantityTooHigh, time, startPrice);
//         });

//         assertThrows(Exception.class, () -> {
//             stockRepo.addProductToBid(storeId1, productId, quantityTooHigh);
//         });
//     }

// }
