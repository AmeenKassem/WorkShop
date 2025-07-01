package workshop.demo.UnitTests.UsersTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

//import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.*;

@Service
@SpringBootTest
@ActiveProfiles("test")
public class SuspendTests {

 @Autowired
    StoreTreeJPARepository tree;
    @Autowired
    private NodeJPARepository node;
    // @Autowired
    // private NotificationRepository notificationRepository;
   
    @Autowired
    private IStockRepoDB stockRepositoryjpa;
    @Autowired
    private IStoreRepoDB storeRepositoryjpa;
    @Autowired
    private IOrderRepoDB orderRepository;
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
    // ======================== Services ========================
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

    // ======================== Payment / Supply ========================
    @Autowired
    private PaymentServiceImp payment;
    @Autowired
    private SupplyServiceImp serviceImp;

    // ======================== Utility ========================
    @Autowired
    private Encoder encoder;

    // ======================== Test Data ========================
    //String NOToken;
    //String NGToken;
    //String GToken;
    //String Admin;
    //ItemStoreDTO itemStoreDTO;
    //int PID;

    //int createdStoreId;

    @BeforeEach
    void setup() throws Exception {
        node.deleteAll();
        orderRepository.deleteAll();
        tree.deleteAll();
        userRepo.deleteAll();

        guestRepo.deleteAll();

        stockRepositoryjpa.deleteAll();
        offerRepo.deleteAll();
        storeRepositoryjpa.deleteAll();
        storeStockRepo.deleteAll();

       
        suspensionRepo.deleteAll();
            orderRepository.deleteAll();
    }
    public SuspendTests() throws Exception {
    }

    @Test
    public void test_suspendRegisteredUser() throws Exception {
        System.out.println("nfdjjklhsdfljkdsfhjldsfkjdfs");
        String token = userService.generateGuest();
        userService.register(token, "admin1", "admin1", 22);
        String adminToken = userService.login(token, "admin1", "admin1");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String token2 = userService.generateGuest();
        userService.register(token2, "user1", "user1", 22);
        String userToken = userService.login(token2, "user1", "user1");

        int userId = authRepo.getUserId(userToken);
        suspensionService.suspendRegisteredUser(userId, 1, adminToken);
        assertTrue(suspensionService.isUserSuspended(userId));
        Thread.sleep(65000);
     userId = authRepo.getUserId(userToken);

        assertFalse(suspensionService.isUserSuspended(userId));
    }

    @Test
    public void test_suspendGuestUser() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin2", "admin2", 22);
        String adminToken = userService.login(token, "admin2", "admin2");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String guestToken = userService.generateGuest();
        int guestId = authRepo.getUserId(guestToken);

        suspensionService.suspendGuestUser(guestId, 1, adminToken);
        assertTrue(suspensionService.isUserSuspended(guestId));
        Thread.sleep(65000);
        assertFalse(suspensionService.isUserSuspended(guestId));
    }

    @Test
    public void test_pauseAndResumeSuspension_behavior() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin3", "admin3", 22);
        String adminToken = userService.login(token, "admin3", "admin3");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String guestToken = userService.generateGuest();
        int guestId = authRepo.getUserId(guestToken);

        userService.register(token, "sus", "sus", 22);
         guestToken = userService.login(guestToken, "sus", "sus");
         guestId = authRepo.getUserId(guestToken);

        suspensionService.suspendRegisteredUser(guestId, 2, adminToken);
        assertTrue(suspensionService.isUserSuspended(guestId));

        suspensionService.pauseSuspension(guestId, adminToken);
        assertFalse(suspensionService.isUserSuspended(guestId));

        suspensionService.resumeSuspension(guestId, adminToken);
        assertTrue(suspensionService.isUserSuspended(guestId));

        //Thread.sleep(65000);
       // assertFalse(suspensionService.isUserSuspended(guestId));
    }

    @Test
    public void test_suspendRegisteredUser_AlreadySuspended() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin4", "admin4", 22);
        String adminToken = userService.login(token, "admin4", "admin4");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String token2 = userService.generateGuest();
        userService.register(token2, "user2", "user2", 22);
        int userId = authRepo.getUserId(userService.login(token2, "user2", "user2"));

        suspensionService.suspendRegisteredUser(userId, 3, adminToken);

        UIException ex = assertThrows(UIException.class, () -> {
            suspensionService.suspendRegisteredUser(userId, 3, adminToken);
        });
        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getErrorCode());
    }

    @Test
    public void test_suspendGuestUser_AlreadySuspended() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin5", "admin5", 22);
        String adminToken = userService.login(token, "admin5", "admin5");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String guestToken = userService.generateGuest();
        int guestId = authRepo.getUserId(guestToken);

        suspensionService.suspendGuestUser(guestId, 3, adminToken);

        UIException ex = assertThrows(UIException.class, () -> {
            suspensionService.suspendGuestUser(guestId, 3, adminToken);
        });
        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getErrorCode());
    }

    @Test
    public void test_isUserSuspended_ReturnsCorrectStatus() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin6", "admin6", 22);
        String adminToken = userService.login(token, "admin6", "admin6");
        userService.setAdmin(adminToken, "123321", authRepo.getUserId(adminToken));

        String guestToken = userService.generateGuest();
        int guestId = authRepo.getUserId(guestToken);

        assertFalse(suspensionService.isUserSuspended(guestId));

        suspensionService.suspendGuestUser(guestId, 1, adminToken);
        assertTrue(suspensionService.isUserSuspended(guestId));
    }

    @Test
    public void test_failure_wrongAdminKey() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "admin7", "admin7", 22);
        String adminToken = userService.login(token, "admin7", "admin7");

        String token2 = userService.generateGuest();
        userService.register(token2, "user7", "user7", 22);
        int userId = authRepo.getUserId(userService.login(token2, "user7", "user7"));

        assertThrows(UIException.class, () -> {
            suspensionService.suspendRegisteredUser(userId, 2, "WRONG_TOKEN");
        });
        assertFalse(suspensionService.isUserSuspended(userId));
    }

    @Test
    void test_suspendGuestUser_UserIsNotAdmin_ThrowsException() throws Exception {
        // Arrange: Create a non-admin user
        String token = userService.generateGuest();
        userService.register(token, "nonadmin", "nonadmin", 22);
        String nonAdminToken = userService.login(token, "nonadmin", "nonadmin");
        int nonAdminId = authRepo.getUserId(nonAdminToken);

        // Create a guest to suspend
        String guestToken = userService.generateGuest();
        int guestId = authRepo.getUserId(guestToken);

        // Act + Assert: Try to suspend using non-admin token
        UIException ex = assertThrows(UIException.class, () -> {
            suspensionService.suspendGuestUser(guestId, 1, nonAdminToken);
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getErrorCode());
        assertEquals("Only admins can suspend.", ex.getMessage());
    }
}
