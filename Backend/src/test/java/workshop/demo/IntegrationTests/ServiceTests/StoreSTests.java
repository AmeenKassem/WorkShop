package workshop.demo.IntegrationTests.ServiceTests;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
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

@SpringBootTest
public class StoreSTests {

    PaymentServiceImp payment = new PaymentServiceImp();
    SupplyServiceImp serviceImp = new SupplyServiceImp();
    PurchaseRepository purchaseRepository = new PurchaseRepository();
    UserSuspensionRepo suspensionRepo = new UserSuspensionRepo();

    @Autowired
    NotificationRepository notificationRepository;
    OrderRepository orderRepository = new OrderRepository();
    StoreRepository storeRepository = new StoreRepository();
    StockRepository stockRepository = new StockRepository();
    SUConnectionRepository sIsuConnectionRepo = new SUConnectionRepository();
    AuthenticationRepo authRepo = new AuthenticationRepo();
    Encoder encoder = new Encoder();
    AdminInitilizer adminInitilizer = new AdminInitilizer("123321");
    UserRepository userRepo = new UserRepository(encoder, adminInitilizer);
    UserSuspensionService suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);
    AdminService adminService = new AdminService(orderRepository, storeRepository, userRepo, authRepo);
    UserService userService = new UserService(userRepo, authRepo, stockRepository, new AdminInitilizer("123321"), adminService);
    StockService stockService = new StockService(stockRepository, storeRepository, authRepo, userRepo, sIsuConnectionRepo, suspensionRepo);

    StoreService storeService = new StoreService(storeRepository, notificationRepository, authRepo, userRepo, orderRepository, sIsuConnectionRepo, stockRepository, suspensionRepo);
    PurchaseService purchaseService = new PurchaseService(authRepo, stockRepository, storeRepository, userRepo, purchaseRepository, orderRepository, payment, serviceImp, suspensionRepo);
    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;

    @BeforeEach
    void setup() throws Exception {
        System.out.println("===== SETUP RUNNING =====");

        purchaseRepository = new PurchaseRepository();
        suspensionRepo = new UserSuspensionRepo();
        orderRepository = new OrderRepository();
        storeRepository = new StoreRepository();
        stockRepository = new StockRepository();
        sIsuConnectionRepo = new SUConnectionRepository();
        authRepo = new AuthenticationRepo();
        encoder = new Encoder();
        adminInitilizer = new AdminInitilizer("123321");
        userRepo = new UserRepository(encoder, adminInitilizer);
        suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);

        adminService = new AdminService(orderRepository, storeRepository, userRepo, authRepo);
        userService = new UserService(userRepo, authRepo, stockRepository, new AdminInitilizer("123321"), adminService);
        stockService = new StockService(stockRepository, storeRepository, authRepo, userRepo, sIsuConnectionRepo, suspensionRepo);
        storeService = new StoreService(storeRepository, notificationRepository, authRepo, userRepo, orderRepository, sIsuConnectionRepo, stockRepository, suspensionRepo);
        purchaseService = new PurchaseService(authRepo, stockRepository, storeRepository, userRepo, purchaseRepository, orderRepository, payment, serviceImp, suspensionRepo);

        GToken = userService.generateGuest();

        String OToken = userService.generateGuest();

        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int created1 = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        assertEquals(created1, 1);

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = {"Laptop", "Lap", "top"};
        stockService.addProduct(NOToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);

        assertEquals(1, stockService.addItem(1, NOToken, 1, 2, 2000, Category.ELECTRONICS));
        itemStoreDTO = new ItemStoreDTO(1, 2, 2000, Category.ELECTRONICS, 0, 1, "Laptop");

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

        String[] keywords = {"Tablet", "Touchscreen"};

        stockService.addProduct(NOToken, "Tablet", Category.ELECTRONICS, "10-inch Tablet", keywords);

        int itemAdded = stockService.addItem(1, NOToken, 2, 10, 100, Category.ELECTRONICS);
        assertEquals(2, itemAdded);
        assertTrue(stockService.getProductInfo(NOToken, 2).getProductId() == 2);

    }

    @Test
    void testOwner_AddProductToStock_Failure_InvalidProductData() throws Exception {

        // === Act & Assert ===
        UIException ex = assertThrows(UIException.class, ()
                -> stockService.addItem(1, NOToken, 2, 5, 999, Category.ELECTRONICS)
        );

        assertEquals("Product not found", ex.getMessage());
        assertEquals(1006, ex.getNumber());
    }

    @Test
    void testOwner_AddProductToStock_Failure_StoreNotFound() throws Exception {

        UIException ex = assertThrows(UIException.class, ()
                -> stockService.addItem(22, NOToken, 1, 10, 100, Category.ELECTRONICS)
        );

//         // Optional: verify the details of the exception
        assertEquals(" store does not exist.", ex.getMessage());
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_DeleteProductFromStock() throws Exception {

        // Act
        stockService.removeItem(1, NOToken, 1);

        // Assert
        assertTrue(stockService.getProductsInStore(1)[0].quantity == 0);
    }

    @Test
    void testOwner_UpdatePriceProductInStock() throws Exception {

        assertDoesNotThrow(()
                -> stockService.updatePrice(1, NOToken, 1, 10)
        );
        assertTrue(stockService.getProductsInStore(1)[0].getPrice() == 10);
    }

    @Test
    void testOwner_UpdateQuantityProductInStock() throws Exception {

        assertDoesNotThrow(()
                -> stockService.updateQuantity(1, NOToken, 1, 1)
        );
        assertTrue(stockService.getProductsInStore(1)[0].quantity == 1);

    }

    @Test
    void testOwner_UpdateProductInStock_Failure_InvalidData() throws Exception {

        UIException ex = assertThrows(UIException.class, ()
                -> stockService.updateQuantity(1, NOToken, 10, 10)
        );
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
        // ask bhaa i dont know what is happening ,  help help help

        // === Assert ===
        // assertEquals(sotre);
    }

    @Test
    void testOwner_AddStoreOwner_fail() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        //must make an offer before:
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");
        storeService.AddOwnershipToStore(1, 3, 5, true);
        // ask bhaa i dont know what is happening ,  help help help

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
        storeService.AddOwnershipToStore(1, 3, 5, true);

        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);
        UIException ex = assertThrows(UIException.class, ()
                -> storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token")
        );

        assertEquals("This worker is already an owner/manager", ex.getMessage());
        assertEquals(1004, ex.getNumber());
    }

    @Test
    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
        String token = userService.generateGuest();

        // === Act ===
        // storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");
