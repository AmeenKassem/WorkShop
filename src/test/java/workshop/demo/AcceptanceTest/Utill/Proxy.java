package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class Proxy implements Bridge {
    private final Real real = new Real();

    /////////////////////// System /////////////////////////////
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return real.testSystem_InitMarket(admin);
    }

    @Override
    public String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        return real.testSystem_sendDMessageToAll(receiversIds, message, senderId);
    }

    @Override
    public String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        return real.testSystem_sendRTMessageToAll(receiversIds, message, senderId);
    }

    /////////////////////// Guest /////////////////////////////
    @Override
    public String testGuest_Enter() throws Exception {
        return real.testGuest_Enter();
    }

    @Override
    public String testGuest_Exit(String token) throws Exception {
        return real.testGuest_Exit(token);
    }

    @Override
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        return real.testGuest_Register(token, username, password, age);
    }

    @Override
    public String testGuest_GetStoreInfo(String token, int storeID) throws Exception {
        return real.testGuest_GetStoreInfo(token, storeID);
    }

    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return real.testGuest_GetProductInfo(token, productID);
    }

    @Override
    public String testGuest_SearchProduct(String token, String productName) throws Exception {
        return real.testGuest_SearchProduct(token, productName);
    }

    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return real.testGuest_SearchProductInStore(token, storeID, productID);
    }

    @Override
    public String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        return real.testGuest_AddProductToCart(token, storeID, productID, count);
    }

    @Override
    public String testGuest_ModifyCart(String token, int cartID) throws Exception {
        return real.testGuest_ModifyCart(token, cartID);
    }

    @Override
    public String testGuest_BuyCart(String token, int cartID) throws Exception {
        return real.testGuest_BuyCart(token, cartID);
    }
    @Override
    public String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception {
        return real.testGuest_GetPurchasePolicy(token, storeID);
    }


    //////////////////////////// User ////////////////////////////
    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return real.testUser_LogIn(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return real.testUser_LogOut(token);
    }

    @Override
    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        return real.testUser_OpenStore(token, storeName, category);
    }

    @Override
    public String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception {
        return real.testUser_AddReviewToStore(token, storeId, review);
    }

    @Override
    public String testUser_AddReviewToProduct(String token, int storeId, int productId, String review)
            throws Exception {
        return real.testUser_AddReviewToProduct(token, storeId, productId, review);
    }

    @Override
    public String testUser_RateProduct(String token, int storeID, int productID, int rate) throws Exception {
        return real.testUser_RateProduct(token, storeID, productID, rate);
    }

    @Override
    public String testUser_RateStore(String token, int storeID, int productID, int rate) throws Exception {
        return real.testUser_RateStore(token, storeID, productID, rate);
    }

    @Override
    public String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception {
        return real.testUser_SendMessageToStoreOwner(userId, ownerId, msg);
    }

    @Override
    public String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception {
        return real.testUser_SendMessageToAdmin(msg, userId, adminId);
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token) throws Exception {
        return real.testUser_CheckPurchaseHistory(token);
    }

    @Override
    public String testUser_updateProfile(String token) throws Exception {
        return real.testUser_updateProfile(token);
    }

    @Override
    public String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception {
        return real.testUser_AddBid(token, storeID, productID, bid);
    }

    @Override
    public String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception {
        return real.testUser_JoinAuction(token, storeID, auctionID);
    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return real.testUser_JoinRandom(token, storeID, randomID, num);
    }

    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return real.testUser_setAdmin(token, newAdminUsername);
    }

    //////////////////////////// Owner ////////////////////////////
    @Override
    public String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount)
            throws Exception {
        return real.testOwner_ManageInventory_AddProduct(token, storeID, productID, amount);
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return real.testOwner_ManageInventory_RemoveProduct(token, storeID, productID);
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID)
            throws Exception {
        return real.testOwner_ManageInventory_UpdateProductDetails(token, storeID, productID);
    }

    @Override
    public String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        return real.testOwner_SetPurchasePolicies(token, storeID, newPolicies);
    }

    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return real.testOwner_SetDiscountPolicies(token, storeID, newPolicies);
    }

    @Override
    public String testOwner_AssignNewOwner(String token, int storeID, int newOwnerId) throws Exception {
        return real.testOwner_AssignNewOwner(token, storeID, newOwnerId);
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        return real.testOwner_RemoveOwner(token, storeID, ownerToRemoveId);
    }

    @Override
    public String testOwner_AssignManager(String token, int storeID, int managerId) throws Exception {
        return real.testOwner_AssignManager(token, storeID, managerId);
    }

    @Override
    public String testOwner_EditManagerPermissions(String token, int managerId, int storeID,
            List<Permission> authorization) throws Exception {
        return real.testOwner_EditManagerPermissions(token, managerId, storeID, authorization);
    }

    @Override
    public String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception {
        return real.testOwner_RemoveManager(token, storeID, managerId);
    }

    @Override
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        return real.testOwner_CloseStore(token, storeID);
    }

    @Override
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return real.testOwner_ReopenStore(token, storeID);
    }

    @Override
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return real.testOwner_ViewRolesAndPermissions(token, storeID);
    }

    @Override
    public String testOwner_ReceiveNotifications(int userId) throws Exception {
        return real.testOwner_ReceiveNotifications(userId);
    }

    @Override
    public String testOwner_ReplyToMessages(String msg, int ownerId, int userId) throws Exception {
        return real.testOwner_ReplyToMessages(msg, ownerId, userId);
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return real.testOwner_ViewStorePurchaseHistory(token, storeID);
    }

    //////////////////////////// Admin ////////////////////////////
    @Override
    public String testAdmin_CloseStore(int storeID, String token) throws Exception {
        return real.testAdmin_CloseStore(storeID, token);
    }

    @Override
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return real.testAdmin_RemoveUser(token, userToRemove);
    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return real.testAdmin_ViewSystemPurchaseHistory(token);
    }

    @Override
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        return real.testAdmin_ViewSystemInfo(token);
    }
}
