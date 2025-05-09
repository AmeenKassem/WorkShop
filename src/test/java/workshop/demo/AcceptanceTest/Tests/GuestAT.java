package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.User.ShoppingCart;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GuestAT extends AcceptanceTests {
    Real real = new Real();
    @BeforeEach
    void setup() throws Exception {
        real = new Real();

        // Setup admin
        Mockito.when(real.mockAuthRepo.validToken("admin-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("admin-token")).thenReturn(999);
        Mockito.when(real.mockAuthRepo.getUserName("admin-token")).thenReturn("admin");
        Mockito.when(real.mockUserRepo.isRegistered(999)).thenReturn(true);
        Mockito.when(real.mockUserRepo.isOnline(999)).thenReturn(true);
        testSystem_InitMarket("admin-token");

    }
    @Test
    void testGuestEnter_Success() throws Exception {
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");

        String token = real.testGuest_Enter();
        assertEquals("guest-token-456", token);
    }

    @Test
    void testGuestEnter_Failure() {
        Mockito.when(real.mockUserRepo.generateGuest()).thenThrow(new RuntimeException("DB error"));

        Exception ex = assertThrows(RuntimeException.class, real::testGuest_Enter);
        assertEquals("DB error", ex.getMessage());
    }

    @Test
    void testGuestExit_Success() throws Exception {
        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");

        String token = real.testGuest_Enter();
        assertEquals("guest-token-456", token);
        //exit guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(1);

        boolean result = real.testGuest_Exit("guest-token-456");
        assertTrue(result);
    }

    @Test
    void testGuestExit_Failure() throws Exception {
        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        assertEquals("guest-token-456", token);
        Mockito.when(real.mockAuthRepo.validToken("invalid")).thenReturn(false);
        Exception ex = assertThrows(Exception.class, () -> real.testGuest_Exit("invalid"));
        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testGuestRegister_Success() throws Exception {
        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        assertEquals("guest-token-456", token);
        //reg guest

        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);

        String result = real.testGuest_Register("guest-token-456", "guest1", "pass");
        assertEquals("true", result);
    }

    @Test
    void testGuestRegister_Failure_InvalidToken() throws Exception {
        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        assertEquals("guest-token-456", token);

        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(false);

        Exception ex = assertThrows(Exception.class, () -> real.testGuest_Register("guest-token-456", "guest1", "badpass"));
        assertEquals("Invalid token!", ex.getMessage());
    }

    @Test
    void testGuestGetStoreProducts() throws Exception {
        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        //assertEquals("guest-token-456", token);
        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
        //String token = real.testGuest_Enter();
        //assertEquals("guest-token-123", token);
        //reg guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
        //String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //open store and add item
        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));

        //get store product
        List<ItemStoreDTO> products = List.of(
                new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 3, 16));

        Mockito.when(real.mockStoreRepo.getProductsInStore(99)).thenReturn(products);
        String result = real.testGuest_GetStoreProducts(99);
        System.out.println(result);
        assertFalse(result.equals("[]"));
    }

    @Test
    void testGuestGetStoreProducts_Failure() throws Exception {

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        //assertEquals("guest-token-456", token);
        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
        //String token = real.testGuest_Enter();
        //assertEquals("guest-token-123", token);
        //reg guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
        //String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //open store and add item
        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));

        //Mockito.when(real.mockStoreRepo.getProductsInStore(16)).thenReturn(new ArrayList<>());
        String result = real.testGuest_GetStoreProducts(99);
        System.out.println(result);
        assertEquals("[]", result);
    }

    @Test
    void testGuestGetProductInfo() throws Exception {
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        //assertEquals("guest-token-456", token);
        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
        //String token = real.testGuest_Enter();
        //assertEquals("guest-token-123", token);
        //reg guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
        //String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //open store and add item
        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));
        //INVALIDDDDDDDDDDDDDD TOKEEEEEN
        //get store product
        List<ItemStoreDTO> products = List.of(
                new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 3, 16));
        ProductDTO a=new ProductDTO(1,"GALAXY 24",Category.ELECTRONICS,"PERFECT PHONE");
        Mockito.when(real.mockStockRepo.GetProductInfo(1)).thenReturn(a);
        String result = real.testGuest_GetProductInfo("guest-token-456",1);
        System.out.println(result);
        assertTrue(result.equals("1 GALAXY 24 ELECTRONICS PERFECT PHONE"));
    }
    @Test
    void testGuestGetProductInfo_Failure() throws Exception {
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        //assertEquals("guest-token-456", token);
        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
        //String token = real.testGuest_Enter();
        //assertEquals("guest-token-123", token);
        //reg guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
        //String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //open store and add item
        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
        //Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));

        //get store product
        List<ItemStoreDTO> products = List.of(
                new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 3, 16));
        ProductDTO a=new ProductDTO(1,"GALAXY 24",Category.ELECTRONICS,"PERFECT PHONE");
        //Mockito.when(real.mockStockRepo.GetProductInfo(1)).thenReturn(a);

        Exception ex = assertThrows(Exception.class, () -> real.testGuest_GetProductInfo("guest-token-456",1));
        assertEquals("Product not found.", ex.getMessage());
