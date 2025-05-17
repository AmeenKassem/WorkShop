package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Owner_ManagerAT extends AcceptanceTests {
    Real real = new Real();

    @BeforeEach
    void setup() throws Exception {
        // ====== ADMIN SETUP ======
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        String adminGuest = real.userService.generateGuest();
        assertEquals(adminGuestToken, adminGuest);
        assertTrue(real.userService.register(adminGuest, "admin", "adminPass", 18));
        String adminToken = real.userService.login(adminGuest, "admin", "adminPass");
        assertEquals(adminUserToken, adminToken);
        testSystem_InitMarket(adminUserToken);

        // ====== STORE OWNER SETUP ======
        int ownerId = 10;
        String ownerGuestToken = "guest-token";
        String ownerUserToken = "user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(ownerGuestToken);
        when(real.mockAuthRepo.validToken(ownerGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(ownerUserToken);
        when(real.mockAuthRepo.validToken(ownerUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerUserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(ownerUserToken)).thenReturn("owner");
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);

        String ownerGuest = real.userService.generateGuest();
        assertEquals(ownerGuestToken, ownerGuest);
        assertTrue(real.userService.register(ownerGuest, "owner", "owner", 18));
        String ownerToken = real.userService.login(ownerGuest, "owner", "owner");
        assertEquals(ownerUserToken, ownerToken);

        int storeId = 100;
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);

        // ====== ADD PRODUCT TO SYSTEM ======
        int productId = 200;
        String[] keywords = {"Phone", "Smartphone"};
        when(real.mockStockRepo.addProduct("Phone", Category.ELECTRONICS, "Smartphone", keywords)).thenReturn(productId);
        int returnedProductId = real.stockService.addProduct(ownerToken, "Phone", Category.ELECTRONICS, "Smartphone", keywords);
        assertEquals(productId, returnedProductId);

        // ====== ADD ITEM TO STORE ======
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", keywords));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        int itemAdded = real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);
        assertEquals(itemAdded, productId);

        // ====== REGISTERED USER SETUP ======
        int userId = 20;
        String userGuestToken = "guest-token-2";
        String userToken = "user-token-2";

        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(userGuestToken);
        when(real.mockAuthRepo.validToken(userGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("user2", "pass2")).thenReturn(userId);
        when(real.mockAuthRepo.generateUserToken(userId, "user2")).thenReturn(userToken);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(userToken)).thenReturn("user2");
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);
        when(real.mockAuthRepo.isRegistered(userToken)).thenReturn(true);

        String guestToken = real.userService.generateGuest();
        assertEquals(userGuestToken, guestToken);
        assertTrue(real.userService.register(guestToken, "user2", "pass2", 18));
        String loginToken = real.userService.login(guestToken, "user2", "pass2");
        assertEquals(userToken, loginToken);
    }

    // ========== Store Owner Use Cases ==========

    @Test
    void testOwner_AddProductToStock() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        String ownerToken = "user-token";

        // === Add new product to system ===
        int productId = 300;
        String[] keywords = {"Tablet", "Touchscreen"};
        when(real.mockStockRepo.addProduct("Tablet", Category.ELECTRONICS, "10-inch Tablet", keywords))
                .thenReturn(productId);
        int returnedProductId = real.stockService.addProduct(ownerToken, "Tablet", Category.ELECTRONICS, "10-inch Tablet", keywords);
        assertEquals(productId, returnedProductId);

        // === Add item to store ===
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Tablet", productId, Category.ELECTRONICS, "10-inch Tablet", keywords));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));

        int itemAdded = real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);
        assertEquals(productId, itemAdded);
    }

    //todo:this case is not checked
    @Test
    void testOwner_AddProductToStock_Failure_InvalidProductData() throws Exception {
        int storeId = 100;
        int productId = 200;
        String ownerToken = "owner-token";
        int ownerId = 10;

        // === Auth & Setup ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);

        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );

        // === Act & Assert ===
        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addItem(storeId, ownerToken, productId, -5, -999, Category.ELECTRONICS)
        );

        assertEquals("price or quantity not in range", ex.getMessage());
        assertEquals(404, ex.getNumber());
    }


    @Test
    void testOwner_AddProductToStock_Failure_StoreNotFound() throws Exception {
        int storeId = 999; // Non-existent store
        int productId = 200;
        String ownerToken = "owner-token";
        int ownerId = 10;

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );
        when(real.mockStoreRepo.checkStoreExistance(storeId))
                .thenThrow(new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND));

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS)
        );

        // Optional: verify the details of the exception
        assertEquals(" store does not exist.", ex.getMessage());
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_DeleteProductFromStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        // Mock that owner has permission to delete from stock
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);

        // Mock that the product still exists before deletion
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );

        // No need to mock removeItem return, just verify the correct result
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true); // optional safety

        // Act
        int removedProductId = real.stockService.removeItem(storeId, ownerToken, productId);

        // Assert
        assertEquals(productId, removedProductId);
    }



    @Test
    void testOwner_DeleteProductFromStock_Failure_NoPermission() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        // Auth + user setup
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);

        // Simulate lack of permission
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(false);

        // Ensure product exists so we reach the permission check
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.removeItem(storeId, ownerToken, productId)
        );

        assertEquals("this worker is not authorized!", ex.getMessage());
        assertEquals(ErrorCodes.NO_PERMISSION, ex.getNumber());
    }

    @Test
    void testOwner_UpdatePriceProductInStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdatePrice)).thenReturn(true);

        when(real.mockStockRepo.updatePrice(storeId, productId, 10)).thenReturn(true);

        assertDoesNotThrow(() ->
                real.stockService.updatePrice(storeId, ownerToken, productId, 10)
        );
    }

    @Test
    void testOwner_UpdateQuantityProductInStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdateQuantity)).thenReturn(true);
        when(real.mockStockRepo.updateQuantity(storeId, productId, 10)).thenReturn(true);

        assertDoesNotThrow(() ->
                real.stockService.updateQuantity(storeId, ownerToken, productId, 10)
        );

        //verify(real.mockStockRepo).updateQuantity(storeId, productId, 10);
    }
    //todo:this case is not checked
    @Test
    void testOwner_UpdateProductInStock_Failure_InvalidData() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdateQuantity)).thenReturn(true);

        // Simulate validation failure: negative price
        when(real.mockStockRepo.updateQuantity(storeId, productId, -10)).thenThrow(new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION));
        //real.stockService.updateQuantity(storeId, ownerToken, productId,  -10),new UIException("This worker is not authorized!", ErrorCodes.NO_PERMISSION));
        //suppose to return -1 or something or throw ex .or delete this test
        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.updateQuantity(storeId, ownerToken, productId, -10)
        );
        assertEquals(ex.getMessage(), "the quantity not in range");
        assertEquals(ex.getNumber(), 404);

    }

    @Test
    void testOwner_AddStoreOwner_Success() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        // === Auth & Registration ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === Ownership logic ===
        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        // === Approval messaging ===
        //when(real.storeService.sendMessageToTakeApproval(ownerId, userId)).thenReturn(true);

        // === Act ===
        int result = real.storeService.AddOwnershipToStore(storeId, ownerToken, userId);

        // === Assert ===
        assertEquals(userId, result);
    }


    @Test
    void testOwner_AddStoreOwner_ReassignSameUser_Failure() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        // === Auth & Registration ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === First assignment (succeeds) ===
        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        int first = real.storeService.AddOwnershipToStore(storeId, ownerToken, userId);
        assertEquals(userId, first);

        // === Second assignment (fails) ===
        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId))
                .thenThrow(new UIException("Already assigned", 1006));

        UIException ex = assertThrows(UIException.class, () ->
                real.storeService.AddOwnershipToStore(storeId, ownerToken, userId)
        );

        assertEquals("Already assigned", ex.getMessage());
        assertEquals(1006, ex.getNumber());
    }

    @Test
    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int newOwnerId = 404;
        String ownerToken = "user-token";

        // === Auth for assigner ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === Simulate that new owner check fails
        doThrow(new UIException("You are not regestered user!", ErrorCodes.USER_NOT_LOGGED_IN))
                .when(real.mockUserRepo)
                .checkUserRegister_ThrowException(newOwnerId);

        // === Act + Assert
        UIException ex = assertThrows(UIException.class, () ->
                real.storeService.AddOwnershipToStore(storeId, ownerToken, newOwnerId)
        );

        assertEquals("You are not regestered user!", ex.getMessage());
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }


    @Test
    void testOwner_AddStoreOwner_Rejected() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        // === Auth & Registration ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === Ownership logic ===
        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        // === Approval messaging ===
        real.storeService.sendMessageToTakeApproval(ownerId, ownerId);

        // === Act ===
        //todo:implement it when u finish then function "sendMessageToTakeApproval"
        int result = real.storeService.AddOwnershipToStore(storeId, ownerToken, ownerId);

        // === Assert ===
        assertEquals(-1, result);
    }


    @Test
    void testOwner_DeleteStoreOwner() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        // === Auth & Registration ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === Ownership logic ===
        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        // === Act ===
        int result = real.storeService.AddOwnershipToStore(storeId, ownerToken, userId);

        // === Assert ===
        assertEquals(userId, result);

        assertDoesNotThrow(() -> real.storeService.DeleteOwnershipFromStore(storeId, ownerToken, userId));

    }
    @Test
    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        //int userId = 20;
        String ownerToken = "user-token";
        //String userToken = "user-token-2";

        // === Auth & Registration ===
        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

