package workshop.demo.AcceptanceTests.Tests;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.AcceptanceTests.Utill.Real;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.CouponContext;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

@SpringBootTest
@ActiveProfiles("test")
public class DiscountTests {

    private static final Logger logger = LoggerFactory.getLogger(DiscountTests.class);

    Real real = new Real();

    public DiscountTests() throws Exception {
    }

    @BeforeEach
    void setup() throws Exception {
        int adminId = 999;
        String adminGuestToken = "admin-guest-token";
        String adminUserToken = "admin-user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(adminId);
        when(real.mockAuthRepo.generateGuestToken(adminId)).thenReturn(adminGuestToken);
        when(real.mockAuthRepo.validToken(adminGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("admin", "adminPass")).thenReturn(adminId);
        when(real.mockAuthRepo.generateUserToken(adminId, "admin")).thenReturn(adminUserToken);
        when(real.mockAuthRepo.validToken(adminUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(adminUserToken)).thenReturn(adminId);
        when(real.mockAuthRepo.getUserName(adminUserToken)).thenReturn("admin");

        String adminGuest = real.userService.generateGuest();
        assertEquals(adminGuestToken, adminGuest);
        assertTrue(real.userService.register(adminGuest, "admin", "adminPass", 18));
        String adminToken = real.userService.login(adminGuest, "admin", "adminPass");
        assertEquals(adminUserToken, adminToken);
        real.testSystem_InitMarket(adminUserToken);

        int ownerId = 10;
        String ownerGuestToken = "guest-token";
        String ownerUserToken = "user-token";

        when(real.mockUserRepo.generateGuest()).thenReturn(ownerId);
        when(real.mockAuthRepo.generateGuestToken(ownerId)).thenReturn(ownerGuestToken);
        when(real.mockAuthRepo.validToken(ownerGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("owner", "owner")).thenReturn(ownerId);
        when(real.mockAuthRepo.generateUserToken(ownerId, "owner")).thenReturn(ownerUserToken);
        when(real.mockAuthRepo.validToken(ownerUserToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(ownerUserToken)).thenReturn(ownerId);
        when(real.mockAuthRepo.getUserName(ownerUserToken)).thenReturn("owner");
        when(real.mockUserRepo.isRegistered(ownerId)).thenReturn(true);

        String ownerGuest = real.userService.generateGuest();
        assertEquals(ownerGuestToken, ownerGuest);
        assertTrue(real.userService.register(ownerGuest, "owner", "owner", 18));
        String ownerToken = real.userService.login(ownerGuest, "owner", "owner");
        assertEquals(ownerUserToken, ownerToken);

        int storeId = 100;
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "Electronics")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "Electronics");
        assertEquals(storeId, createdStoreId);

        int productId = 200;
        String[] keywords = {"Phone", "Smartphone"};
        when(real.mockStockRepo.addProduct("Phone", Category.Electronics, "Smartphone", keywords)).thenReturn(productId);
        int returnedProductId = real.stockService.addProduct(ownerToken, "Phone", Category.Electronics, "Smartphone", keywords);
        assertEquals(productId, returnedProductId);

        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.AddToStock)).thenReturn(true);
        when(real.mockStockRepo.findByIdInSystem_throwException(productId)).thenReturn(new Product("Phone", productId, Category.Electronics, "Smartphone", keywords));
        when(real.mockStockRepo.addItem(storeId, productId, 10, 100, Category.Electronics))
                .thenReturn(new item(productId, 10, 100, Category.Electronics));
        int itemAdded = real.stockService.addItem(storeId, ownerToken, productId, 10, 100, Category.Electronics);
        assertEquals(itemAdded, productId);

        int userId = 20;
        String userGuestToken = "guest-token-2";
        String userToken = "user-token-2";

        when(real.mockUserRepo.generateGuest()).thenReturn(userId);
        when(real.mockAuthRepo.generateGuestToken(userId)).thenReturn(userGuestToken);
        when(real.mockAuthRepo.validToken(userGuestToken)).thenReturn(true);
        when(real.mockUserRepo.login("user2", "pass2")).thenReturn(userId);
        when(real.mockAuthRepo.generateUserToken(userId, "user2")).thenReturn(userToken);
        when(real.mockAuthRepo.validToken(userToken)).thenReturn(true);
        when(real.mockAuthRepo.getUserId(userToken)).thenReturn(userId);
        when(real.mockAuthRepo.getUserName(userToken)).thenReturn("user2");
        when(real.mockUserRepo.isRegistered(userId)).thenReturn(true);
        when(real.mockAuthRepo.isRegistered(userToken)).thenReturn(true);

        String guestToken = real.userService.generateGuest();
        assertEquals(userGuestToken, guestToken);
        assertTrue(real.userService.register(guestToken, "user2", "pass2", 18));
        String loginToken = real.userService.login(guestToken, "user2", "pass2");
        assertEquals(userToken, loginToken);
        when(real.mockIOSrepo.manipulateItem(ownerId, storeId, Permission.SpecialType)).thenReturn(true);

        String guest = "guest-token";
        when(real.mockUserRepo.generateGuest()).thenReturn(3);
        when(real.mockAuthRepo.generateGuestToken(3)).thenReturn(guest);
        when(real.mockAuthRepo.validToken(guest)).thenReturn(true);
    }

