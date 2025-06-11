package workshop.demo.IntegrationTests.ServiceTests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import org.springframework.test.util.ReflectionTestUtils;
import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.BaseNotifier;
import workshop.demo.DomainLayer.Notification.DelayedNotificationDecorator;
import workshop.demo.DomainLayer.Notification.RealTimeNotificationDecorator;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Store.PurchasePolicy;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;
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
import workshop.demo.SocketCommunication.SocketHandler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StoreSTests {

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
    @Autowired
    ReviewService reviewService;

    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;
    String Admin;

    @BeforeEach
    void setup() throws Exception {

        GToken = userService.generateGuest();
        userService.register(GToken, "User", "User", 16);
        NGToken = userService.login(GToken, "User", "User");

        assertTrue(authRepo.getUserName(NGToken).equals("User"));

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int created1 = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        assertEquals(created1, 1);

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = { "Laptop", "Lap", "top" };
        stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);
        itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.Electronics, 0, 1, "Laptop", "TestStore");

        assertEquals(1, stockService.addItem(1, NOToken, 1, 10, 2000, Category.Electronics));

        // ======================= SECOND GUEST SETUP =======================
    }

    @AfterEach

    void tearDown() {
        if (userRepo != null) {
            userRepo.clear();
        }
        if (storeRepository != null) {
            storeRepository.clear();
        }
        if (stockRepository != null) {
            stockRepository.clear();
        }
        if (orderRepository != null) {
            orderRepository.clear();
        }
        if (suspensionRepo != null) {
            suspensionRepo.clear();
        }
        // Add clear() for all other repos you wrote it for
    }

    // ========== Store Owner Use Cases ==========
    @Test
    void testOwner_AddProductToStock() throws Exception {

        String[] keywords = { "Tablet", "Touchscreen" };

        stockService.addProduct(NOToken, "Tablet", Category.Electronics, "10-inch Tablet", keywords);

        int itemAdded = stockService.addItem(1, NOToken, 2, 10, 100, Category.Electronics);
        assertEquals(2, itemAdded);
        assertTrue(stockService.getProductInfo(NOToken, 2).getProductId() == 2);

    }

    @Test
    void testOwner_AddProductToStock_Failure_InvalidProductData() throws Exception {

        // === Act & Assert ===
        UIException ex = assertThrows(UIException.class,
                () -> stockService.addItem(1, NOToken, 2, 5, 999, Category.Electronics));

        assertEquals("Product not found", ex.getMessage());
        assertEquals(1006, ex.getNumber());
    }

    @Test
    void testOwner_AddProductToStock_Failure_Invalidqunatity() throws Exception {

        // === Act & Assert ===
        UIException ex = assertThrows(UIException.class,
                () -> stockService.addItem(1, NOToken, 2, -10, 999, Category.Electronics));

    }

    @Test
    void testOwner_AddProductToStock_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> stockService.addItem(22, NOToken, 1, 10, 100, Category.Electronics));

        // // Optional: verify the details of the exception
        assertEquals(" store does not exist.", ex.getMessage());
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_DeleteProductFromStock() throws Exception {

        // Act
        stockService.removeItem(1, NOToken, 1);

        // Assert
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 0);
    }

    @Test
    void testOwner_UpdatePriceProductInStock() throws Exception {

        assertDoesNotThrow(() -> stockService.updatePrice(1, NOToken, 1, 10));
        assertTrue(stockService.getProductsInStore(1)[0].getPrice() == 10);
    }

    @Test
    void testOwner_UpdateQuantityProductInStock() throws Exception {

        assertDoesNotThrow(() -> stockService.updateQuantity(1, NOToken, 1, 1));
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 1);

    }

    @Test
    void testOwner_UpdateProductInStock_Failure_InvalidData() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.updateQuantity(1, NOToken, 10, 10));
        assertEquals(ex.getMessage(), "Item not found with ID 10");
        assertEquals(ex.getNumber(), 1006);

    }

    @Test
    void testOwner_AddStoreOwner_Success() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, authRepo.getUserName(token1));

        // === Act ===
        storeService.AddOwnershipToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);
        // ask bhaa i dont know what is happening , help help help

        // === Assert ===
        // assertEquals(sotre);
    }

    @Test
    void testOwner_AddStoreOwner_fail() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        // must make an offer before:
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");
        storeService.reciveAnswerToOffer(1, "owner", "token", true, true);
        // ask bhaa i dont know what is happening , help help help

        // shouldnt work without offer
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);

        // === Assert ===
        // assertEquals(sotre);
    }

    @Test
    void testOwner_AddStoreOwner_ReassignSameUser_Failure() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");
        storeService.AddOwnershipToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);

        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);
        Exception ex = assertThrows(Exception.class,
                () -> storeService.reciveAnswerToOffer(1, "owner", "token", true, true));

    }

    @Test
    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
        String token = userService.generateGuest();

        // === Act ===
        // storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");
        // this function need to take id to use this test guest doesnt have a username
        // === Act + Assert
        UIException ex = assertThrows(UIException.class,
                () -> storeService.AddOwnershipToStore(1, 3, authRepo.getUserId(token), true)
        // shouldnt be able to add a guest
        );

        assertEquals("You are not regestered user!", ex.getMessage());
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }

    // todo:: send approval to user
    @Test
    void testOwner_AddStoreOwner_Rejected() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");

        storeService.AddOwnershipToStore(1, authRepo.getUserId(NGToken), authRepo.getUserId(token1), false);
        assertTrue(storeService.ViewRolesAndPermissions(token1, 1).size() == 1);

    }

    @Test
    void testOwner_DeleteStoreOwner() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");

        storeService.AddOwnershipToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);

        assertDoesNotThrow(() -> storeService.DeleteOwnershipFromStore(1, NOToken, authRepo.getUserId(token1)));
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 1);

    }

    @Test
    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.DeleteOwnershipFromStore(1, NOToken, authRepo.getUserId(token1));
        });
        assertEquals("Cannot delete: user is not an owner", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Success() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, authRepo.getUserName(token1), a);
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "token", true, false); // false = toBeOwner →
                                                                                                  // false = manager
        // when decide equals true some list is null (i think its permissions list)
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);

    }

    @Test
    void testOwner_AddStoreManager_Rejected() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
        storeService.AddManagerToStore(1, authRepo.getUserId(NGToken), authRepo.getUserId(token1), false);
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 1);

    }

    @Test
    void testOwner_AddStoreManager_Failure_UserNotExist() throws Exception {

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
        });
        assertEquals("No user found with username: token", ex.getMessage());
        // assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }

    @Test
    void testOwner_AddStoreManager_Failure_AlreadyManager() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
        storeService.AddManagerToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
            // already a manager need to throw error
            // he can make an offer but can't add it, come on GUYS

        });

        assertEquals("This worker is already an owner/manager", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Failure_OwnerTargetNotFound() throws Exception {

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", permissions);
        });

        assertEquals("No user found with username: token", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Failure_StoreClosed() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.MakeOfferToAddManagerToStore(2, NOToken, "token", permissions);
        });

        assertEquals(" store does not exist.", ex.getMessage());
    }

    @Test
    void testDeleteManager_Success() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
        storeService.AddManagerToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);

        storeService.deleteManager(1, NOToken, authRepo.getUserId(token1));
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 1);
    }

    @Test
    void testDeleteManager_Failure_StoreExist() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);

        int result = storeService.AddManagerToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);
        // dunno why it doesnt work with true ????
        // when decide true some list is null (i think its permissions list)

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(2, NOToken, authRepo.getUserId(token1));
        });
        assertEquals(ex.getMessage(), " store does not exist.");
        assertEquals(ex.getNumber(), 1005);
    }

    @Test
    void testDeleteManager_Failure_StoreNotActive() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);

        int result = storeService.AddManagerToStore(1, authRepo.getUserId(NOToken), authRepo.getUserId(token1), true);
        // dunno why it doesnt work with true ????
        storeService.deactivateteStore(1, NOToken);

        DevException ex = assertThrows(DevException.class, () -> {
            storeService.deleteManager(1, NOToken, authRepo.getUserId(token1));
        });
        assertEquals(ex.getMessage(), " store is not active");
    }

    // @Test
    // void testOwner_ManageStoreManagerPermissions() throws Exception {
    // throw new Exception("need to impl view roles and permissons");
    // }
    //
    // @Test
    // void testOwner_ManageStoreManagerPermissions_Failure_NotAManagerFlag() throws
    // Exception {
    // throw new Exception("need to impl view roles and permissons");
    // }
    @Test
    void testOwner_DeactivateStore() throws Exception {
        int result = storeService.deactivateteStore(1, NOToken);
        DevException ex = assertThrows(DevException.class, () -> {
            storeRepository.checkStoreIsActive(1);
        });

        assertEquals(ex.getMessage(), " store is not active");

    }

    @Test
    void testOwner_DeactivateStore_Failure_AlreadyInactive() throws Exception {
        int result = storeService.deactivateteStore(1, NOToken);
        DevException ex = assertThrows(DevException.class, () -> {
            storeRepository.checkStoreIsActive(1);
        });

        assertEquals(ex.getMessage(), " store is not active");
        UIException ex1 = assertThrows(UIException.class, () -> {
            int result1 = storeService.deactivateteStore(1, NOToken);
        });

        assertEquals(ex.getMessage(), " store is not active");

        assertEquals("can't deactivate an DEactivated store", ex1.getMessage());
    }

    @Test
    void testOwner_ViewStorePurchaseHistory() throws Exception {
        userService.addToUserCart(GToken, itemStoreDTO, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);

        List<OrderDTO> history = storeService.veiwStoreHistory(1);
        assertTrue(history.get(0).getStoreId() == 1);
        assertTrue(history.get(0).getFinalPrice() == 2000);
        assertTrue(history.get(0).getProductsList().size() == 1);
        assertTrue(history.get(0).getUserId() == authRepo.getUserId(GToken));

    }

    @Test
    void testOwner_ViewStorePurchaseHistory_Failure_StoreNotExist() throws Exception {

        // Assert exception is thrown
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.veiwStoreHistory(2);
        });

        assertEquals("Store does not exist!", ex.getMessage());

    }

    @Test
    void testOwner_RequestStoreRolesInfoAndPermission_sucess() throws Exception {
        List<WorkerDTO> workers = storeService.ViewRolesAndPermissions(NOToken, 1);
        assertTrue(workers.size() == 1);
        assertTrue(workers.get(0).isOwner() == true);
        assertTrue(workers.get(0).getWorkerId() == authRepo.getUserId(NOToken));
        assertTrue(workers.get(0).getUsername().equals(authRepo.getUserName(NOToken)));

    }

    @Test
    void testOwner_RequestStoreRolesInfoAndPermission_failure() throws Exception {
        UIException ex = assertThrows(UIException.class, () -> storeService.ViewRolesAndPermissions("ivalid", 1));
        Exception ex1 = assertThrows(Exception.class, () -> storeService.ViewRolesAndPermissions(NOToken, 2));

    }

    @Test
    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {

        // === Act & Assert
        UIException ex = assertThrows(UIException.class, () -> stockService.removeItem(1, NOToken, 3));

        assertEquals("Item not found with ID 3", ex.getMessage());
        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_AddToAuction_Success() throws Exception {

        stockService.setProductToAuction(NOToken, 1, 1, 1, 5000, 2);
        assertTrue(stockService.getAllAuctions(NOToken, 1).length == 1);
        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].productId == 1);

        assertTrue(stockService.getAllAuctions(NOToken, 1)[0].storeId == 1);

    }

    @Test
    void testOwner_AddToBID_Success() throws Exception {
        stockService.setProductToBid(NOToken, 1, 1, 1);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1).length == 1);
        assertTrue(stockService.getAllBidsStatus(NOToken, 1)[0].productId == 1);

    }

    @Test
    void testOwner_AddToRandom_Success() throws Exception {

        stockService.setProductToRandom(NOToken, 1, 1, 100, 1, 5000);

        assertTrue(stockService.getAllRandomInStore(NOToken, 1).length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, 1)[0].productId == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, 1).length == 1);

    }

    @Test
    void testOwner_rankstore_Success() throws Exception {

        storeService.rankStore(NGToken, 1, 1);
        assertTrue(storeService.getFinalRateInStore(1) == 1);

    }

    @Test
    void testOwner_rankstore_Invalidrating() throws Exception {

        storeService.rankStore(NGToken, 1, 0);
        assertTrue(storeService.getFinalRateInStore(1) == 3);

        /// 3 is deafult

    }

    @Test
    void testOwner_rankstore_Invalidstore() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> storeService.rankStore(NGToken, 2, authRepo.getUserId(NGToken)));

    }

    @Test
    void testOwner_rankstore_Invalidtoken() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> storeService.rankStore(GToken, 1, authRepo.getUserId(NGToken)));
    }

    @Test
    void testOwner_rankitem_Success() throws Exception {

        assertTrue(stockService.getProductsInStore(1)[0].getRank() == 3);

        stockService.rankProduct(1, NGToken, 1, 1);
        // System.out.println("afhwlsdfkjEGn " +
        // stockService.getProductsInStore(1)[0].rank);
        assertTrue(stockService.getProductsInStore(1)[0].getRank() == 1);

    }

    @Test
    void testOwner_rankitem_Invalidrating() throws Exception {

        assertTrue(stockService.getProductsInStore(1)[0].getRank() == 3);
        UIException ex = assertThrows(UIException.class, () -> stockService.rankProduct(1, NGToken, 1, -1));

    }

    @Test
    void testOwner_rankitem_Invalidstore() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.rankProduct(2, NGToken, 1, 1));
    }

    @Test
    void testOwner_rankitem_Invaliditem() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.rankProduct(1, NGToken, 2, 1));

    }

    @Test
    void testOwner_rankitem_Invalidtoken() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> stockService.rankProduct(1, GToken, 1, 1));

    }

    @Test
    void testOwner_reviewstore_Success() throws Exception {
        assertTrue(reviewService.getReviewsForStore(1).size() == 0);

        reviewService.AddReviewToStore(NGToken, 1, "first review");

        assertTrue(reviewService.getReviewsForStore(1).size() == 1);
        assertTrue(reviewService.getReviewsForStore(1).get(0).getReviewMsg().equals("first review"));

    }

    @Test
    void testOwner_reviewstore_Invalidstore() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> reviewService.AddReviewToStore(NGToken, 2, "first review"));

    }

    @Test
    void testOwner_reviewitem_Success() throws Exception {

        reviewService.AddReviewToProduct(NGToken, 1, 1, "first review");

        assertTrue(reviewService.getReviewsForProduct(1, 1).get(0).getReviewMsg().equals("first review"));

    }

    @Test
    void testOwner_reviewitem_Invalidstore() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> reviewService.AddReviewToProduct(NGToken, 2, 1, "first review"));

    }

    @Test
    void testOwner_reviewitem_Invaliditem() throws Exception {

        UIException ex = assertThrows(UIException.class,
                () -> reviewService.AddReviewToProduct(NGToken, 1, 5, "first review"));

    }

    @Test
    void testCloseStore_Success() throws Exception {
        // ACT
        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        String token1 = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(token1, "123321", authRepo.getUserId(token1));

        int result = storeService.closeStore(1, token1);

        assertEquals(1, result);
        assertTrue(storeService.getAllStores().isEmpty());
        // assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getCode());
    }

    @Test
    void testCloseStore_Fail_NotAdmin() {
        Exception ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(1, NGToken); // NGToken is not admin
        });

        // assertEquals(ErrorCodes., ((UIException) ex).getCode());
    }

    @Test
    void testCloseStore_Fail_StoreNotFound() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "adminUser3", "adminPass3", 25);
        String adminToken = userService.login(token, "adminUser3", "adminPass3");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        Exception ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(9999, adminToken); // non-existent store
        });

    }

    @Test
    void testCloseStore_Fail_InvalidToken() {
        Exception ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(1, "invalid-token");
        });

    }

    @Test
    void testGetStoreDTO_Success() throws Exception {
        StoreDTO dto = storeService.getStoreDTO(NOToken, 1);
        assertEquals("TestStore", dto.getStoreName());
        assertEquals(1, dto.getStoreId());
    }

    @Test
    void testGetStoreDTO_Fail_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.getStoreDTO("bad-token", 1);
        });
    }

    @Test
    void testGetStoreDTO_Fail_NotRegistered() {
        Exception ex = assertThrows(UIException.class, () -> {
            storeService.getStoreDTO(GToken, 1); // Guest, not registered
        });
    }

    @Test
    void testGetStoreDTO_Fail_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.getStoreDTO(NOToken, 9999);
        });
    }

    @Test
    void testGetStoresOwnedByUser_Success() throws Exception {
        List<StoreDTO> owned = storeService.getStoresOwnedByUser(NOToken);
        assertEquals(1, owned.size());
        assertEquals("TestStore", owned.get(0).getStoreName());
    }

    @Test
    void testGetStoresOwnedByUser_Fail_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.getStoresOwnedByUser("invalid-token");
        });
    }

    @Test
    void testGetStoresOwnedByUser_Fail_NotRegistered() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.getStoresOwnedByUser(GToken);
        });
    }

    @Test
    void testGetAllStores() {
        List<StoreDTO> all = storeService.getAllStores();
        assertTrue(!all.isEmpty());
        assertEquals("TestStore", all.get(0).getStoreName());
    }

    @Test
    void testChangePermissions_Success() throws Exception {
        // Register and login new user (manager)
        String token = userService.generateGuest();
        userService.register(token, "managerUser", "managerPass", 22);
        String managerToken = userService.login(token, "managerUser", "managerPass");
        int managerId = authRepo.getUserId(managerToken);

        List<Permission> perms = List.of(Permission.AddToStock, Permission.DeleteFromStock);

        // Make manager
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "managerUser", perms);
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "managerUser", true, false);

        // New permissions
        List<Permission> newPerms = List.of(Permission.SpecialType, Permission.MANAGE_STORE_POLICY);
        storeService.changePermissions(NOToken, managerId, 1, newPerms);

        for (Permission perm : storeService.ViewRolesAndPermissions(NOToken, 1).get(1).getPermessions()) {
            System.out.println(perm.name());
        }
        assertEquals(4, storeService.ViewRolesAndPermissions(NOToken, 1).get(1).getPermessions().length);

    }

    @Test
    void testChangePermissions_Fail_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions("bad-token", 5, 1, List.of());
        });
    }

    @Test
    void testChangePermissions_Fail_ManagerNotRegistered() throws Exception {
        String fakeToken = userService.generateGuest();
        userService.register(fakeToken, "newOwner", "pass", 30);
        String ownerToken = userService.login(fakeToken, "newOwner", "pass");
        userService.setAdmin(ownerToken, "123321", authRepo.getUserId(ownerToken));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions(ownerToken, 9999, 1, List.of());
        });
    }

    @Test
    void testChangePermissions_Fail_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions(NOToken, 1, 9999, List.of());
        });
    }

    @Test
    void testChangePermissions_Fail_StoreInactive() throws Exception {
        storeService.deactivateteStore(1, NOToken); // store 1 is now inactive

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.changePermissions(NOToken, 1, 1, List.of());
        });

    }

    @Test
    void testAddItem_ManagerNoPermission_Fail() throws Exception {
        // Step 1: Register and login manager
        String token = userService.generateGuest();
        userService.register(token, "noAddPerm", "noAddPerm", 30);
        String managerToken = userService.login(token, "noAddPerm", "noAddPerm");
        int managerId = authRepo.getUserId(managerToken);

        // Step 2: Assign manager with no permissions
        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noAddPerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noAddPerm", true, false);

        // Step 3: Attempt to add item — should fail
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.addItem(1, managerToken, itemStoreDTO.getProductId(), 1, 1000, Category.Electronics);
        });

        // Step 4: Assert exception
        assertEquals("This worker is not authorized!", ex.getMessage());
    }

    @Test
    void testRemoveItem_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noDeletePerm", "noDeletePerm", 30);
        String managerToken = userService.login(token, "noDeletePerm", "noDeletePerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noDeletePerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noDeletePerm", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.removeItem(1, managerToken, itemStoreDTO.getProductId());
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("this worker is not authorized!", ex.getMessage());
    }

    @Test
    void testUpdateQuantity_ManagerNoPermission_Fail() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noUpdatePerm", "noUpdatePerm", 30);
        String managerToken = userService.login(token, "noUpdatePerm", "noUpdatePerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noUpdatePerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noUpdatePerm", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.updateQuantity(1, managerToken, itemStoreDTO.getProductId(), 99);
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("This worker is not authorized!", ex.getMessage());
    }

    @Test
    void testUpdatePrice_ManagerNoPermission_Fail() throws Exception {
        // Step 1: Register and login manager
        String token = userService.generateGuest();
        userService.register(token, "noUpdatePerm", "noUpdatePerm", 30);
        String managerToken = userService.login(token, "noUpdatePerm", "noUpdatePerm");
        int managerId = authRepo.getUserId(managerToken);

        storeService.MakeOfferToAddManagerToStore(1, NOToken, "noUpdatePerm", new ArrayList<>());
        storeService.reciveAnswerToOffer(1, authRepo.getUserName(NOToken), "noUpdatePerm", true, false);

        UIException ex = assertThrows(UIException.class, () -> {
            stockService.updatePrice(1, managerToken, itemStoreDTO.getProductId(), 99);
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("This worker is not authorized!", ex.getMessage());
    }

    @Test
    void testGetAllProducts_Success() throws Exception {
        ProductDTO[] products = stockService.getAllProducts(NGToken); // user from setup
        assertNotNull(products);
        assertTrue(products.length >= 1); // at least the one from setup
        assertEquals("Laptop", products[0].getName());
    }

    @Test
    void testGetAllProducts_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            stockService.getAllProducts("invalid-token");
        });
    }

    @Test
    void testGetAllOrdersByStore_Success() throws Exception {
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // must be a registered user
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());

        assertEquals(1, receipts.length); // confirm purchase success

        List<OrderDTO> orders = orderService.getAllOrderByStore(itemStoreDTO.getStoreId());

        assertNotNull(orders);
        assertTrue(!orders.isEmpty());
        assertEquals(1, orders.get(0).getStoreId());
    }

    @Test
    void testGetAllOrdersByStore_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            orderService.getAllOrderByStore(9999); // store doesn't exist
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Store not found", ex.getMessage());
    }

    // Bashar ..........
    @Test
    void testAddtwostoresamename() throws UIException, DevException {
        storeService.addStoreToSystem(NOToken, "AA", "ELECTRONICS");
        // assertThrows(storeService.addStoreToSystem(NGToken,"AA","ELECTRONICS"));
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.addStoreToSystem(NGToken, "AA", "ELECTRONICS"); // store doesn't exist
        });

    }

    @Test
    void test_closeStore_storeDoesNotExist_shouldNotRemoveOtherStores() throws Exception {
        int before = storeRepository.getStores().size();
        assertDoesNotThrow(() -> storeRepository.closeStore(999));
        int after = storeRepository.getStores().size();
        assertEquals(before, after); // size should stay the same
    }

    @Test
    void test_closeStore_storeDoesNotExist_shouldNotThrow() {
        assertDoesNotThrow(() -> storeRepository.closeStore(999)); // storeId 999 does not exist
    }

    @Test
    void test_rankStore_storeDoesNotExist_throwsException() {
        UIException exception = assertThrows(UIException.class, () -> storeRepository.rankStore(999, 5));
        assertEquals(ErrorCodes.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void test_getFinalRateInStore_storeDoesNotExist_throwsException() {
        UIException exception = assertThrows(UIException.class, () -> storeRepository.getFinalRateInStore(999));
        assertEquals(ErrorCodes.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void test_checkStoreIsActive_storeNotFound_throwsDevException() {
        DevException exception = assertThrows(DevException.class, () -> {
            storeRepository.checkStoreIsActive(999); // 999 does not exist
        });
        assertTrue(exception.getMessage().contains("Store not found with ID"));
    }

    @Test
    void test_getStoreDTO_storeNotFound_throwsUIException() {
        UIException exception = assertThrows(UIException.class, () -> {
            storeRepository.getStoreDTO(999); // 999 does not exist
        });
        assertEquals(ErrorCodes.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void test_checkStoreIsActive_storeExistsButNotActive_throwsDevException() throws Exception {
        int storeId = storeService.addStoreToSystem(NOToken, "InactiveStore", "ELECTRONICS");

        // simulate the store being inactive (you may need a method or reflection if not
        // public)
        storeRepository.getStores().stream()
                .filter(s -> s.getStoreID() == storeId)
                .findFirst()
                .ifPresent(store -> store.setActive(false)); // assuming such setter exists

        DevException exception = assertThrows(DevException.class, () -> {
            storeRepository.checkStoreIsActive(storeId);
        });

        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    void test_fillWithStoreName_randomDTO_storeNotFound() {
        RandomDTO[] randoms = new RandomDTO[1];
        randoms[0] = new RandomDTO();
        randoms[0].storeId = 999; // store does not exist

        storeRepository.fillWithStoreName(randoms);

        assertNull(randoms[0].storeName); // storeName should remain null
    }

    @Test
    void test_fillWithStoreName_auctionDTO_storeNotFound() {
        AuctionDTO[] auctions = new AuctionDTO[1];
        auctions[0] = new AuctionDTO();
        auctions[0].storeId = 999; // store does not exist

        storeRepository.fillWithStoreName(auctions);

        assertNull(auctions[0].storeName); // storeName should remain null
    }

    @Test
    void test_fillWithStoreName_bidDTO_storeNotFound() {
        BidDTO[] bids = new BidDTO[1];
        bids[0] = new BidDTO();
        bids[0].storeId = 999; // store does not exist

        storeRepository.fillWithStoreName(bids);

        assertNull(bids[0].storeName); // storeName should remain null
    }

    @Test
    void test_validatedParticipation_storeNotInitialized_throwsDevException() {
        DevException ex = assertThrows(DevException.class,
                () -> stockRepository.validatedParticipation(10, 1, 999, 200.0) // storeId 999 doesn't exist
        );
        assertTrue(ex.getMessage().contains("Store stock not initialized"));
    }

    @Test
    void test_getRandomCardIfWinner_exception_returnsNull() {
        ParticipationInRandomDTO result = stockRepository.getRandomCardforuser(999, 1, 10); // storeId 999 not set up
        assertNull(result);
    }

    @Test
    void test_getBidIfWinner_exception_returnsNull() {
        SingleBid result = stockRepository.getBidIfWinner(999, 1, 1, SpecialType.BID); // storeId 999 not set up
        assertNull(result);
    }

    @Test
    void test_getOrderDTOsByUserId_shouldReturnOrderForUser() throws Exception {
        int userId = authRepo.getUserId(NGToken);

        // Add item to user cart using UserService
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // quantity = 1

        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("Israel", "Beer Sheva", "8410501", "Ringelblum");

        // Purchase cart
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, payment, supply);

        // Fetch and assert order
        List<OrderDTO> orders = orderRepository.getOrderDTOsByUserId(userId);

        assertNotNull(orders);
        assertEquals(1, orders.size());

        OrderDTO order = orders.get(0);
        assertEquals(userId, order.getUserId());
        assertEquals(1, order.getStoreId()); // from setup
        assertEquals(1, order.getProductsList().size());
        assertTrue(order.getFinalPrice() > 0);
    }

    @Test
    void testSendDelayedMessage_UserOnline() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        when(mockBase.isUserOnline("user")).thenReturn(true);

        DelayedNotificationDecorator decorator = new DelayedNotificationDecorator(mockBase);
        decorator.sendDelayedMessageToUser("user", "hello");

        verify(mockBase).send("user", "hello");
    }

    @Test
    void testSendDelayedMessage_UserOffline_NewEntry() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        when(mockBase.isUserOnline("user")).thenReturn(false);

        DelayedNotificationDecorator decorator = new DelayedNotificationDecorator(mockBase);
        decorator.sendDelayedMessageToUser("user", "offline-msg");

        assertEquals(1, decorator.getDelayedMessages("user").length);
    }

    @Test
    void testSendDelayedMessage_UserOffline_ExistingEntry() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        when(mockBase.isUserOnline("user")).thenReturn(false);

        DelayedNotificationDecorator decorator = new DelayedNotificationDecorator(mockBase);
        decorator.sendDelayedMessageToUser("user", "msg1");
        decorator.sendDelayedMessageToUser("user", "msg2");

        String[] messages = decorator.getDelayedMessages("user");
        assertArrayEquals(new String[] { "msg1", "msg2" }, messages);
    }

    @Test
    void testGetDelayedMessages_NoMessages() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        DelayedNotificationDecorator decorator = new DelayedNotificationDecorator(mockBase);

        assertNull(decorator.getDelayedMessages("unknown"));
    }

    @Test
    void testSendRTMessageToUser_Online() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        when(mockBase.isUserOnline("user")).thenReturn(true);

        RealTimeNotificationDecorator decorator = new RealTimeNotificationDecorator(mockBase);
        decorator.sendRTMessageToUser("user", "realtime");

        verify(mockBase).send("user", "realtime");
    }

    @Test
    void testSendRTMessageToUser_Offline() {
        BaseNotifier mockBase = mock(BaseNotifier.class);
        when(mockBase.isUserOnline("user")).thenReturn(false);

        RealTimeNotificationDecorator decorator = new RealTimeNotificationDecorator(mockBase);
        decorator.sendRTMessageToUser("user", "delayed");

        // No send call expected
        verify(mockBase, never()).send(any(), any());
    }

    @Test
    void testBaseNotifierSend_Success() throws Exception {
        SocketHandler handler = mock(SocketHandler.class);
        BaseNotifier notifier = new BaseNotifier();
        ReflectionTestUtils.setField(notifier, "socketHandler", handler);

        notifier.send("user", "msg");
        verify(handler).sendMessage("user", "msg");
    }

    // @Test
    // void testBaseNotifierSend_ExceptionThrown() throws Exception {
    // SocketHandler handler = mock(SocketHandler.class);
    // doThrow(new Exception("fail")).when(handler).sendMessage(any(), any());
    //
    // BaseNotifier notifier = new BaseNotifier();
    // ReflectionTestUtils.setField(notifier, "socketHandler", handler);
    //
    // assertThrows(RuntimeException.class, () -> notifier.send("user", "msg"));
    // }

    @Test
    void testBaseNotifierIsUserOnline() {
        SocketHandler handler = mock(SocketHandler.class);
        when(handler.hasUserSession("user")).thenReturn(true);

        BaseNotifier notifier = new BaseNotifier();
        ReflectionTestUtils.setField(notifier, "socketHandler", handler);

        assertTrue(notifier.isUserOnline("user"));
    }

    private SuperDataStructure setupStoreTreeWithOwner(int storeId, int ownerId) {
        SuperDataStructure superDS = new SuperDataStructure();
        superDS.addNewStore(storeId, ownerId);
        return superDS;
    }

    @Test
    void testGetWorkersTreeInStore_StoreNotExists() {
        SuperDataStructure superDS = new SuperDataStructure();
        Exception ex = assertThrows(Exception.class, () -> superDS.getWorkersTreeInStore(999));
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testDeleteManager_StoreNotExists() {
        SuperDataStructure superDS = new SuperDataStructure();
        DevException ex = assertThrows(DevException.class, () -> superDS.deleteManager(999, 1, 2));
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testDeleteManager_ManagerNotFound() throws Exception {
        SuperDataStructure superDS = setupStoreTreeWithOwner(1, 10);
        UIException ex = assertThrows(UIException.class, () -> superDS.deleteManager(1, 10, 999));
        assertEquals(ErrorCodes.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testDeleteManager_NotAManager() throws Exception {
        SuperDataStructure superDS = setupStoreTreeWithOwner(1, 10);
        superDS.addNewOwner(1, 10, 11); // not a manager
        UIException ex = assertThrows(UIException.class, () -> superDS.deleteManager(1, 10, 11));
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testDeleteManager_NotParent() throws Exception {
        SuperDataStructure superDS = setupStoreTreeWithOwner(1, 10);
        superDS.addNewOwner(1, 10, 11);
        superDS.addNewManager(1, 10, 12);
        // now 11 is owner, but try deleting 12 using 11 (not the parent)
        UIException ex = assertThrows(UIException.class, () -> superDS.deleteManager(1, 11, 12));
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testCheckDeactivateStore_StoreNotExists() {
        SuperDataStructure superDS = new SuperDataStructure();
        DevException ex = assertThrows(DevException.class, () -> superDS.checkDeactivateStore(999, 1));
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testGetWorkersInStore_StoreNotExists() {
        SuperDataStructure superDS = new SuperDataStructure();
        Exception ex = assertThrows(Exception.class, () -> superDS.getWorkersInStore(999));
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testCloseStore_StoreNotExists() {
        SuperDataStructure superDS = new SuperDataStructure();
        Exception ex = assertThrows(Exception.class, () -> superDS.closeStore(999));
        assertEquals("store does not exist in superDS", ex.getMessage());
    }

    @Test
    void testDeleteOffer_StoreOffersNull() {
        SuperDataStructure superDS = new SuperDataStructure();
        Exception ex = assertThrows(Exception.class, () -> superDS.deleteOffer(1, 10, 20));
        assertEquals("store offers is null", ex.getMessage());
    }

    @Test
    void testGetOffer_OfferNotFound() throws Exception {
        SuperDataStructure superDS = new SuperDataStructure();
        superDS.makeOffer(new Offer(1, 10, true, List.of(), "sdfsd"), 1);
        Exception ex = assertThrows(Exception.class, () -> superDS.getOffer(1, 99, 88));
        assertTrue(ex.getMessage().contains("No offer found"));
    }

    @Test
    void testRemoveUserAccordingly_UserNotExistsAnywhere() throws Exception {
        SuperDataStructure superDS = new SuperDataStructure();
        int result = superDS.removeUserAccordingly(999);
        assertEquals(-1, result);
    }

    @Test
    void testRemoveUserAccordingly_UserExistsInOffer() throws Exception {
        SuperDataStructure superDS = new SuperDataStructure();
        superDS.makeOffer(new Offer(1, 10, true, List.of(), "sdfsd"), 1);
        int result = superDS.removeUserAccordingly(10);
        assertEquals(10, result);
    }

    @Test
    void testRemoveUserAccordingly_UserExistsInTree() throws Exception {
        SuperDataStructure superDS = setupStoreTreeWithOwner(1, 10);
        superDS.addNewOwner(1, 10, 20);
        int result = superDS.removeUserAccordingly(20);
        assertEquals(20, result);
    }

    @Test
    void testAddPolicy_InvalidPermission_Fails() throws Exception {
        // user NGToken is not the store owner
        UIException ex = assertThrows(UIException.class,
                () -> storeService.addPurchasePolicy(NGToken, 1, "NO_ALCOHOL", null));
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
    }

    @Test
    void testRemovePolicy_UnknownPolicyKey_Fails() throws Exception {
        UIException ex = assertThrows(UIException.class,
                () -> storeService.removePurchasePolicy(NOToken, 1, "UNKNOWN", null));
        assertEquals(ErrorCodes.NO_POLICY, ex.getErrorCode());
    }

    @Test
    void testRemovePolicy_MinQtyMissingParam_Fails() throws Exception {
        Exception ex = assertThrows(Exception.class,
                () -> storeService.removePurchasePolicy(NOToken, 1, "MIN_QTY", null));
        assertEquals("Param is required!", ex.getMessage());
    }

    @Test
    void testAddAndRemove_NoAlcoholPolicy_Success_sucessbuy() throws Exception {
        // Add NO_ALCOHOL policy
        storeService.addPurchasePolicy(NOToken, 1, "NO_ALCOHOL", null);
        stockService.addProduct(NOToken, "Red Wine", Category.ALCOHOL, "1948 vintage alcohol", null);
        stockService.addItem(1, NOToken, 2, 10, 2000, Category.ALCOHOL);
        var itemStoreDTO1 = new ItemStoreDTO(2, 10, 2000, Category.ALCOHOL, 0, 1, "Red Wine", "TestStore");

        Store store = storeRepository.findStoreByID(1);
        assertEquals(1, store.getPurchasePolicies().size());
        userService.addToUserCart(NOToken, itemStoreDTO1, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NOToken, paymentDetails, supplyDetails);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(2000.0,
                receipts[0].getProductsList().size() * receipts[0].getProductsList().get(0).getPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NOToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(2000, r.getFinalPrice());
        // assertTrue(stockService.getProductsInStore(1)[1].getQuantity() ==9);

        assertEquals(2, stockService.getProductsInStore(1).length);

        // Remove NO_ALCOHOL policy
        PurchasePolicy policy = store.getPurchasePolicies().getFirst();
        store.removePurchasePolicy(policy);

        assertTrue(store.getPurchasePolicies().isEmpty());
    }

    @Test
    void testAddAndRemove_NoAlcoholPolicy_Success_failed() throws Exception {
        // Add NO_ALCOHOL policy
        storeService.addPurchasePolicy(NOToken, 1, "NO_ALCOHOL", null);
        stockService.addProduct(NOToken, "Red Wine", Category.ALCOHOL, "1948 vintage alcohol", null);
        stockService.addItem(1, NOToken, 2, 10, 1000, Category.ALCOHOL);
        var itemStoreDTO1 = new ItemStoreDTO(2, 10, 2000, Category.ALCOHOL, 0, 1, "Red Wine", "TestStore");

        Store store = storeRepository.findStoreByID(1);
        assertEquals(1, store.getPurchasePolicies().size());
        userService.addToUserCart(NGToken, itemStoreDTO1, 1);
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails));
        for (var a : stockService.getProductsInStore(1)) {
            System.out.println(a.getProductName());
        }
        assertEquals(2, stockService.getProductsInStore(1).length);
        assertEquals(10, stockService.getProductsInStore(1)[1].getQuantity());

        // Remove NO_ALCOHOL policy
        PurchasePolicy policy = store.getPurchasePolicies().getFirst();
        store.removePurchasePolicy(policy);

        assertTrue(store.getPurchasePolicies().isEmpty());
    }

    @Test
    void testAddAndRemove_MinQtyPolicy_Success_failedbuy() throws Exception {
        int minQty = 2;
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        // Add MIN_QTY policy
        storeService.addPurchasePolicy(NOToken, 1, "MIN_QTY", minQty);

        Store store = storeRepository.findStoreByID(1);
        assertTrue(store.getPurchasePolicies().size() == 1);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        Exception ex = assertThrows(Exception.class,
                () -> purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails));
        PurchasePolicy policy = store.getPurchasePolicies().get(0);
        store.removePurchasePolicy(policy);

        assertFalse(store.getPurchasePolicies().stream()
                .anyMatch(p -> p.toString().contains("MinQuantity")));
    }

    @Test
    void testAddAndRemove_MinQtyPolicy_Success_sucessbuy() throws Exception {
        int minQty = 2;
        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed

        // Add MIN_QTY policy
        storeService.addPurchasePolicy(NOToken, 1, "MIN_QTY", minQty);

        Store store = storeRepository.findStoreByID(1);
        assertTrue(store.getPurchasePolicies().size() == 1);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, paymentDetails, supplyDetails);
        PurchasePolicy policy = store.getPurchasePolicies().get(0);
        store.removePurchasePolicy(policy);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(4000.0,
                receipts[0].getFinalPrice());

        List<ReceiptDTO> result = orderService.getReceiptDTOsByUser(NGToken);

        assertEquals(1, result.size());
        ReceiptDTO r = result.get(0);
        assertEquals("TestStore", r.getStoreName());
        assertEquals(4000, r.getFinalPrice());
        assertTrue(stockService.getProductsInStore(1)[0].getQuantity() == 8);

        assertEquals(1, stockService.getProductsInStore(1).length);

        assertTrue(store.getPurchasePolicies().isEmpty());
    }

}