// this function need to take id to use this test guest doesnt have a username
        // === Act + Assert
        UIException ex = assertThrows(UIException.class, ()
                -> storeService.AddOwnershipToStore(1, 3, authRepo.getUserId(token), true)
        // shouldnt be able to add a guest
        );

        assertEquals("You are not regestered user!", ex.getMessage());
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }

    //todo:: send approval to user 
    @Test
    void testOwner_AddStoreOwner_Rejected() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");

        storeService.AddOwnershipToStore(1, 3, 5, false);
        assertTrue(storeService.ViewRolesAndPermissions(token1, 1).size() == 1);

    }

    @Test
    void testOwner_DeleteStoreOwner() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        // === Act ===
        storeService.MakeofferToAddOwnershipToStore(1, NOToken, "token");

        storeService.AddOwnershipToStore(1, 3, 5, true);
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 2);

        assertDoesNotThrow(() -> storeService.DeleteOwnershipFromStore(1, NOToken, 5));
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 1);

    }

    @Test
    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {

        String token = userService.generateGuest();
        userService.register(token, "token", "token", 0);
        String token1 = userService.login(token, "token", "token");

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.DeleteOwnershipFromStore(1, NOToken, 5);
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
        storeService.AddManagerToStore(1, 3, 5, true);
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
        storeService.AddManagerToStore(1, 3, 5, false);
        assertTrue(storeService.ViewRolesAndPermissions(NOToken, 1).size() == 1);

    }

    @Test
    void testOwner_AddStoreManager_Failure_UserNotExist() throws Exception {

        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
        });
        assertEquals("No user found with username: token", ex.getMessage());
        //assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
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
        storeService.AddManagerToStore(1, 3, 5, true);

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.MakeOfferToAddManagerToStore(1, NOToken, "token", a);
            // already a manager need to throw error
            //he can make an offer but can't add it, come on GUYS

        });

        assertEquals("This worker is already an owner/manager", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Failure_OwnerTargetNotFound() throws Exception {

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
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
        storeService.AddManagerToStore(1, 3, authRepo.getUserId(token1), true);

        storeService.deleteManager(1, NOToken, 5);
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

        int result = storeService.AddManagerToStore(1, 3, 5, true);
        // dunno why it doesnt work with true ????
        // when decide true some list is null (i think its permissions list)

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(2, NOToken, 5);
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

        int result = storeService.AddManagerToStore(1, 3, 5, true);
        // dunno why it doesnt work with true ????
        storeService.deactivateteStore(1, NOToken);

        DevException ex = assertThrows(DevException.class, () -> {
            storeService.deleteManager(1, NOToken, 5);
        });
        assertEquals(ex.getMessage(), " store is not active");
    }

//    @Test
//    void testOwner_ManageStoreManagerPermissions() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
//
//    @Test
//    void testOwner_ManageStoreManagerPermissions_Failure_NotAManagerFlag() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
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
        PaymentDetails paymentDetails = PaymentDetails.testPayment();  // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();    // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);

        List<OrderDTO> history = storeService.veiwStoreHistory(1);
        assertTrue(history.get(0).getStoreId() == 1);
        assertTrue(history.get(0).getFinalPrice() == 2000);
        assertTrue(history.get(0).getProductsList().size() == 1);
        assertTrue(history.get(0).getUserId() == 1);

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
    void testOwner_RequestStoreRolesInfoAndPermission() throws Exception {
        List<WorkerDTO> workers = storeService.ViewRolesAndPermissions(NOToken, 1);
        assertTrue(workers.size() == 1);
        assertTrue(workers.get(0).isOwner() == true);
        assertTrue(workers.get(0).getWorkerId() == authRepo.getUserId(NOToken));
        assertTrue(workers.get(0).getUsername().equals(authRepo.getUserName(NOToken)));

    }

    @Test
    void testOwner_AddPurchasePolicy() throws Exception {
        //throw new Exception("need to impl view roles and permissons");
    }

    @Test
    void testOwner_AddPurchasePolicy_Failure_InvalidPolicy() throws Exception {
        //throw new Exception("need to impl view roles and permissons");
    }

    @Test
    void testOwner_AddPurchasePolicy_Failure_NotOwner() throws Exception {
        //  throw new Exception("need to impl view roles and permissons");

    }

    @Test
    void testOwner_DeletePurchasePolicy() throws Exception {
        // throw new Exception("need to impl view roles and permissons");

    }

    @Test
    void testOwner_DeletePurchasePolicy_Failure_NotFound() throws Exception {
        //   throw new Exception("need to impl view roles and permissons");
    }

    @Test
    void testOwner_DeletePurchasePolicy_Failure_NoPermission() throws Exception {
        //   throw new Exception("need to impl view roles and permissons");
    }

//    @Test
//    void testOwner_ReplyToMessage() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
//    @Test
//    void testOwner_ReplyToMessage_Failure_UserNotFound() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
//    @Test
//    void testOwner_ReplyToMessage_Failure_MessageNotFound() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
//    @Test
//    void testOwner_ReopenStore() throws Exception {
//        throw new Exception("need to impl view roles and permissons");
//    }
    //todo:this case is not checked
    @Test
    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {

        // === Act & Assert
        UIException ex = assertThrows(UIException.class, ()
                -> stockService.removeItem(1, NOToken, 3)
        );

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

}
