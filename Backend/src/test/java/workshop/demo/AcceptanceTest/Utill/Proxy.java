package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class Proxy implements Bridge {
    private final Real real = new Real();

    // System
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

    // Guest
    @Override
    public String testGuest_Enter() throws Exception {
        return real.testGuest_Enter();
    }

    @Override
    public boolean testGuest_Exit(String token) throws Exception {
        return real.testGuest_Exit(token);
    }

    @Override
    public boolean testGuest_Register(String token, String username, String password, int age) throws Exception {
        return real.testGuest_Register(token, username, password, age);
    }

    @Override
    public String testGuest_GetStoreProducts(int storeID) throws Exception {
        return real.testGuest_GetStoreProducts(storeID);
    }

    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return real.testGuest_GetProductInfo(token, productID);
    }

    @Override
    public ItemStoreDTO[] testGuest_SearchProduct(String token, ProductSearchCriteria criteria) throws Exception {
        return real.testGuest_SearchProduct(token, criteria);
    }

    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return real.testGuest_SearchProductInStore(token, storeID, productID);
    }

    @Override
    public boolean testGuest_AddProductToCart(String token, ItemStoreDTO a) throws Exception {
        return real.testGuest_AddProductToCart(token, a);
    }

    @Override
    public String testGuest_ModifyCartAddQToBuy(int storeId, String token, int productId) throws Exception {
        return real.testGuest_ModifyCartAddQToBuy(storeId, token, productId);
    }

    @Override
    public String testGuest_BuyCart(String token) throws Exception {
        return real.testGuest_BuyCart(token);
    }

    @Override
    public String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception {
        return real.testGuest_GetPurchasePolicy(token, storeID);
    }

    // User
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
    public String testUser_RateProduct(int storeId, String token, int productId, int newRank) throws Exception {
        return real.testUser_RateProduct(storeId, token, productId, newRank);
    }

    @Override
    public String testUser_RateStore(String token, int storeId, int newRank) throws Exception {
        return real.testUser_RateStore(token, storeId, newRank);
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
    public List<ReceiptDTO> testUser_CheckPurchaseHistory(String token) throws Exception {
        return real.testUser_CheckPurchaseHistory(token);
    }

//    @Override
//    public String testUser_updateProfile(String token) throws Exception {
//        return real.testUser_updateProfile(token);
//    }

    @Override
    public String testUser_AddBid(String token, int bitId, int storeId, double price) throws Exception {
        return real.testUser_AddBid(token, bitId, storeId, price);
    }

    @Override
    public String testUser_JoinAuction(String token, int auctionId, int storeId, double price) throws Exception {
        return real.testUser_JoinAuction(token, auctionId, storeId, price);
    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return real.testUser_JoinRandom(token, storeID, randomID, num);
    }

    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return real.testUser_setAdmin(token, newAdminUsername);
    }

    @Override
    public String testUser_getAllAucationInStore(String token, int storeId) throws Exception {
        return real.testUser_getAllAucationInStore(token, storeId);
    }

    @Override
    public String testUser_getAllRandomInStore(String token, int storeId) throws Exception {
        return real.testUser_getAllRandomInStore(token, storeId);
    }

    @Override
    public String testUser_BuyCart(String token) throws Exception {
        return real.testUser_BuyCart(token);
    }

    // Owner
    @Override
    public String testOwner_ManageInventory_AddProduct(int storeId, String token, int productId, int quantity,
                                                       int price, Category category) throws Exception {
        return real.testOwner_ManageInventory_AddProduct(storeId, token, productId, quantity, price, category);
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(int storeId, String token, int productId) throws Exception {
        return real.testOwner_ManageInventory_RemoveProduct(storeId, token, productId);
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductPrice(int storeId, String token, int productId, int newPrice)
            throws Exception {
        return real.testOwner_ManageInventory_UpdateProductPrice(storeId, token, productId, newPrice);
    }

    @Override
    public String testOwner_SetPurchasePolicies(int storeId, String token, int productId) throws Exception {
        return real.testOwner_SetPurchasePolicies(storeId, token, productId);
    }

    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return real.testOwner_SetDiscountPolicies(token, storeID, newPolicies);
    }

    @Override
    public String testOwner_AssignNewOwner(String token, int storeID, int NewOwner) throws Exception {
        return real.testOwner_AssignNewOwner(token, storeID, NewOwner);
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        return real.testOwner_RemoveOwner(token, storeID, ownerToRemoveId);
    }

//    @Override
//    public String testOwner_AssignManager(String token, int storeID, int mangerId) throws Exception {
//        return real.testOwner_AssignManager(token, storeID, mangerId);
//    }

    @Override
    public String testOwner_EditManagerPermissions(String token, int managerId, int storeID,
                                                   List<Permission> autorization) throws Exception {
        return real.testOwner_EditManagerPermissions(token, managerId, storeID, autorization);
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
    public String testOwner_ReplyToMessages(String msg, int ownerId, int UserId) throws Exception {
        return real.testOwner_ReplyToMessages(msg, ownerId, UserId);
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory(int storeId) throws Exception {
        return real.testOwner_ViewStorePurchaseHistory(storeId);
    }

    @Override
    public String testOwner_ViewStoreRanks(int storeId) throws Exception {
        return real.testOwner_ViewStoreRanks(storeId);
    }

    @Override
    public String testOwner_addProductToAucation(String token, int id, int productId, int quantity, long time,
                                                 double startPrice) throws Exception {
        return real.testOwner_addProductToAucation(token, id, productId, quantity, time, startPrice);
    }

    @Override
    public String testOwner_addProductToBid(String token, int storeid, int productId, int quantity)
            throws Exception {
        return real.testOwner_addProductToBid(token, storeid, productId, quantity);
    }

    @Override
    public String testOwner_EndBid(String token, int storeId, int randomId) throws Exception {
        return real.testOwner_EndBid(token, storeId, randomId);
    }

    @Override
    public String testOwner_AcceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception {
        return real.testOwner_AcceptBid(token, storeId, bidId, bidToAcceptId);
    }

    @Override
    public String testOwner_BidStatus(String token, int storeId) throws Exception {
        return real.testOwner_BidStatus(token, storeId);
    }

    @Override
    public String testOwner_addProductToRandom(String token, int storeId, int quantity, int productId,
                                               int numberOfCards, double priceForCard) throws Exception {
        return real.testOwner_addProductToRandom(token, storeId, quantity, productId, numberOfCards, priceForCard);
    }

    // manager
    @Override
    public String testManager_PerformPermittedActions(String token, int storeID)
            throws Exception {
        return real.testManager_PerformPermittedActions(token, storeID);
    }

    // Admin
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

    public Real getReal() {
        return real;
    }
}