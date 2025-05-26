package workshop.demo.AcceptanceTest.Tests;
//UpdateProductInStock_Failure_InvalidData() throws Exception

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Registered;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Owner_ManagerAT extends AcceptanceTests {

    Real real = new Real();
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    public Owner_ManagerAT() throws Exception {
    }

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

    @Test
    void testOwner_AddProductToStock() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        String ownerToken = "user-token";

        int productId = 300;
        String[] keywords = {"Tablet", "Touchscreen"};
        when(real.mockStockRepo.addProduct("Tablet", Category.ELECTRONICS, "10-inch Tablet", keywords))
                .thenReturn(productId);
        int returnedProductId = real.stockService.addProduct(ownerToken, "Tablet", Category.ELECTRONICS, "10-inch Tablet", keywords);
        assertEquals(productId, returnedProductId);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Tablet", productId, Category.ELECTRONICS, "10-inch Tablet", keywords));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));

        int itemAdded = real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);
        assertEquals(productId, itemAdded);
    }

    @Test
    void testOwner_AddProductToStock_Failure_InvalidProductData() throws Exception {
        int storeId = 100;
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

        Exception ex = assertThrows(Exception.class, ()
                -> real.stockService.addItem(storeId, ownerToken, productId, -5, -999, Category.ELECTRONICS)
        );

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

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS)
        );

        assertEquals(" store does not exist.", ex.getMessage());
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testOwner_DeleteProductFromStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);

        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true); // optional safety

        int removedProductId = real.stockService.removeItem(storeId, ownerToken, productId);

        assertEquals(productId, removedProductId);
    }

    @Test
    void testOwner_DeleteProductFromStock_Failure_NoPermission() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(false);

        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(
                new Product("Phone", productId, Category.ELECTRONICS, "Smartphone", new String[]{"Phone"})
        );

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.removeItem(storeId, ownerToken, productId)
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

        assertDoesNotThrow(()
                -> real.stockService.updatePrice(storeId, ownerToken, productId, 10)
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

        assertDoesNotThrow(()
                -> real.stockService.updateQuantity(storeId, ownerToken, productId, 10)
        );

    }

    @Test
    void testOwner_AddStoreOwner_Success() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        int result = real.storeService.AddOwnershipToStore(storeId, 10, userId, true);

        assertEquals(userId, result);
    }

    @Test
    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int newOwnerId = 404;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        doThrow(new UIException("You are not regestered user!", ErrorCodes.USER_NOT_LOGGED_IN))
                .when(real.mockUserRepo)
                .checkUserRegister_ThrowException(newOwnerId);

        UIException ex = assertThrows(UIException.class, ()
                -> real.storeService.AddOwnershipToStore(storeId, 10, newOwnerId, true)
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

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        int result = real.storeService.AddOwnershipToStore(storeId, 10, ownerId, false);

        assertEquals(-1, result);
    }

    @Test
    void testOwner_DeleteStoreOwner() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        int userId = 20;
        String ownerToken = "user-token";
        String userToken = "user-token-2";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, userId)).thenReturn(true);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, userId)).thenReturn(true);

        int result = real.storeService.AddOwnershipToStore(storeId, 10, userId, true);

        assertEquals(userId, result);

        assertDoesNotThrow(() -> real.storeService.DeleteOwnershipFromStore(storeId, ownerToken, userId));

    }

    @Test
    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {
        int storeId = 100;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockUserRepo.isOnline(ownerId)).thenReturn(true);

        when(real.mockUserRepo.isRegistered(21)).thenReturn(false);

        doThrow(new IllegalArgumentException("NO SUCH USER"))
                .when(real.mockUserRepo)
                .checkUserRegister_ThrowException(21);
        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, 21)).thenReturn(false);
        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, 21)).thenReturn(false);

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
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, true);
        assertEquals(result, managerId);
    }

    @Test
    void testOwner_AddStoreManager_Rejected() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);

        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, false);

        assertEquals(result, -1);
    }

    @Test
    void testOwner_AddStoreManager_Failure_UserNotExist() throws Exception {
        int nonExistentUserId = 9999;
        int ownerId = 10;
        String ownerToken = "user-token";
        int storeId = 100;

        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doThrow(new UIException("User not registered", ErrorCodes.USER_NOT_FOUND))
                .when(real.mockUserRepo).checkUserRegister_ThrowException(nonExistentUserId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        assertThrows(UIException.class, () -> {
            real.storeService.AddManagerToStore(storeId, 10, nonExistentUserId, true);
        });
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

        assertFalse((real.storeService.AddManagerToStore(storeId, 10, managerId, true)) == -1);

    }

    @Test
    void testDeleteManager_Success() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, ownerId, managerId, true);
        assertEquals(result, managerId);
        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(ownerId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(managerId);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);
        real.storeService.deleteManager(storeId, ownerToken, managerId);
    }

    @Test
    void testDeleteManager_Failure_StoreExist() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, true);
        assertEquals(result, managerId);
        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(ownerId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(managerId);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenThrow(new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND));
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenReturn(true);

        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.deleteManager(storeId, ownerToken, managerId);
        });
        assertEquals(ex.getMessage(), " store does not exist.");
        assertEquals(ex.getNumber(), 1005);
    }

    @Test
    void testDeleteManager_Failure_StoreNotActive() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, true);
        assertEquals(result, managerId);
        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(ownerId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(managerId);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenThrow(new DevException(" store is not active"));

        DevException ex = assertThrows(DevException.class, () -> {
            real.storeService.deleteManager(storeId, ownerToken, managerId);
        });
        assertEquals(ex.getMessage(), " store is not active");
    }

    @Test
    void testDeleteManager_Failure_StoreNotFound() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);

        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, false);
        assertEquals(result, -1);
        doNothing().when(real.mockUserRepo).checkUserRegister_ThrowException(ownerId);
        doNothing().when(real.mockSusRepo).checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        doNothing().when(real.mockUserRepo).checkUserRegisterOnline_ThrowException(managerId);

        when(real.mockStoreRepo.checkStoreExistance(storeId)).thenReturn(true);
        when(real.mockStoreRepo.checkStoreIsActive(storeId)).thenThrow(new DevException("Store not found with ID: " + storeId));

        DevException ex = assertThrows(DevException.class, () -> {
            real.storeService.deleteManager(storeId, ownerToken, managerId);
        });
        assertEquals(ex.getMessage(), "Store not found with ID: " + storeId);
    }

    @Test
    void testOwner_ManageStoreManagerPermissions() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int managerId = 20;
        String ownerToken = "owner-token";
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        doNothing().when(real.mockIOSrepo).checkToAddManager(storeId, ownerId, managerId);
        List<Permission> a = new LinkedList<>();
        a.add(Permission.AddToStock);
        a.add(Permission.DeleteFromStock);
        int result = real.storeService.AddManagerToStore(storeId, 10, managerId, true);
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
        int result = real.storeService.deactivateteStore(100, ownerToken);
        assertEquals(result, storeId);
    }

    @Test
    void testOwner_DeactivateStore_Failure_AlreadyInactive() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";

        Store s = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(s);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        real.storeService.deactivateteStore(storeId, ownerToken);

        doThrow(new UIException("can't deactivate an DEactivated store", ErrorCodes.DEACTIVATED_STORE))
                .when(real.mockStoreRepo).deactivateStore(storeId, ownerId);

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
        List<OrderDTO> history = real.storeService.veiwStoreHistory(storeId);
        assertTrue(history.isEmpty());
    }

    @Test
    void testOwner_ViewStorePurchaseHistory_Failure_StoreNotExist() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "owner-token";
        doThrow(new UIException("Store does not exist!", ErrorCodes.STORE_NOT_FOUND))
                .when(real.mockOrderRepo).getAllOrderByStore(storeId);

        UIException ex = assertThrows(UIException.class, () -> {
            real.storeService.veiwStoreHistory(storeId);
        });

        assertEquals("Store does not exist!", ex.getMessage());

    }

    @Test
    void testViewRolesAndPermissions_Success() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        String ownerToken = "user-token"; // set in @BeforeEach
        String username = "owner";
        String storeName = "TestStore";

        Node mockNode = new Node(ownerId, false, -1);  // Owner (not manager), parentId = -1
        Permission[] permissions = new Permission[]{Permission.AddToStock};

        when(real.mockIOSrepo.getAllWorkers(storeId)).thenReturn(List.of(mockNode));
        when(real.mockIOSrepo.getPermissions(mockNode)).thenReturn(permissions);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn(storeName);
        when(real.mockUserRepo.getRegisteredUser(ownerId)).thenReturn(new Registered(ownerId, username, "email", 30));

        List<WorkerDTO> workers = real.storeService.ViewRolesAndPermissions(ownerToken, storeId);

        assertEquals(1, workers.size());
        WorkerDTO w = workers.get(0);
        assertEquals(ownerId, w.getWorkerId());
        assertEquals(username, w.getUsername());
        assertTrue(w.isOwner());           // isManager == false -> isOwner == true
        assertFalse(w.isManager());
        assertEquals(storeName, w.getStoreName());
    }

    @Test
    void testViewRolesAndPermissions_failure() throws Exception {
        doThrow(new UIException("Invalid token", ErrorCodes.INVALID_TOKEN))
                .when(real.mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq("invalid-token"), any(Logger.class));

        UIException ex = assertThrows(UIException.class, ()
                -> real.storeService.ViewRolesAndPermissions("invalid-token", 100) // 100 is a valid store, but token is fake
        );
        assertEquals("Invalid token", ex.getMessage());
        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());

        String validToken = "user-token"; // from setup
        int invalidStoreId = 999;

        doThrow(new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND))
                .when(real.mockIOSrepo)
                .getAllWorkers(invalidStoreId);

        UIException ex2 = assertThrows(UIException.class, ()
                -> real.storeService.ViewRolesAndPermissions(validToken, invalidStoreId)
        );
        assertEquals("Store not found", ex2.getMessage());
        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex2.getNumber());
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

    @Test
    void testOwner_DeletePurchasePolicy() throws Exception {
        //TODO

    }

    @Test
    void testOwner_DeletePurchasePolicy_Failure_NotFound() throws Exception {
        //TODO
    }

    @Test
    void testOwner_DeletePurchasePolicy_Failure_NoPermission() throws Exception {
        //TODO
    }

    @Test
    void testOwner_ReplyToMessage() throws Exception {
        //TODO
    }

    @Test
    void testOwner_ReplyToMessage_Failure_UserNotFound() throws Exception {
        //TODO
    }

    @Test
    void testOwner_ReplyToMessage_Failure_MessageNotFound() throws Exception {
        //TODO
    }

    @Test
    void testOwner_ReopenStore() throws Exception {
        //TODO
    }

    //    //todo:this case is not checked
