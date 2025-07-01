package workshop.demo.AcceptanceTests.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DemoApplication;
import workshop.demo.ApplicationLayer.*;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.*;

// import workshop.demo.InfrastructureLayer.DiscountEntities.DiscountJpaRepository;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DemoApplication.class)
@ActiveProfiles("test")
public class AcceptanceTests {

    // Replace manual mocks with Spring's @MockBean
    @MockBean
    protected AuthenticationRepo mockAuthRepo;

    @MockBean
    protected UserJpaRepository mockUserRepo;

    @MockBean
    protected GuestJpaRepository mockGuestRepo;

    @MockBean
    protected IStoreRepoDB mockStoreRepo;

    @MockBean
    protected IStockRepoDB mockStockRepo1;

    @MockBean
    protected IStoreStockRepo mockStoreStock;

    @MockBean
    protected NodeJPARepository mockNodeRepo;

    // @MockBean
    // protected PurchaseRepository mockPurchaseRepo;

    @MockBean
    protected IOrderRepoDB mockOrderRepo;

    @MockBean
    protected DelayedNotificationRepository mockNotiRepo;

    @MockBean
    protected ReviewJpaRepository mockReviewRepo;

    @MockBean
    protected UserSuspensionJpaRepository mockSusRepo;

    //ISUConnectionRepo mockiosRepo = Mockito.mock(ISUConnectionRepo.class);

    @MockBean
    protected SUConnectionRepository suConnectionRepo;

    @MockBean
    protected StoreTreeJPARepository mockStoreTreeRepo;

    @MockBean
    protected OfferJpaRepository mockOfferRepo;


    @MockBean
    protected IActivePurchasesRepo mockActivePurchases;
    @MockBean
    protected DiscountJpaRepository mockdiscountrepo;
    @MockBean
    protected PolicyManagerRepository policyManagerRepository;
    //    @MockBean
//    protected AISearch mockAISearch;
    //    @MockBean
//    protected CartRepo mockCartRepo;
//
//    @MockBean
//    protected AISearch mockAISearch;
    @MockBean
    protected Encoder encoder;
    @MockBean
    protected AdminInitilizer adminInitilizer;
    @Autowired
    protected UserService userService;

    @Autowired
    protected StoreService storeService;

    @Autowired
    protected StockService stockService;

    @Autowired
    protected PurchaseService purchaseService;

    @Autowired
    protected OrderService orderService;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected ReviewService reviewService;

    @Autowired
    protected ActivePurchasesService activePurchesesService;



    @Autowired
    protected PaymentServiceImp paymentServiceImp;

    @Autowired
    protected SupplyServiceImp supplyServiceImp;

    @Autowired
    protected UserSuspensionService userSuspensionService;

    @BeforeEach
    public void init() {
        // Optional: configure behaviors for mocks
        //when(mockUserRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    protected void saveUserRepo(Registered R) {
        when(mockUserRepo.save(R)).thenReturn(R);
    }

    protected void saveGuestRepo(Guest R) {
        when(mockGuestRepo.save(R)).thenReturn(R);
    }

    protected void mockSaveGuestFailure() {
        when(mockGuestRepo.save(any(Guest.class))).thenThrow(new RuntimeException("DB error saving guest"));
    }



    private Field getFieldRecursively(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // חיפוש במחלקת־אב
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
    }


    protected void forceStoreId(Store store, int id) throws Exception {
        Field idField = Store.class.getDeclaredField("storeId");
        idField.setAccessible(true);
        idField.set(store, id);
    }

    protected void forceField(Object obj, String fieldName, int value) throws Exception {
        Field field = getFieldRecursively(obj.getClass(), fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

}