//        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(21)).thenReturn(false);

        when(real.mockUserRepo.checkUserRegister_ThrowException(21)).thenThrow(new IllegalArgumentException("NO SUCH USER"));
        // === Store status ===
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        // === Ownership logic ===

        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, 21)).thenReturn(false);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, 21)).thenReturn(false);

        // === Act ===
        //int result = real.storeService.AddOwnershipToStore(storeId, ownerToken, userId);

        // === Assert ===
        //assertEquals(userId, result);

        Exception ex = assertThrows(Exception.class, () -> {
            real.storeService.DeleteOwnershipFromStore(storeId, ownerToken, 21);
        });
        assertEquals(ex.getMessage(), "NO SUCH USER");
    }


    @Test
    void testOwner_AddStoreManager_Success() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
    }

    @Test
    void testOwner_AddStoreManager_Rejected() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);

//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.AddManagerToStore(ownerToken, storeId, managerId);
//        });
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, ownerId, a);

        assertEquals(result, -1);
    }


    @Test
    void testOwner_AddStoreManager_Failure_UserNotExist() throws Exception {
        int nonExistentUserId = 9999;
        int ownerId = 10;
        String ownerToken = "user-token"; // מתוך setup
        int storeId = 100;

        // סימולציה: הבעלים מורשה למנות מנהלים
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        // סימולציה: המשתמש שמנסים למנות לא רשום בכלל — לכן תיזרק שגיאה
        doThrow(new UIException("User not registered", ErrorCodes.USER_NOT_FOUND))
                .when(real.mockUserRepo).checkUserRegister_ThrowException(nonExistentUserId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        // ניסיון מינוי – אמור להיכשל
        assertThrows(UIException.class, () -> {
            real.storeService.AddManagerToStore(storeId, ownerToken, nonExistentUserId, a);
        });
    }

    @Test
    void testOwner_AddStoreManager_Failure_AlreadyManager() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doThrow(new UIException("This worker is already an owner/manager", ErrorCodes.NO_PERMISSION))
                .when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.AddManagerToStore(storeId, ownerToken, managerId, permissions);
        });

        assertEquals("This worker is already an owner/manager", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Failure_OwnerTargetNotFound() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doThrow(new DevException("Owner does not exist in this store"))
                .when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        DevException ex = assertThrows(DevException.class, () -> {
            real.storeService.AddManagerToStore(storeId, ownerToken, managerId, permissions);
        });

        assertEquals("Owner does not exist in this store", ex.getMessage());
    }

    @Test
    void testOwner_AddStoreManager_Failure_StoreClosed() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doThrow(new DevException("Store not found with ID: " + storeId))
                .when(real.mockStoreRepo).checkStoreIsActive(storeId);

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.DeleteFromStock);

        DevException ex = assertThrows(DevException.class, () -> {
            real.storeService.AddManagerToStore(storeId, ownerToken, managerId, permissions);
        });

        assertEquals("Store not found with ID: " + storeId, ex.getMessage());
    }
    //delete manager
    @Test
    void testDeleteManager_Success() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
        when(real.mockUserRepo.checkUserRegister_ThrowException(ownerId)).thenReturn(true);
        when(real.mockSusRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId)).thenReturn(true);
        when(real.mockUserRepo.checkUserRegisterOnline_ThrowException(managerId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);
        real.storeService.deleteManager(storeId,ownerToken,managerId);
    }
    @Test
    void testDeleteManager_Failure_StoreExist() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
        when(real.mockUserRepo.checkUserRegister_ThrowException(ownerId)).thenReturn(true);
        when(real.mockSusRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId)).thenReturn(true);
        when(real.mockUserRepo.checkUserRegisterOnline_ThrowException(managerId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenThrow(new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND));
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);
        //real.storeService.deleteManager(storeId,ownerToken,managerId);

        UIException ex=assertThrows(UIException.class, () -> {
            real.storeService.deleteManager(storeId,ownerToken,managerId);
        });
        assertEquals(ex.getMessage()," store does not exist.");
        assertEquals(ex.getNumber(),1005);
    }

    @Test
    void testDeleteManager_Failure_StoreNotActive() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
        when(real.mockUserRepo.checkUserRegister_ThrowException(ownerId)).thenReturn(true);
        when(real.mockSusRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId)).thenReturn(true);
        when(real.mockUserRepo.checkUserRegisterOnline_ThrowException(managerId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenThrow(new DevException(" store is not active"));
        //real.storeService.deleteManager(storeId,ownerToken,managerId);

        DevException ex=assertThrows(DevException.class, () -> {
            real.storeService.deleteManager(storeId,ownerToken,managerId);
        });
        assertEquals(ex.getMessage()," store is not active");
    }
    @Test
    void testDeleteManager_Failure_StoreNotFound() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
        when(real.mockUserRepo.checkUserRegister_ThrowException(ownerId)).thenReturn(true);
        when(real.mockSusRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId)).thenReturn(true);
        when(real.mockUserRepo.checkUserRegisterOnline_ThrowException(managerId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenThrow(new DevException("Store not found with ID: " + storeId));
        //real.storeService.deleteManager(storeId,ownerToken,managerId);

        DevException ex=assertThrows(DevException.class, () -> {
            real.storeService.deleteManager(storeId,ownerToken,managerId);
        });
        assertEquals(ex.getMessage(),"Store not found with ID: "+storeId);
    }

    @Test
    void testOwner_ManageStoreManagerPermissions() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        //doNothing().when(real.mockIOSrepo).AddManagerToStore(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerToken, managerId, a);
        assertEquals(result, managerId);
        List<Permission> updatedPermissions = new LinkedList<>();
        updatedPermissions.add(Permission.UpdatePrice);

        doNothing().when(real.mockIOSrepo).changePermissions(ownerId, managerId, storeId, updatedPermissions);

        assertDoesNotThrow(() -> {
            real.storeService.changePermissions(ownerToken, managerId, storeId, updatedPermissions);
        });
    }
    @Test
    void testOwner_ManageStoreManagerPermissions_Failure_NotAManagerFlag() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int notManagerId = 20;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.UpdatePrice);

        doThrow(new UIException("User is not a manager", ErrorCodes.NO_PERMISSION))
                .when(real.mockIOSrepo)
                .changePermissions(ownerId, notManagerId, storeId, permissions);

        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.changePermissions(ownerToken, notManagerId, storeId, permissions);
        });

        assertEquals("User is not a manager", ex.getMessage());
    }

    @Test
    void testOwner_DeactivateStore() throws Exception {
        int storeId = 100;
        String ownerToken = "owner-token";
        int result=real.storeService.deactivateteStore(100,ownerToken);
        assertEquals(result,storeId);
    }

    @Test
    void testOwner_DeactivateStore_Failure_AlreadyInactive() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";

        Store s = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(s);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        real.storeService.deactivateteStore(storeId,ownerToken);


        // Simulate store already deactivated → should throw
        doThrow(new UIException("can't deactivate an DEactivated store", ErrorCodes.DEACTIVATED_STORE))
                .when(real.mockStoreRepo).deactivateStore(storeId, ownerId);

        // Assert exception is thrown
        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.deactivateteStore(storeId, ownerToken);
        });

        assertEquals("can't deactivate an DEactivated store", ex.getMessage());
    }

    @Test
    void testOwner_ViewStorePurchaseHistory() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";
        List<OrderDTO> history=real.storeService.veiwStoreHistory(storeId);
        assertTrue(history.isEmpty());
    }

    @Test
    void testOwner_ViewStorePurchaseHistory_Failure_StoreNotExist() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";
        doThrow(new UIException("Store does not exist!", ErrorCodes.STORE_NOT_FOUND))
                .when(real.mockOrderRepo).getAllOrderByStore(storeId);

        // Assert exception is thrown
        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.veiwStoreHistory(storeId);
        });

        assertEquals("Store does not exist!", ex.getMessage());


    }

    //todo not implemented
    @Test
    void testOwner_RequestStoreRolesInfoAndPermission() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";
        List<WorkerDTO> worker=real.storeService.ViewRolesAndPermissions(storeId);
        assertTrue(worker.isEmpty());
    }

    @Test
    void testOwner_AddPurchasePolicy() throws Exception {
        //TODO
    }
    @Test
    void testOwner_AddPurchasePolicy_Failure_InvalidPolicy() throws Exception {
        //TODO
    }
    @Test
    void testOwner_AddPurchasePolicy_Failure_NotOwner() throws Exception {
        //TODO

    }