//    @Test
//    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {
//        int storeId = 100;
//        int productId = 999;  // Non-existent product
//        int ownerId = 10;
//        String ownerToken = "user-token";  // from setup
//
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);
//
//        when(real.mockStockRepo.findByIdInSystem_throwException(productId))
//                .thenThrow(new UIException("Product not found!", ErrorCodes.PRODUCT_NOT_FOUND));
//
//        assertDoesNotThrow(() ->
//                real.stockService.removeItem(storeId, ownerToken, productId)
//        );
//
//
//    }
    @Test
    void testOwner_AddToAuction_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToAuction(ownerToken, storeId, productId, 1, 5000, 2);
    }

    @Test
    void testOwner_AddToBID_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToBid(ownerToken, storeId, productId, 1);
    }

    @Test
    void testOwner_AddToRandom_Success() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(true);
        real.stockService.setProductToRandom(ownerToken, productId, 1, 100, storeId, 5000);
    }

    @Test
    void testOwner_AddToBid_Failure() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        // Simulate missing permission
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(false);

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.setProductToBid(ownerToken, storeId, productId, 1)
        );

        assertEquals("you have no permession to set product to bid.", ex.getMessage());
        assertEquals(1004, ex.getNumber());
    }

    @Test
    void testOwner_AddToRandom_Failure() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";
        List<Permission> permissions = new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.ViewAllProducts);
        permissions.add(Permission.DeleteFromStock);
        permissions.add(Permission.SpecialType);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.UpdateQuantity);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, null)).thenReturn(false);

        int res= real.stockService.setProductToRandom(ownerToken, productId, 1, 1.0, 1, 10000);
        assertEquals(0,res);
    }

    @Test
    void testOwner_AddToAuction_Failure() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(false);

        UIException ex = assertThrows(UIException.class, ()
                -> real.stockService.setProductToAuction(ownerToken, storeId, productId, 1, 5000, 2)
        );

        //assertEquals("you have no permession to set product to auction.", ex.getMessage());
        assertEquals(1004, ex.getNumber());
    }

}