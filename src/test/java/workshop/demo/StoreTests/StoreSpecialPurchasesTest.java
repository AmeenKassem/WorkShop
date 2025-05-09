package workshop.demo.StoreTests;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;

@SpringBootTest
public class StoreSpecialPurchasesTest {

    private IStoreRepo storeRepo = new StoreRepository();
    private ISUConnectionRepo sUConnectionRepo = new SUConnectionRepository();

    // @BeforeEach
    // public void setup() {
    // // Create a mock of the storeRepo interface
    // storeRepo = new StoreRepository();
    // }
    int storeId1;

    public void initTestStoreWithProducts1(IStoreRepo storeRepo) throws Exception {
        // 1. Add a new store
        int bossId = 10;
        String storeName = "Test Store";
        String storeCategory = "Electronics";
        int id = storeRepo.addStoreToSystem(bossId, storeName, storeCategory);
        this.sUConnectionRepo.addNewStoreOwner(id, bossId);

        // 2. Find the store ID (assuming the last one added is the new one)
        List<Store> stores = storeRepo.getStores();
        Store newStore = stores.get(stores.size() - 1);
        storeId1 = newStore.getStoreID();

        // 3. Add a product to the store
        int productId = 101; // You should ensure this product exists in your product catalog
        int quantity = 3;
        int price = 150;
        Category category = Category.ELECTRONICS; // Adjust this to match your enum or object

        storeRepo.addItem(storeId1, productId, quantity, price, category);

    }

    @Test
    public void testAddAuctionAndBidToStore_Success() throws Exception {
        initTestStoreWithProducts1(storeRepo);

        // int storeId = 1;
        int userId = 10;
        int productId = 101;
        int quantity = 2;
        long time = 1000; // +1 minute
        double startPrice = 99.99;

        int auctionId = storeRepo.addAuctionToStore(storeId1, userId, productId, quantity, time, startPrice);
        // assertEquals(123, auctionId);
        assertTrue(true);

        // try{
        SingleBid first = storeRepo.bidOnAuction(storeId1, 1, auctionId, startPrice + 1);
        SingleBid second = storeRepo.bidOnAuction(storeId1, 2, auctionId, startPrice + 2);
        Thread.sleep(1010);
        assertFalse(first.isWon());
        assertTrue(second.isWon());

        int bidId = storeRepo.addProductToBid(storeId1, userId, productId, 1);
        first = storeRepo.bidOnBid(bidId, 10, 2, storeId1);
        second = storeRepo.bidOnBid(bidId, 10, 1, storeId1);
        storeRepo.rejectBid(userId, storeId1, bidId, second.getId());
        storeRepo.acceptBid(storeId1, bidId, userId, first.getId());
        assertTrue(first.isWon());
        assertFalse(second.isWon());
    }

    // @Test
    // public void testAddAuctionByUnauthorizedUser_Failure() throws Exception { //-> it will fail 
    //     //becuse we change the checking og authorized to the service layer
    //     initTestStoreWithProducts1(storeRepo);
    //     int unauthorizedUserId = 999; // not the boss (boss is 1)
    //     int productId = 101;
    //     int quantity = 1;
    //     long time = 1000;
    //     double startPrice = 50.0;
    //     assertThrows(Exception.class, () -> {
    //         storeRepo.addAuctionToStore(storeId1, unauthorizedUserId, productId, quantity, time, startPrice);
    //     });
    //     assertThrows(Exception.class, () -> {
    //         storeRepo.addProductToBid(storeId1, unauthorizedUserId, productId, quantity);
    //     });
    // }
    @Test
    public void testAddAuctionWithInsufficientQuantity_Failure() throws Exception {
        initTestStoreWithProducts1(storeRepo);

        int bossId = 10; // owner
        int productId = 101;
        int quantityTooHigh = 9999;
        long time = 1000;
        double startPrice = 100.0;

        assertThrows(Exception.class, () -> {
            storeRepo.addAuctionToStore(storeId1, bossId, productId, quantityTooHigh, time, startPrice);
        });

        assertThrows(Exception.class, () -> {
            storeRepo.addProductToBid(storeId1, bossId, productId, quantityTooHigh);
        });
    }

    @Test
    public void testSpecialPurchasePermession() throws Exception {
        initTestStoreWithProducts1(storeRepo);
        this.sUConnectionRepo.AddManagerToStore(storeId1, 10, 2);
        List<Permission> perms = new ArrayList<>();
        perms.add(Permission.SpecialType);
        this.sUConnectionRepo.changePermissions(10, 2, storeId1, perms);
        storeRepo.addProductToBid(storeId1, 2, 101, 1);
        storeRepo.addAuctionToStore(storeId1, 2, 101, 1, 10, 0);
    }
}
