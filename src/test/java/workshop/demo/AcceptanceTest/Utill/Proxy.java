package workshop.demo.AcceptanceTest.Utill;

public class Proxy implements Bridge {
    public Bridge real = new Real();
    // System
    // @Override
    // public String testSystem_InitMarket(String admin) throws Exception {
    // return "T";
    // }
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

    //////////////////////////// User////////////////////////////
    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return real.testUser_LogIn(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return real.testUser_LogOut(token);
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

    @Override
    public String testUser_setAdmin() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'testUser_setAdmin'");
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

    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'testSystem_InitMarket'");
    }

}