    //SINGLE DISCOUNTS
    @Test
    void testBuyRegisteredCart_WithDiscount_Success_Item() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        List<CartItem> a = new LinkedList<>();
        a.add(item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_CATEGORY() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_TOTAL() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>150", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_QUANTITY() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_ITEM() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:200", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_STORE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_CATEGORY_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Home);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "HOME");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Home);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_TOTAL_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 300, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>300", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 300, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(300.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 300.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(300.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_QUANTITY_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_ITEM_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_STORE_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "STORE:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //COMPOSITE DISCOUNTS
    //OR
    @Test
    void testBuyRegisteredCart_WithOrDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //XOR
    @Test
    void testBuyRegisteredCart_WithXorDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithXorDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>4", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithXorDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true); //
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //AND
    @Test
    void testBuyRegisteredCart_WithAndDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 134.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(134.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //MAX
    @Test
    void testBuyRegisteredCart_WithMaxDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.8, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMaxDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //MUL
    @Test
    void testBuyRegisteredCart_WithMultiplyDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 50.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(50.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMultiplyDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //INVISBLE DISCOUNTS
    @Test
    void testBuyRegisteredCart_WithDiscount_Success_Item_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        List<CartItem> a = new LinkedList<>();
        a.add(item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("d1");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_CATEGORY_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("CategoryDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_TOTAL_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>150", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("TotalPriceDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_QUANTITY_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("QuantityDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_ITEM_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:200", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ItemDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_STORE_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("StoreDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_CATEGORY_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Home);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "HOME");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Home);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("CategoryDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_TOTAL_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 300, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>300", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 300, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(300.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 300.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("TotalPriceDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(300.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_QUANTITY_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("QuantityDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_ITEM_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ItemDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_STORE_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("StoreDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //COMPOSITE DISCOUNTS
    //OR
    @Test
    void testBuyRegisteredCart_WithOrDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //XOR
    @Test
    void testBuyRegisteredCart_WithXorDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithXorDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>4", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithXorDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //AND
    @Test
    void testBuyRegisteredCart_WithAndDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 134.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(134.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithAndDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //MAX
    @Test
    void testBuyRegisteredCart_WithMaxDiscount_Success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.8, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MAXDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MAXDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMaxDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MAXDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MAXDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //MUL
    @Test
    void testBuyRegisteredCart_WithMultiplyDiscount_Success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 50.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MulDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(50.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMultiplyDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MulDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyGuestCart_WithDiscount_Success_Item() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        List<CartItem> a = new LinkedList<>();
        a.add(item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_CATEGORY() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_TOTAL() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>150", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_QUANTITY() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_ITEM() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:200", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_STORE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_CATEGORY_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Home);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "HOME");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Home);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_TOTAL_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 300, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>300", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 300, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(300.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 300.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_QUANTITY_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_ITEM_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_STORE_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.VISIBLE, "STORE:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //COMPOSITE DISCOUNTS
//OR
    @Test
    void testbuyGuestCart_WithOrDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithOrDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithOrDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //XOR
    @Test
    void testbuyGuestCart_WithXorDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithXorDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>4", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithXorDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true); //
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //AND
    @Test
    void testbuyGuestCart_WithAndDiscount1() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount2() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount_success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 134.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(134.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //MAX
    @Test
    void testbuyGuestCart_WithMaxDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.8, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithMaxDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //MUL
    @Test
    void testbuyGuestCart_WithMultiplyDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 50.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(50.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithMultiplyDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //INVISBLE DISCOUNTS
    @Test
    void testbuyGuestCart_WithDiscount_Success_Item_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        List<CartItem> a = new LinkedList<>();
        a.add(item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("d1");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_CATEGORY_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("CategoryDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_TOTAL_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>150", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("TotalPriceDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_QUANTITY_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("QuantityDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_ITEM_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:200", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ItemDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_STORE_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 140.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("StoreDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(140.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_CATEGORY_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Home);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "HOME");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "CategoryDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Home);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("CategoryDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_TOTAL_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 300, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "TotalPriceDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>300", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 300, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(300.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 300.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("TotalPriceDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_QUANTITY_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "QuantityDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("QuantityDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_ITEM_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "ItemDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ItemDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    @Test
    void testbuyGuestCart_WithDiscountCondition_Success_STORE_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        int ownerId = 10;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addDiscountToStore(storeId, ownerToken, "StoreDiscount", 0.3,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:3", CreateDiscountDTO.Logic.SINGLE, new String[0]);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("StoreDiscount");

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );
//        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

//        assertEquals(1, receipts.length);
//        assertEquals("TestStore", receipts[0].getStoreName());
//        assertEquals(200.0, receipts[0].getFinalPrice());
    }

    //COMPOSITE DISCOUNTS
//OR
    @Test
    void testbuyGuestCart_WithOrDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithOrDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithOrDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("OrDiscount");

        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //XOR
    @Test
    void testbuyGuestCart_WithXorDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithXorDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>4", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        //Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithXorDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "XOrDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("XOrDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //AND
    @Test
    void testbuyGuestCart_WithAndDiscount1_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount2_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>0", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 80.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(80.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount_success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 134.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(134.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithAndDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "ANDDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("ANDDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //MAX
    @Test
    void testbuyGuestCart_WithMaxDiscount_Success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.8, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MAXDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MAXDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithMaxDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MAXDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MAXDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

    //MUL
    @Test
    void testbuyGuestCart_WithMultiplyDiscount_Success_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 50.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MulDiscount");
        ReceiptDTO[] receipts = real.purchaseService.buyGuestCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(50.0, receipts[0].getFinalPrice());
    }

    @Test
    void testbuyGuestCart_WithMultiplyDiscount_Failure_INVISIBLE() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem item1 = new CartItem(item);
        cart.addItem(storeId, item1);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[]{"d1", "d2"};
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "MulDiscount", 0.0, CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MULTIPLY, subDiscountNames);

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));
        CouponContext.set("MulDiscount");
        assertThrows(Exception.class, ()
                -> real.purchaseService.buyGuestCart(token, payment, supply)
        );

    }

}
