package workshop.demo.AcceptanceTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import workshop.demo.AcceptanceTest.Utill.Bridge;
import workshop.demo.AcceptanceTest.Utill.Proxy;

public class AcceptanceTests {

    static Bridge bridge;

    @BeforeAll
    public static void setUpClass() throws Exception {
        bridge = new Proxy();
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /////////////////////// System /////////////////////////////

    @Test
    public void testSystem_InitMarket() throws Exception {
        String result = bridge.testSystem_InitMarket("admin");
        assertNotNull(result);
        assertTrue(result.contains("Done") || result.contains("Success"));
    }

    @Test
    public void testSystem_sendDMessageToAll() {
        /* not for now */ }

    @Test
    public void testSystem_sendRTMessageToAll() {
        /* not for now */ }

    /////////////////////// Guest /////////////////////////////

    @Test
    public void testGuest_Enter() throws Exception {
        String token = bridge.testGuest_Enter();
        assertNotNull(token);
    }

    @Test
    public void testGuest_Exit() {
        /* not for now */ }

    @Test
    public void testGuest_Register() throws Exception {
        String token = bridge.testGuest_Enter();
        bridge.testGuest_Register(token, "testuser", "1234", 22);
    }

    @Test
    public void testGuest_GetStoreInfo() {
        /* not for now */ }

    @Test
    public void testGuest_GetProductInfo() {
        /* not for now */ }

    @Test
    public void testGuest_SearchProduct() {
        /* not for now */ }

    @Test
    public void testGuest_SearchProductInStore() {
        /* not for now */ }

    @Test
    public void testGuest_AddProductToCart() {
        /* not for now */ }

    @Test
    public void testGuest_ModifyCart() {
        /* not for now */ }

    @Test
    public void testGuest_BuyCart() {
        /* not for now */ }

    //////////////////////////// User ////////////////////////////

    @Test
    public void testUser_LogIn() throws Exception {
        String token = bridge.testGuest_Enter();
        bridge.testGuest_Register(token, "user2", "pass", 21);
        String loggedIn = bridge.testUser_LogIn(token, "user2", "pass");
        assertNotNull(loggedIn);
    }

    @Test
    public void testUser_LogOut() {
        /* not for now */ }

    @Test
    public void testUser_OpenStore() throws Exception {
        String token = bridge.testGuest_Enter();
        bridge.testGuest_Register(token, "owner", "pass", 25);
        bridge.testUser_LogIn(token, "owner", "pass");
        bridge.testUser_OpenStore(token, "MyStore", "General");
    }

    @Test
    public void testUser_AddReviewToStore() {
        /* not for now */ }

    @Test
    public void testUser_AddReviewToProduct() {
        /* not for now */ }

    @Test
    public void testUser_RateProduct() {
        /* not for now */ }

    @Test
    public void testUser_RateStore() {
        /* not for now */ }

    @Test
    public void testUser_SendMessageToStoreOwner() {
        /* not for now */ }

    @Test
    public void testUser_SendMessageToAdmin() {
        /* not for now */ }

    @Test
    public void testUser_CheckPurchaseHistory() {
        /* not for now */ }

    @Test
    public void testUser_updateProfile() {
        /* not for now */ }

    @Test
    public void testUser_AddBid() {
        /* not for now */ }

    @Test
    public void testUser_JoinAuction() {
        /* not for now */ }

    @Test
    public void testUser_JoinRandom() {
        /* not for now */ }

    @Test
    public void testUser_setAdmin() {
        /* not for now */ }

    //////////////////////////// Owner ////////////////////////////

    @Test
    public void testOwner_ManageInventory_AddProduct() {
        /* not for now */ }

    @Test
    public void testOwner_ManageInventory_RemoveProduct() {
        /* not for now */ }

    @Test
    public void testOwner_ManageInventory_UpdateProductDetails() {
        /* not for now */ }

    @Test
    public void testOwner_SetPurchasePolicies() {
        /* not for now */ }

    @Test
    public void testOwner_SetDiscountPolicies() {
        /* not for now */ }

    @Test
    public void testOwner_AssignNewOwner() {
        /* not for now */ }

    @Test
    public void testOwner_RemoveOwner() {
        /* not for now */ }

    @Test
    public void testOwner_AssignManager() {
        /* not for now */ }

    @Test
    public void testOwner_EditManagerPermissions() {
        /* not for now */ }

    @Test
    public void testOwner_RemoveManager() {
        /* not for now */ }

    @Test
    public void testOwner_CloseStore() {
        /* not for now */ }

    @Test
    public void testOwner_ReopenStore() {
        /* not for now */ }

    @Test
    public void testOwner_ViewRolesAndPermissions() {
        /* not for now */ }

    @Test
    public void testOwner_ReceiveNotifications() {
        /* not for now */ }

    @Test
    public void testOwner_ReplyToMessages() {
        /* not for now */ }

    @Test
    public void testOwner_ViewStorePurchaseHistory() {
        /* not for now */ }

    //////////////////////////// Admin ////////////////////////////

    @Test
    public void testAdmin_CloseStore() {
        /* not for now */ }

    @Test
    public void testAdmin_RemoveUser() {
        /* not for now */ }

    @Test
    public void testAdmin_ViewSystemPurchaseHistory() {
        /* not for now */ }

    @Test
    public void testAdmin_ViewSystemInfo() {
        /* not for now */ }

    //////////////////////////// Extra (Required by Rule 6)
    //////////////////////////// ////////////////////////////

    @Test
    public void testGuest_GetPurchasePolicy() throws Exception {
        String token = bridge.testGuest_Enter();
        bridge.testGuest_Register(token, "policyUser", "pass", 30);
        bridge.testUser_LogIn(token, "policyUser", "pass");
        bridge.testUser_OpenStore(token, "PolicyStore", "Books");

        String policy = bridge.testGuest_GetPurchasePolicy(token, 1); // assuming store ID = 1
        assertNotNull(policy);
        assertFalse(policy.isEmpty());
        assertTrue(policy.toLowerCase().contains("default") || policy.toLowerCase().contains("no restriction"));
    }
}
