package workshop.demo.AcceptanceTest.Utill;

public interface Bridge {
    // System
    String testSystem_InitMarket(String admin) throws Exception;

    // String testSystem_ConnectToPaymentService() throws Exception;
    // String testSystem_ConnectToSupplyService() throws Exception;
    // Guest
    String testGuest_LogIn() throws Exception;

    String testGuest_LogOut(String username) throws Exception;

    String testGuest_Register(String username, String password, int age) throws Exception;

    String testGuest_GetStoreInfo(String username, int storeID) throws Exception;

    String testGuest_GetProductInfo(String username, int productID) throws Exception;

    String testGuest_SearchProduct(String username, String catagory) throws Exception;

    String testGuest_SearchProductInStore(String username, int storeID, String catagory) throws Exception;

    String testGuest_AddProductToCart(String username, int storeID, int productID, int count) throws Exception;

    String testGuest_ModifyCart(String username, int storeID) throws Exception;

    String testGuest_BuyCart(String username, int i) throws Exception;

    // User
    String testUser_LogIn() throws Exception;

    String testUser_LogOut() throws Exception;

    String testUser_OpenStore() throws Exception;

    String testUser_AddReview() throws Exception;

    String testUser_RateProductAndStore() throws Exception;

    String testUser_SendMessageToStoreOwner() throws Exception;

    String testUser_SendMessageToAdmin() throws Exception;

    String testUser_CheckPurchaseHistory() throws Exception;

    String testUser_updateProfile() throws Exception;

    String testUser_AddBid() throws Exception;

    String testUser_JoinAuction() throws Exception;

    String testUser_JoinRaffle() throws Exception;

    String testUser_ReceiveNotifications() throws Exception;

    String testUser_ReceiveDelayedNotifications() throws Exception;

    // Owner
    String testOwner_ManageInventory_AddProduct() throws Exception;

    String testOwner_ManageInventory_RemoveProduct() throws Exception;

    String testOwner_ManageInventory_UpdateProductDetails() throws Exception;

    String testOwner_SetPurchasePolicies() throws Exception;

    String testOwner_SetDiscountPolicies() throws Exception;

    String testOwner_AssignNewOwner() throws Exception;

    String testOwner_RemoveOwner() throws Exception;

    String testOwner_ResignOwnership() throws Exception;

    String testOwner_AssignManager() throws Exception;

    String testOwner_EditManagerPermissions() throws Exception;

    String testOwner_RemoveManager() throws Exception;

    String testOwner_CloseStore() throws Exception;

    String testOwner_ReopenStore() throws Exception;

    String testOwner_ViewRolesAndPermissions() throws Exception;

    String testOwner_ReceiveNotifications() throws Exception;

    String testOwner_ReplyToMessages() throws Exception;

    String testOwner_ViewStorePurchaseHistory() throws Exception;

    String testOwner_OpenNewStore() throws Exception;

    // Manager
    String testManager_PerformPermittedActions() throws Exception;

    // Admin
    String testAdmin_CloseStore() throws Exception;

    String testAdmin_RemoveUser() throws Exception;

    String testAdmin_ReplyToMsg() throws Exception;

    String testAdmin_SendMessageToUsers() throws Exception;

    String testAdmin_ViewSystemPurchaseHistory() throws Exception;

    String testAdmin_ViewSystemInfo() throws Exception;
}