//
    @Test
    void testOwner_DeletePurchasePolicy() throws Exception {
        //TODO

    }

    @Test
    void testOwner_DeletePurchasePolicy_Failure_NotFound() throws Exception {
        //TODO
    }
//
    @Test
    void testOwner_DeletePurchasePolicy_Failure_NoPermission() throws Exception {
        //TODO
    }

    @Test
    void testOwner_ReplyToMessage() throws Exception {
        //TODO
    }
//
    @Test
    void testOwner_ReplyToMessage_Failure_UserNotFound() throws Exception {
        //TODO
    }
//
    @Test
    void testOwner_ReplyToMessage_Failure_MessageNotFound() throws Exception {
        //TODO
    }

        @Test
    void testOwner_ReopenStore() throws Exception {
        //TODO
    }
    //todo:this case is not checked
    @Test
    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {
        int storeId = 100;
        int productId = 999;  // Non-existent product
        int ownerId = 10;
        String ownerToken = "user-token";  // from setup

        // === Simulate permission granted
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);

        // === Simulate product does NOT exist by throwing exception
        when(real.mockStockRepo.findByIdInSystem_throwException(productId))
                .thenThrow(new UIException("Product not found!", ErrorCodes.PRODUCT_NOT_FOUND));

        // === Act & Assert
        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.removeItem(storeId, ownerToken, productId)
        );

        assertEquals("Product not found!", ex.getMessage());
        assertEquals(ErrorCodes.PRODUCT_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_AddToAuction_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        //create list have all permissions
        List<Permission> permissions=new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId,storeId,Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToAuction(ownerToken,storeId,productId,1,5000,2);
    }

    @Test
    void testOwner_AddToBID_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        //create list have all permissions
        List<Permission> permissions=new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId,storeId,Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToBid(ownerToken,storeId,productId,1);
    }

    @Test
    void testOwner_AddToRandom_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        //create list have all permissions
        List<Permission> permissions=new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId,storeId,Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToRandom(ownerToken,productId,1,100,storeId,5000);
    }
    @Test
    void testOwner_AddtoBID_RANDOM_AUCTION_FAILURE() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        //create list have all permissions
        List<Permission> permissions=new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId,storeId,null)).thenReturn(false);
        //real.stockService.setProductToBid(ownerToken,storeId,productId,1);

        UIException ex = assertThrows(UIException.class, () ->
                real.stockService.setProductToBid(ownerToken,storeId,productId,1)
        );
        assertEquals(ex.getMessage(),"you have no permession to set product to bid.");
        assertEquals(1004,ex.getNumber());
    }


}

