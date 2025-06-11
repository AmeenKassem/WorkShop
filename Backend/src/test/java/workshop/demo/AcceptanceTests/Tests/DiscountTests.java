package workshop.demo.AcceptanceTests.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import workshop.demo.AcceptanceTests.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
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
        when(real.mockStoreRepo.addStoreToSystem(ownerId, "TestStore", "ELECTRONICS")).thenReturn(storeId);
        when(real.mockIOSrepo.addNewStoreOwner(storeId, ownerId)).thenReturn(true);
        int createdStoreId = real.storeService.addStoreToSystem(ownerToken, "TestStore", "ELECTRONICS");
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
        int randomid = real.stockService.setProductToRandom(ownerToken, productId, 1, 100, storeId, 5000);
        int bidid = real.stockService.setProductToBid(ownerToken, storeId, productId, 1);
        int auctionid = real.stockService.setProductToAuction(ownerToken, storeId, productId, 1, 5000, 2);
//        System.out.println(randomid + "" + bidid + "" + auctionid);
    }

    @Test
    void testBuyRegisteredCart_WithDiscount_Success() throws Exception {
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

        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        when(real.mockIOSrepo.hasPermission(ownerId, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        // Add a 30% discount on the product directly
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
        assertEquals(140.0, receipts[0].getFinalPrice()); // Confirm discount was applied
    }

    @Test
    void testBuyRegisteredCart_WithOrDiscount() throws Exception {
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
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, new String[0]);
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
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[] { "d1", "d2" };
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

    @Test
    void testBuyRegisteredCart_WithXorDiscount() throws Exception {
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
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);


        //Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true); // Expecting max discount = 50%
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
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, subDiscountNames);


        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true); // 200 - 30% = 140
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice()); // Should only apply d1 with 30% discount
    }








    @Test
    void testBuyRegisteredCart_WithAndDiscount() throws Exception {
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


        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.3, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, new String[0]);
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
        assertEquals(80.0, receipts[0].getFinalPrice()); // adjust this if your actual chain is multiplicative or additive
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



        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");

        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.03, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, subDiscountNames);


        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore"))).thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 134.0)).thenReturn(true); // final price
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(134.0, receipts[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMaxDiscount_TakesHighest() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Cart with one ELECTRONICS product
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        // Discount permissions
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        // Sub-discounts for MAX logic

        // Store setup
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:"+productId, CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.8, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);


        // Cart processing
        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // base price

        // Payment/Supply
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true); // 200 - 40%
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        // Purchase call
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        // Validate result
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(40.0, receipts[0].getFinalPrice()); // 40% discount applied
    }

    @Test
    void testBuyRegisteredCart_WithMaxDiscount_Failure() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Cart with one ELECTRONICS product
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO item = new ItemCartDTO(storeId, productId, 1, 200, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(item);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        // Discount permissions
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);


        // Store setup
        Store store = new Store(storeId, "TestStore", "ELECTRONICS");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        String[] subDiscountNames = new String[] { "d1", "d2" };
        real.storeService.addDiscountToStore(storeId, ownerToken, "d1", 0.3, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + (productId + 1), CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "d2", 0.5, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, new String[0]);
        real.storeService.addDiscountToStore(storeId, ownerToken, "OrDiscount", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, subDiscountNames);

        // Cart processing
        ReceiptProduct receiptProduct = new ReceiptProduct("Phone", "TestStore", 1, 200, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(receiptProduct));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // base price

        // Payment/Supply
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 40.0)).thenReturn(true); // 200 - 40%
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        // Purchase call
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        // Validate result
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(200.0, receipts[0].getFinalPrice());
    }

}
