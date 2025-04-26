package workshop.demo.AcceptanceTest.Utill;

public class Real implements Bridge {
    public Bridge real = new Real();

    // System
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return "T";
    }
    // @Override
    // String testSystem_ConnectToPaymentService() throws Exception{
    // return "T";
    // }
    // @Override
    // String testSystem_ConnectToSupplyService() throws Exception{
    // return "T";
    // }

    // Guest
    @Override
    public String testGuest_LogIn() throws Exception {
        return "T";
    }

    @Override
    public String testGuest_LogOut(String username) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_Register(String username, String password, int age) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_GetStoreInfo(String username, int storeID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_GetProductInfo(String username, int productID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_SearchProduct(String username, String catagory) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_SearchProductInStore(String username, int storeID, String catagory) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_AddProductToCart(String username, int storeID, int productID, int count) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_ModifyCart(String username, int storeID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_BuyCart(String username, int i) throws Exception {
        return "T";
    }

    // User
    @Override
    public String testUser_LogIn() throws Exception {
        return "T";
    }

    @Override
    public String testUser_LogOut() throws Exception {
        return "T";
    }

    @Override
    public String testUser_OpenStore() throws Exception {
        return "T";
    }

    @Override
    public String testUser_AddReview() throws Exception {
        return "T";
    }

    @Override
    public String testUser_RateProductAndStore() throws Exception {
        return "T";
    }

    @Override
    public String testUser_SendMessageToStoreOwner() throws Exception {
        return "T";
    }

    @Override
    public String testUser_SendMessageToAdmin() throws Exception {
        return "T";
    }

    @Override
    public String testUser_CheckPurchaseHistory() throws Exception {
        return "T";
    }

    @Override
    public String testUser_updateProfile() throws Exception {
        return "T";
    }

    @Override
    public String testUser_AddBid() throws Exception {
        return "T";
    }

    @Override
    public String testUser_JoinAuction() throws Exception {
        return "T";
    }

    @Override
    public String testUser_JoinRaffle() throws Exception {
        return "T";
    }

    @Override
    public String testUser_ReceiveNotifications() throws Exception {
        return "T";
    }

    @Override
    public String testUser_ReceiveDelayedNotifications() throws Exception {
        return "T";
    }

    // Owner
    @Override
    public String testOwner_ManageInventory_AddProduct() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_SetPurchasePolicies() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_SetDiscountPolicies() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_AssignNewOwner() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_RemoveOwner() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ResignOwnership() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_AssignManager() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_EditManagerPermissions() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_RemoveManager() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_CloseStore() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ReopenStore() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ViewRolesAndPermissions() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ReceiveNotifications() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ReplyToMessages() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory() throws Exception {
        return "T";
    }

    @Override
    public String testOwner_OpenNewStore() throws Exception {
        return "T";
    }

    // Manager
    @Override
    public String testManager_PerformPermittedActions() throws Exception {
        return "T";
    }

    // Admin
    @Override
    public String testAdmin_CloseStore() throws Exception {
        return "T";
    }

    @Override
    public String testAdmin_RemoveUser() throws Exception {
        return "T";
    }

    @Override
    public String testAdmin_ReplyToMsg() throws Exception {
        return "T";
    }

    @Override
    public String testAdmin_SendMessageToUsers() throws Exception {
        return "T";
    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory() throws Exception {
        return "T";
    }

    @Override
    public String testAdmin_ViewSystemInfo() throws Exception {
        return "T";
    }
}
