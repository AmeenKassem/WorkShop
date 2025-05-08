package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Exceptions.UIException;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserAT extends AcceptanceTests {
    private Real real;

    @BeforeEach
    void setup() throws Exception {
        real = new Real();

        // Setup admin
        Mockito.when(real.mockAuthRepo.validToken("admin-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("admin-token")).thenReturn(999);
        Mockito.when(real.mockAuthRepo.getUserName("admin-token")).thenReturn("admin");
        Mockito.when(real.mockUserRepo.isRegistered(999)).thenReturn(true);
        Mockito.when(real.mockUserRepo.isOnline(999)).thenReturn(true);
        testSystem_InitMarket("admin-token");

        // Setup user
        Mockito.when(real.mockAuthRepo.validToken("user-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("user-token")).thenReturn(1);
        Mockito.when(real.mockAuthRepo.getUserName("user-token")).thenReturn("user1");
        Mockito.when(real.mockUserRepo.registerUser("user1", "pass")).thenReturn(1);
        Mockito.when(real.mockUserRepo.login("user1", "pass")).thenReturn(1);
    }

    @Test
    void testUser_LogIn_Success() throws Exception {
        Mockito.when(real.mockAuthRepo.generateUserToken(1, "user1")).thenReturn("user-token");
        String result = real.testUser_LogIn("user-token", "user1", "pass");
        assertEquals("Done", result);
    }

    @Test
    void testUser_LogIn_Failure_WrongPassword() throws UIException {
        Mockito.when(real.mockUserRepo.login("user1", "wrongpass"))
                .thenThrow(new IllegalArgumentException("Incorrect password"));

        Exception ex = assertThrows(
                IllegalArgumentException.class,
                () -> real.testUser_LogIn("user-token", "user1", "wrongpass")
        );
        assertEquals("Incorrect password", ex.getMessage());
    }

    @Test
    void testUser_LogOut_Success() throws Exception {
        Mockito.when(real.mockUserRepo.logoutUser("user1")).thenReturn(1);
        String result = real.testUser_LogOut("user-token");
        assertEquals("Done", result);
    }

    @Test
    void testUser_LogOut_Failure() throws Exception {
        Mockito.when(real.mockUserRepo.logoutUser("user1")).thenReturn(0);
        String result = real.testUser_LogOut("user-token");
        assertEquals("Done", result); // Adjust if your service gives a failure message
    }

    @Test
    void testUser_setAdmin_Success() throws Exception {
        Mockito.when(real.mockUserRepo.logoutUser("user1")).thenReturn(42);
        Mockito.when(real.mockUserRepo.setUserAsAdmin(42, "newAdminUser")).thenReturn(true);
        String result = real.testUser_setAdmin("user-token", "newAdminUser");
        assertEquals("Done", result);
    }

    @Test
    void testUser_setAdmin_Failure_InvalidToken() {
        Mockito.when(real.mockAuthRepo.validToken("badToken")).thenReturn(false);
        Exception ex = assertThrows(
                Exception.class,
                () -> real.testUser_setAdmin("badToken", "someone")
        );
        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testUser_CheckPurchaseHistory() throws Exception {
        List<ReceiptProduct> productList = List.of(
                new ReceiptProduct("item1", Category.ELECTRONICS, "PERFECT", "FREES", 5, 10)
        );
        List<ReceiptDTO> receiptList = List.of(
                new ReceiptDTO("MyStore", "2025-05-07", productList, 20.0)
        );

        Mockito.when(real.mockUserRepo.isRegistered(1)).thenReturn(true);
        Mockito.when(real.mockOrderRepo.getReceiptDTOsByUser(1)).thenReturn(receiptList);

        String result = real.testUser_CheckPurchaseHistory("user-token");
        assertFalse(result.equals("[]"));
    }

    @Test
    void testUser_CheckPurchaseHistory_Failure() throws Exception {
        Mockito.when(real.mockUserRepo.isRegistered(1)).thenReturn(true);
        Mockito.when(real.mockOrderRepo.getReceiptDTOsByUser(1)).thenReturn(new LinkedList<>());

        String result = real.testUser_CheckPurchaseHistory("user-token");
        assertTrue(result.equals("[]"));
    }
}
