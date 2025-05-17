package workshop.demo.IntegrationTests.ServiceTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

@SpringBootTest
class StoreSTests {

    @Autowired
    private StoreService storeService;

    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private IUserRepo userRepo;

    @Autowired
    private IUserSuspensionRepo susRepo;

    @Autowired
    private IStoreRepo storeRepo;
    @Autowired
    private ISUConnectionRepo suConnectionRepo;

    // @Autowired
    // private ISUConnectionRepo suConnectionRepo;
    // @Autowired
    // private IStockRepo stockRepo;
    // // Optional: only if needed
    // @Autowired
    // private INotificationRepo notiRepo;
    // @Autowired
    // private IOrderRepo orderRepo;
    private String testToken;
    private String testUserPassword;
    private int userId;
    private String userName;

    @BeforeEach
    void setUp() throws Exception {

    }

    @Test
    void testAddStoreToSystem() throws Exception {
        // STEP 1: Create test user
        testUserPassword = "999";
        userName = "rahaf@example.com";

        userId = userRepo.registerUser(userName, testUserPassword, 22); // Add this method to IUserRepo fake
        testToken = authRepo.generateUserToken(userId, testUserPassword);// Add to IAuthRepo fake
        userRepo.login(userName, testUserPassword);
        assertTrue(userRepo.isOnline(userId), "User should be logged in but is not!");
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId); // Add this to IUserSuspensionRepo fake
        String storeName = "TestBookStore";
        String category = "Books";

        int storeId = storeService.addStoreToSystem(testToken, storeName, category);

        // Assertions
        assertNotNull(storeId);
        assertTrue(storeRepo.checkStoreExistance(storeId)); // create exists() method in StoreRepo for now
    }

    @Test
    void testAddOwnershipSuccessfully() throws Exception {
        // STEP 1: Create test user
        testUserPassword = "999";
        userName = "rahaf1@example.com";

        userId = userRepo.registerUser(userName, testUserPassword, 22); // Add this method to IUserRepo fake
        testToken = authRepo.generateUserToken(userId, testUserPassword);// Add to IAuthRepo fake
        userRepo.login(userName, testUserPassword);
        assertTrue(userRepo.isOnline(userId), "User should be logged in but is not!");
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId); // Add this to IUserSuspensionRepo fake
        // STEP 1: Add a store
        String storeName = "MyTestStore";
        String category = "Books";
        int storeId = storeService.addStoreToSystem(testToken, storeName, category);
        int ownerId = authRepo.getUserId(testToken);
        // STEP 2: Create a new user who will become the new owner
        String newOwnerEmail = "newowner@example.com";
        int newOwnerId = userRepo.registerUser(newOwnerEmail, testUserPassword, 30);
        userRepo.login(newOwnerEmail, testUserPassword);
        assertTrue(userRepo.isOnline(newOwnerId), "New owner should be logged in");
        // STEP 3: Add ownership
        int returnedId = storeService.AddOwnershipToStore(storeId, ownerId, newOwnerId, true);
        // STEP 4: Assert
        assertEquals(newOwnerId, returnedId);
        assertTrue(suConnectionRepo.getData().getWorkersInStore(storeId).contains(newOwnerId));
    }
}
