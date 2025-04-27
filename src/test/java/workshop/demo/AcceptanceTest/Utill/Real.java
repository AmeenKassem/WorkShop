package workshop.demo.AcceptanceTest.Utill;

import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import static org.mockito.Mockito.*;

public class Real implements Bridge {

    private UserService user;

    public Real() {
        IUserRepo fakeUserRepo = mock(IUserRepo.class);
        IAuthRepo fakeAuthRepo = mock(IAuthRepo.class);

        when(fakeUserRepo.generateGuest()).thenReturn(123);
        when(fakeAuthRepo.generateGuestToken(123)).thenReturn("guest-token");

        this.user = new UserService(fakeUserRepo, fakeAuthRepo);
    }

    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_Enter() throws Exception {
        return user.generateGuest();
    }

    @Override
    public String testGuest_Exit(String token) throws Exception {
        user.destroyGuest(token);
        return "T";
    }

    @Override
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        user.register(token, username, password);
        return "T";
    }

    @Override
    public String testGuest_GetStoreInfo(String token, String username, int storeID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_GetProductInfo(String token, String username, int productID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_SearchProduct(String token, String username, String productname) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_SearchProductInStore(String token, String username, int storeID, int productId)
            throws Exception {
        return "T";
    }

    @Override
    public String testGuest_AddProductToCart(String token, String username, int storeID, int productID, int count)
            throws Exception {
        return "T";
    }

    @Override
    public String testGuest_ModifyCart(String token, String username, int cartID) throws Exception {
        return "T";
    }

    @Override
    public String testGuest_BuyCart(String token, String username, String cartID) throws Exception {
        return "T";
    }

    // ================= USER =================

    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        user.login(token, username, password);
        return "T";
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        user.logoutUser(token);
        return "T";
    }

    @Override
    public String testUser_OpenStore(String token, String username) throws Exception {
        return "T";
    }

    @Override
    public String testUser_AddReview(String token, String username, int storeId, String review) throws Exception {
        return "T";
    }

    @Override
    public String testUser_RateProductAndStore(String token, String username, int storeID, int productId, int rate)
            throws Exception {
        return "T";
    }

    @Override
    public String testUser_SendMessageToStoreOwner(String token, String username, String msg, int storeID)
            throws Exception {
        return "T";
    }

    @Override
    public String testUser_SendMessageToAdmin(String token, String username, String msg) throws Exception {
        return "T";
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token, String username, String pass) throws Exception {
        return "T";
    }

    @Override
    public String testUser_updateProfile(String token, String username) throws Exception {
        return "T";
    }

    @Override
    public String testUser_AddBid(String token, String username, int storeid, int productID, int bid) throws Exception {
        return "T";
    }

    @Override
    public String testUser_JoinAuction(String token, String username, int storeID, int Auctionid) throws Exception {
        return "T";
    }

    @Override
    public String testUser_JoinRaffle(String token, String username, int storeID, int raffelid, int num)
            throws Exception {
        return "T";
    }

    @Override
    public String testUser_setAdmin() throws Exception {
        return "T";
    }

    // ================= OWNER =================

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

    // ================= MANAGER =================

    @Override
    public String testManager_PerformPermittedActions() throws Exception {
        return "T";
    }

    // ================= ADMIN =================

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