//        String result = real.testGuest_GetProductInfo("guest-token-456",1);
//        System.out.println(result);
//        assertTrue(result.equals("1 GALAXY 24 ELECTRONICS PERFECT PHONE"));
    }

    @Test
    void testGuestAddProductToCart_Success() throws Exception {
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
        String token = real.testGuest_Enter();
        //assertEquals("guest-token-456", token);
        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //enter guest
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
        //String token = real.testGuest_Enter();
        //assertEquals("guest-token-123", token);
        //reg guest
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
        //String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
        //assertEquals("true", result1);

        //open store and add item
        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));

        ItemStoreDTO storeItem = new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 1, 99);
//        Mockito.doNothing().when(real.mockUserRepo).addItemToGeustCart(Mockito.eq(1), Mockito.any());
        ItemCartDTO cartitem=new ItemCartDTO(storeItem);
        Mockito.doNothing().when(real.mockUserRepo).addItemToGeustCart(17, cartitem);
        boolean result = real.testGuest_AddProductToCart("guest-token-456", storeItem);
        System.out.println(result);
        assertTrue(result);
    }

    @Test
    void testGuestAddProductToCart_Failure() throws Exception {
        // --- Step 1: Owner enters and registers ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        Mockito.when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store and adds product ---
        int storeId = 100;
        int productId = 200;
        Mockito.when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");
        Mockito.when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        real.testOwner_ManageInventory_AddProduct(storeId, "owner-token", productId, 10, 100, Category.ELECTRONICS);

        // --- Step 3: Guest enters (no registration)
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);


        ItemStoreDTO storeItem = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 1, storeId);
        ItemCartDTO cartItem = new ItemCartDTO(storeItem);

        Mockito.doThrow(new UIException("Guest not found", ErrorCodes.GUEST_NOT_FOUND))
                .when(real.mockUserRepo).addItemToGeustCart(Mockito.eq(20), Mockito.any(ItemCartDTO.class));

        Exception ex = assertThrows(UIException.class,
                () -> real.testGuest_AddProductToCart("guest-token", storeItem));
        assertEquals("Guest not found", ex.getMessage());
    }


    @Test
    void testGuestBuyCart_Success() throws Exception {
        // --- Step 1: Owner enters and registers ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        Mockito.when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store and adds product ---
        int storeId = 100;
        int productId = 200;
        Mockito.when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");
        Mockito.when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        real.testOwner_ManageInventory_AddProduct(storeId, "owner-token", productId, 10, 100, Category.ELECTRONICS);

        // --- Step 3: Guest enters (no registration) ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // --- Step 4: Guest adds product to cart ---
        ItemStoreDTO storeItem = new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 1, storeId);
        ItemCartDTO cartItem = new ItemCartDTO(storeItem);
        Mockito.doNothing().when(real.mockUserRepo).addItemToGeustCart(20, cartItem);
        real.testGuest_AddProductToCart("guest-token", storeItem);

        // --- Step 5: Setup mocks for purchase service ---
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        Mockito.when(real.mockUserRepo.getUserCart(20)).thenReturn(mockCart);
        Mockito.when(mockCart.getAllCart()).thenReturn(List.of(cartItem));
        Mockito.when(mockCart.getBaskets()).thenReturn(new HashMap<>());

        Mockito.when(real.mockStoreRepo.checkAvailability(Mockito.any())).thenReturn(true);
        Mockito.when(real.mockStockRepo.findById(productId))
                .thenReturn(new Product("name", productId, Category.ELECTRONICS, "desc", null));
        Mockito.when(real.mockStoreRepo.getStoreNameById(storeId)).thenReturn("store1");
        //TODO:somthing need to update
        Mockito.when(real.mockPay.processPayment(Mockito.any(), Mockito.anyDouble())).thenReturn(true);
        Mockito.when(real.mockSupply.processSupply(Mockito.any())).thenReturn(true);




        String result = real.testGuest_BuyCart("guest-token");
        assertEquals("Done", result);
    }
    @Test
    void testGuestBuyCart_Failure() throws Exception {
        // --- Step 1: Owner enters and registers ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        Mockito.when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store and adds product ---
        int storeId = 100;
        int productId = 200;
        Mockito.when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");
        Mockito.when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        real.testOwner_ManageInventory_AddProduct(storeId, "owner-token", productId, 10, 100, Category.ELECTRONICS);

        // --- Step 3: Guest enters (no registration) ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // --- Step 4: Simulate guest cart as EMPTY ---
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        Mockito.when(real.mockUserRepo.getUserCart(20)).thenReturn(mockCart);
        Mockito.when(mockCart.getAllCart()).thenReturn(List.of());  // empty list

        // --- Step 5: Expect failure on empty cart ---
        Exception ex = assertThrows(UIException.class, () -> real.testGuest_BuyCart("guest-token"));
        assertEquals("Shopping cart is empty or not found", ex.getMessage());

        // --- Step 6: Add dummy item but simulate store availability failure ---
        ItemCartDTO dummyItem = Mockito.mock(ItemCartDTO.class);
        Mockito.when(mockCart.getAllCart()).thenReturn(List.of(dummyItem));
        Mockito.when(mockCart.getBaskets()).thenReturn(new HashMap<>());
        Mockito.when(real.mockStoreRepo.checkAvailability(Mockito.any())).thenReturn(false);

        Exception ex2 = assertThrows(UIException.class, () -> real.testGuest_BuyCart("guest-token"));
        assertEquals("Not all items are available for guest purchase", ex2.getMessage());

    }




    @Test
    void testGuestGetPurchasePolicy() throws Exception {
        // --- Step 1: Owner enters and registers ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        Mockito.when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");

        // --- Step 2: Owner creates store ---
        int storeId = 100;
        Mockito.when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");

        // --- Step 3: Guest enters (no registration) ---
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // TODO: need to implement this

        String result = real.testGuest_GetPurchasePolicy("guest-token", storeId);


    }


    @Test
    void testGuestSearchProduct() throws Exception {
        // Step 1: Guest enters
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token");
        String guestToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("guest-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token")).thenReturn(20);

        // Step 2: Owner enters and creates store and product
        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("owner-token");
        String ownerToken = real.testGuest_Enter();
        Mockito.when(real.mockAuthRepo.validToken("owner-token")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("owner-token")).thenReturn(10);
        Mockito.when(real.mockUserRepo.registerUser("owner1", "pass")).thenReturn(10);
        real.testGuest_Register("owner-token", "owner1", "pass");
        Mockito.when(real.mockUserRepo.isRegistered(10)).thenReturn(true);

        int storeId = 100;
        int productId = 200;

        Mockito.when(real.mockStoreRepo.addStoreToSystem(10, "store1", "ELECTRONICS")).thenReturn(storeId);
        real.testUser_OpenStore("owner-token", "store1", "ELECTRONICS");

        Mockito.when(real.mockStoreRepo.addItem(storeId, productId, 10, 100, Category.ELECTRONICS))
                .thenReturn(new item(productId, 10, 100, Category.ELECTRONICS));
        real.testOwner_ManageInventory_AddProduct(storeId, "owner-token", productId, 10, 100, Category.ELECTRONICS);

        // Step 3: Mock search flow
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                "TestProduct", Category.ELECTRONICS, "TestKeyword", storeId, 0, 1000, 0, 5);

        ProductDTO[] matchedProducts = new ProductDTO[]{
                new ProductDTO(productId, "TestProduct", Category.ELECTRONICS, "TestDescription")
        };
        ItemStoreDTO[] matchedItems = new ItemStoreDTO[]{
                new ItemStoreDTO(productId, 10, 100, Category.ELECTRONICS, 4, storeId)
        };

        Mockito.when(real.mockStockRepo.getMatchesProducts(Mockito.eq(criteria)))
                .thenReturn(matchedProducts);
        Mockito.when(real.mockStoreRepo.getMatchesItems(Mockito.eq(criteria), Mockito.eq(matchedProducts)))
                .thenReturn(matchedItems);
        //error in understood the search
        // Step 4: Execute test
        String result = real.testGuest_SearchProduct("guest-token", criteria);
        System.out.println(result);
        assertFalse(result.isEmpty(), "Expected non-empty search result");
    }


    @Test
    void testGuestSearchProductInStore() throws Exception {

    }
    @Test
    void testGuestModifyCartAddQToBuy() throws Exception {
    }

}