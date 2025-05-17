package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public interface Bridge {
    String testSystem_InitMarket(String admin) throws Exception;

    String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId) throws Exception;

    String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId) throws Exception;

    String testGuest_Enter() throws Exception;

    boolean testGuest_Exit(String token) throws Exception;

    boolean testGuest_Register(String token, String username, String password, int age) throws Exception;

    String testGuest_GetStoreProducts(int storeID) throws Exception;

    String testGuest_GetProductInfo(String token, int productID) throws Exception;

    ItemStoreDTO[] testGuest_SearchProduct(String token, ProductSearchCriteria criteria) throws Exception;

    String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception;

    boolean testGuest_AddProductToCart(String token, ItemStoreDTO a) throws Exception;

    String testGuest_ModifyCartAddQToBuy(int storeId, String token, int productId) throws Exception;

    String testGuest_BuyCart(String token) throws Exception;

    String testGuest_GetPurchasePolicy(String token, int storeID) throws Exception;

    String testUser_LogIn(String token, String username, String password) throws Exception;

    String testUser_LogOut(String token) throws Exception;

    String testUser_OpenStore(String token, String storeName, String category) throws Exception;

    String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception;

    String testUser_AddReviewToProduct(String token, int storeId, int productId, String review) throws Exception;

    String testUser_RateProduct(int storeId, String token, int productId, int newRank) throws Exception;

    String testUser_RateStore(String token, int storeId, int newRank) throws Exception;

    String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception;

    String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception;

    List<ReceiptDTO> testUser_CheckPurchaseHistory(String token) throws Exception;

    //String testUser_updateProfile(String token) throws Exception;
    String testUser_AddBid(String token, int bitId, int storeId, double price) throws Exception;

    String testUser_JoinAuction(String token, int auctionId, int storeId, double price) throws Exception;

    String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception;

    String testUser_setAdmin(String token, String newAdminUsername) throws Exception;

    String testUser_getAllAucationInStore(String token, int storeId) throws Exception;

    String testUser_getAllRandomInStore(String token, int storeId) throws Exception;

    String testUser_BuyCart(String token) throws Exception;

    String testOwner_ManageInventory_AddProduct(int storeId, String token, int productId, int quantity, int price, Category category) throws Exception;

    String testOwner_ManageInventory_RemoveProduct(int storeId, String token, int productId) throws Exception;

    String testOwner_ManageInventory_UpdateProductPrice(int storeId, String token, int productId, int newPrice) throws Exception;

    String testOwner_SetPurchasePolicies(int storeId, String token, int productId) throws Exception;

    String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception;

    String testOwner_AssignNewOwner(String token, int storeID, int NewOwner) throws Exception;

    String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception;

    //String testOwner_AssignManager(String token, int storeID, int mangerId) throws Exception;
    String testOwner_EditManagerPermissions(String token, int managerId, int storeID, List<Permission> autorization) throws Exception;

    String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception;

    String testOwner_CloseStore(String token, int storeID) throws Exception;

    String testOwner_ReopenStore(String token, int storeID) throws Exception;

    String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception;

    String testOwner_ReceiveNotifications(int userId) throws Exception;

    String testOwner_ReplyToMessages(String msg, int ownerId, int UserId) throws Exception;

    String testOwner_ViewStorePurchaseHistory(int storeId) throws Exception;

    String testOwner_ViewStoreRanks(int storeId) throws Exception;

    String testOwner_addProductToAucation(String token, int id, int productId, int quantity, long time, double startPrice) throws Exception;

    String testOwner_addProductToBid(String token, int storeid, int productId, int quantity) throws Exception;

    String testOwner_EndBid(String token, int storeId, int randomId) throws Exception;

    String testOwner_AcceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws Exception;

    String testOwner_BidStatus(String token, int storeId) throws Exception;

    String testOwner_addProductToRandom(String token, int storeId, int quantity, int productId, int numberOfCards, double priceForCard) throws Exception;

    String testManager_PerformPermittedActions(String token, int storeID) throws Exception;

    String testAdmin_CloseStore(int storeID, String token) throws Exception;

    String testAdmin_RemoveUser(String token, String userToRemove) throws Exception;

    String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception;

    String testAdmin_ViewSystemInfo(String token) throws Exception;
}
