package workshop.demo.AcceptanceTest.Tests;

import java.util.List;

import workshop.demo.AcceptanceTest.Utill.Bridge;
import workshop.demo.AcceptanceTest.Utill.Proxy;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;


public abstract class AcceptanceTests {
    protected Bridge bridge = new Proxy();

    // System
    public String testSystem_InitMarket(String admin) throws Exception {
        return bridge.testSystem_InitMarket(admin);
    }
    public String testSystem_sendDMessageToAll(List<Integer> ids, String msg, int sender) throws Exception {
        return bridge.testSystem_sendDMessageToAll(ids, msg, sender);
    }
    public String testSystem_sendRTMessageToAll(List<Integer> ids, String msg, int sender) throws Exception {
        return bridge.testSystem_sendRTMessageToAll(ids, msg, sender);
    }

    // Guest
    public String testGuest_Enter() throws Exception {
        return bridge.testGuest_Enter();
    }
    public String testGuest_Exit(String token) throws Exception {
        return bridge.testGuest_Exit(token);
    }
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        return bridge.testGuest_Register(token, username, password, age);
    }
    public String testGuest_GetStoreInfo(String token, int storeID) throws Exception {
        return bridge.testGuest_GetStoreInfo(token, storeID);
    }
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return bridge.testGuest_GetProductInfo(token, productID);
    }
    public String testGuest_SearchProduct(String token, String productName) throws Exception {
        return bridge.testGuest_SearchProduct(token, productName);
    }
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return bridge.testGuest_SearchProductInStore(token, storeID, productID);
    }
    public String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        return bridge.testGuest_AddProductToCart(token, storeID, productID, count);
    }
    public String testGuest_ModifyCart(String token, int cartID) throws Exception {
        return bridge.testGuest_ModifyCart(token, cartID);
    }
    public String testGuest_BuyCart(String token, int cartID) throws Exception {
        return bridge.testGuest_BuyCart(token, cartID);
    }
    public String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception {
        return bridge.testGuest_GetPurchasePolicy(token, storeID);
    }

    // User
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return bridge.testUser_LogIn(token, username, password);
    }
    public String testUser_LogOut(String token) throws Exception {
        return bridge.testUser_LogOut(token);
    }
    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        return bridge.testUser_OpenStore(token, storeName, category);
    }
    public String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception {
        return bridge.testUser_AddReviewToStore(token, storeId, review);
    }
    public String testUser_AddReviewToProduct(String token, int storeId, int productId, String review) throws Exception {
        return bridge.testUser_AddReviewToProduct(token, storeId, productId, review);
    }
    public String testUser_RateProduct(String token, int storeID, int productID, int rate) throws Exception {
        return bridge.testUser_RateProduct(token, storeID, productID, rate);
    }
    public String testUser_RateStore(String token, int storeID, int productID, int rate) throws Exception {
        return bridge.testUser_RateStore(token, storeID, productID, rate);
    }
    public String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception {
        return bridge.testUser_SendMessageToStoreOwner(userId, ownerId, msg);
    }
    public String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception {
        return bridge.testUser_SendMessageToAdmin(msg, userId, adminId);
    }
    public String testUser_CheckPurchaseHistory(String token) throws Exception {
        return bridge.testUser_CheckPurchaseHistory(token);
    }
    public String testUser_updateProfile(String token) throws Exception {
        return bridge.testUser_updateProfile(token);
    }
    public String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception {
        return bridge.testUser_AddBid(token, storeID, productID, bid);
    }
    public String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception {
        return bridge.testUser_JoinAuction(token, storeID, auctionID);
    }
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return bridge.testUser_JoinRandom(token, storeID, randomID, num);
    }
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return bridge.testUser_setAdmin(token, newAdminUsername);
    }

    // Owner
    public String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount) throws Exception {
        return bridge.testOwner_ManageInventory_AddProduct(token, storeID, productID, amount);
    }
    public String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return bridge.testOwner_ManageInventory_RemoveProduct(token, storeID, productID);
    }
    public String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID) throws Exception {
        return bridge.testOwner_ManageInventory_UpdateProductDetails(token, storeID, productID);
    }
    public String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        return bridge.testOwner_SetPurchasePolicies(token, storeID, newPolicies);
    }
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return bridge.testOwner_SetDiscountPolicies(token, storeID, newPolicies);
    }
    public String testOwner_AssignNewOwner(String token, int storeID, int newOwnerId) throws Exception {
        return bridge.testOwner_AssignNewOwner(token, storeID, newOwnerId);
    }
    public String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        return bridge.testOwner_RemoveOwner(token, storeID, ownerToRemoveId);
    }
    public String testOwner_AssignManager(String token, int storeID, int managerId) throws Exception {
        return bridge.testOwner_AssignManager(token, storeID, managerId);
    }
    public String testOwner_EditManagerPermissions(String token, int managerId, int storeID, List<Permission> authorization) throws Exception {
        return bridge.testOwner_EditManagerPermissions(token, managerId, storeID, authorization);
    }
    public String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception {
        return bridge.testOwner_RemoveManager(token, storeID, managerId);
    }
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        return bridge.testOwner_CloseStore(token, storeID);
    }
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return bridge.testOwner_ReopenStore(token, storeID);
    }
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return bridge.testOwner_ViewRolesAndPermissions(token, storeID);
    }
    public String testOwner_ReceiveNotifications(int userId) throws Exception {
        return bridge.testOwner_ReceiveNotifications(userId);
    }
    public String testOwner_ReplyToMessages(String msg, int ownerId, int userId) throws Exception {
        return bridge.testOwner_ReplyToMessages(msg, ownerId, userId);
    }
    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return bridge.testOwner_ViewStorePurchaseHistory(token, storeID);
    }

    // Admin
    public String testAdmin_CloseStore(int storeID, String token) throws Exception {
        return bridge.testAdmin_CloseStore(storeID, token);
    }
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return bridge.testAdmin_RemoveUser(token, userToRemove);
    }
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return bridge.testAdmin_ViewSystemPurchaseHistory(token);
    }
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        return bridge.testAdmin_ViewSystemInfo(token);
    }
}
