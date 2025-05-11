package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Owner_ManagerAT extends AcceptanceTests {
    Real real = new Real();

    @BeforeEach
    void setup() throws Exception {
        // ========== ADMIN SETUP ==========
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
        assertTrue(real.userService.register(adminGuest, "admin", "adminPass"));
        String adminToken = real.userService.login(adminGuest, "admin", "adminPass");
        assertEquals(adminUserToken, adminToken);
        testSystem_InitMarket(adminUserToken);

        // ========== STORE OWNER SETUP ==========
        int ownerId = 10;
        String ownerGuestToken = "guest-token-owner";
        String ownerUserToken = "owner-token";
        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(ownerGuestToken);
        when(real.mockAuthRepo.validToken(ownerGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("owner", "pass")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(ownerUserToken);
        when(real.mockAuthRepo.validToken(ownerUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerUserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(ownerUserToken)).thenReturn("owner");
        String ownerGuest = real.userService.generateGuest();
        assertEquals(ownerGuestToken, ownerGuest);
        assertTrue(real.userService.register(ownerGuest, "owner", "pass"));
        String ownerToken = real.userService.login(ownerGuest, "owner", "pass");
        assertEquals(ownerUserToken, ownerToken);
        int storeId = 100;
        when(real.mockAuthRepo.validToken(ownerUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerUserToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
        assertEquals(storeId, createdStoreId);
        //ADD PRODUCT TO STORE
        int productId = 200;
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        item newItem = new item(productId, 10, 100, Category.ELECTRONICS);
        when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS)).thenReturn(newItem);
        real.storeService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);


        // ========== USER FOR TESTING (e.g., to be added as manager) ==========
        int testUserId = 20;
        String testGuestToken = "guest-token-2";
        String testUserToken = "user-token-2";
        when(real.mockUserRepo.generateGuest()).thenReturn(testUserId);
        when(real.mockAuthRepo.generateGuestToken(testUserId)).thenReturn(testGuestToken);
        when(real.mockAuthRepo.validToken(testGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("testUser", "pass2")).thenReturn(testUserId);
        when(real.mockAuthRepo.generateUserToken(testUserId, "testUser")).thenReturn(testUserToken);
        when(real.mockAuthRepo.validToken(testUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(testUserToken)).thenReturn(testUserId);
        when(real.mockAuthRepo.getUserName(testUserToken)).thenReturn("testUser");
        String guestGenerated = real.userService.generateGuest();
        assertEquals(testGuestToken, guestGenerated);
        when(real.mockAuthRepo.isRegistered(testUserToken)).thenReturn(true);
        when(real.mockUserRepo.isRegistered(testUserId)).thenReturn(true);
        assertTrue(real.userService.register(guestGenerated, "testUser", "pass2"));
        String loginToken2 = real.userService.login(guestGenerated, "testUser", "pass2");
        assertEquals(testUserToken, loginToken2);

        // Optionally: assign this user as manager of the store via mock
        //when(real.mockIOSrepo.isStoreManager(storeId, testUserId)).thenReturn(true);
    }

    // ========== Store Owner Use Cases ==========

    @Test
    void testOwner_AddProductToStock() throws Exception {
        int ownerId = 10;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "owner-token";


        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);

        item newItem = new item(productId, 10, 100, Category.ELECTRONICS);
        when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS)).thenReturn(newItem);

        boolean result = real.storeService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);
        assertTrue(result);
    }


    @Test
    void testOwner_AddProductToStock_Failure_InvalidToken() throws Exception {
        int storeId = 100;
        int productId = 200;
        String invalidToken = "invalid-owner-token";

        when(real.mockAuthRepo.validToken(invalidToken)).thenReturn(false);

        boolean result = real.storeService.addItem(storeId, invalidToken, productId, 10, 100, Category.ELECTRONICS);

        assertFalse(result);
    }

    @Test
    void testOwner_AddProductToStock_Failure_InsufficientPermissions() throws Exception {
        int storeId = 100;
        int productId = 200;
        String ownerToken = "owner-token";
        int ownerId = 10;

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(false); // Permission denied

        boolean result = real.storeService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);

        assertFalse(result);
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

        // Simulate storeRepo rejecting invalid data
        when(real.mockStoreRepo.addItem(storeId, productId, -5, -999, Category.ELECTRONICS))
                .thenThrow(new IllegalArgumentException("Invalid quantity or price"));

        boolean result = real.storeService.addItem(storeId, ownerToken, productId, -5, -999, Category.ELECTRONICS);

        assertFalse(result);
    }
    @Test
    void testOwner_AddProductToStock_Failure_StoreNotFound() throws Exception {
        int storeId = 999; // non-existent store
        int productId = 200;
        String ownerToken = "owner-token";
        int ownerId = 10;

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);

        when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenThrow(new RuntimeException("Store not found"));

        boolean result = real.storeService.addItem(storeId, ownerToken, productId, 10, 100, Category.ELECTRONICS);

        assertFalse(result);
    }

    @Test
    void testOwner_DeleteProductFromStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);
        //doNothing().when(real.mockStoreRepo).removeItem(storeId,productId);
        real.mockStoreRepo.removeItem(storeId,productId);
        boolean result = real.storeService.removeItem(storeId, ownerToken, productId);

        assertTrue(result);
    }

    //TODO:THE CODE DIDN'T CHECK THIS CASE WHEN WE DO IT FIX THE NOTE TO BE NOT NOTE
    @Test
    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {
        int storeId = 100;
        int productId = 999;  // Non-existent product
        int ownerId = 10;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        //when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);
        doNothing().when(real.mockStoreRepo).removeItem(storeId,productId);
        //real.mockStoreRepo.removeItem(storeId,productId);

        boolean result = real.storeService.removeItem(storeId, ownerToken, productId);

        assertFalse(result);
    }
    @Test
    void testOwner_DeleteProductFromStock_Failure_NoPermission() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(false);

        boolean result = real.storeService.removeItem(storeId, ownerToken, productId);

        assertFalse(result);
    }


    //    //Todo the code need fix related to more check in correct way
