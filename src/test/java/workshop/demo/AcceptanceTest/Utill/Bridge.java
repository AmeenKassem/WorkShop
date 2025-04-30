package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public interface Bridge {

    /////////////////////// System /////////////////////////////
    String testSystem_InitMarket(String admin) throws Exception;

    String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId) throws Exception;

    String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId) throws Exception;

    /////////////////////// Guest /////////////////////////////
    String testGuest_Enter() throws Exception;

    String testGuest_Exit(String token) throws Exception;

    String testGuest_Register(String token, String username, String password, int age) throws Exception;

    String testGuest_GetStoreInfo(String token, int storeID) throws Exception;

    String testGuest_GetProductInfo(String token, int productID) throws Exception;

    String testGuest_SearchProduct(String token, String productName) throws Exception;

    String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception;

    String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception;

    String testGuest_ModifyCart(String token, int cartID) throws Exception;

    String testGuest_BuyCart(String token, int cartID) throws Exception;

    String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception;

    //////////////////////////// User ////////////////////////////
    String testUser_LogIn(String token, String username, String password) throws Exception;

    String testUser_LogOut(String token) throws Exception;

    String testUser_OpenStore(String token, String storeName, String category) throws Exception;

    String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception;

    String testUser_AddReviewToProduct(String token, int storeId, int productId, String review) throws Exception;

    String testUser_RateProduct(String token, int storeID, int productID, int rate) throws Exception;

    String testUser_RateStore(String token, int storeID, int productID, int rate) throws Exception;

    String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception;

    String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception;

    String testUser_CheckPurchaseHistory(String token) throws Exception;

    String testUser_updateProfile(String token) throws Exception;

    String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception;

    String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception;

    String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception;

    String testUser_setAdmin(String token, String newAdminUsername) throws Exception;

    //////////////////////////// Owner ////////////////////////////
    String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount) throws Exception;

    String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception;

    String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID) throws Exception;

    String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception;

    String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception;

    String testOwner_AssignNewOwner(String token, int storeID, int newOwnerId) throws Exception;

    String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception;

    // Resign Ownership עוד לא מומש ולכן לא ייכנס
    String testOwner_AssignManager(String token, int storeID, int managerId) throws Exception;

    String testOwner_EditManagerPermissions(String token, int managerId, int storeID, List<Permission> authorization)
            throws Exception;

    String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception;

    String testOwner_CloseStore(String token, int storeID) throws Exception;

    String testOwner_ReopenStore(String token, int storeID) throws Exception;

    String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception;

    String testOwner_ReceiveNotifications(int userId) throws Exception;

    String testOwner_ReplyToMessages(String msg, int ownerId, int userId) throws Exception;

    String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception;

    //////////////////////////// Admin ////////////////////////////
    String testAdmin_CloseStore(int storeID, String token) throws Exception;

    String testAdmin_RemoveUser(String token, String userToRemove) throws Exception;

    String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception;

    String testAdmin_ViewSystemInfo(String token) throws Exception;
}
