package workshop.demo.ConcurrencyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

import java.util.concurrent.atomic.AtomicInteger;

// @Service
@SpringBootTest
public class Suspendtestscon {

    private final UserSuspensionRepo suspensionRepo = new UserSuspensionRepo();
    private final IAuthRepo authRepo = new AuthenticationRepo();
    private final Encoder encoder = new Encoder();
    private final String adminKey = "123321";

    private final AdminInitilizer adminInitilizer = new AdminInitilizer(adminKey);
    private final IUserRepo userRepo = new UserRepository(encoder, adminInitilizer);
    private final UserSuspensionService suspensionService = new UserSuspensionService(suspensionRepo, userRepo,
            authRepo);
    private final UserService userService = new UserService(userRepo, authRepo);

    @Test
    public void test_twoAdminsSuspend_twoUsers() throws Exception {
        // Admin A setup
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA2", "passA2");
        String adminTokenA = userService.login(tokenA, "adminA2", "passA2");

        // Admin B setup
        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB2", "passB2");
        String adminTokenB = userService.login(tokenB, "adminB2", "passB2");

        // Set both as admin with error checks
        try {
            boolean successA = userService.setAdmin(adminTokenA, adminKey, 4);
            System.out.println("Admin A setAdmin result: " + successA);
            Assertions.assertTrue(successA, "Admin A failed to become admin!");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Admin A setAdmin threw exception: " + e.getMessage());
        }

        try {
            boolean successB = userService.setAdmin(adminTokenB, adminKey, 2);
            System.out.println("Admin B setAdmin result: " + successB);
            Assertions.assertTrue(successB, "Admin B failed to become admin!");
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("Admin B setAdmin threw exception: " + e.getMessage());
        }

        // Register two users
        int userA = userRepo.registerUser("userA", "pass");
        int userB = userRepo.registerUser("userB", "pass");

        // Atomic counter for success tracking
        AtomicInteger successCount = new AtomicInteger(0);

        // Suspend userA by adminA
        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(userA, 1, adminTokenA);
                successCount.incrementAndGet();
                System.out.println("Admin A suspended User A");
            } catch (UIException e) {
                e.printStackTrace();
                System.out.println("Admin A failed to suspend User A: " + e.getMessage());
            }
        });

        // Suspend userB by adminB
        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(userB, 1, adminTokenB);
                successCount.incrementAndGet();
                System.out.println("Admin B suspended User B");
            } catch (UIException e) {
                e.printStackTrace();
                System.out.println("Admin B failed to suspend User B: " + e.getMessage());
            }
        });

        // Start and wait for both threads
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Check suspensions
        System.out.println("Suspension success count: " + successCount.get());
        Assertions.assertEquals(2, successCount.get(), "Both admins should have suspended the users");
        Assertions.assertTrue(suspensionService.isUserSuspended(userA), "User A should be suspended");
        Assertions.assertTrue(suspensionService.isUserSuspended(userB), "User B should be suspended");

        // Wait for suspensions to expire
        Thread.sleep(65_000);

        // Verify suspensions expired
        Assertions.assertFalse(suspensionService.isUserSuspended(userA), "User A's suspension should expire");
        Assertions.assertFalse(suspensionService.isUserSuspended(userB), "User B's suspension should expire");
    }

    @Test
    public void test_twoAdminsTryToSuspendSameUser() throws Exception {
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA2", "passA2");
        String adminTokenA = userService.login(tokenA, "adminA2", "passA2");

        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB2", "passB2");
        String adminTokenB = userService.login(tokenB, "adminB2", "passB2");

        userService.setAdmin(adminTokenA, adminKey, 2);
        userService.setAdmin(adminTokenB, adminKey, 4);

        int user = userRepo.registerUser("sharedUser", "pass");

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(user, 1, adminTokenA);
                successCount.incrementAndGet();
                System.out.println("Admin A successfully suspended the user.");
            } catch (UIException e) {
                failureCount.incrementAndGet();
                System.out.println("Admin A failed: " + e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(user, 1, adminTokenB);
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

        // Only one admin should succeed, one should fail
        Assertions.assertEquals(1, successCount.get(), "Only one admin should be able to suspend the user");
        Assertions.assertEquals(1, failureCount.get(), "One admin should fail due to already suspended");

        Assertions.assertTrue(suspensionService.isUserSuspended(user), "User should be suspended");

        Thread.sleep(65_000); // Wait for suspension to expire

        Assertions.assertFalse(suspensionService.isUserSuspended(user), "User suspension should expire");
    }

}