//    @Test
//    void testOwner_DeleteProductFromStock_Failure_InvalidStoreId() throws Exception {
//        int storeId = 999; // non-existent store
//        int productId = 200;
//        int ownerId = 10;
//        String ownerToken = "owner-token";
//
//        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
//        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);
//
//        doNothing().when(real.mockStoreRepo).removeItem(storeId,productId);
//
//        boolean result = real.storeService.removeItem(storeId, ownerToken, productId);
//
//        assertFalse(result);
//    }
//
    @Test
    void testOwner_UpdateProductInStock() throws Exception {
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "owner-token";

        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdatePrice)).thenReturn(true);
        doNothing().when(real.mockStoreRepo).updatePrice(storeId, productId, 10);

        boolean result = real.storeService.updatePrice(storeId, ownerToken, productId, 10);

        assertTrue(result);
    }
//    @Test
//    void testOwner_UpdateProductInStock1() throws Exception {
//        int storeId = 100;
//        int productId = 200;
//        int ownerId = 10;
//        String ownerToken = "owner-token";
//
//        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
//        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdateQuantity)).thenReturn(true);
//        doNothing().when(real.mockStoreRepo).updateQuantity(storeId, productId, 10);
//
//        boolean result = real.storeService.updateQuantity(storeId, ownerToken, productId, 10);
//
//        assertTrue(result);
//    }
//    @Test
//    void testOwner_UpdateProductInStock_Failure_InvalidData() throws Exception {
//        int storeId = 100;
//        int productId = 200;
//        int ownerId = 10;
//        String ownerToken = "owner-token";
//
//        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
//        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdateQuantity)).thenReturn(true);
//
//        // Simulate validation failure: negative price
//        when(real.mockStoreRepo.updateQuantity(storeId, productId, 10, -50);).thenReturn(false);
//
//        boolean result = real.storeService.updatePrice(storeId, ownerToken, productId,  -50);
//
//        assertFalse(result);
//    }
//    @Test
//    void testOwner_UpdateProductInStock_Failure_ProductNotFound() throws Exception {
//        int storeId = 100;
//        int productId = 999;  // non-existent
//        int ownerId = 10;
//        String ownerToken = "owner-token";
//
//        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
//        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdateQuantity)).thenReturn(true);
//        when(real.mockStoreRepo.updateItem(storeId, productId, 5, 150)).thenReturn(false);  // product not found
//
//        boolean result = real.storeService.updateItem(storeId, ownerToken, productId, 5, 150);
//
//        assertFalse(result);
//    }
//    @Test
//    void testOwner_UpdateProductInStock_Failure_NoPermission() throws Exception {
//        int storeId = 100;
//        int productId = 200;
//        int ownerId = 10;
//        String ownerToken = "owner-token";
//
//        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
//        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.UpdatePrice)).thenReturn(false);  // No permission
//
//        boolean result = real.storeService.updateItem(storeId, ownerToken, productId, 10, 100);
//
//        assertFalse(result);
//    }
//
////    @Test
////    void testOwner_AddPurchasePolicy() throws Exception {
////        int ownerId = 10;
////        int storeId = 100;
////        String ownerToken = "owner-token";
////
////        PurchasePolicyDTO validPolicy = new PurchasePolicyDTO("MIN_QUANTITY", "productId=200;quantity=2");
////
////        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
////        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, ownerId)).thenReturn(true);
////        when(real.mockStoreRepo.addPurchasePolicy(storeId, validPolicy)).thenReturn(true);
////
////        boolean result = real.storeService.addPurchasePolicy(storeId, ownerToken, validPolicy);
////
////        assertTrue(result);
////    }
////    @Test
////    void testOwner_AddPurchasePolicy_Failure_InvalidPolicy() throws Exception {
////        int ownerId = 10;
////        int storeId = 100;
////        String ownerToken = "owner-token";
////
////        // Empty or malformed policy
////        PurchasePolicyDTO invalidPolicy = new PurchasePolicyDTO("", "");
////
////        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
////        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, ownerId)).thenReturn(true);
////        when(real.mockStoreRepo.addPurchasePolicy(storeId, invalidPolicy)).thenReturn(false);
////
////        boolean result = real.storeService.addPurchasePolicy(storeId, ownerToken, invalidPolicy);
////
////        assertFalse(result);
////    }
////    @Test
////    void testOwner_AddPurchasePolicy_Failure_NotOwner() throws Exception {
////        int notOwnerId = 20;
////        int storeId = 100;
////        String userToken = "user-token-2";
////
////        PurchasePolicyDTO policy = new PurchasePolicyDTO("MAX_QUANTITY", "productId=300;quantity=5");
////
////        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(notOwnerId);
////        when(real.mockUserRepo.isRegistered(notOwnerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, notOwnerId)).thenReturn(false);
////
////        UIException ex = assertThrows(UIException.class, () ->
////                real.storeService.addPurchasePolicy(storeId, userToken, policy)
////        );
////
////        assertEquals("Not authorized as store owner!", ex.getMessage());
////    }
////
////    @Test
////    void testOwner_DeletePurchasePolicy() throws Exception {
////        int ownerId = 10;
////        int storeId = 100;
////        String ownerToken = "owner-token";
////        int policyId = 1;
////
////        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
////        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, ownerId)).thenReturn(true);
////        when(real.mockStoreRepo.deletePurchasePolicy(storeId, policyId)).thenReturn(true);
////
////        boolean result = real.storeService.deletePurchasePolicy(storeId, ownerToken, policyId);
////
////        assertTrue(result);
////    }
////    @Test
////    void testOwner_DeletePurchasePolicy_Failure_NotFound() throws Exception {
////        int ownerId = 10;
////        int storeId = 100;
////        String ownerToken = "owner-token";
////        int nonExistentPolicyId = 99;
////
////        when(real.mockAuthRepo.validToken(ownerToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(ownerToken)).thenReturn(ownerId);
////        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, ownerId)).thenReturn(true);
////        when(real.mockStoreRepo.deletePurchasePolicy(storeId, nonExistentPolicyId)).thenReturn(false);
////
////        boolean result = real.storeService.deletePurchasePolicy(storeId, ownerToken, nonExistentPolicyId);
////
////        assertFalse(result);
////    }
////    @Test
////    void testOwner_DeletePurchasePolicy_Failure_NoPermission() throws Exception {
////        int notOwnerId = 20;
////        int storeId = 100;
////        String userToken = "user-token-2";
////        int policyId = 1;
////
////        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
////        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(notOwnerId);
////        when(real.mockUserRepo.isRegistered(notOwnerId)).thenReturn(true);
////        when(real.mockIOSrepo.isStoreOwner(storeId, notOwnerId)).thenReturn(false);
////
////        UIException ex = assertThrows(UIException.class, () ->
////                real.storeService.deletePurchasePolicy(storeId, userToken, policyId)
////        );
////
////        assertEquals("Not authorized to delete policy!", ex.getMessage());
////    }
////
//
//
//
//    @Test
//    void testOwner_AddStoreOwner_Success() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int newOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, newOwnerId)).thenReturn(true);
//        when(real.mockIOSrepo.AddOwnershipToStore(storeId, ownerId, newOwnerId)).thenReturn(true);
//
//        boolean result = real.storeService.assignStoreOwner(ownerToken, storeId, newOwnerId);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Failure_SelfAssignment() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(ownerToken, storeId, ownerId);
//        });
//
//        assertEquals("Cannot assign ownership to yourself", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Failure_DuplicateOwner() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int newOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, newOwnerId)).thenThrow(new UIException("Already assigned", 1006));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(ownerToken, storeId, newOwnerId);
//        });
//
//        assertEquals("Already assigned", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int newOwnerId = 404;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, newOwnerId)).thenThrow(new UIException("Target user not found", 1004));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(ownerToken, storeId, newOwnerId);
//        });
//
//        assertEquals("Target user not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Failure_NotAuthorized() throws Exception {
//        int notOwnerId = 30;
//        String notOwnerToken = "not-owner-token";
//        int storeId = 100;
//        int newOwnerId = 20;
//
//        when(real.mockAuthRepo.validToken(notOwnerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(notOwnerToken)).thenReturn(notOwnerId);
//        when(real.mockUserRepo.isRegistered(notOwnerId)).thenReturn(true);
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, notOwnerId, newOwnerId)).thenThrow(new UIException("You are not authorized", 1003));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(notOwnerToken, storeId, newOwnerId);
//        });
//
//        assertEquals("You are not authorized", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Rejected() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int newOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, newOwnerId)).thenReturn(false);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(ownerToken, storeId, newOwnerId);
//        });
//
//        assertEquals("Ownership request was rejected", ex.getMessage());
//    }
//    @Test
//    void testOwner_AddStoreOwner_Failure_AlreadyOwner() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int newOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddOwner(storeId, ownerId, newOwnerId)).thenThrow(new UIException("Already assigned", 1006));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreOwner(ownerToken, storeId, newOwnerId);
//        });
//
//        assertEquals("Already assigned", ex.getMessage());
//    }
//
//
//
//    @Test
//    void testOwner_DeleteStoreOwner() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int targetOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removeOwnership(storeId, ownerId, targetOwnerId)).thenReturn(true);
//        boolean result = real.storeService.removeStoreOwner(ownerToken, storeId, targetOwnerId);
//        assertTrue(result);
//    }    @Test
//    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int targetOwnerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removeOwnership(storeId, ownerId, targetOwnerId)).thenThrow(new UIException("Store owner not found", 1011));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.removeStoreOwner(ownerToken, storeId, targetOwnerId);
//        });
//
//        assertEquals("Store owner not found", ex.getMessage());
//    }    @Test
//    void testOwner_DeleteStoreOwner_Failure_CannotRemoveSelf() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.removeStoreOwner(ownerToken, storeId, ownerId);
//        });
//
//        assertEquals("Cannot remove self from store ownership", ex.getMessage());
//    }
//    @Test
//    void testOwner_AddStoreManager_Success() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddManager(storeId, ownerId, managerId)).thenReturn(true);
//        when(real.mockIOSrepo.addNewStoreManager(storeId, ownerId, managerId)).thenReturn(true);
//
//        boolean result = real.storeService.assignStoreManager(ownerToken, storeId, managerId);
//        assertTrue(result);
//    }
//    @Test
//    void testOwner_AddStoreManager_Rejected() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddManager(storeId, ownerId, managerId)).thenReturn(false);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreManager(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("Request rejected: Manager addition was denied", ex.getMessage());
//    }
//    @Test
//    void testOwner_AddStoreManager_Failure_NotAuthorized() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String fakeToken = "unauthorized-token";
//
//        when(real.mockAuthRepo.validToken(fakeToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(fakeToken)).thenReturn(555); // someone else not owner
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreManager(fakeToken, storeId, managerId);
//        });
//
//        assertEquals("this user does not have permission to assign manager", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Failure_AlreadyManager() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddManager(storeId, ownerId, managerId))
//                .thenThrow(new UIException("User is already a manager", 1015));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreManager(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("User is already a manager", ex.getMessage());
//    }
//    @Test
//    void testOwner_AddStoreManager_Failure_TargetNotFound() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int invalidManagerId = 404;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddManager(storeId, ownerId, invalidManagerId))
//                .thenThrow(new UIException("Target user not found", 1012));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreManager(ownerToken, storeId, invalidManagerId);
//        });
//
//        assertEquals("Target user not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Failure_StoreClosed() throws Exception {
//        int ownerId = 10;
//        int storeId = 100;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.checkToAddManager(storeId, ownerId, managerId))
//                .thenThrow(new UIException("Store is closed", 1016));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.assignStoreManager(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("Store is closed", ex.getMessage());
//    }
//    @Test
//    void testOwner_ManageStoreManagerPermissions() throws Exception {
//        int ownerId = 10;
//        int managerId = 20;
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, managerId, Permission.AddToStock)).thenReturn(true);
//
//        boolean result = real.storeService.setManagerPermission(ownerToken, storeId, managerId, Permission.AddToStock);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_ManageStoreManagerPermissions_Failure_NotManager() throws Exception {
//        int ownerId = 10;
//        int managerId = 20;
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, managerId, Permission.AddToStock))
//                .thenThrow(new UIException("Target user is not a store manager", 3001));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.setManagerPermission(ownerToken, storeId, managerId, Permission.AddToStock);
//        });
//
//        assertEquals("Target user is not a store manager", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ManageStoreManagerPermissions_Failure_NoPermission() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String attackerToken = "unauthorized-token";
//
//        when(real.mockAuthRepo.validToken(attackerToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(attackerToken)).thenReturn(888);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.setManagerPermission(attackerToken, storeId, managerId, Permission.AddToStock);
//        });
//
//        assertEquals("this user does not have permission to assign manager", ex.getMessage());
//    }
//
//
//    @Test
//    void testOwner_SetPermissionsForManager() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, managerId, Permission.AddToStock)).thenReturn(true);
//
//        boolean result = real.storeService.setManagerPermission(ownerToken, storeId, managerId, Permission.AddToStock);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_SetPermissionsForManager_Failure_InvalidPermissions() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, managerId, null)).thenThrow(new UIException("Invalid permission value", 3002));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.setManagerPermission(ownerToken, storeId, managerId, null);
//        });
//
//        assertEquals("Invalid permission value", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_SetPermissionsForManager_Failure_ManagerNotFound() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 9999;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, managerId, Permission.AddToStock)).thenThrow(new UIException("Manager not found", 3003));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.setManagerPermission(ownerToken, storeId, managerId, Permission.AddToStock);
//        });
//
//        assertEquals("Manager not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_SetPermissions_Failure_TargetIsNotManager() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int notManagerId = 30;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.setPermissions(storeId, ownerId, notManagerId, Permission.AddToStock)).thenThrow(new UIException("Target is not a manager", 3004));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.setManagerPermission(ownerToken, storeId, notManagerId, Permission.AddToStock);
//        });
//
//        assertEquals("Target is not a manager", ex.getMessage());
//    }
//
//
//    @Test
//    void testOwner_RemovePermissionsFromManager() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removePermissions(storeId, ownerId, managerId)).thenReturn(true);
//
//        boolean result = real.storeService.removeManagerPermission(ownerToken, storeId, managerId);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_RemovePermissionsFromManager_Failure_NotFound() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 9999;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removePermissions(storeId, ownerId, managerId)).thenThrow(new UIException("Manager not found", 3010));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.removeManagerPermission(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("Manager not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_RemovePermissionsFromManager_Failure_NoPermissionsSet() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removePermissions(storeId, ownerId, managerId)).thenThrow(new UIException("No permissions assigned to this manager", 3011));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.removeManagerPermission(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("No permissions assigned to this manager", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_RemoveManager_Failure_AlreadyRemoved() throws Exception {
//        int storeId = 100;
//        int ownerId = 10;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        when(real.mockIOSrepo.removePermissions(storeId, ownerId, managerId)).thenThrow(new UIException("Manager already removed", 3012));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.removeManagerPermission(ownerToken, storeId, managerId);
//        });
//
//        assertEquals("Manager already removed", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_DeactivateStore() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockStoreRepo.deactivateStore(storeId)).thenReturn(true);
//
//        boolean result = real.storeService.deactivateStore(ownerToken, storeId);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_DeactivateStore_Failure_AlreadyInactive() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockStoreRepo.deactivateStore(storeId)).thenThrow(new UIException("Store is already inactive", 4001));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.deactivateStore(ownerToken, storeId);
//        });
//
//        assertEquals("Store is already inactive", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_DeactivateStore_Failure_NoPermission() throws Exception {
//        int storeId = 100;
//        String unauthorizedToken = "not-owner-token";
//
//        when(real.mockAuthRepo.validToken(unauthorizedToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(unauthorizedToken)).thenReturn(777);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.deactivateStore(unauthorizedToken, storeId);
//        });
//
//        assertEquals("this user does not have permission to deactivate the store", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ReopenStore() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockStoreRepo.reopenStore(storeId)).thenReturn(true);
//
//        boolean result = real.storeService.reopenStore(ownerToken, storeId);
//        assertTrue(result);
//    }
//
//    @Test
//    void testOwner_ViewStoreInfo() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        StoreInfo mockInfo = new StoreInfo("TestStore", "ELECTRONICS", true);
//        when(real.mockStoreRepo.getStoreInfo(storeId)).thenReturn(mockInfo);
//
//        StoreInfo result = real.storeService.viewStoreInfo(ownerToken, storeId);
//        assertNotNull(result);
//        assertEquals("TestStore", result.getStoreName());
//    }
//
//    @Test
//    void testOwner_ViewStoreInfo_Failure_InvalidStoreId() throws Exception {
//        int invalidStoreId = 9999;
//        String ownerToken = "owner-token";
//
//        when(real.mockStoreRepo.getStoreInfo(invalidStoreId)).thenThrow(new UIException("Store not found", 4002));
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.viewStoreInfo(ownerToken, invalidStoreId);
//        });
//
//        assertEquals("Store not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ViewStoreInfo_Failure_NotAuthorized() throws Exception {
//        int storeId = 100;
//        String unauthorizedToken = "not-owner-token";
//
//        when(real.mockAuthRepo.validToken(unauthorizedToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(unauthorizedToken)).thenReturn(888);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.viewStoreInfo(unauthorizedToken, storeId);
//        });
//
//        assertEquals("this user does not have permission to view store info", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ReplyToMessage() throws Exception {
//        String ownerToken = "owner-token";
//        int storeId = 100;
//        String msg = "We are happy to help!";
//
//        doNothing().when(real.mockStoreRepo).replyToMessage(ownerToken, storeId, msg);
//
//        assertDoesNotThrow(() -> {
//            real.storeService.replyToMessage(ownerToken, storeId, msg);
//        });
//    }
//
//    @Test
//    void testOwner_ReplyToMessage_Failure_UserNotFound() throws Exception {
//        String ownerToken = "owner-token";
//        int storeId = 100;
//        String msg = "We are happy to help!";
//
//        doThrow(new UIException("User not found", 4004))
//                .when(real.mockStoreRepo).replyToMessage(ownerToken, storeId, msg);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.replyToMessage(ownerToken, storeId, msg);
//        });
//
//        assertEquals("User not found", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ReplyToMessage_Failure_MessageNotFound() throws Exception {
//        String ownerToken = "owner-token";
//        int storeId = 100;
//        String msg = "We are happy to help!";
//
//        doThrow(new UIException("Message not found", 4005))
//                .when(real.mockStoreRepo).replyToMessage(ownerToken, storeId, msg);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.replyToMessage(ownerToken, storeId, msg);
//        });
//
//        assertEquals("Message not found", ex.getMessage());
//    }
//    @Test
//    void testOwner_ViewStorePurchaseHistory() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        ReceiptDTO receipt = new ReceiptDTO("StoreName", List.of());
//        when(real.mockOrderRepo.getStorePurchaseHistory(storeId)).thenReturn(List.of(receipt));
//
//        List<ReceiptDTO> history = real.orderService.getStorePurchaseHistory(ownerToken, storeId);
//        assertNotNull(history);
//        assertFalse(history.isEmpty());
//        assertEquals("StoreName", history.get(0).getStoreName());
//    }
//
//    @Test
//    void testOwner_ViewStorePurchaseHistory_Failure_NoHistory() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        when(real.mockOrderRepo.getStorePurchaseHistory(storeId)).thenReturn(Collections.emptyList());
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.orderService.getStorePurchaseHistory(ownerToken, storeId);
//        });
//
//        assertEquals("No purchase history found for this store", ex.getMessage());
//    }
//
//    @Test
//    void testOwner_ViewStorePurchaseHistory_Failure_NotAuthorized() throws Exception {
//        int storeId = 100;
//        String otherToken = "unauth-token";
//        int otherId = 222;
//
//        when(real.mockAuthRepo.validToken(otherToken)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(otherToken)).thenReturn(otherId);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.orderService.getStorePurchaseHistory(otherToken, storeId);
//        });
//
//        assertEquals("this user does not have permission to view store history", ex.getMessage());
//    }
//
//
//    @Test
//    void testOwner_RequestStoreRolesInfo() throws Exception {
//        int storeId = 100;
//        String ownerToken = "owner-token";
//
//        List<String> mockRoles = List.of("owner: owner", "manager: testUser");
//        when(real.mockStoreRepo.getStoreRolesInfo(storeId)).thenReturn(mockRoles);
//
//        List<String> roles = real.storeService.getStoreRoles(ownerToken, storeId);
//
//        assertNotNull(roles);
//        assertTrue(roles.contains("owner: owner"));
//        assertTrue(roles.contains("manager: testUser"));
//    }
//
//    @Test
//    void testOwner_ViewManagerPermissions() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String ownerToken = "owner-token";
//
//        List<PermissionDTO> mockPermissions = List.of(new PermissionDTO("AddToStock"));
//        when(real.mockIOSrepo.getPermissions(storeId, managerId)).thenReturn(mockPermissions);
//
//        List<PermissionDTO> perms = real.storeService.getManagerPermissions(ownerToken, storeId, managerId);
//
//        assertNotNull(perms);
//        assertEquals("AddToStock", perms.get(0).getName());
//    }
//
//    // ========== Store Manager Use Cases ==========
//
//    void AddTheUserAsManager() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String token = "user-token-2";
//
//        when(real.mockAuthRepo.validToken(token)).thenReturn(true);
//        when(real.mockAuthRepo.getUserId(token)).thenReturn(managerId);
//        when(real.mockAuthRepo.getUserName(token)).thenReturn("testUser");
//        when(real.mockAuthRepo.isRegistered(token)).thenReturn(true);
//        when(real.mockUserRepo.isRegistered(managerId)).thenReturn(true);
//
//        when(real.mockIOSrepo.addStoreManager(storeId, 10, managerId)).thenReturn(true);
//        when(real.mockIOSrepo.isStoreManager(storeId, managerId)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(managerId, storeId, Permission.AddToStock)).thenReturn(true);
//        when(real.mockIOSrepo.manipulateItem(managerId, storeId, Permission.UpdateStock)).thenReturn(true);
//    }
//    @Test
//    void testManager_AddProduct_WithPermission() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        item expectedItem = new item(300, 5, 200, Category.ELECTRONICS);
//        when(real.mockStoreRepo.addItem(storeId, 300, 5, 200, Category.ELECTRONICS)).thenReturn(expectedItem);
//
//        boolean result = real.storeService.addItem(storeId, token, 300, 5, 200, Category.ELECTRONICS);
//        assertTrue(result);
//    }
//    @Test
//    void testManager_UpdateProduct_WithPermission() throws Exception {
//        int storeId = 100;
//        int managerId = 20;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        item updatedItem = new item(300, 10, 250, Category.ELECTRONICS);
//        when(real.mockStoreRepo.updateItem(storeId, 300, 10, 250, Category.ELECTRONICS)).thenReturn(updatedItem);
//
//        boolean result = real.storeService.updateItem(storeId, token, 300, 10, 250, Category.ELECTRONICS);
//        assertTrue(result);
//    }
//
//    @Test
//    void testManager_ViewStoreInfo_WithPermission() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//
//        StoreInfo mockInfo = new StoreInfo("TestStore", "ELECTRONICS", true);
//        when(real.mockStoreRepo.getStoreInfo(storeId)).thenReturn(mockInfo);
//
//        StoreInfo result = real.storeService.viewStoreInfo(token, storeId);
//        assertNotNull(result);
//        assertEquals("TestStore", result.getStoreName());
//    }
//
//    @Test
//    void testManager_ViewPurchaseHistory_WithPermission() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        ReceiptDTO receipt = new ReceiptDTO("TestStore", List.of());
//        when(real.mockOrderRepo.getStorePurchaseHistory(storeId)).thenReturn(List.of(receipt));
//
//        List<ReceiptDTO> history = real.orderService.getStorePurchaseHistory(token, storeId);
//        assertNotNull(history);
//        assertFalse(history.isEmpty());
//        assertEquals("TestStore", history.get(0).getStoreName());
//    }
//
//    @Test
//    void testManager_ReplyToMessage_WithPermission() throws Exception {
//        String token = "user-token-2";
//        int storeId = 100;
//        String msg = "Thanks for your feedback!";
//        AddTheUserAsManager();
//
//        doNothing().when(real.mockStoreRepo).replyToMessage(token, storeId, msg);
//
//        assertDoesNotThrow(() -> real.storeService.replyToMessage(token, storeId, msg));
//    }
//
//    @Test
//    void testManager_Action_Failure_NoPermission() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        when(real.mockIOSrepo.manipulateItem(20, storeId, Permission.AddToStock)).thenReturn(false);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.addItem(storeId, token, 300, 5, 200, Category.ELECTRONICS);
//        });
//
//        assertEquals("this worker is not authorized!", ex.getMessage());
//    }
//
//
//    @Test
//    void testManager_DeleteProduct_WithPermission() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        when(real.mockStoreRepo.deleteItem(storeId, 300)).thenReturn(true);
//
//        boolean result = real.storeService.deleteItem(storeId, token, 300);
//        assertTrue(result);
//    }
//
//    @Test
//    void testManager_DeleteProduct_WithoutPermission() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        when(real.mockIOSrepo.manipulateItem(20, storeId, Permission.RemoveFromStock)).thenReturn(false);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            real.storeService.deleteItem(storeId, token, 300);
//        });
//
//        assertEquals("this worker is not authorized!", ex.getMessage());
//    }
//
//    @Test
//    void testManager_DeleteProduct_Failure_ProductNotFound() throws Exception {
//        int storeId = 100;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        when(real.mockStoreRepo.deleteItem(storeId, 999)).thenReturn(false);
//
//        boolean result = real.storeService.deleteItem(storeId, token, 999);
//        assertFalse(result);
//    }
//
//    @Test
//    void testManager_DeleteProduct_Failure_InvalidStoreId() throws Exception {
//        int invalidStoreId = -1;
//        String token = "user-token-2";
//        AddTheUserAsManager();
//
//        when(real.mockIOSrepo.manipulateItem(20, invalidStoreId, Permission.RemoveFromStock)).thenReturn(true);
//        when(real.mockStoreRepo.deleteItem(invalidStoreId, 300)).thenReturn(false);
//
//        boolean result = real.storeService.deleteItem(invalidStoreId, token, 300);
//        assertFalse(result);
//    }
}

