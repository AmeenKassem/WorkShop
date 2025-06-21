package workshop.demo.AcceptanceTests.Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import workshop.demo.ApplicationLayer.*;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.*;
import workshop.demo.DataAccessLayer.*;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Review.IReviewRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AcceptanceTests {

    // Global test data
    protected Guest guest;
    protected Registered registered;

    // Mocked repositories
    protected AuthenticationRepo mockAuthRepo =Mockito.mock(AuthenticationRepo.class);
    protected UserJpaRepository mockUserRepo = Mockito.mock(UserJpaRepository.class);
    protected GuestJpaRepository mockGuestRepo = Mockito.mock(GuestJpaRepository.class);
    protected IStoreRepoDB mockStoreRepo = Mockito.mock(IStoreRepoDB.class);
    protected StockRepository mockStockRepo = Mockito.mock(StockRepository.class);
    protected PurchaseRepository mockPurchaseRepo = Mockito.mock(PurchaseRepository.class);
    protected OrderRepository mockOrderRepo = Mockito.mock(OrderRepository.class);
    protected NotificationRepository mockNotiRepo = Mockito.mock(NotificationRepository.class);
    protected ReviewRepository mockReviewRepo = Mockito.mock(ReviewRepository.class);
    protected UserSuspensionJpaRepository mockSusRepo = Mockito.mock(UserSuspensionJpaRepository.class);
    protected ISUConnectionRepo mockIOSrepo = Mockito.mock(ISUConnectionRepo.class);
    protected Encoder encoder=Mockito.mock(Encoder.class);
    // Services
    protected UserService userService;
    protected StoreService storeService;
    protected StockService stockService;
    protected PurchaseService purchaseService;
    protected OrderService orderService;
    protected NotificationService notificationService;
    protected ReviewService reviewService;

    @BeforeEach
    public void init() {
        try {
            userService = new UserService(mockUserRepo, mockAuthRepo, mockStockRepo,
                    new AdminInitilizer("123321"), mockGuestRepo, mockStoreRepo);
            notificationService = new NotificationService(mockNotiRepo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    protected void saveUserRepo(Registered R){
        when(mockUserRepo.save(R)).thenReturn(R);
    }
    protected void saveGuestRepo(Guest R){
        when(mockGuestRepo.save(R)).thenReturn(R);
    }






    protected void mockSaveRegisteredSuccess() {
        registered = new Registered("bashar", "finish", 18);
        setId(registered, 3);
        when(mockUserRepo.save(any(Registered.class))).thenReturn(registered);
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

    protected void mockSaveGuestSuccess() {
        guest = new Guest();
        setId(guest, 3);
        when(mockGuestRepo.save(any(Guest.class))).thenReturn(guest);
    }

    protected void mockSaveGuestFailure() {
        when(mockGuestRepo.save(any(Guest.class))).thenThrow(new RuntimeException("DB error saving guest"));
    }

    protected void setId(Object entity, int id) {
        try {
            Field idField = findFieldInHierarchy(entity.getClass(), "id");
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID for " + entity.getClass().getSimpleName(), e);
        }
    }

    protected Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class hierarchy.");
    }



}