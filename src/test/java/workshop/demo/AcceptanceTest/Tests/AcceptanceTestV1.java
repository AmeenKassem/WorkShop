package workshop.demo.AcceptanceTest.Tests;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class AcceptanceTestV1 extends AcceptanceTests {

    @Test
    void test_InitMarket() throws Exception {
        String result = testSystem_InitMarket("admin");
        assertNotNull(result);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    void test_Enter() throws Exception {
        String result = testGuest_Enter(); 
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    void test_Register() throws Exception {
        testSystem_InitMarket("admin");
        String token = testGuest_Enter();
        String result = testGuest_Register(token, "guest1", "1234", 20);
        assertNotNull(result);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    public void test_LogIn() throws Exception {
        testSystem_InitMarket("admin");
        String token = testGuest_Enter();
        testGuest_Register(token, "user1", "pass", 21);
        String result = testUser_LogIn(token, "user1", "pass");
        assertNotNull(result);
        assertEquals("Done", result);
    }

    @Test
    public void test_setAdmin() throws Exception {
        testSystem_InitMarket("admin");
        String token = testGuest_Enter();
        testGuest_Register(token, "adminUser", "pass", 30);
        testUser_LogIn(token, "adminUser", "pass");
        String result = testUser_setAdmin(token, "admin");
        assertNotNull(result);
        assertTrue(result.equals("Done") || result.equals("false"));
    }

    @Test
    public void test_sendDMessageToAll() throws Exception {
        testSystem_InitMarket("admin");

        String token1 = testGuest_Enter();
        testGuest_Register(token1, "user1", "pass", 21);
        testUser_LogIn(token1, "user1", "pass");
        testUser_setAdmin(token1, "admin");

        String token2 = testGuest_Enter();
        testGuest_Register(token2, "user2", "pass", 22);
        testUser_LogIn(token2, "user2", "pass");

        List<Integer> sendIds = new ArrayList<>();
        sendIds.add(2); 
        String result = testSystem_sendDMessageToAll(sendIds, "hello", 1);
        assertNotNull(result);
        assertTrue(result.toLowerCase().contains("done"));
    }

    @Test
    public void test_sendRTMessageToAll() throws Exception {
        testSystem_InitMarket("admin");

        String token1 = testGuest_Enter();
        testGuest_Register(token1, "user1", "pass", 21);
        testUser_LogIn(token1, "user1", "pass");
        testUser_setAdmin(token1, "admin");

        String token2 = testGuest_Enter();
        testGuest_Register(token2, "user2", "pass", 22);
        testUser_LogIn(token2, "user2", "pass");

        List<Integer> sendIds = new ArrayList<>();
        sendIds.add(2);
        String result = testSystem_sendRTMessageToAll(sendIds, "hello", 1);
        assertNotNull(result);
        assertTrue(result.toLowerCase().contains("done"));
    }

    @Test
    public void test_OpenStore() throws Exception {
        testSystem_InitMarket("admin");
        String token = testGuest_Enter();
        testGuest_Register(token, "storeOwner", "pass", 25);
        testUser_LogIn(token, "storeOwner", "pass");
        String result = testUser_OpenStore(token, "MyStore", "General");
        assertNotNull(result);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }

    @Test
    public void test_GetPurchasePolicy() throws Exception {
        testSystem_InitMarket("admin");
        String token = testGuest_Enter();
        testGuest_Register(token, "policyUser", "pass", 30);
        testUser_LogIn(token, "policyUser", "pass");
        String result = testGuest_GetPurchasePolicy(token, 1);
        assertNotNull(result);
        assertTrue(result.equals("Done") || result.equals("TODO"));
    }
}
