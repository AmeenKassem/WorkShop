package workshop.demo.AcceptanceTest.Tests;

import java.util.List;

import workshop.demo.AcceptanceTest.Utill.Bridge;
import workshop.demo.AcceptanceTest.Utill.Proxy;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public abstract class AcceptanceTests {

    private final Bridge bridge = new Proxy();

    protected AcceptanceTests() throws Exception {
    }

    // System
    String testSystem_InitMarket(String admin) throws Exception {
        return bridge.testSystem_InitMarket(admin);
    }

    String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        return bridge.testSystem_sendDMessageToAll(receiversIds, message, senderId);
    }

    String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        return bridge.testSystem_sendRTMessageToAll(receiversIds, message, senderId);
    }

    // Guest
    String testGuest_Enter() throws Exception {
        return bridge.testGuest_Enter();
    }

    boolean testGuest_Exit(String token) throws Exception {
        return bridge.testGuest_Exit(token);
    }

    boolean testGuest_Register(String token, String username, String password, int age) throws Exception {
        return bridge.testGuest_Register(token, username, password, age);
    }

    String testGuest_GetStoreProducts(int storeID) throws Exception {
        return bridge.testGuest_GetStoreProducts(storeID);
    }

    String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return bridge.testGuest_GetProductInfo(token, productID);
    }

    ItemStoreDTO[] testGuest_SearchProduct(String token, ProductSearchCriteria criteria) throws Exception {
        return bridge.testGuest_SearchProduct(token, criteria);
    }

    String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return bridge.testGuest_SearchProductInStore(token, storeID, productID);
    }

    boolean testGuest_AddProductToCart(String token, ItemStoreDTO a, int quantity) throws Exception {
        return bridge.testGuest_AddProductToCart(token, a, quantity);
    }

    String testGuest_ModifyCartAddQToBuy(int storeId, String token, int productId) throws Exception {
        return bridge.testGuest_ModifyCartAddQToBuy(storeId, token, productId);
    }

    String testGuest_BuyCart(String token) throws Exception {
        return bridge.testGuest_BuyCart(token);
    }

    String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception {
        return bridge.testGuest_GetPurchasePolicy(token, storeID);
    }

    // User
    String testUser_LogIn(String token, String username, String password) throws Exception {
        return bridge.testUser_LogIn(token, username, password);
    }

    String testUser_LogOut(String token) throws Exception {
        return bridge.testUser_LogOut(token);
    }

    String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        return bridge.testUser_OpenStore(token, storeName, category);
    }

    String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception {
        return bridge.testUser_AddReviewToStore(token, storeId, review);
    }

    String testUser_AddReviewToProduct(String token, int storeId, int productId, String review) throws Exception {
        return bridge.testUser_AddReviewToProduct(token, storeId, productId, review);
    }

    String testUser_RateProduct(int storeId, String token, int productId, int newRank) throws Exception {
        return bridge.testUser_RateProduct(storeId, token, productId, newRank);
    }

    String testUser_RateStore(String token, int storeId, int newRank) throws Exception {
        return bridge.testUser_RateStore(token, storeId, newRank);
    }

    String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception {
        return bridge.testUser_SendMessageToStoreOwner(userId, ownerId, msg);
    }

    String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception {
        return bridge.testUser_SendMessageToAdmin(msg, userId, adminId);
    }

    List<ReceiptDTO> testUser_CheckPurchaseHistory(String token) throws Exception {
        return bridge.testUser_CheckPurchaseHistory(token);
    }

