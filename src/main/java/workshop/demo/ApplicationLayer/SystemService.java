package workshop.demo.ApplicationLayer;

public class SystemService {

    /////////////////////// System /////////////////////////////
    // TODO: implement InitMarket
    public String InitMarket(String admin) throws Exception {
        return "true";
    }

    /////////////////////// Guest /////////////////////////////
    // TODO: implement Enter
    public String Enter() throws Exception {
        return "true";
    }

    // TODO: implement Exit
    public String Exit(String token) throws Exception {
        return "true";
    }

    // TODO: implement Register
    public String Register(String token, String username, String password, int age) throws Exception {
        return "true";
    }

    // TODO: implement GetStoreInfo
    public String GetStoreInfo(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement GetProductInfo
    public String GetProductInfo(String token, int productID) throws Exception {
        return "true";
    }

    // TODO: implement SearchProduct
    public String SearchProduct(String token, String productname) throws Exception {
        return "true";
    }

    // TODO: implement SearchProductInStore
    public String SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return "true";
    }

    // TODO: implement AddProductToCart
    public String AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        return "true";
    }

    // TODO: implement ModifyCart
    public String ModifyCart(String token, int cartID) throws Exception {
        return "true";
    }

    // TODO: implement BuyCart
    public String BuyCart(String token, int cartID) throws Exception {
        return "true";
    }

    //////////////////////////// User ////////////////////////////
    // TODO: implement LogIn
    public String LogIn(String token, String username, String password) throws Exception {
        return "true";
    }

    // TODO: implement LogOut
    public String LogOut(String token) throws Exception {
        return "true";
    }

    // TODO: implement OpenStore
    public String OpenStore(String token, String storeName, String category) throws Exception {
        return "true";
    }

    // TODO: implement AddReview
    public String AddReview(String token, int storeId, String review) throws Exception {
        return "true";
    }

    // TODO: implement RateProductAndStore
    public String RateProductAndStore(String token, int storeID, int productID, int rate) throws Exception {
        return "true";
    }

    // TODO: implement SendMessageToStoreOwner
    public String SendMessageToStoreOwner(String token, int storeID, String msg) throws Exception {
        return "true";
    }

    // TODO: implement SendMessageToAdmin
    public String SendMessageToAdmin(String token, String msg) throws Exception {
        return "true";
    }

    // TODO: implement CheckPurchaseHistory
    public String CheckPurchaseHistory(String token) throws Exception {
        return "true";
    }

    // TODO: implement updateProfile
    public String updateProfile(String token) throws Exception {
        return "true";
    }

    // TODO: implement AddBid
    public String AddBid(String token, int storeID, int productID, int bid) throws Exception {
        return "true";
    }

    // TODO: implement JoinAuction
    public String JoinAuction(String token, int storeID, int auctionID) throws Exception {
        return "true";
    }

    // TODO: implement JoinRandom
    public String JoinRandom(String token, int storeID, int RandomID, int num) throws Exception {
        return "true";
    }

    // TODO: implement setAdmin
    public String setAdmin(String token, String newAdminUsername) throws Exception {
        return "true";
    }

    //////////////////////////// Owner ////////////////////////////
    // TODO: implement ManageInventory_AddProduct
    public String ManageInventory_AddProduct(String token, int storeID, int productID, int amount) throws Exception {
        return "true";
    }

    // TODO: implement ManageInventory_RemoveProduct
    public String ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return "true";
    }

    // TODO: implement ManageInventory_UpdateProductDetails
    public String ManageInventory_UpdateProductDetails(String token, int storeID, int productID) throws Exception {
        return "true";
    }

    // TODO: implement SetPurchasePolicies
    public String SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        return "true";
    }

    // TODO: implement SetDiscountPolicies
    public String SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return "true";
    }

    // TODO: implement AssignNewOwner
    public String AssignNewOwner(String token, int storeID, String newOwnerUsername) throws Exception {
        return "true";
    }

    // TODO: implement RemoveOwner
    public String RemoveOwner(String token, int storeID, String ownerToRemoveUsername) throws Exception {
        return "true";
    }

    // TODO: implement ResignOwnership
    public String ResignOwnership(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement AssignManager
    public String AssignManager(String token, int storeID, String managerUsername) throws Exception {
        return "true";
    }

    // TODO: implement EditManagerPermissions
    public String EditManagerPermissions(String token, int storeID, String managerUsername, String newPermission)
            throws Exception {
        return "true";
    }

    // TODO: implement RemoveManager
    public String RemoveManager(String token, int storeID, String managerUsername) throws Exception {
        return "true";
    }

    // TODO: implement CloseStore
    public String CloseStore(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement ReopenStore
    public String ReopenStore(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement ViewRolesAndPermissions
    public String ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement ReceiveNotifications
    public String ReceiveNotifications(String token) throws Exception {
        return "true";
    }

    // TODO: implement ReplyToMessages
    public String ReplyToMessages(String token) throws Exception {
        return "true";
    }

    // TODO: implement ViewStorePurchaseHistory
    public String ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return "true";
    }

    //////////////////////////// Admin ////////////////////////////
    // TODO: implement Admin_CloseStore
    public String Admin_CloseStore(String token, int storeID) throws Exception {
        return "true";
    }

    // TODO: implement RemoveUser
    public String RemoveUser(String token, String userToRemove) throws Exception {
        return "true";
    }

    // TODO: implement ViewSystemPurchaseHistory
    public String ViewSystemPurchaseHistory(String token) throws Exception {
        return "true";
    }

    // TODO: implement ViewSystemInfo
    public String ViewSystemInfo(String token) throws Exception {
        return "true";
    }
}
