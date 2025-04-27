package workshop.demo.AcceptanceTest.Utill;

public class Proxy implements Bridge {
    public Bridge real = new Real();

    // =================== SYSTEM ===================
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return real.testSystem_InitMarket(admin);
    }

    // =================== GUEST ===================
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
    public String testGuest_GetStoreInfo(String token, String username, int storeID) throws Exception {
        return real.testGuest_GetStoreInfo(token, username, storeID);
    }

    @Override
    public String testGuest_GetProductInfo(String token, String username, int productID) throws Exception {
        return real.testGuest_GetProductInfo(token, username, productID);
    }

    @Override
    public String testGuest_SearchProduct(String token, String username, String productname) throws Exception {
        return real.testGuest_SearchProduct(token, username, productname);
    }

    @Override
    public String testGuest_SearchProductInStore(String token, String username, int storeID, int productId)
            throws Exception {
        return real.testGuest_SearchProductInStore(token, username, storeID, productId);
    }

    @Override
    public String testGuest_AddProductToCart(String token, String username, int storeID, int productID, int count)
            throws Exception {
        return real.testGuest_AddProductToCart(token, username, storeID, productID, count);
    }

    @Override
    public String testGuest_ModifyCart(String token, String username, int cartID) throws Exception {
        return real.testGuest_ModifyCart(token, username, cartID);
    }

    @Override
    public String testGuest_BuyCart(String token, String username, String cartID) throws Exception {
        return real.testGuest_BuyCart(token, username, cartID);
    }

    // =================== USER ===================

    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return real.testUser_LogIn(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return real.testUser_LogOut(token);
    }

    @Override
    public String testUser_OpenStore(String token, String username) throws Exception {
        return real.testUser_OpenStore(token, username);
    }

    @Override
    public String testUser_AddReview(String token, String username, int storeId, String review) throws Exception {
        return real.testUser_AddReview(token, username, storeId, review);
    }

    @Override
    public String testUser_RateProductAndStore(String token, String username, int storeID, int productId, int rate)
            throws Exception {
        return real.testUser_RateProductAndStore(token, username, storeID, productId, rate);
    }

    @Override
    public String testUser_SendMessageToStoreOwner(String token, String username, String msg, int storeID)
            throws Exception {
        return real.testUser_SendMessageToStoreOwner(token, username, msg, storeID);
    }

    @Override
    public String testUser_SendMessageToAdmin(String token, String username, String msg) throws Exception {
        return real.testUser_SendMessageToAdmin(token, username, msg);
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token, String username, String pass) throws Exception {
        return real.testUser_CheckPurchaseHistory(token, username, pass);
    }

    @Override
    public String testUser_updateProfile(String token, String username) throws Exception {
        return real.testUser_updateProfile(token, username);
    }

    @Override
    public String testUser_AddBid(String token, String username, int storeid, int productID, int bid) throws Exception {
        return real.testUser_AddBid(token, username, storeid, productID, bid);
    }

    @Override
    public String testUser_JoinAuction(String token, String username, int storeID, int Auctionid) throws Exception {
        return real.testUser_JoinAuction(token, username, storeID, Auctionid);
    }

    @Override
    public String testUser_JoinRaffle(String token, String username, int storeID, int raffelid, int num)
            throws Exception {
        return real.testUser_JoinRaffle(token, username, storeID, raffelid, num);
    }

    @Override
    public String testUser_ReceiveNotifications() throws Exception {
        return real.testUser_ReceiveNotifications();
    }

    @Override
    public String testUser_ReceiveDelayedNotifications() throws Exception {
        return real.testUser_ReceiveDelayedNotifications();
    }

    @Override
    public String testUser_setAdmin() throws Exception {
        return real.testUser_setAdmin();
    }

    // =================== OWNER ===================

    @Override
    public String testOwner_ManageInventory_AddProduct() throws Exception {
        return real.testOwner_ManageInventory_AddProduct();
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct() throws Exception {
        return real.testOwner_ManageInventory_RemoveProduct();
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails() throws Exception {
        return real.testOwner_ManageInventory_UpdateProductDetails();
    }

    @Override
    public String testOwner_SetPurchasePolicies() throws Exception {
        return real.testOwner_SetPurchasePolicies();
    }

    @Override
    public String testOwner_SetDiscountPolicies() throws Exception {
        return real.testOwner_SetDiscountPolicies();
    }

    @Override
    public String testOwner_AssignNewOwner() throws Exception {
        return real.testOwner_AssignNewOwner();
    }

    @Override
    public String testOwner_RemoveOwner() throws Exception {
        return real.testOwner_RemoveOwner();
    }

    @Override
    public String testOwner_ResignOwnership() throws Exception {
        return real.testOwner_ResignOwnership();
    }

    @Override
    public String testOwner_AssignManager() throws Exception {
        return real.testOwner_AssignManager();
    }

    @Override
    public String testOwner_EditManagerPermissions() throws Exception {
        return real.testOwner_EditManagerPermissions();
    }

    @Override
    public String testOwner_RemoveManager() throws Exception {
        return real.testOwner_RemoveManager();
    }

    @Override
    public String testOwner_CloseStore() throws Exception {
        return real.testOwner_CloseStore();
    }

    @Override
    public String testOwner_ReopenStore() throws Exception {
        return real.testOwner_ReopenStore();
    }

    @Override
    public String testOwner_ViewRolesAndPermissions() throws Exception {
        return real.testOwner_ViewRolesAndPermissions();
    }

    @Override
    public String testOwner_ReceiveNotifications() throws Exception {
        return real.testOwner_ReceiveNotifications();
    }

    @Override
    public String testOwner_ReplyToMessages() throws Exception {
        return real.testOwner_ReplyToMessages();
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory() throws Exception {
        return real.testOwner_ViewStorePurchaseHistory();
    }

    // =================== MANAGER ===================

    @Override
    public String testManager_PerformPermittedActions() throws Exception {
        return real.testManager_PerformPermittedActions();
    }

    // =================== ADMIN ===================

    @Override
    public String testAdmin_CloseStore() throws Exception {
        return real.testAdmin_CloseStore();
    }

    @Override
    public String testAdmin_RemoveUser() throws Exception {
        return real.testAdmin_RemoveUser();
    }

    @Override
    public String testAdmin_ReplyToMsg() throws Exception {
        return real.testAdmin_ReplyToMsg();
    }

    @Override
    public String testAdmin_SendMessageToUsers() throws Exception {
        return real.testAdmin_SendMessageToUsers();
    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory() throws Exception {
        return real.testAdmin_ViewSystemPurchaseHistory();
    }

    @Override
    public String testAdmin_ViewSystemInfo() throws Exception {
        return real.testAdmin_ViewSystemInfo();
    }
}
