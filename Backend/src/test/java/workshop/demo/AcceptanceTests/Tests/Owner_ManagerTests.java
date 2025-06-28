package workshop.demo.AcceptanceTests.Tests;
//UpdateProductInStock_Failure_InvalidData() throws Exception

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.test.util.AopTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.DelayedNotification;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

@SpringBootTest
@ActiveProfiles("test")
public class Owner_ManagerTests extends AcceptanceTests {


    private static final Logger logger = LoggerFactory.getLogger(UserTests.class);

    private String user1Token;
    private String user2Token;

    // Entities
    private Registered user1;
    private Registered user2;
    private Store store;
    private Product product;

    // Constants
    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String PASSWORD = "pass123";
    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final int USER1_ID = 1;
    private static final int USER2_ID = 2;

    private static final String STORE_NAME = "CoolStore";
    private static final String STORE_CATEGORY = "Electronics";

    private static final String PRODUCT_NAME = "Phone";
    private static final double PRODUCT_PRICE = 100.0;
    private static final String PRODUCT_DESC = "SMART PHONE";
    private static final String[] KEYWORD = {"Phone"};

    @BeforeEach
    void setup() throws Exception {
        mockGuestRepo.deleteAll();
        mockUserRepo.deleteAll();

        // ===== ENCODER =====
        Field encoderField = UserService.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        encoderField.set(userService, encoder);

        when(encoder.encodePassword(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(encoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // ===== USER1 guest =====
        Guest guest = new Guest();
        saveGuestRepo(guest);
        when(mockAuthRepo.generateGuestToken(0)).thenReturn("guest-token");
        when(mockAuthRepo.getUserId("guest-token")).thenReturn(0);
        userService.generateGuest();

        when(mockUserRepo.existsByUsername(USER1_USERNAME)).thenReturn(0);
        when(mockUserRepo.save(any(Registered.class))).thenAnswer(inv -> {
            Registered reg = inv.getArgument(0);
            if (USER1_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER1_ID);
            } else if (USER2_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER2_ID);
            } else {
                forceField(reg, "id", 999); // fallback
            }
            return reg;
        });

        userService.register("guest-token", USER1_USERNAME, PASSWORD, 20);

        user1 = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 20);
        saveUserRepo(user1);
        when(mockUserRepo.findRegisteredUsersByUsername(USER1_USERNAME)).thenReturn(List.of(user1));
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));

        when(mockAuthRepo.generateUserToken(USER1_ID, USER1_USERNAME)).thenReturn("user1Token");
        when(mockAuthRepo.getUserId("user1Token")).thenReturn(USER1_ID);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user1Token", logger);
        user1Token = userService.login("guest-token", USER1_USERNAME, PASSWORD);

        // ===== USER2  =====
        user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 22);
        saveUserRepo(user2);
        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockAuthRepo.generateUserToken(USER2_ID, USER2_USERNAME)).thenReturn("user2Token");
        when(mockAuthRepo.getUserId("user2Token")).thenReturn(USER2_ID);
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException("user2Token", logger);
        user2Token = userService.login("guest-token", USER2_USERNAME, PASSWORD);

        // ===== STORE =====
        store = new Store(STORE_NAME, STORE_CATEGORY);
        forceStoreId(store, 0);
        when(mockStoreRepo.save(any(Store.class))).thenReturn(store);
        when(mockStoreRepo.findById(0)).thenReturn(Optional.of(store));
        when(mockiosRepo.addNewStoreOwner(0, USER1_ID)).thenReturn(true);
        when(mockActivePurchases.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int storeId = storeService.addStoreToSystem(user1Token, STORE_NAME, STORE_CATEGORY);
        assertEquals(0, storeId);

        // ===== PRODUCT =====
        product = new Product(PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);
        forceField(product, "productId", 0);
        when(mockStockRepo1.save(any(Product.class))).thenReturn(product);
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));

        int productId = stockService.addProduct(user1Token, PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);
        assertEquals(0, productId);

        // ===== ITEM =====
        StoreStock stock = new StoreStock(storeId);
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(0)).thenReturn(Optional.of(store));
        when(suConnectionRepo.manipulateItem(USER1_ID, 0, Permission.AddToStock)).thenReturn(true);
        when(mockStoreStock.findById(0)).thenReturn(Optional.of(stock));
        when(mockStoreStock.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int itemId = stockService.addItem(0, user1Token, 0, 10, 200, Category.Electronics);
        assertEquals(0, itemId);
    }
    @Test
    void testAddStoreToSystem_Success() throws Exception {
        String storeName = "NewStore";
        String category = "Books";
        int newStoreId = 5;

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        // No suspension
        when(mockSusRepo.findById(USER2_ID)).thenReturn(Optional.empty());

        // Prepare store creation
        Store newStore = new Store();
        forceStoreId(newStore, newStoreId);
        when(mockStoreRepo.save(any(Store.class))).thenReturn(newStore);

        // Prepare rest of flow
        when(mockiosRepo.addNewStoreOwner(newStoreId, USER2_ID)).thenReturn(true);
        when(mockActivePurchases.save(any(ActivePurcheses.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mockStoreStock.save(any(StoreStock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        int returnedStoreId = storeService.addStoreToSystem(user2Token, storeName, category);

        // Assert
        assertEquals(newStoreId, returnedStoreId);
    }
    @Test
    void testAddStoreToSystem_Failure_InvalidToken() throws Exception {
        String storeName = "AnotherStore";
        String category = "Games";

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(eq("bad-token"), any());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.addStoreToSystem("bad-token", storeName, category);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }
    @Test
    void testAddStoreToSystem_Failure_SuspendedUser() throws Exception {
        String storeName = "SuspendedStore";
        String category = "Music";

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        // Prepare suspension
        UserSuspension suspension = Mockito.spy(new UserSuspension(USER2_ID, System.currentTimeMillis() + 100000));
        when(suspension.isExpired()).thenReturn(false);
        when(suspension.isPaused()).thenReturn(false);
        when(mockSusRepo.findById(USER2_ID)).thenReturn(Optional.of(suspension));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.addStoreToSystem(user2Token, storeName, category);
        });

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
    }
    @Test
    void testMakeOfferToAddOwnership_Success() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));
        when(suConnectionRepo.checkToAddOwner(storeId, USER1_ID, USER2_ID)).thenReturn(true);

        when(mockNotiRepo.save(any(DelayedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        storeService.MakeofferToAddOwnershipToStore(storeId, user1Token, USER2_USERNAME);

        // Assert
        verify(mockNotiRepo).save(any(DelayedNotification.class));
    }



    @Test
    void testMakeOfferToAddOwnership_Failure_OwnerNotRegistered() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        Exception ex = assertThrows(UIException.class, () -> {
            storeService.MakeofferToAddOwnershipToStore(storeId, user1Token, USER2_USERNAME);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ((UIException)ex).getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("user not registered"));
    }

    @Test
    void testMakeOfferToAddOwnership_Failure_NewOwnerNotRegistered() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));

        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of());

        Exception ex = assertThrows(IndexOutOfBoundsException.class, () -> {
            storeService.MakeofferToAddOwnershipToStore(storeId, user1Token, USER2_USERNAME);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("index"), "Must fail on accessing empty list");
    }

    @Test
    void testMakeOfferToAddOwnership_Failure_Suspended() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(new UserSuspension(USER1_ID, System.currentTimeMillis() + 10000)));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.MakeofferToAddOwnershipToStore(storeId, user1Token, USER2_USERNAME);
        });

        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
    }

    @Test
    void testMakeOfferToAddOwnership_Failure_StoreNotFound() throws Exception {
        int invalidStoreId = 999;

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findRegisteredUsersByUsername(USER2_USERNAME)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockStoreRepo.findById(invalidStoreId)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.MakeofferToAddOwnershipToStore(invalidStoreId, user1Token, USER2_USERNAME);
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
    }

    @Test
    void testReciveAnswerToOffer_Success_AddOwner() throws Exception {
        int storeId = store.getstoreId();
        String senderName = USER1_USERNAME;
        String receiverName = USER2_USERNAME;

        // Arrange
        when(mockUserRepo.findRegisteredUsersByUsername(senderName)).thenReturn(List.of(user1));
        when(mockUserRepo.findRegisteredUsersByUsername(receiverName)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockiosRepo.getOffer(storeId,user1.getId(),user2.getId())).thenReturn(new Offer(storeId,user1.getId(),user2.getId(),true,null,"i dont know"));
        when(mockiosRepo.AddOwnershipToStore(storeId,user1.getId(),user2.getId())).thenReturn(true);
        when(mockiosRepo.deleteOffer(storeId,user1.getId(),user2.getId())).thenReturn(null);


        assertDoesNotThrow(() -> {
            storeService.reciveAnswerToOffer(storeId, senderName, receiverName, true, true);
        });

//        System.out.println(""+user1.getId()+"  "+user2.getId());
//        assertEquals(result,user2.getId());
    }

    @Test
    void testReciveAnswerToOffer_Success_AddManager() throws Exception {
        int storeId = store.getstoreId();
        String senderName = USER1_USERNAME;
        String receiverName = USER2_USERNAME;

        // Arrange
        when(mockUserRepo.findRegisteredUsersByUsername(senderName)).thenReturn(List.of(user1));
        when(mockUserRepo.findRegisteredUsersByUsername(receiverName)).thenReturn(List.of(user2));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockiosRepo.getOffer(storeId,user1.getId(),user2.getId())).thenReturn(new Offer(storeId,user1.getId(),user2.getId(),true,null,"i dont know"));
        doNothing().when(mockiosRepo).AddManagerToStore(storeId,user1.getId(),user2.getId());
        Node p=new Node(storeId,user1.getId(),false,null);//i am not sure about null
        Node n=new Node(storeId,user2.getId(),true,p);
        when(mockNodeRepo.save(n)).thenReturn(n);
        List<Permission> permissions=new LinkedList<>();
        permissions.add(Permission.AddToStock);
        permissions.add(Permission.UpdatePrice);
        permissions.add(Permission.SpecialType);
        when(mockiosRepo.deleteOffer(storeId,user1.getId(),user2.getId())).thenReturn(permissions);




        assertDoesNotThrow(() -> {
            storeService.reciveAnswerToOffer(storeId, senderName, receiverName, true, false);
        });
