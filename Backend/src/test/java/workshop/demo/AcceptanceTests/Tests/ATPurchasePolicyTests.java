package workshop.demo.AcceptanceTests.Tests;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.*;

@ActiveProfiles("test")
public class ATPurchasePolicyTests extends AcceptanceTests {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseTests.class);

    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final int USER1_ID = 1;
    private static final int USER2_ID = 2;

    private static final String STORE_NAME = "CoolStore";
    private static final String STORE_CATEGORY = "Electronics";
    private static final String PRODUCT_NAME = "Phone";
    private static final int PRODUCT_PRICE = 100;
    private static final String PRODUCT_DESC = "SMART PHONE";
    private static final String[] KEYWORD = {"Phone"};

    private String user1Token = "user1Token";
    private String user2Token = "user2Token";

    private Registered user1 = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 19);
    private Registered user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 22);
    //  NEW FOR GUEST
    private Guest guestUser = new Guest();
    private String guestToken = "guestToken";

    private Store store;
    private Product product;
    private StoreStock storeStock;

    @BeforeEach
    void setup() throws Exception {
        when(mockUserRepo.save(any(Registered.class))).thenAnswer(inv -> {
            Registered reg = inv.getArgument(0);
            if (USER1_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER1_ID);
            } else if (USER2_USERNAME.equals(reg.getUsername())) {
                forceField(reg, "id", USER2_ID);
            } else {
                forceField(reg, "id", 999);
            }
            return reg;
        });

        user1 = mockUserRepo.save(user1);
        user2 = mockUserRepo.save(user2);
        user1.login();
        user2.login();

        store = new Store(STORE_NAME, STORE_CATEGORY);
        PolicyManager policyManager = new PolicyManager();
        policyManager.setStore(store);
        store.setPolicyManager(policyManager);

        when(mockStoreRepo.save(any(Store.class))).thenAnswer(inv -> {
            Store s = inv.getArgument(0);
            forceField(s, "storeId", 0);
            return s;
        });
        store = mockStoreRepo.save(store);

        product = new Product(PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);

        item normalItem = new item(product.getProductId(), 10, PRODUCT_PRICE, Category.Electronics);
        storeStock = new StoreStock(store.getstoreId());
        storeStock.addItem(normalItem);

        var p = PurchasePolicy.noProductUnderAge(product.getProductId(), 20);
        p.setParam(20);
        p.setProductId(product.getProductId());
        store.addPurchasePolicy(p);

        var p1 = PurchasePolicy.minQuantityPerProduct(product.getProductId(), 2);
        p1.setParam(2);
        p1.setProductId(product.getProductId());
        store.addPurchasePolicy(p1);
        //  NEW FOR GUEST
        forceField(guestUser, "id", 77); // any id for the guest
        when(mockGuestRepo.save(any(Guest.class))).thenReturn(guestUser);
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));
        guestUser = mockGuestRepo.save(guestUser);

    }

    // ======================
    // BASE SUCCESS + FAILURE
    // ======================

    @Test
    void testAddToCart_Policy_Success() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        assertTrue(userService.addToUserCart(user2Token,
                new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2));
    }

    @Test
    void testAddToCart_Policy_Failure_Age() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user1Token,
                    new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2);
        });
        System.out.println(ex);
        assertTrue(ex.getMessage().contains("20 or older"));
    }

    @Test
    void testAddToCart_Policy_Failure_MinQuantity() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user2Token,
                    new ItemStoreDTO(product.getProductId(),1,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 1);
        });
        System.out.println(ex);

        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    void testAddToCart_Policy_Failure_Both() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user1Token, logger);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user1Token,
                    new ItemStoreDTO(product.getProductId(),1,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 1);
        });
        System.out.println(ex);

        assertTrue(ex.getMessage().contains("20 or older") || ex.getMessage().contains("at least"));
    }

    // =========================
    // ADVANCED CUSTOM SCENARIOS
    // =========================

    @Test
    void testAddToCart_Policy_Success_MultiplePolicies() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        assertTrue(userService.addToUserCart(user2Token,
                new ItemStoreDTO(product.getProductId(),3,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 3));
    }

    @Test
    void testAddToCart_Policy_Failure_TooStrictAge() throws Exception {
        store.addPurchasePolicy(PurchasePolicy.noProductUnderAge(product.getProductId(), 23));
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user2Token,
                    new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2);
        });
        System.out.println(ex);

        assertTrue(ex.getMessage().contains("23"));
    }

    @Test
    void testAddToCart_Policy_Failure_TooStrictQuantity() throws Exception {
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 5));
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user2Token,
                    new ItemStoreDTO(product.getProductId(),3,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 3);
        });
        System.out.println(ex);

        assertTrue(ex.getMessage().contains("at least"));
    }

    @Test
    void testAddToCart_Policy_Success_StackedPolicies() throws Exception {
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 2));
        store.addPurchasePolicy(PurchasePolicy.noProductUnderAge(product.getProductId(), 18));
        store.addPurchasePolicy(PurchasePolicy.noProductUnderAge(product.getProductId(), 15));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        assertTrue(userService.addToUserCart(user2Token,
                new ItemStoreDTO(product.getProductId(),3,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 3));
    }

    @Test
    void testAddToCart_Policy_Failure_BothFailNew() throws Exception {
        store.addPurchasePolicy(PurchasePolicy.noProductUnderAge(product.getProductId(), 25));
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 5));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(user2Token,
                    new ItemStoreDTO(product.getProductId(),3,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 3);
        });
        System.out.println(ex);

        assertTrue(ex.getMessage().contains("25") || ex.getMessage().contains("at least"));
    }
    @Test
    void testAddToCart_Policy_Failure_GuestDueToAge() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(guestToken,
                    new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2);
        });
        System.out.println(ex);
        assertTrue(ex.getMessage().contains("20") || ex.getMessage().contains("older"));
    }
    @Test
    void testAddToCart_Policy_Success_GuestNoAgePolicy() throws Exception {
        store.getPolicyManager().setPurchasePolicies(new HashSet<>()); // 🔥 clear all existing
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 2));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        assertTrue(userService.addToUserCart(guestToken,
                new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2));
    }

    @Test
    void testAddToCart_Policy_Failure_Guest_AgePolicy() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(guestToken,
                    new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2);
        });
        System.out.println("Guest age policy fail: " + ex);
        assertTrue(ex.getMessage().contains("20") || ex.getMessage().toLowerCase().contains("older"));
    }
    @Test
    void testAddToCart_Policy_Failure_Guest_QuantityPolicy() throws Exception {
        store.getPolicyManager().setPurchasePolicies(new HashSet<>());
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 5));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        UIException ex = assertThrows(UIException.class, () -> {
            userService.addToUserCart(guestToken,
                    new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2);
        });
        System.out.println("Guest quantity fail: " + ex);
        assertTrue(ex.getMessage().toLowerCase().contains("quantity") || ex.getMessage().contains("5"));
    }
    @Test
    void testAddToCart_Policy_Success_Guest_OnlyQuantity() throws Exception {
        store.getPolicyManager().setPurchasePolicies(new HashSet<>());
        store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(product.getProductId(), 2));

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        assertTrue(userService.addToUserCart(guestToken,
                new ItemStoreDTO(product.getProductId(),2,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 2));
    }
    @Test
    void testAddToCart_Policy_Success_Guest_NoPolicies() throws Exception {
        store.getPolicyManager().setPurchasePolicies(new HashSet<>()); // no policies

        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(guestToken, logger);
        when(mockAuthRepo.getUserId(guestToken)).thenReturn(guestUser.getId());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(mockGuestRepo.findById(guestUser.getId())).thenReturn(Optional.of(guestUser));

        assertTrue(userService.addToUserCart(guestToken,
                new ItemStoreDTO(product.getProductId(),1,100,Category.Electronics,3,store.getstoreId(),product.getName(),store.getStoreName()), 1));
    }


}