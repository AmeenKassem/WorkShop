 package workshop.demo.ConcurrencyTests;

 import java.util.concurrent.atomic.AtomicInteger;

 import org.junit.jupiter.api.Assertions;
 import org.junit.jupiter.api.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.context.SpringBootTest;

 import workshop.demo.ApplicationLayer.*;
 import workshop.demo.DomainLayer.Exceptions.UIException;
 import workshop.demo.InfrastructureLayer.*;

 @SpringBootTest
 public class SuspendconTests
 {

     @Autowired
     private NotificationRepository notificationRepository;
     @Autowired
     private StoreRepository storeRepository;
     @Autowired
     private StockRepository stockRepository;
     @Autowired
     private OrderRepository orderRepository;
     @Autowired
     private PurchaseRepository purchaseRepository;
     @Autowired
     private UserSuspensionRepo suspensionRepo;
     @Autowired
     private AuthenticationRepo authRepo;

     @Autowired
     PaymentServiceImp payment;
     @Autowired
     SupplyServiceImp serviceImp;

     @Autowired
     SUConnectionRepository sIsuConnectionRepo;

     @Autowired
     Encoder encoder;
     @Autowired
     UserRepository userRepo;
     @Autowired
     UserSuspensionService suspensionService;
     @Autowired
     AdminHandler adminService;
     @Autowired
     UserService userService;
     @Autowired
     StockService stockService;
     @Autowired
     StoreService storeService;
     @Autowired
     PurchaseService purchaseService;
     @Autowired
     OrderService orderService;


     public SuspendconTests() throws Exception {
     }

     @Test
     public void test_twoAdminsSuspend_twoUsers() throws Exception {
         // Admin A setup
         String tokenA = userService.generateGuest();
         userService.register(tokenA, "adminA21", "passA2", 30);
         String adminTokenA = userService.login(tokenA, "adminA21", "passA2");

         // Admin B setup
         String tokenB = userService.generateGuest();
         userService.register(tokenB, "adminB21", "passB2", 22);
         String adminTokenB = userService.login(tokenB, "adminB21", "passB2");

         userService.setAdmin(adminTokenA, "123321", authRepo.getUserId(adminTokenA));
         userService.setAdmin(adminTokenB, "123321", authRepo.getUserId(adminTokenB));

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

         userService.setAdmin(adminTokenA, "123321", authRepo.getUserId(adminTokenA));
         userService.setAdmin(adminTokenB, "123321", authRepo.getUserId(adminTokenB));

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