//        System.out.println(""+user1.getId()+"  "+user2.getId());
//        assertEquals(result,user2.getId());
    }

    @Test
    void testReciveAnswerToOffer_Failure_ReceiverNotFound() {
        int storeId = store.getstoreId();
        String senderName = USER1_USERNAME;
        String receiverName = "not_found_user";

        // Arrange
        when(mockUserRepo.findRegisteredUsersByUsername(senderName)).thenReturn(List.of(user1));
        when(mockUserRepo.findRegisteredUsersByUsername(receiverName)).thenReturn(List.of());

        // Spy on service
        StoreService spyStoreService = Mockito.spy(storeService);

        // Act & Assert
        Exception ex = assertThrows(Exception.class, () -> {
            spyStoreService.reciveAnswerToOffer(storeId, senderName, receiverName, true, false);
        });
        assertTrue(ex instanceof IndexOutOfBoundsException);
    }


    @Test
    void testChangePermissions_Success() throws Exception {
        int storeId = store.getstoreId();
        List<Permission> newPermissions = List.of(Permission.AddToStock);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1)); // owner
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2)); // manager

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // simulate suConnectionRepo action
        doNothing().when(suConnectionRepo).changePermissions(USER1_ID, USER2_ID, storeId, newPermissions);

        // act
        //storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);

        assertDoesNotThrow(() -> {
            storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);        });
    }

    @Test
    void testChangePermissions_Failure_InvalidToken() throws Exception {
        int storeId = store.getstoreId();
        List<Permission> newPermissions = List.of(Permission.AddToStock);

        doThrow(new UIException("invalid token", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo).checkAuth_ThrowTimeOutException(eq(user1Token), any());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("invalid"));
    }
    @Test
    void testChangePermissions_Failure_OwnerNotRegistered() throws Exception {
        int storeId = store.getstoreId();
        List<Permission> newPermissions = List.of(Permission.AddToStock);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty()); // owner not found

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("not registered"));
    }
    @Test
    void testChangePermissions_Failure_ManagerNotRegistered() throws Exception {
        int storeId = store.getstoreId();
        List<Permission> newPermissions = List.of(Permission.AddToStock);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getErrorCode());
    }
    @Test
    void testChangePermissions_Failure_StoreNotFound() throws Exception {
        int storeId = store.getstoreId();
        List<Permission> newPermissions = List.of(Permission.AddToStock);

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(Exception.class, () -> {
            storeService.changePermissions(user1Token, USER2_ID, storeId, newPermissions);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testDeleteOwnershipFromStore_Success() throws Exception {
        int storeId = store.getstoreId();

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // Act
        assertDoesNotThrow(() -> {
            storeService.DeleteOwnershipFromStore(storeId, user1Token, USER2_ID);
        });

        // Assert
    }
    @Test
    void testDeleteOwnershipFromStore_Failure_Suspended() throws UIException {
        int storeId = store.getstoreId();

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(new UserSuspension(USER1_ID, System.currentTimeMillis() + 100000)));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.DeleteOwnershipFromStore(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
    }
    @Test
    void testDeleteOwnershipFromStore_Failure_StoreNotActive() throws UIException {
        int storeId = store.getstoreId();
        store.setActive(false); // deactivate store

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.DeleteOwnershipFromStore(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
        assertEquals(ErrorCodes.DEACTIVATED_STORE, ex.getNumber());
    }
    @Test
    void testDeleteOwnershipFromStore_Failure_OwnerNotRegistered() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty()); // simulate not registered

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.DeleteOwnershipFromStore(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().contains("not registered"));
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }
    @Test
    void testDeleteManager_Success() throws Exception {
        int storeId = store.getstoreId();

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1)); // owner
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2)); // manager
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // Act
        assertDoesNotThrow(() -> {
            storeService.deleteManager(storeId, user1Token, USER2_ID);
        });

    }
    @Test
    void testDeleteManager_Failure_SuspendedOwner() throws UIException {
        int storeId = store.getstoreId();

        // Arrange
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.of(new UserSuspension(USER1_ID, System.currentTimeMillis() + 100000)));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("suspended"));
        assertEquals(ErrorCodes.USER_SUSPENDED, ex.getNumber());
    }
    @Test
    void testDeleteManager_Failure_StoreNotActive() throws UIException {
        int storeId = store.getstoreId();
        store.setActive(false); // deactivate store

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("not active"));
        assertEquals(ErrorCodes.DEACTIVATED_STORE, ex.getNumber());
    }
    @Test
    void testDeleteManager_Failure_OwnerNotRegistered() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().contains("not registered"));
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }
    @Test
    void testDeleteManager_Failure_ManagerNotRegistered() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.empty());
        when(mockSusRepo.findById(USER1_ID)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deleteManager(storeId, user1Token, USER2_ID);
        });

        assertTrue(ex.getMessage().contains("not registered"));
        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
    }
    @Test
    void testDeactivateStore_Success() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // checkMainOwner
        doNothing().when(suConnectionRepo).checkMainOwnerToDeactivateStore_ThrowException(storeId, USER1_ID);

        when(suConnectionRepo.getWorkersInStore(storeId)).thenReturn(List.of(USER2_ID));

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // Mock deactivate
        doNothing().when(mockStoreRepo).deactivateStore(storeId);

        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockNotiRepo.save(any(DelayedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        int result = storeService.deactivateteStore(storeId, user1Token);

        // Assert
        assertEquals(storeId, result);
    }
    @Test
    void testDeactivateStore_Failure_UserNotRegistered() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deactivateteStore(storeId, user1Token);
        });

        assertEquals(ErrorCodes.USER_NOT_LOGGED_IN, ex.getNumber());
        assertTrue(ex.getMessage().contains("not registered"));
    }
    @Test
    void testDeactivateStore_Failure_StoreNotFound() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.deactivateteStore(storeId, user1Token);
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }

    @Test
    void testDeactivateStore_Failure_NotifyUserNotFound() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        doNothing().when(suConnectionRepo).checkMainOwnerToDeactivateStore_ThrowException(storeId, USER1_ID);
        when(suConnectionRepo.getWorkersInStore(storeId)).thenReturn(List.of(USER2_ID));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.empty());

        Exception ex = assertThrows(NoSuchElementException.class, () -> {
            storeService.deactivateteStore(storeId, user1Token);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("no value"));
    }
    @Test
    void testCloseStore_Success() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        // user1 acting as admin
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));

        // Store exists
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // Employees to notify
        when(suConnectionRepo.getWorkersInStore(storeId)).thenReturn(List.of(USER2_ID));

        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockNotiRepo.save(any(DelayedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        doNothing().when(suConnectionRepo).closeStore(storeId);

        // Act
        int result = storeService.closeStore(storeId, user1Token);

        // Assert
        assertEquals(storeId, result);

    }
    @Test
    void testCloseStore_Failure_NotAdmin() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.empty()); // triggers checkAdmin_ThrowException

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(storeId, user1Token);
        });

        assertEquals(ErrorCodes.NO_PERMISSION, ex.getNumber());
        assertTrue(ex.getMessage().contains("not admin"));
    }
    @Test
    void testCloseStore_Failure_StoreNotFound() throws UIException {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(storeId, user1Token);
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().toLowerCase().contains("store"));
    }
    @Test
    void testCloseStore_Failure_NotifyUserNotFound() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockUserRepo.findById(USER1_ID)).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));
        when(suConnectionRepo.getWorkersInStore(storeId)).thenReturn(List.of(USER2_ID));

        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.empty());

        doNothing().when(suConnectionRepo).closeStore(storeId);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            storeService.closeStore(storeId, user1Token);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("no value"));
    }
    @Test
    void testCloseStore_Failure_InvalidToken() throws Exception {
        int storeId = store.getstoreId();

        doThrow(new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(user1Token), any());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.closeStore(storeId, user1Token);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Invalid token"));
    }
    @Test
    void testViewRolesAndPermissions_Success() throws Exception {
        int storeId = store.getstoreId();

        // ===== arrange auth =====
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);
        Node p=new Node(storeId,USER1_ID,false,null);
        // ===== arrange nodes =====
        Node managerNode = new Node(storeId, USER2_ID, true, p); // manager set by user1
        when(suConnectionRepo.getAllWorkers(storeId)).thenReturn(List.of(managerNode));

        // ===== arrange store exists =====
        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.of(store));

        // ===== arrange user exists =====
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));

        // ===== arrange permissions =====
        Permission[] permissions = new Permission[]{Permission.AddToStock, Permission.AddToStock};
        when(suConnectionRepo.getPermissions(managerNode)).thenReturn(permissions);

        // ===== act =====
        List<WorkerDTO> result = storeService.ViewRolesAndPermissions(user1Token, storeId);

        // ===== assert =====
        assertNotNull(result);
        assertEquals(1, result.size());

        WorkerDTO worker = result.get(0);
        assertEquals(USER2_ID, worker.getWorkerId());
        assertEquals(USER2_USERNAME, worker.getUsername());
        assertTrue(worker.isManager());
        assertFalse(worker.isOwner());
        assertEquals(STORE_NAME, worker.getStoreName());
        assertArrayEquals(permissions, worker.getPermessions());
        assertTrue(worker.isSetByMe());
    }
    @Test
    void testViewRolesAndPermissions_Failure_InvalidToken() throws Exception {
        int storeId = store.getstoreId();

        doThrow(new UIException("Token expired", ErrorCodes.INVALID_TOKEN))
                .when(mockAuthRepo)
                .checkAuth_ThrowTimeOutException(eq(user1Token), any());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.ViewRolesAndPermissions(user1Token, storeId);
        });

        assertEquals(ErrorCodes.INVALID_TOKEN, ex.getNumber());
        assertTrue(ex.getMessage().contains("Token expired"));
    }

    @Test
    void testViewRolesAndPermissions_Failure_StoreNotFound() throws Exception {
        int storeId = store.getstoreId();

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(USER1_ID);

        when(mockStoreRepo.findById(storeId)).thenReturn(Optional.empty());

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.ViewRolesAndPermissions(user1Token, storeId);
        });

        assertEquals(ErrorCodes.STORE_NOT_FOUND, ex.getNumber());
        assertTrue(ex.getMessage().contains("store not found"));
    }


