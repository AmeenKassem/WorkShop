package workshop.demo.UnitTests.UsersTests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;

@SpringBootTest
public class AuthentecationTests {

    private final IAuthRepo auth = new AuthenticationRepo();

    @Test
    public void testAuth() throws UIException {
        int userId = 42;
        String userName = "test_user";

        String token = auth.generateUserToken(userId, userName);

        // Assert token is not null
        Assertions.assertNotNull(token);

        // Assert token is valid
        Assertions.assertTrue(auth.validToken(token));

        // Assert correct data extracted
        Assertions.assertEquals(userName, auth.getUserName(token));
        Assertions.assertEquals(userId, auth.getUserId(token));

        // Assert isRegistered works
        Assertions.assertTrue(auth.isRegistered(token));
    }

    @Test
    public void testGuestToken() throws UIException {
        int guestId = 99;

        String guestToken = auth.generateGuestToken(guestId);

        // Assert token is valid
        Assertions.assertTrue(auth.validToken(guestToken));

        // Username should be null for guest
        Assertions.assertNull(auth.getUserName(guestToken));

        // ID should match
        Assertions.assertEquals(guestId, auth.getUserId(guestToken));

        // Should return false for isRegistered
        Assertions.assertFalse(auth.isRegistered(guestToken));
    }
}
