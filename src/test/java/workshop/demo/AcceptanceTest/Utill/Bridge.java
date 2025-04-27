package workshop.demo.AcceptanceTest.Utill;

public interface Bridge {
    // System
    String testSystem_InitMarket(String admin) throws Exception;

    // String testSystem_ConnectToPaymentService() throws Exception;
    // String testSystem_ConnectToSupplyService() throws Exception;

    /////////////////////// Guest///////////////////////////////
    String testGuest_Enter() throws Exception;

    String testGuest_Exit(String token) throws Exception;

    String testGuest_Register(String token, String username, String password, int age) throws Exception;

    String testGuest_GetStoreInfo(String token, String username, int storeID) throws Exception;

    String testGuest_GetProductInfo(String token, String username, int productID) throws Exception;

    String testGuest_SearchProduct(String token, String username, String productname) throws Exception;

    String testGuest_SearchProductInStore(String token, String username, int storeID, int productId) throws Exception;

    String testGuest_AddProductToCart(String token, String username, int storeID, int productID, int count)
            throws Exception;

    String testGuest_ModifyCart(String token, String username, int cartID) throws Exception;

    String testGuest_BuyCart(String token, String username, String cartID) throws Exception;

    //////////////////////////// User////////////////////////////

    // login
    String testUser_LogIn(String token, String username, String password) throws Exception;

    // logoutUser
    String testUser_LogOut(String token) throws Exception;

    // addStoreToSystem
    String testUser_OpenStore(String token, String username) throws Exception;

    String testUser_AddReview(String token, String username, int storeId, String review) throws Exception;

    String testUser_RateProductAndStore(String token, String usernmae, int storeID, int productId, int rate)
            throws Exception;

    String testUser_SendMessageToStoreOwner(String token, String username, String msg, int storeID) throws Exception;

    String testUser_SendMessageToAdmin(String token, String username, String msg) throws Exception;

    String testUser_CheckPurchaseHistory(String token, String username, String pass) throws Exception;

    String testUser_updateProfile(String token, String username) throws Exception;

    String testUser_AddBid(String token, String usernmae, int storeid, int productID, int bid) throws Exception;

    String testUser_JoinAuction(String token, String username, int storeID, int Auctionid) throws Exception;

    String testUser_JoinRaffle(String token, String username, int storeID, int raffelid, int num) throws Exception;

    // setAdmin
    String testUser_setAdmin(String token, String admin, String new_admin) throws Exception;

    // Owner
    String testOwner_ManageInventory_AddProduct(String token, String username, int storeid, int productid, int amount)
            throws Exception;

    String testOwner_ManageInventory_RemoveProduct(String token, String username, int storeid, int productid)
            throws Exception;

    String testOwner_ManageInventory_UpdateProductDetails(String token, String username, int storeid, int productid)
            throws Exception;

    String testOwner_SetPurchasePolicies(String token, String username, int storeid, String new_policies)
            throws Exception;

    String testOwner_SetDiscountPolicies(String token, String username, int storeid, String new_policies)
            throws Exception;

    String testOwner_AssignNewOwner(String token, String username, int storeid, String new_owner_name) throws Exception;

    // DeleteOwnershipFromStore
    String testOwner_RemoveOwner(String token, String username, int storeid, String new_owner_name) throws Exception;

    // AddOwnershipToStore
    String testOwner_ResignOwnership(String token, String username, int storeid) throws Exception;

    // AddManagerToStore
    String testOwner_AssignManager(String token, String username, int storeid, String new_manager_name)
            throws Exception;

    // changePermissions
    String testOwner_EditManagerPermissions(String token, String username, int storeid, String new_manager_name,
            String newpermission) throws Exception;

    // deleteManager
    String testOwner_RemoveManager(String token, String username, int storeid, String new_manager_name)
            throws Exception;

    // closeStore
    String testOwner_CloseStore(String token, String username, int storeid) throws Exception;

    String testOwner_ReopenStore(String token, String username, int storeid) throws Exception;

    // deactivateteStore
    String testOwner_ViewRolesAndPermissions(String token, String username, int storeid) throws Exception;

    String testOwner_ReceiveNotifications() throws Exception;

    String testOwner_ReplyToMessages() throws Exception;

    String testOwner_ViewStorePurchaseHistory(String token, String username, int storeid) throws Exception;

    // Manager
    String testManager_PerformPermittedActions(String token, String username, int storeid) throws Exception;

    // Admin
    // closeStore
    String testAdmin_CloseStore(String token, String username, int storeid) throws Exception;

    String testAdmin_RemoveUser(String token, String username, String userremove) throws Exception;

    String testAdmin_ViewSystemPurchaseHistory(String token, String username) throws Exception;

    String testAdmin_ViewSystemInfo(String token, String username) throws Exception;
}
