// package workshop.demo.UnitTests.Users;

// import org.junit.jupiter.api.Assertions;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.stereotype.Service;

// import workshop.demo.ApplicationLayer.UserService;
// import workshop.demo.ApplicationLayer.UserSuspensionService;
// import workshop.demo.DomainLayer.Authentication.IAuthRepo;
// import workshop.demo.DomainLayer.Exceptions.UIException;
// import workshop.demo.DomainLayer.User.AdminInitilizer;
// import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.InfrastructureLayer.AuthenticationRepo;
// import workshop.demo.InfrastructureLayer.Encoder;
// import workshop.demo.InfrastructureLayer.StockRepository;
// import workshop.demo.InfrastructureLayer.UserRepository;
// import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

// @Service
// @SpringBootTest
// public class Suspendtests {

//    private final UserSuspensionRepo suspensionRepo = new UserSuspensionRepo();
//    private final IAuthRepo authRepo = new AuthenticationRepo();
//    private final Encoder encoder = new Encoder();
//    private final AdminInitilizer adminInitilizer = new AdminInitilizer("123321");
//    private final IUserRepo userRepo = new UserRepository(encoder, adminInitilizer);
//    private final UserSuspensionService suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);
//    private final UserService userService = new UserService(userRepo, authRepo,new StockRepository());

//    @Test
//    public void test_suspendRegisteredUser() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminUser2", "adminPass2",22);
//        String token1 = userService.login(token, "adminUser2", "adminPass2");
//        userService.setAdmin(token1, "123321", 2);

//        int userId = userRepo.registerUser("suspendedUser", "pass123",22);

//        suspensionService.suspendRegisteredUser(userId, 1, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(userId));
//        Thread.sleep(60_000);
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId));
//    }

//    @Test
//    public void test_suspendGuestUser() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminUser3", "adminPass3",22);
//        String token1 = userService.login(token, "adminUser3", "adminPass3");
//        userService.setAdmin(token1, "123321", 2);

//        int guestId = userRepo.generateGuest();

//        suspensionService.suspendGuestUser(guestId, 1, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(guestId));
//        Thread.sleep(60_000);
//        Assertions.assertFalse(suspensionService.isUserSuspended(guestId));
//    }

//    @Test
//    public void test_pauseAndResumeSuspension_behavior() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminUser7", "adminPass7",22);
//        String token1 = userService.login(token, "adminUser7", "adminPass7");
//        userService.setAdmin(token1, "123321", 2);

//        int userId = userRepo.registerUser("basicPauseUser", "pass123",22);

//        suspensionService.suspendRegisteredUser(userId, 10, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(userId));

//        suspensionService.pauseSuspension(userId, token1);
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId));

//        suspensionService.resumeSuspension(userId, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(userId));
//    }

//    @Test
//    public void test_pausePreventsExpirationAndRemovesAfterResume() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminUser10", "adminPass10",22);
//        String token1 = userService.login(token, "adminUser10", "adminPass10");
//        userService.setAdmin(token1, "123321", 2);

//        int userId = userRepo.registerUser("pausePreventExpireUser", "pass123",22);

//        suspensionService.suspendRegisteredUser(userId, 1, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(userId));

//        suspensionService.pauseSuspension(userId, token1);
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId));

//        Thread.sleep(65_000);
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId));

//        suspensionService.resumeSuspension(userId, token1);
//        Assertions.assertTrue(suspensionService.isUserSuspended(userId));

//        Thread.sleep(65_000);
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId));
//    }

//    // -------- INTENTIONAL FAILURES (with new structure) --------
// // suspending wrong user
//    @Test
//    public void test_failure_userNotSuspendedButExpectedToBe() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminFail1", "failPass1",22);
//        String token1 = userService.login(token, "adminFail1", "failPass1");
//        userService.setAdmin(token1, "123321", 2);

//        int userId = userRepo.registerUser("failUser1", "failPass1",22);
//        suspensionService.suspendRegisteredUser(10, 1, token1);

//        Assertions.assertFalse(suspensionService.isUserSuspended(userId),
//                "FAIL: User was never suspended but expected to be suspended.");
//    }

//    // using wrong admin key
//    @Test
//    public void test_failure_wrongAdminKey() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "adminFail2", "failPass2",22);
//        String token1 = userService.login(token, "adminFail2", "failPass2");
//        userService.setAdmin(token1, "1233321", 2);

//        int userId = userRepo.registerUser("failUser2", "failPass2",22);

//        // Attempting with wrong token (simulated as "WRONG_TOKEN")
//        assertThrows(UIException.class, () -> {
//            suspensionService.suspendRegisteredUser(userId, 5, "WRONG_TOKEN");
//        }, "Expected UIException to be thrown due to invalid admin token");
//        Assertions.assertFalse(suspensionService.isUserSuspended(userId),
//                "FAIL: Suspension should not occur with wrong admin token.");
//    }

// }
