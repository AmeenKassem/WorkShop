package workshop.demo.AcceptanceTests.Tests;

import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.AcceptanceTests.Utill.Real;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class ATPurchasePolicyTests {

    Real real = new Real();
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImp.class);

    public ATPurchasePolicyTests() throws Exception {
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
        int randomid = real.stockService.setProductToRandom(ownerToken, productId, 1, 100, storeId, 5000);
        int bidid = real.stockService.setProductToBid(ownerToken, storeId, productId, 1);
        int auctionid = real.stockService.setProductToAuction(ownerToken, storeId, productId, 1, 5000, 2);
    }


    @Test
    void testBuyRegisteredCart_WithMinQuantityPolicy_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Setup cart with quantity = 2 (meets min quantity of 1)
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 2, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        List<CartItem> userCart = List.of(cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        real.storeService.addPurchasePolicy("user-token", storeId, "MIN_QTY", 2);

        // Mock product processing
        ReceiptProduct processedItem = new ReceiptProduct("Phone", "TestStore", 2, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);

        // Mock payment & supply
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
    void testBuyRegisteredCart_NoAlcoholPolicy_BlockUnder18() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Setup cart with quantity = 2 (meets min quantity of 1)
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 2, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        List<CartItem> userCart = List.of(cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        real.storeService.addPurchasePolicy("user-token", storeId, "NO_ALCOHOL", null);

        // Mock product processing
        ReceiptProduct processedItem = new ReceiptProduct("Phone", "TestStore", 2, 100, productId, Category.ALCOHOL);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0);
        when(real.mockUserRepo.getUserDTO(userId)).thenReturn(new UserDTO(userId, "user2", 17, true, false));

        // Mock payment & supply
        PaymentDetails payment = new PaymentDetails("1234123412341234", "Test User", "12/26", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);

        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyRegisteredCart(token, payment, supply);
        });

        assertTrue(ex.getMessage().contains("18"));


    }

    @Test
    void testBuyRegisteredCart_NoAlcoholPolicy_AllowForAdult() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 50, "Beer", "TestStore", Category.ALCOHOL);
        cart.addItem(storeId, new CartItem(itemDTO));
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);

        real.storeService.addPurchasePolicy("user-token", storeId, "NO_ALCOHOL", null);

        when(real.mockUserRepo.getUserDTO(userId)).thenReturn(new UserDTO(userId, "user2", 19, true, false));
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(new ReceiptProduct("Beer", "TestStore", 1, 50, productId, Category.ALCOHOL)));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(50.0);
        when(real.mockPay.processPayment(any(), eq(50.0))).thenReturn(true);
        when(real.mockSupply.processSupply(any())).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] result = real.purchaseService.buyRegisteredCart(token, PaymentDetails.testPayment(), new SupplyDetails("City", "State", "Zip", "Address"));

        assertEquals(1, result.length);
        assertEquals("TestStore", result[0].getStoreName());
        assertEquals(50.0, result[0].getFinalPrice());
    }

    @Test
    void testBuyRegisteredCart_WithMinQuantityPolicy_FailDueToLowQuantity() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Quantity = 1 < required 2
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 100, "Phone", "TestStore", Category.Electronics);
        cart.addItem(storeId, new CartItem(itemDTO));
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        real.storeService.addPurchasePolicy("user-token", storeId, "MIN_QTY", 2);

        // Product info
        ReceiptProduct processedItem = new ReceiptProduct("Phone", "TestStore", 1, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);

        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyRegisteredCart(token, PaymentDetails.testPayment(), new SupplyDetails("City", "State", "Zip", "Address"));
        });

        assertTrue(ex.getMessage().contains("quantity"));
    }

    @Test
    void testBuyRegisteredCart_InvalidPolicyName_ShouldNotBlock() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 100, "Phone", "TestStore", Category.Electronics);
        cart.addItem(storeId, new CartItem(itemDTO));
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);

        // Add invalid policy name (should be ignored or pass)
        Exception ex = assertThrows(Exception.class, () -> {
            real.storeService.addPurchasePolicy("user-token", storeId, "INVALID_POLICY", null);
        });

        assertEquals(ex.getMessage(), "Unknown Policy!");

    }

    @Test
    void testBuyRegisteredCart_WithMinQtyPolicyAndDiscount_Success() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        // Mock user and cart
        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 2, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        // Mock store and permissions
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        // Apply purchase policy: MIN_QTY = 2
        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);

        // Apply visible discount: 50% on Electronics
        CreateDiscountDTO discount = new CreateDiscountDTO(
                "50% Electronics",
                0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                List.of()
        );
        String []a= new String[0];
        real.storeService.addDiscountToStore(storeId, ownerToken, "50% Electronics",
                0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                a);

        // Mock processed cart item and total
        ReceiptProduct processedItem = new ReceiptProduct("Phone", "TestStore", 2, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // 2 × 100

        // Mock payment & supply
        PaymentDetails payment = new PaymentDetails("4111111111111111", "Test User", "12/30", "123");
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 100.0)).thenReturn(true); // after 50% discount
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        // Run the purchase
        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        // Assertions
        assertEquals(1, receipts.length);
        assertEquals("TestStore", receipts[0].getStoreName());
        assertEquals(100.0, receipts[0].getFinalPrice()); // After 50% discount
    }

    @Test
    void testBuyGuestCart_WithDiscountButFailsMinQtyPolicy() throws Exception {
        int guestId = 55;
        String guestToken = "guest-token-55";
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        // Arrange guest and token
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockUserRepo.isRegistered(guestId)).thenReturn(false);

        // Guest cart with quantity = 1 (less than required MIN_QTY=2)
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(cart);

        // Store and permissions
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        // Policy: MIN_QTY = 2
        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);

        // Discount: 40% on Electronics
        CreateDiscountDTO discount = new CreateDiscountDTO(
                "40% Discount", 0.4,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                List.of()
        );
        String []a= new String[0];
        real.storeService.addDiscountToStore(storeId, ownerToken, "40% Discount", 0.4,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                a);

        // Mock product processing
        ReceiptProduct processedItem = new ReceiptProduct("Phone", "TestStore", 1, 100, productId, Category.Electronics);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);

        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");

        // Act + Assert
        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyGuestCart(guestToken, payment, supply);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
    }

    @Test
    void testBuyRegisteredCart_BothPolicyAndDiscountInvalid_ShouldFail() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);

        CreateDiscountDTO discount = new CreateDiscountDTO(
                "InvalidDiscount", 0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE,
                List.of()
        );
        String []a= new String[0];
        real.storeService.addDiscountToStore(storeId, ownerToken, "InvalidDiscount", 0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE,
                a);
        ReceiptProduct item = new ReceiptProduct("Phone", "TestStore", 1, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(item));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);

        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");

        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyRegisteredCart(token, payment, supply);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
    }

    @Test
    void testBuyRegisteredCart_MinQtyPolicyValid_DiscountIgnoredDueToScope() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 2, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);

        CreateDiscountDTO discount = new CreateDiscountDTO(
                "Invalid 50%", 0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:BOOKS",
                CreateDiscountDTO.Logic.SINGLE,
                List.of()
        );
        String []a= new String[0];
        real.storeService.addDiscountToStore(storeId, ownerToken, "Invalid 50%", 0.5,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:BOOKS",
                CreateDiscountDTO.Logic.SINGLE,
                a);
        ReceiptProduct item = new ReceiptProduct("Phone", "TestStore", 2, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(item));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(200.0); // full price

        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");
        when(real.mockPay.processPayment(payment, 200.0)).thenReturn(true);
        when(real.mockSupply.processSupply(supply)).thenReturn(true);
        doNothing().when(real.mockOrderRepo).setOrderToStore(eq(storeId), eq(userId), any(), eq("TestStore"));

        ReceiptDTO[] receipts = real.purchaseService.buyRegisteredCart(token, payment, supply);

        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice()); // discount not applied
    }

    @Test
    void testBuyRegisteredCart_MinQtyPolicyFails_DiscountValid_ShouldFail() throws Exception {
        String token = "user-token-2";
        int userId = 20;
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        when(real.mockAuthRepo.getUserId(token)).thenReturn(userId);

        // Quantity = 1 (less than MIN_QTY=2)
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 1, 100, "Phone", "TestStore", Category.Electronics);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(userId)).thenReturn(cart);

        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_STORE_POLICY)).thenReturn(true);

        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);

        CreateDiscountDTO discount = new CreateDiscountDTO(
                "20% Electronics", 0.2,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                List.of()
        );
        String []a= new String[0];
        real.storeService.addDiscountToStore(storeId, ownerToken, "20% Electronics", 0.2,
                CreateDiscountDTO.Type.VISIBLE,
                "CATEGORY:Electronics",
                CreateDiscountDTO.Logic.SINGLE,
                a);

        ReceiptProduct item = new ReceiptProduct("Phone", "TestStore", 1, 100, productId, Category.Electronics);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(false), eq("TestStore")))
                .thenReturn(List.of(item));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);

        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");

        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyRegisteredCart(token, payment, supply);
        });

        assertTrue(ex.getMessage().toLowerCase().contains("quantity"));
    }

    @Test
    void testGuestBuy_WithMinQtyAndNoAlcoholPolicy_FailsDueToAge() throws Exception {
        int guestId = 55;
        String guestToken = "guest-token-55";
        int storeId = 100;
        int productId = 200;
        String ownerToken = "user-token";

        // Guest token and repo setup
        when(real.mockAuthRepo.getUserId(guestToken)).thenReturn(guestId);
        when(real.mockAuthRepo.validToken(guestToken)).thenReturn(true);
        when(real.mockUserRepo.isRegistered(guestId)).thenReturn(false);

        // Cart setup: quantity is valid
        ShoppingCart cart = new ShoppingCart();
        ItemCartDTO itemDTO = new ItemCartDTO(storeId, productId, 2, 50, "Beer", "TestStore", Category.ALCOHOL);
        CartItem cartItem = new CartItem(itemDTO);
        cart.addItem(storeId, cartItem);
        when(real.mockUserRepo.getUserCart(guestId)).thenReturn(cart);

        // Store and permissions
        Store store = new Store(storeId, "TestStore", "Electronics");
        when(real.mockStoreRepo.findStoreByID(storeId)).thenReturn(store);
        when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("TestStore");
        when(real.mockIOSrepo.hasPermission(10, storeId, Permission.MANAGE_PURCHASE_POLICY)).thenReturn(true);

        // Add both policies
        real.storeService.addPurchasePolicy(ownerToken, storeId, "MIN_QTY", 2);
        real.storeService.addPurchasePolicy(ownerToken, storeId, "NO_ALCOHOL", null);

        // Processed cart
        ReceiptProduct processedItem = new ReceiptProduct("Beer", "TestStore", 2, 50, productId, Category.ALCOHOL);
        when(real.mockStockRepo.checkAvailability(any())).thenReturn(true);
        when(real.mockStockRepo.processCartItemsForStore(eq(storeId), any(), eq(true), eq("TestStore")))
                .thenReturn(List.of(processedItem));
        when(real.mockStockRepo.calculateTotalPrice(any())).thenReturn(100.0);
        when(real.mockUserRepo.getUserDTO(guestId)).thenReturn(new UserDTO(guestId, "user2", 17, true, false));

        // Payment & supply
        PaymentDetails payment = PaymentDetails.testPayment();
        SupplyDetails supply = new SupplyDetails("City", "State", "Zip", "Address");

        // Should fail because NO_ALCOHOL blocks underage guests (default guest age = under 18)
        Exception ex = assertThrows(Exception.class, () -> {
            real.purchaseService.buyGuestCart(guestToken, payment, supply);
        });
        System.out.println(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("18"));
    }

}