//    String testUser_updateProfile(String token) throws Exception {
//        return bridge.testUser_updateProfile(token);
//    }
    String testUser_AddBid(String token, int bitId, int storeId, double price) throws Exception {
        return bridge.testUser_AddBid(token, bitId, storeId, price);
    }

    String testUser_JoinAuction(String token, int auctionId, int storeId, double price) throws Exception {
        return bridge.testUser_JoinAuction(token, auctionId, storeId, price);
    }

    String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return bridge.testUser_JoinRandom(token, storeID, randomID, num);
    }

    String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return bridge.testUser_setAdmin(token, newAdminUsername);
    }

    String testUser_getAllAucationInStore(String token, int storeId) throws Exception {
        return bridge.testUser_getAllAucationInStore(token, storeId);
    }

    String testUser_getAllRandomInStore(String token, int storeId) throws Exception {
        return bridge.testUser_getAllRandomInStore(token, storeId);
    }

    String testUser_BuyCart(String token) throws Exception {
        return bridge.testUser_BuyCart(token);
    }

    // Owner
    String testOwner_ManageInventory_AddProduct(int storeId, String token, int productId, int quantity,
            int price, Category category) throws Exception {
        return bridge.testOwner_ManageInventory_AddProduct(storeId, token, productId, quantity, price, category);
    }

    String testOwner_ManageInventory_RemoveProduct(int storeId, String token, int productId) throws Exception {
        return bridge.testOwner_ManageInventory_RemoveProduct(storeId, token, productId);
    }

    String testOwner_ManageInventory_UpdateProductPrice(int storeId, String token, int productId, int newPrice)
            throws Exception {
        return bridge.testOwner_ManageInventory_UpdateProductPrice(storeId, token, productId, newPrice);
    }

    String testOwner_SetPurchasePolicies(int storeId, String token, int productId) throws Exception {
        return bridge.testOwner_SetPurchasePolicies(storeId, token, productId);
    }

    String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return bridge.testOwner_SetDiscountPolicies(token, storeID, newPolicies);
    }

    String testOwner_AssignNewOwner(int token, int storeID, int NewOwner, boolean decide) throws Exception {
        return bridge.testOwner_AssignNewOwner(token, storeID, NewOwner, decide);
    }

    String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        return bridge.testOwner_RemoveOwner(token, storeID, ownerToRemoveId);
    }
//
//    String testOwner_AssignManager(String token, int storeID, int mangerId) throws Exception {
//        return bridge.testOwner_AssignManager(token, storeID, mangerId);
//    }

    String testOwner_EditManagerPermissions(String token, int managerId, int storeID,
            List<Permission> autorization) throws Exception {
        return bridge.testOwner_EditManagerPermissions(token, managerId, storeID, autorization);
    }

    String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception {
        return bridge.testOwner_RemoveManager(token, storeID, managerId);
    }

    String testOwner_CloseStore(String token, int storeID) throws Exception {
        return bridge.testOwner_CloseStore(token, storeID);
    }

    String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return bridge.testOwner_ReopenStore(token, storeID);
    }

    String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return bridge.testOwner_ViewRolesAndPermissions(token, storeID);
    }

    String testOwner_ReceiveNotifications(int userId) throws Exception {
        return bridge.testOwner_ReceiveNotifications(userId);
    }

    String testOwner_ReplyToMessages(String msg, int ownerId, int UserId) throws Exception {
        return bridge.testOwner_ReplyToMessages(msg, ownerId, UserId);
    }

    String testOwner_ViewStorePurchaseHistory(int storeId) throws Exception {
        return bridge.testOwner_ViewStorePurchaseHistory(storeId);
    }

    String testOwner_ViewStoreRanks(int storeId) throws Exception {
        return bridge.testOwner_ViewStoreRanks(storeId);
    }

    String testOwner_addProductToAucation(String token, int id, int productId, int quantity, long time,
            double startPrice) throws Exception {
        return bridge.testOwner_addProductToAucation(token, id, productId, quantity, time, startPrice);
    }

    String testOwner_addProductToBid(String token, int storeid, int productId, int quantity) throws Exception {
        return bridge.testOwner_addProductToBid(token, storeid, productId, quantity);
    }

    String testOwner_EndBid(String token, int storeId, int randomId) throws Exception {
        return bridge.testOwner_EndBid(token, storeId, randomId);
    }

    String testOwner_AcceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception {
        return bridge.testOwner_AcceptBid(token, storeId, bidId, bidToAcceptId);
    }

    String testOwner_BidStatus(String token, int storeId) throws Exception {
        return bridge.testOwner_BidStatus(token, storeId);
    }

    String testOwner_addProductToRandom(String token, int storeId, int quantity, int productId,
            int numberOfCards, double priceForCard) throws Exception {
        return bridge.testOwner_addProductToRandom(token, storeId, quantity, productId, numberOfCards, priceForCard);
    }

    // Admin
    String testAdmin_CloseStore(int storeID, String token) throws Exception {
        return bridge.testAdmin_CloseStore(storeID, token);
    }

    String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return bridge.testAdmin_RemoveUser(token, userToRemove);
    }

    String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return bridge.testAdmin_ViewSystemPurchaseHistory(token);
    }

    String testAdmin_ViewSystemInfo(String token) throws Exception {
        return bridge.testAdmin_ViewSystemInfo(token);
    }
}
