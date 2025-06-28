package workshop.demo.AcceptanceTests.Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DemoApplication;
import workshop.demo.ApplicationLayer.*;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.BaseNotifier;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.*;

import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = DemoApplication.class)
@ActiveProfiles("test")
public class AcceptanceTests {

    // Replace manual mocks with Spring's @Mock
    @Mock
    protected AuthenticationRepo mockAuthRepo;
    @Mock
    protected UserJpaRepository mockUserRepo;
    @Mock
    protected GuestJpaRepository mockGuestRepo;
    @Mock
    protected IStoreRepoDB mockStoreRepo;
    @Mock
    protected IStockRepoDB mockStockRepo1;
    @Mock
    protected IStoreStockRepo mockStoreStock;
    @Mock
    protected NodeJPARepository mockNodeRepo;
    @Mock
    protected IStockRepo mockStockRepo;
    @Mock
    protected PurchaseRepository mockPurchaseRepo;
    @Mock
    protected IOrderRepoDB mockOrderRepo;
    @Mock
    protected DelayedNotificationRepository mockNotiRepo;
    @Mock
    protected ReviewJpaRepository mockReviewRepo;
    @Mock
    protected UserSuspensionJpaRepository mockSusRepo;
    // @Mock
    // protected ISUConnectionRepo mockIOSrepo;
    // @Mock
    // protected SUConnectionRepository suConnectionRepo;

    protected Encoder encoder = new Encoder();

    @InjectMocks
    protected UserService userService;
    @InjectMocks
    protected StoreService storeService;
    @InjectMocks
    protected StockService stockService;
    @InjectMocks
    protected PurchaseService purchaseService;
    @InjectMocks
    protected OrderService orderService;
    @InjectMocks
    protected NotificationService notificationService;
    @InjectMocks
    protected ReviewService reviewService;

    @BeforeEach
    public void init() {
        // Optional: configure behaviors for mocks
        when(mockUserRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

    protected void mockSaveRegisteredFailure() {
        when(mockUserRepo.save(any(Registered.class))).thenThrow(new RuntimeException("DB error saving registered"));
    }

    protected void mockExistsByUsernameSuccess() {
        when(mockUserRepo.existsByUsername(any())).thenReturn(1);
    }

    protected void mockExistsByUsernameFailure() {
        when(mockUserRepo.existsByUsername(any())).thenReturn(0);
    }

}
