package workshop.demo.ConcurrencyTest;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

@SpringBootTest
public class Suspendtestscon {

    private final UserSuspensionRepo suspensionRepo = new UserSuspensionRepo();
    private final IAuthRepo authRepo = new AuthenticationRepo();
    private final Encoder encoder = new Encoder();
    private final String adminKey = "123321";

    private final AdminInitilizer adminInitilizer = new AdminInitilizer(adminKey);
    private final IUserRepo userRepo = new UserRepository(encoder, adminInitilizer);

    private final UserSuspensionService suspensionService = new UserSuspensionService(suspensionRepo, userRepo, authRepo);
    private final UserService userService = new UserService(userRepo, authRepo, new StockRepository(),adminInitilizer,new AdminService(new OrderRepository(), new StoreRepository(), userRepo, authRepo));

    public Suspendtestscon() throws Exception {
    }

    @Test
    public void test_twoAdminsSuspend_twoUsers() throws Exception {
        // Admin A setup
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA2", "passA2", 30);
        String adminTokenA = userService.login(tokenA, "adminA2", "passA2");

        // Admin B setup
        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB2", "passB2", 22);
        String adminTokenB = userService.login(tokenB, "adminB2", "passB2");

        userService.setAdmin(adminTokenA, adminKey, 4);
        userService.setAdmin(adminTokenB, adminKey, 2);

        // Register two users
        int userA = userRepo.registerUser("userA", "pass", 30);
        int userB = userRepo.registerUser("userB", "pass", 30);

        AtomicInteger successCount = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(userA, 2, adminTokenA);
                successCount.incrementAndGet();
                System.out.println("Admin A suspended User A");
            } catch (UIException e) {
                System.out.println("Admin A failed to suspend User A: " + e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(userB, 2, adminTokenB);
                successCount.incrementAndGet();
                System.out.println("Admin B suspended User B");
            } catch (UIException e) {
                System.out.println("Admin B failed to suspend User B: " + e.getMessage());
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Assertions.assertEquals(2, successCount.get(), "Both admins should have suspended the users");
        Assertions.assertTrue(suspensionService.isUserSuspended(userA), "User A should be suspended");
        Assertions.assertTrue(suspensionService.isUserSuspended(userB), "User B should be suspended");

        Thread.sleep(2100); // wait for 2 seconds suspensions to expire

        Assertions.assertFalse(suspensionService.isUserSuspended(userA), "User A's suspension should expire");
        Assertions.assertFalse(suspensionService.isUserSuspended(userB), "User B's suspension should expire");
    }

    @Test
    public void test_twoAdminsTryToSuspendSameUser() throws Exception {
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA2", "passA2", 30);
        String adminTokenA = userService.login(tokenA, "adminA2", "passA2");

        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB2", "passB2", 30);
        String adminTokenB = userService.login(tokenB, "adminB2", "passB2");

        userService.setAdmin(adminTokenA, adminKey, 2);
        userService.setAdmin(adminTokenB, adminKey, 4);

        int user = userRepo.registerUser("sharedUser", "pass", 30);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(user, 2, adminTokenA);
                successCount.incrementAndGet();
                System.out.println("Admin A successfully suspended the user.");
            } catch (UIException e) {
                failureCount.incrementAndGet();
                System.out.println("Admin A failed: " + e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(user, 2, adminTokenB);
                successCount.incrementAndGet();
                System.out.println("Admin B successfully suspended the user.");
            } catch (UIException e) {
                failureCount.incrementAndGet();
                System.out.println("Admin B failed: " + e.getMessage());
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        Assertions.assertEquals(1, successCount.get(), "Only one admin should be able to suspend the user");
        Assertions.assertEquals(1, failureCount.get(), "One admin should fail due to already suspended");

        Assertions.assertTrue(suspensionService.isUserSuspended(user), "User should be suspended");

        Thread.sleep(2100); // Wait for 2-second suspension to expire

        Assertions.assertFalse(suspensionService.isUserSuspended(user), "User suspension should expire");
    }
}
