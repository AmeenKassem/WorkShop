package workshop.demo.IntegrationTests.ServiceTests;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.vaadin.flow.component.notification.Notification;

import workshop.demo.ApplicationLayer.NotificationService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.NodeJPARepository;
import workshop.demo.InfrastructureLayer.OfferJpaRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StoreTreeJPARepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableCaching

public class TestTest {

    // @Autowired
    // private StoreTreeJPARepository tree;
    // @Autowired
    // private NodeJPARepository node;
    // @Autowired
    // private NotificationService notificationRepository;

    // @Autowired
    // private IStockRepoDB stockRepositoryjpa;
    // @Autowired
    // private IStoreRepoDB storeRepositoryjpa;
    // @Autowired
    // private IOrderRepoDB orderRepository;
    // @Autowired
    // private PurchaseRepository purchaseRepository;
    // @Autowired
    // private UserSuspensionJpaRepository suspensionRepo;
    // @Autowired
    // private AuthenticationRepo authRepo;
    // @Autowired
    // private UserJpaRepository userRepo;
    // @Autowired
    // private SUConnectionRepository sIsuConnectionRepo;
    // @Autowired
    // private GuestJpaRepository guestRepo;
    // @Autowired
    // private IStoreStockRepo storeStockRepo;
    // @Autowired
    // private OfferJpaRepository offerRepo;
    // // ======================== Services ========================
    @Autowired
    public UserService userService;
    // @Autowired
    // private StoreService storeService;
    // @Autowired
    // private StockService stockService;
    // @Autowired
    // private PurchaseService purchaseService;
    // @Autowired
    // private OrderService orderService;
    // @Autowired
    // private UserSuspensionService suspensionService;

    // // ======================== Payment / Supply ========================
    // @Autowired
    // private PaymentServiceImp payment;
    // @Autowired
    // private SupplyServiceImp serviceImp;

    // // ======================== Utility ========================
    // @Autowired
    // private Encoder encoder;

    // ======================== Test Data ========================
    String NOToken;
    String NGToken;
    String GToken;
    String Admin;
    ItemStoreDTO itemStoreDTO;
    int PID;

    int createdStoreId;

    @Test
    public void test() {
        // assertNotNull(userService);
        try {
            GToken  = userService.generateGuest();
            System.out.println(GToken);
        } catch (UIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
