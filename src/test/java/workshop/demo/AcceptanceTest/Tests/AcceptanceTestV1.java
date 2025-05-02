package workshop.demo.AcceptanceTest.Tests;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import workshop.demo.DTOs.Category;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class AcceptanceTestV1 extends AcceptanceTests {
    @BeforeEach
    void setup() throws Exception {
        String result = testSystem_InitMarket("admin");
        assertTrue(result.equals("TODO") || result.equals("Done"));
    }

    @Test
    void sendDMessageToAll() throws Exception {

        String t1 = testGuest_Enter();
        testGuest_Register(t1, "user1", "pass", 21);
        testUser_LogIn(t1, "user1", "pass");
        String t2 = testGuest_Enter();
        testGuest_Register(t2, "user2", "pass", 22);
        testUser_LogIn(t2, "user2", "pass");
        testUser_setAdmin(t1, "admin");
        List<Integer> receivers = List.of(2);
        String result = testSystem_sendDMessageToAll(receivers, "Hello everyone", 1); // user1 ID=1
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    void sendRTMessageToAll() throws Exception {

        String t1 = testGuest_Enter();
        testGuest_Register(t1, "user1", "pass", 21);
        testUser_LogIn(t1, "user1", "pass");
        String t2 = testGuest_Enter();
        testGuest_Register(t2, "user2", "pass", 22);
        testUser_LogIn(t2, "user2", "pass");
        testUser_setAdmin(t1, "admin");
        List<Integer> receivers = List.of(2);
        String result = testSystem_sendRTMessageToAll(receivers, "Hello everyone", 1); // user1 ID=1
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    void testGuestEnter() throws Exception {
        String result = testGuest_Enter();
        assertEquals("Done", result);
    }

    @Test
    void testGuestExit() throws Exception {
        testGuest_Enter();
        String result = testGuest_Exit("123123");
        assertEquals("Done", result);
    }

    @Test
    void testGuestRegister() throws Exception {
        testGuest_Enter();
        String result = testGuest_Register("123123", "guest1", "pass", 20);
        assertEquals("Done", result);
    }

    @Test
    void testGuestGetStoreProducts() throws Exception {
        testGuest_Enter();
        String result = testGuest_GetStoreProducts(1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void GetProductInfo() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "pass", 25);
        testUser_LogIn(owner, "owner", "pass");
        testUser_OpenStore(owner, "store1", "Electronics");
        Category category = Category.ELECTRONICS;
        int productId = 100;
        testOwner_ManageInventory_AddProduct(1, owner, productId, 10, 100, category);
        String result = testGuest_GetProductInfo("123123", productId);
        assertTrue(result.equals("TODO") || result.equals("Done"));
    }

    @Test
    void SearchProduct() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "456456", 30);
        testUser_LogIn(owner, "owner", "456456");
        testUser_OpenStore(owner, "store1", "Electronics");
        Category c = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 500, c);
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setSearchType("category");
        criteria.setCategoryFilter("Electronics");
        criteria.setCategorySpecified(true);
        String result = testGuest_SearchProduct("123123", criteria);
        assertEquals("Done", result);
    }

    @Test
    void SearchProductInStore() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "456456", 30);
        testUser_LogIn(owner, "owner", "456456");
        testUser_OpenStore(owner, "store1", "Electronics");
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 500, category);
        String result = testGuest_SearchProductInStore("123123", 1, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AddProductToCart() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "456456", 30);
        testUser_LogIn(owner, "owner", "456456");
        testUser_OpenStore(owner, "store1", "Electronics");
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 500, category);
        String result = testGuest_AddProductToCart("123123", 1, 2, 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ModifyCartAddQToBuy() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "456456", 30);
        testUser_LogIn(owner, "owner", "456456");
        testUser_OpenStore(owner, "store1", "Electronics");
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 500, category);
        String addResult = testGuest_AddProductToCart("123123", 1, 2, 1);
        assertTrue(addResult.equals("Done") || addResult.equals("TODO"));
        String result = testGuest_ModifyCartAddQToBuy(1, "123123", 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void BuyCart() throws Exception {
        String guestToken = testGuest_Enter();

        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "pass123", 30);
        testUser_LogIn(owner, "owner", "pass123");
        testUser_OpenStore(owner, "TechStore", "Electronics");

        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 300, category);

        String addResult = testGuest_AddProductToCart(guestToken, 1, 2, 1);
        assertTrue(addResult.equals("Done") || addResult.equals("TODO"));

        String buyResult = testGuest_BuyCart(guestToken);
        assertTrue(buyResult.equals("Done") || buyResult.equals("TODO"));
    }

    @Test
    void GetPurchasePolicy() throws Exception {
        testGuest_Enter();
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "pass123", 30);
        testUser_LogIn(owner, "owner", "pass123");
        testUser_OpenStore(owner, "TechStore", "Electronics");
        String result = testGuest_GetPurchasePolicy("123123", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    // user
    @Test
    void LogIn() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        String result = testUser_LogIn("789789", "user1", "pass123");
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    void LogOut() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String result = testUser_LogOut("789789");
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    void OpenStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String result = testUser_OpenStore("789789", "TechStore", "Electronics");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AddReviewToStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");

        // Create another user to open a store
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "PublicStore", "Books");

        String result = testUser_AddReviewToStore("789789", 1, "Great store!");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AddReviewToProduct() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "PublicStore", "Books");
        testOwner_ManageInventory_AddProduct(1, owner, 2, 10, 200, Category.ELECTRONICS);

        String result = testUser_AddReviewToProduct("789789", 1, 2, "Amazing book!");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void RateProduct() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");

        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "RateStore", "Electronics");
        testOwner_ManageInventory_AddProduct(1, owner, 3, 10, 500, Category.ELECTRONICS);

        String result = testUser_RateProduct(1, "789789", 3, 5);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void RateStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "store ", "Clothing");
        String result = testUser_RateStore("789789", 1, 4);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void SendMessageToStoreOwner() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "TalkStore", "Home");
        String result = testUser_SendMessageToStoreOwner(1, 2, "Is this product available?");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void SendMessageToAdmin() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");
        testUser_setAdmin(admin, "adminUser");
        String result = testUser_SendMessageToAdmin("Hello Admin", 1, 3);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void CheckPurchaseHistory() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String result = testUser_CheckPurchaseHistory("789789");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void updateProfile() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String result = testUser_updateProfile("789789");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AddBid() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "BidStore", "Art");
        testOwner_ManageInventory_AddProduct(1, owner, 5, 10, 1000, Category.ELECTRONICS);
        String result = testUser_AddBid("789789", 5, 1, 900.0);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void JoinAuction() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "AuctionStore", "Collectibles");
        testOwner_ManageInventory_AddProduct(1, owner, 6, 5, 200, Category.ELECTRONICS);
        String result = testUser_JoinAuction("789789", 6, 1, 250.0);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void JoinRandom() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");

        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "RandomStore", "Gadgets");
        testOwner_ManageInventory_AddProduct(1, owner, 7, 20, 150, Category.ELECTRONICS);
        testOwner_addProductToRandom(owner, 1, 20, 7, 10, 15.0);

        String result = testUser_JoinRandom("789789", 1, 7, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void setAdmin() throws Exception {
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");

        String newAdmin = testGuest_Enter();
        testGuest_Register(newAdmin, "newAdmin", "pass456", 30);
        testUser_LogIn(newAdmin, "newAdmin", "pass456");

        String result = testUser_setAdmin(admin, "newAdmin");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void getAllAucationInStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");

        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "AuctionHouse", "Collectibles");
        testOwner_ManageInventory_AddProduct(1, owner, 8, 5, 300, Category.ELECTRONICS);
        testOwner_addProductToAucation(owner, 8, 8, 2, 60L, 100.0);

        String result = testUser_getAllAucationInStore("789789", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void getAllRandomInStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");

        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "RandomDeals", "Games");
        testOwner_ManageInventory_AddProduct(1, owner, 9, 10, 50, Category.ELECTRONICS);
        testOwner_addProductToRandom(owner, 1, 10, 9, 5, 10.0);

        String result = testUser_getAllRandomInStore("789789", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void UserBuyCart() throws Exception {
        testGuest_Enter();
        testGuest_Register("789789", "user1", "pass123", 22);
        testUser_LogIn("789789", "user1", "pass123");
        String owner = testGuest_Enter();
        testGuest_Register(owner, "owner", "ownerPass", 30);
        testUser_LogIn(owner, "owner", "ownerPass");
        testUser_OpenStore(owner, "UserBuyStore", "Electronics");
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(1, owner, 10, 10, 300, category);
        String addResult = testGuest_AddProductToCart("789789", 1, 10, 1);
        assertTrue(addResult.equals("Done") || addResult.equals("TODO"));
        String buyResult = testUser_BuyCart("789789");
        assertTrue(buyResult.equals("Done") || buyResult.equals("TODO"));
    }

    // owner
    @Test
    void AddProduct() throws Exception {
        testGuest_Enter();
        testGuest_Register("159159", "owner", "1231223", 50);
        testUser_LogIn("159159", "owner", "1231223");
        testUser_OpenStore("159159", "store1", "Electronics");
        String addResult = testOwner_ManageInventory_AddProduct(1, "159159", 2, 10, 500, Category.ELECTRONICS);
        assertTrue(addResult.equals("Done") || addResult.equals("TODO"));
    }

    @Test
    void RemoveProduct() throws Exception {
        testGuest_Enter();
        testGuest_Register("159159", "owner", "1231223", 50);
        testUser_LogIn("159159", "owner", "1231223");
        testUser_OpenStore("159159", "store1", "Electronics");
        int storeId = 1;
        int productId = 111;
        Category category = Category.ELECTRONICS;

        testOwner_ManageInventory_AddProduct(storeId, "managerToken", productId, 10, 500, category);
        String removeResult = testOwner_ManageInventory_RemoveProduct(storeId, "managerToken", productId);
        assertTrue(removeResult.equals("Done") || removeResult.equals("TODO"));
    }

    @Test
    void UpdateProductPrice() throws Exception {
        testGuest_Enter();
        testGuest_Register("159159", "owner", "1231223", 50);
        testUser_LogIn("159159", "owner", "1231223");
        testUser_OpenStore("159159", "store1", "Electronics");
        int storeId = 1;
        int productId = 111;
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(storeId, "managerToken", productId, 10, 500, category);
        String updatePriceResult = testOwner_ManageInventory_UpdateProductPrice(storeId, "managerToken", productId,
                550);
        assertTrue(updatePriceResult.equals("Done") || updatePriceResult.equals("TODO"));
    }

    @Test
    void SetPurchasePolicies() throws Exception {
        testGuest_Enter();
        testGuest_Register("159159", "owner", "1231223", 50);
        testUser_LogIn("159159", "owner", "1231223");
        testUser_OpenStore("159159", "store1", "Electronics");

        int storeId = 1;
        int productId = 333;
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(storeId, "ownerToken", productId, 10, 150, category);

        String setPolicyResult = testOwner_SetPurchasePolicies(storeId, "ownerToken", productId);
        assertTrue(setPolicyResult.equals("Done") || setPolicyResult.equals("TODO"));
    }

    @Test
    void SetDiscountPolicies() throws Exception {
        testGuest_Enter();
        testGuest_Register("159159", "owner", "1231223", 50);
        testUser_LogIn("159159", "owner", "1231223");
        testUser_OpenStore("159159", "store1", "Electronics");
        int storeId = 1;
        int productId = 333;
        Category category = Category.ELECTRONICS;
        testOwner_ManageInventory_AddProduct(storeId, "ownerToken", productId, 10, 150, category);
        String discountPolicy = "1+1";
        String setDiscountResult = testOwner_SetDiscountPolicies("ownerToken", storeId, discountPolicy);
        assertTrue(setDiscountResult.equals("Done") || setDiscountResult.equals("TODO"));
    }

    @Test
    void AssignNewOwner() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ShareStore", "Gadgets");

        String newo = testGuest_Enter();
        testGuest_Register(newo, "753753", "1233", 25);
        testUser_LogIn(newo, "753753", "1233");

        String result = testOwner_AssignNewOwner("1591591", 1, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void RemoveOwner() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "SoloStore", "Toys");

        String newOwnerToken = testGuest_Enter();
        testGuest_Register(newOwnerToken, "456456", "1448", 26);
        testUser_LogIn(newOwnerToken, "456456", "1448");

        testOwner_AssignNewOwner("1591591", 1, 2);

        String result = testOwner_RemoveOwner("1591591", 1, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AssignManager() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ManageStore", "Garden");

        String managerToken = testGuest_Enter();
        testGuest_Register(managerToken, "managerUser", "pass456", 28);
        testUser_LogIn(managerToken, "managerUser", "pass456");

        String result = testOwner_AssignManager("1591591", 1, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void EditManagerPermissions() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ManageStore", "Garden");

        String managerToken = testGuest_Enter();
        testGuest_Register(managerToken, "managerUser", "pass456", 28);
        testUser_LogIn(managerToken, "managerUser", "pass456");

        testOwner_AssignManager("1591591", 1, 2);

        List<Permission> permissions = List.of(Permission.AddToStock, Permission.DeleteFromStock);
        String result = testOwner_EditManagerPermissions("1591591", 2, 1, permissions);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void RemoveManager() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ManageStore", "Garden");

        String managerToken = testGuest_Enter();
        testGuest_Register(managerToken, "managerUser", "pass456", 28);
        testUser_LogIn(managerToken, "managerUser", "pass456");

        testOwner_AssignManager("1591591", 1, 2);

        String result = testOwner_RemoveManager("1591591", 1, 2);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void CloseStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ClosableStore", "Books");
        String result = testOwner_CloseStore("1591591", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ReopenStore() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "ClosableStore", "Books");
        testOwner_CloseStore("1591591", 1);
        String result = testOwner_ReopenStore("1591591", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ViewRolesAndPermissions() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "RoleViewStore", "Toys");
        String result = testOwner_ViewRolesAndPermissions("1591591", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ReceiveNotifications() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "MessageStore", "Office");
        String userToken = testGuest_Enter();
        testGuest_Register(userToken, "userA", "userPass", 24);
        testUser_LogIn(userToken, "userA", "userPass");
        testUser_SendMessageToStoreOwner(1, 1, "Hi, I have a question.");
        String result = testOwner_ReceiveNotifications(1);
        assertTrue(result.equals("Done") || result.equals("TODO"));

    }

    @Test
    void ReplyToMessages() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "MessageStore", "Office");
        String userToken = testGuest_Enter();
        testGuest_Register(userToken, "A", "userPass", 24);
        testUser_LogIn(userToken, "A", "userPass");
        testUser_SendMessageToStoreOwner(1, 1, "Hi");
        testOwner_ReceiveNotifications(1);
        String result = testOwner_ReplyToMessages("Thanks !", 1, 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ViewStorePurchaseHistory() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "HistoryStore", "Sports");
        String result = testOwner_ViewStorePurchaseHistory(1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ViewStoreRanks() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "RankedStore", "Kitchen");
        String result = testOwner_ViewStoreRanks(1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void addProductToAucation() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "AuctionStore", "Art");
        testOwner_ManageInventory_AddProduct(1, "1591591", 10, 5, 100, Category.ELECTRONICS);
        String result = testOwner_addProductToAucation("1591591", 10, 10, 2, 60L, 300.0);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void addProductToBid() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "BidStore", "Fashion");
        testOwner_ManageInventory_AddProduct(1, "1591591", 11, 5, 150, Category.ELECTRONICS);
        String result = testOwner_addProductToBid("1591591", 1, 11, 3);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void EndBid() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "BidStore", "Fashion");
        testOwner_ManageInventory_AddProduct(1, "1591591", 11, 5, 150, Category.ELECTRONICS);
        testOwner_addProductToBid("1591591", 1, 11, 3);

        String result = testOwner_EndBid("1591591", 1, 11);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void AcceptBid() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "BidStore", "Fashion");

        String result = testOwner_AcceptBid("1591591", 1, 11, 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void BidStatus() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "BidStatusStore", "Tech");

        String result = testOwner_BidStatus("1591591", 1);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void addProductToRandom() throws Exception {
        testGuest_Enter();
        testGuest_Register("1591591", "storeOwner", "pass123", 30);
        testUser_LogIn("1591591", "storeOwner", "pass123");
        testUser_OpenStore("1591591", "RandomStore", "Gadgets");
        testOwner_ManageInventory_AddProduct(1, "1591591", 12, 10, 100, Category.ELECTRONICS);

        String result = testOwner_addProductToRandom("1591591", 1, 10, 12, 5, 20.0);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    // admin
    @Test
    void adminCloseStore() throws Exception {
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");
        testUser_setAdmin(admin, "adminUser");

        String storeOwner = testGuest_Enter();
        testGuest_Register(storeOwner, "storeOwner", "pass123", 30);
        testUser_LogIn(storeOwner, "storeOwner", "pass123");
        testUser_OpenStore(storeOwner, "AdminStore", "Office");

        String result = testAdmin_CloseStore(1, admin);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void RemoveUser() throws Exception {
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");
        testUser_setAdmin(admin, "adminUser");

        String userToRemove = testGuest_Enter();
        testGuest_Register(userToRemove, "badUser", "pass123", 25);
        testUser_LogIn(userToRemove, "badUser", "pass123");

        String result = testAdmin_RemoveUser(admin, "badUser");
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ViewSystemPurchaseHistory() throws Exception {
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");
        testUser_setAdmin(admin, "adminUser");

        String result = testAdmin_ViewSystemPurchaseHistory(admin);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ViewSystemInfo() throws Exception {
        String admin = testGuest_Enter();
        testGuest_Register(admin, "adminUser", "adminPass", 40);
        testUser_LogIn(admin, "adminUser", "adminPass");
        testUser_setAdmin(admin, "adminUser");

        String result = testAdmin_ViewSystemInfo(admin);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void ManageInventoryfailed() throws Exception {
        testGuest_Enter();
        testGuest_Register("ownerToken", "realOwner", "ownerPass", 30);
        testUser_LogIn("ownerToken", "realOwner", "ownerPass");
        testUser_OpenStore("ownerToken", "MainStore", "Books");

        int storeId = 1;
        int productId = 222;
        Category category = Category.ELECTRONICS;

        String outsider = testGuest_Enter();
        testGuest_Register(outsider, "outsider", "outPass", 22);
        testUser_LogIn(outsider, "outsider", "outPass");

        String result = testOwner_ManageInventory_AddProduct(storeId, outsider, productId, 5, 100, category);
        assertFalse(result.equals("Done"), "Unauthorized user should not be able to manage inventory");
    }
}
