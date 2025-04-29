package workshop.demo.AcceptanceTest;

import workshop.demo.AcceptanceTest.Utill.Bridge;
import workshop.demo.AcceptanceTest.Utill.Proxy;

public abstract class AcceptanceTests {

    protected Bridge bridge = new Proxy();
    
    /////////////////////// System /////////////////////////////
    public String testSystem_InitMarket(String admin) throws Exception {
        return bridge.testSystem_InitMarket(admin);
    }

    /////////////////////// Guest /////////////////////////////
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

    public String testGuest_SearchProduct(String token, String productname) throws Exception {
        return bridge.testGuest_SearchProduct(token, productname);
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

    //////////////////////////// User ////////////////////////////
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return bridge.testUser_LogIn(token, username, password);
    }

    public String testUser_LogOut(String token) throws Exception {
        return bridge.testUser_LogOut(token);
    }

    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        return bridge.testUser_OpenStore(token, storeName, category);
    }

    public String testUser_AddReview(String token, int storeId, String review) throws Exception {
        return bridge.testUser_AddReview(token, storeId, review);
    }

    public String testUser_RateProductAndStore(String token, int storeID, int productID, int rate) throws Exception {
        return bridge.testUser_RateProductAndStore(token, storeID, productID, rate);
    }

    public String testUser_SendMessageToStoreOwner(String token, int storeID, String msg) throws Exception {
        return bridge.testUser_SendMessageToStoreOwner(token, storeID, msg);
    }

    public String testUser_SendMessageToAdmin(String token, String msg) throws Exception {
        return bridge.testUser_SendMessageToAdmin(token, msg);
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

    //////////////////////////// Owner ////////////////////////////
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

    public String testOwner_AssignNewOwner(String token, int storeID, String newOwnerUsername) throws Exception {
        return bridge.testOwner_AssignNewOwner(token, storeID, newOwnerUsername);
    }

    public String testOwner_RemoveOwner(String token, int storeID, String ownerToRemoveUsername) throws Exception {
        return bridge.testOwner_RemoveOwner(token, storeID, ownerToRemoveUsername);
    }

    public String testOwner_ResignOwnership(String token, int storeID) throws Exception {
        return bridge.testOwner_ResignOwnership(token, storeID);
    }

    public String testOwner_AssignManager(String token, int storeID, String managerUsername) throws Exception {
        return bridge.testOwner_AssignManager(token, storeID, managerUsername);
    }

    public String testOwner_EditManagerPermissions(String token, int storeID, String managerUsername, String newPermission) throws Exception {
        return bridge.testOwner_EditManagerPermissions(token, storeID, managerUsername, newPermission);
    }

    public String testOwner_RemoveManager(String token, int storeID, String managerUsername) throws Exception {
        return bridge.testOwner_RemoveManager(token, storeID, managerUsername);
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

    public String testOwner_ReceiveNotifications(String token) throws Exception {
        return bridge.testOwner_ReceiveNotifications(token);
    }

    public String testOwner_ReplyToMessages(String token) throws Exception {
        return bridge.testOwner_ReplyToMessages(token);
    }

    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return bridge.testOwner_ViewStorePurchaseHistory(token, storeID);
    }

    //////////////////////////// Admin ////////////////////////////
    public String testAdmin_CloseStore(String token, int storeID) throws Exception {
        return bridge.testAdmin_CloseStore(token, storeID);
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