//
//    @Test
//    void testOwner_AddProductToStock() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddProductToStock_Failure_InvalidProductData() throws Exception {
//
//
//    }
//
//    @Test
//    void testOwner_AddProductToStock_Failure_StoreNotFound() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeleteProductFromStock() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeleteProductFromStock_Failure_NoPermission() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_UpdatePriceProductInStock() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_UpdateQuantityProductInStock() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Success() throws Exception {
//    }
//
//
//    @Test
//    void testOwner_AddStoreOwner_Failure_TargetNotFound() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreOwner_Rejected() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeleteStoreOwner() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeleteStoreOwner_Failure_NotFound() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Success() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Rejected() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Failure_UserNotExist() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddStoreManager_Failure_StoreClosed() throws Exception {
//
//    }
//
//    @Test
//    void testDeleteManager_Success() throws Exception {
//
//    }
//
//    @Test
//    void testDeleteManager_Failure_StoreExist() throws Exception {
//
//    }
//
//    @Test
//    void testDeleteManager_Failure_StoreNotActive() throws Exception {
//
//    }
//
//    @Test
//    void testDeleteManager_Failure_StoreNotFound() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_ManageStoreManagerPermissions() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_ManageStoreManagerPermissions_Failure_NotAManagerFlag() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeactivateStore() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_DeactivateStore_Failure_AlreadyInactive() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_ViewStorePurchaseHistory() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_ViewStorePurchaseHistory_Failure_StoreNotExist() throws Exception {
//
//    }
//
//    @Test
//    void testViewRolesAndPermissions_Success() throws Exception {
//
//    }
//
//    @Test
//    void testViewRolesAndPermissions_failure() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddPurchasePolicy() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_AddPurchasePolicy_Failure_InvalidPolicy() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_AddPurchasePolicy_Failure_NotOwner() throws Exception {
//        //TODO
//
//    }
//
//    @Test
//    void testOwner_DeletePurchasePolicy() throws Exception {
//        //TODO
//
//    }
//
//    @Test
//    void testOwner_DeletePurchasePolicy_Failure_NotFound() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_DeletePurchasePolicy_Failure_NoPermission() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_ReplyToMessage() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_ReplyToMessage_Failure_UserNotFound() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_ReplyToMessage_Failure_MessageNotFound() throws Exception {
//        //TODO
//    }
//
//    @Test
//    void testOwner_ReopenStore() throws Exception {
//        //TODO
//    }
//
//    //    //todo:this case is not checked
//    //    @Test
//    //    void testOwner_DeleteProductFromStock_Failure_ProductNotFound() throws Exception {
//    //        int storeId = 100;
//    //        int productId = 999;  // Non-existent product
//    //        int ownerId = 10;
//    //        String ownerToken = "user-token";  // from setup
//    //
//    //        when(mockIOSrepo.manipulateItem(ownerId, storeId, Permission.DeleteFromStock)).thenReturn(true);
//    //
//    //        when(mockStockRepo1.findByIdInSystem_throwException(productId))
//    //                .thenThrow(new UIException("Product not found!", ErrorCodes.PRODUCT_NOT_FOUND));
//    //
//    //        assertDoesNotThrow(() ->
//    //                stockService.removeItem(storeId, ownerToken, productId)
//    //        );
//    //
//    //
//    //    }
//    @Test
//    void testOwner_AddToAuction_Success() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddToBID_Success() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddToRandom_Success() throws Exception {
//    }
//
//    @Test
//    void testOwner_AddToBid_Failure() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddToRandom_Failure() throws Exception {
//
//    }
//
//    @Test
//    void testOwner_AddToAuction_Failure() throws Exception {
//
//    }

}
