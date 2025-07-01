package workshop.demo.ConcurrencyTests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.ApplicationLayer.*;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class SuspendconTests {

    // ============ Autowired Repositories & Services ============
    @Autowired
    private StoreTreeJPARepository tree;
    @Autowired
    private NodeJPARepository node;

    //@Autowired private StockRepository stockRepository;
    @Autowired
    private IStockRepoDB stockRepositoryjpa;
    @Autowired
    private IStoreRepoDB storeRepositoryjpa;
    // @Autowired
    // private PurchaseRepository purchaseRepository;
    @Autowired
    private UserSuspensionJpaRepository suspensionRepo;
    @Autowired
    private AuthenticationRepo authRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private SUConnectionRepository sIsuConnectionRepo;
    @Autowired
    private GuestJpaRepository guestRepo;
    @Autowired
    private IStoreStockRepo storeStockRepo;
    @Autowired
    private OfferJpaRepository offerRepo;

    @Autowired
    private UserService userService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private StockService stockService;
    @Autowired
    private PurchaseService purchaseService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserSuspensionService suspensionService;

    @Autowired
    private PaymentServiceImp payment;
    @Autowired
    private SupplyServiceImp serviceImp;
    @Autowired
    private Encoder encoder;

    public SuspendconTests() throws Exception {
    }

    @BeforeEach
    void setup() {
        node.deleteAll();
        tree.deleteAll();
        userRepo.deleteAll();
        guestRepo.deleteAll();
        stockRepositoryjpa.deleteAll();
        offerRepo.deleteAll();
        storeRepositoryjpa.deleteAll();
        storeStockRepo.deleteAll();
        //suspensionRepo.deleteAll();
    }

    @Test
    @Order(1)
    public void test_twoAdminsSuspend_twoUsers() throws Exception {
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA21", "passA2", 30);
        String adminTokenA = userService.login(tokenA, "adminA21", "passA2");

        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB21", "passB2", 22);
        String adminTokenB = userService.login(tokenB, "adminB21", "passB2");

        userService.setAdmin(adminTokenA, "123321", authRepo.getUserId(adminTokenA));
        userService.setAdmin(adminTokenB, "123321", authRepo.getUserId(adminTokenB));

        String tokenA1 = userService.generateGuest();
        userService.register(tokenA, "userA", "pass", 30);
        String userTokenA = userService.login(tokenA, "userA", "pass");

        String tokenB1 = userService.generateGuest();
        userService.register(tokenB, "userB", "pass", 30);
        String userTokenB = userService.login(tokenB, "userB", "pass");

        AtomicInteger successCount = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(authRepo.getUserId(userTokenA), 1, adminTokenA);
                successCount.incrementAndGet();
            } catch (UIException ignored) {
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(authRepo.getUserId(userTokenB), 1, adminTokenB);
                successCount.incrementAndGet();
            } catch (UIException ignored) {
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2, successCount.get());
        assertTrue(suspensionService.isUserSuspended(authRepo.getUserId(userTokenA)));
        assertTrue(suspensionService.isUserSuspended(authRepo.getUserId(userTokenB)));

        // Thread.sleep(65000);

        // assertFalse(suspensionService.isUserSuspended(authRepo.getUserId(userTokenA)));
        // assertFalse(suspensionService.isUserSuspended(authRepo.getUserId(userTokenB)));
    }

    @Test
    @Order(2)
    public void test_twoAdminsTryToSuspendSameUser() throws Exception {
        String tokenA = userService.generateGuest();
        userService.register(tokenA, "adminA2", "passA2", 30);
        String adminTokenA = userService.login(tokenA, "adminA2", "passA2");

        String tokenB = userService.generateGuest();
        userService.register(tokenB, "adminB2", "passB2", 30);
        String adminTokenB = userService.login(tokenB, "adminB2", "passB2");

        userService.setAdmin(adminTokenA, "123321", authRepo.getUserId(adminTokenA));
        userService.setAdmin(adminTokenB, "123321", authRepo.getUserId(adminTokenB));

        String token = userService.generateGuest();
        userService.register(token, "sharedUser", "pass", 30);
        String sharedUserToken = userService.login(token, "sharedUser", "pass");

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(authRepo.getUserId(sharedUserToken), 1, adminTokenA);
                successCount.incrementAndGet();
            } catch (UIException e) {
                failureCount.incrementAndGet();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                suspensionService.suspendRegisteredUser(authRepo.getUserId(sharedUserToken), 1, adminTokenB);
                successCount.incrementAndGet();
            } catch (UIException e) {
                failureCount.incrementAndGet();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(1, successCount.get());
        assertEquals(1, failureCount.get());
        assertTrue(suspensionService.isUserSuspended(authRepo.getUserId(sharedUserToken)));

        // Thread.sleep(65000);

        // assertFalse(suspensionService.isUserSuspended(authRepo.getUserId(sharedUserToken)));
        //.
    }
}
