package workshop.demo;



import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.IncorrectLogin;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

@Service
@SpringBootTest
public class Suspendtests {
    private UserSuspensionRepo sss = new UserSuspensionRepo();
    private IAuthRepo auth = new AuthenticationRepo();
    private Encoder enc = new Encoder();
    private String adminKey = "123321";

    @Autowired
    private AdminInitilizer a;

    private IUserRepo userRepo = new UserRepository(enc, a);
    private UserSuspensionService ss = new UserSuspensionService(sss, userRepo, auth);

    private int goodLogin(String username, String password) {
        int userIdFromRegister = userRepo.registerUser(username, password);
        int userIdFromLogIn = userRepo.login(username, password);
        return userIdFromLogIn;
    }

    

    @Test
    public void test_suspendRegisteredUser() throws InterruptedException {
        // need to fix admin
        int adminId = goodLogin("adminUser2", "adminPass2");
        int userId = userRepo.registerUser("suspendedUser", "pass123");

        // Suspend for 0 minutes (simulate immediate suspension for test)
        ss.suspendRegisteredUser("suspendedUser", 1, "adminToken");

        boolean isSuspended = ss.isUserSuspended(null, "suspendedUser");
        Assertions.assertTrue(isSuspended, "User should be suspended");

        System.out.println("Waiting 10 seconds for suspension to expire...");
        Thread.sleep(60_000);  // wait 10 seconds, make sure scheduler has time to clean

        boolean stillSuspended = ss.isUserSuspended(null, "suspendedUser");
        Assertions.assertFalse(stillSuspended, "User should no longer be suspended");
    }

    @Test
    public void test_suspendGuestUser() throws InterruptedException {
        //need to fix admin
        int adminId = goodLogin("adminUser3", "adminPass3");
        int guestId = userRepo.generateGuest();

        ss.suspendGuestUser(guestId, 1, "adminToken");

        boolean isSuspended = ss.isUserSuspended(guestId, null);
        Assertions.assertTrue(isSuspended, "Guest should be suspended");

        System.out.println("Waiting 10 seconds for suspension to expire...");
        Thread.sleep(60_000);

        boolean stillSuspended = ss.isUserSuspended(guestId, null);
        Assertions.assertFalse(stillSuspended, "Guest should no longer be suspended");
    }
}
