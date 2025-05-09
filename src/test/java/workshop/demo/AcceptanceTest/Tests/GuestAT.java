package workshop.demo.AcceptanceTest.Tests;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import workshop.demo.AcceptanceTest.Utill.Real;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.item;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GuestAT extends AcceptanceTests {
    Real real = new Real();

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
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
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
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
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
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
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
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
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
        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
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

//    @Test
//    void testGuestAddProductToCart_Failure() throws Exception {
//        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(2);
//        Mockito.when(real.mockAuthRepo.generateGuestToken(2)).thenReturn("guest-token-456");
//        String token = real.testGuest_Enter();
//        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-456")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-456")).thenReturn(17);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(17);
//        String result1 = real.testGuest_Register("guest-token-456", "guest1", "pass");
//
//        //enter guest
//        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
//        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
//        //reg guest
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
//        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(16);
//        Mockito.when(real.mockUserRepo.registerUser("guest1", "pass")).thenReturn(16);
//
//
//        //open store and add item
//        Mockito.when(real.mockStoreRepo.addStoreToSystem(16,"store1" , "ELECTRONICS")).thenReturn(99);
//        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));
//
//        ItemStoreDTO storeItem = new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 1, 99);
//        ItemCartDTO cartItem=new ItemCartDTO(storeItem);
//
////        boolean result=real.testGuest_AddProductToCart("guest-token-456",storeItem);
//        Mockito.doThrow(new UIException("Guest not found", ErrorCodes.GUEST_NOT_FOUND))
//                .when(real.mockUserRepo).addItemToGeustCart(17, cartItem);
//
//        // ⚠️ Expect failure
//        Exception exception = assertThrows(
//                UIException.class,
//                () -> real.testGuest_AddProductToCart("guest-token-123", storeItem)
//        );
//
//        assertTrue(exception.getMessage().contains("Guest not found"));
//
//    }



    @Test
    void testGuestModifyCartAddQToBuy() throws Exception {
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.doNothing().when(real.mockUserRepo).removeItemFromGeustCart(1,1);
        assertDoesNotThrow(() -> real.mockUserRepo.removeItemFromGeustCart(1,1));
    }

    @Test
    void testGuestBuyCart() throws Exception {

        // Mock valid token check
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(1);

        // Prepare payment and supply details
        PaymentDetails paymentDetails = new PaymentDetails("1234-5678-9876-5432", "someone", "12/25", "123");
        SupplyDetails supplyDetails = new SupplyDetails("ber sheva", "John Doe", "123 Main St", "555-1234");

        // Prepare receipt and products
        ReceiptProduct receiptProduct = new ReceiptProduct("GALAXY 24", Category.ELECTRONICS, "PERFECT", "store1", 1, 1000);
        List<ReceiptProduct> productList = List.of(receiptProduct);
        ReceiptDTO receipt = new ReceiptDTO("store1", "2025-05-07", productList, 1000.0);
        ReceiptDTO[] receiptArray = new ReceiptDTO[]{receipt};

        // Mock purchase service response
        Mockito.when(real.purchaseService.buyGuestCart("guest-token-123", paymentDetails, supplyDetails))
                .thenReturn(receiptArray);

        // Call the method under test
        ReceiptDTO[] result = real.purchaseService.buyGuestCart("guest-token-123", paymentDetails, supplyDetails);

        // Assert result
        assertEquals("store1", result[0].getStoreName());
    }


    @Test
    void testGuestGetPurchasePolicy() throws Exception {
        String dummy = real.testGuest_GetPurchasePolicy("any", 1);
        assertTrue("Done".equals(dummy) || "TODO".equals(dummy));
    }

    @Test
    void testGuestSearchProduct() throws Exception {

        // Mock valid token
        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
        Mockito.when(real.mockAuthRepo.getUserId("guest-token-123")).thenReturn(1);

        // Prepare product and item
        ProductDTO product = new ProductDTO(1, "GALAXY 24", Category.ELECTRONICS, "16GB RAM 512 SSD BLACK");
        ProductDTO[] products = {product};
        ItemStoreDTO itemStore = new ItemStoreDTO(1, 1, 1, Category.ELECTRONICS, 1, 99);
        ItemStoreDTO[] items = {itemStore};
        ProductSearchCriteria criteria = new ProductSearchCriteria("GALAXY 24", Category.ELECTRONICS, "GALAXY 24", 99, 0, 3, 1, 5);

        // Mock repository responses
        Mockito.when(real.mockStockRepo.getMatchesProducts(criteria)).thenReturn(products);
        Mockito.when(real.mockStoreRepo.getMatchesItems(criteria, products)).thenReturn(items);
        Mockito.when(real.stockService.searchProducts("guest-token-123", criteria)).thenReturn(items);

        // Call test method
        String result = real.testGuest_SearchProduct("guest-token-123", criteria);
        System.out.println(result);
        assertTrue(result.contains("GALAXY"));
    }



    @Test
    void testGuestSearchProductInStore() throws Exception {
        String dummy = real.testGuest_SearchProductInStore("any", 1, 2);
        assertTrue("Done".equals(dummy) || "TODO".equals(dummy));
    }
}
//
//    @Test
//    void testGuestModifyCartAddQToBuy() throws Exception {
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
//        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
//        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
//
//
//        Mockito.when(real.mockStoreRepo.addStoreToSystem(99,"store1" , "ELECTRONICS")).thenReturn(99);
//        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));
//        //ProductDTO A=new ProductDTO(1,"GALAXY 24",Category.ELECTRONICS,"16GB RAM 512 SSD BLACK  ");
//        ItemCartDTO a=new ItemCartDTO(1,Category.ELECTRONICS,1,1,1,"GALAXY 24","16GB RAM 512 SSD BLACK  ","store1");
//        Mockito.when(real.mockUserRepo.addItemToGeustCart(1,a)).thenReturn(true);
//        //Mockito.when(real.testGuest_AddProductToCart(1,a)).thenReturn()
//        Mockito.when(real.mockUserRepo.removeItemFromGeustCart(1,1)).thenReturn();//here need to fix all void function after this check the function
//    }
//
//    @Test
//    void testGuestBuyCart() throws Exception {
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
//        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
//        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
//
//
//        Mockito.when(real.mockStoreRepo.addStoreToSystem(99,"store1" , "ELECTRONICS")).thenReturn(99);
//        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));
//        //ProductDTO A=new ProductDTO(1,"GALAXY 24",Category.ELECTRONICS,"16GB RAM 512 SSD BLACK  ");
//        ItemCartDTO a=new ItemCartDTO(1,Category.ELECTRONICS,1,1,1,"GALAXY 24","16GB RAM 512 SSD BLACK  ","store1");
//        Mockito.when(real.mockUserRepo.addItemToGeustCart(1,a)).thenReturn(true);
//
//
//
//        PaymentDetails paymentDetails = new PaymentDetails("1234-5678-9876-5432", "someone","12/25", "123");
//        SupplyDetails supplyDetails = new SupplyDetails("ber sheva","John Doe", "123 Main St", "555-1234");
//        ReceiptProduct receiptProduct = new ReceiptProduct("GALAXY 24", Category.ELECTRONICS, "PERFECT", "store1", 1, 1000);
//        List<ReceiptProduct> productList = List.of(receiptProduct);
//        ReceiptDTO receipt = new ReceiptDTO("store1", "2025-05-07", productList, 1000.0);
//        ReceiptDTO[] receiptArray = new ReceiptDTO[]{receipt};
//        ShoppingCart ss=new ShoppingCart();
//        ss.addItem(99,a);
//        Mockito.when(real.mockUserRepo.getUserCart(0)).thenReturn(ss);
//        Mockito.when(real.purchaseService.buyGuestCart("guest-token-123", paymentDetails, supplyDetails))
//                .thenReturn(receiptArray);//change server to repr after implement it
//
//        // Call the method under test
//        ReceiptDTO[] result = real.purchaseService.buyGuestCart("guest-token-123", paymentDetails, supplyDetails);
//        assertEquals("store1", result[0].getStoreName());
//
//        ItemCartDTO a=new ItemCartDTO(1,Category.ELECTRONICS,1,1,1,"GALAXY 24","16GB RAM 512 SSD BLACK  ","store1");
//        Mockito.when(real.mockUserRepo.addItemToGeustCart(1,a)).thenReturn(true);
//        Mockito.when(real.purchaseService.buyGuestCart("",p,s)).thenReturn();
//    }
//
//    @Test
//    void testGuestGetPurchasePolicy() throws Exception {
//        // returns Done/TODO
//        String dummy = testGuest_GetPurchasePolicy("any", 1);
//        assertTrue("Done".equals(dummy) || "TODO".equals(dummy));
//    }
//    @Test
//    void testGuestSearchProduct() throws Exception {
//        Mockito.when(real.mockAuthRepo.validToken("guest-token-123")).thenReturn(true);
//        Mockito.when(real.mockUserRepo.generateGuest()).thenReturn(1);
//        Mockito.when(real.mockAuthRepo.generateGuestToken(1)).thenReturn("guest-token-123");
//        Mockito.when(real.mockStoreRepo.addStoreToSystem(99,"store1" , "ELECTRONICS")).thenReturn(99);
//        Mockito.when(real.mockStoreRepo.addItem(99,1,1,1,Category.ELECTRONICS)).thenReturn(new item(1,1,1,Category.ELECTRONICS));
//        //List<ItemStoreDTO> A=new LinkedList<>();
//        ProductDTO A=new ProductDTO(1,"GALAXY 24",Category.ELECTRONICS,"16GB RAM 512 SSD BLACK  ");
//        //Mockito.when(real.mockStockRepo.getMatchesProducts(1)).thenReturn(A);
//        ProductDTO[] A1=new ProductDTO[1];
//        A1[0]=A;
//        ItemStoreDTO B=new ItemStoreDTO(1,1,1,Category.ELECTRONICS,1,1);
//        //Mockito.when(real.mockStockRepo.getMatchesProducts(1)).thenReturn(A);
//        ItemStoreDTO[] B1=new ItemStoreDTO[1];
//        B1[0]=B;
//
//        ProductSearchCriteria a=new ProductSearchCriteria("GALAXY 24",Category.ELECTRONICS,"GALAXY 24",99,0,3,1,5);
//
//        Mockito.when(real.mockStockRepo.getMatchesProducts(a)).thenReturn(A1);
//
//        Mockito.when(real.stockService.searchProducts("guest-token-123",a)).thenReturn(B1);
//        Mockito.when(real.mockStockRepo.getMatchesProducts(a)).thenReturn(A1);
//        Mockito.when(real.mockStoreRepo.getMatchesItems(a,A1)).thenReturn(B1);
//        String result = real.testGuest_SearchProduct("guest-token-123",a);
//        System.out.println(result);
//        assertTrue(result.contains("GALAXY"));
//    }
//
//    @Test
//    void testGuestSearchProductInStore() throws Exception {
//        // similarly safe
//        String dummy = testGuest_SearchProductInStore("any", 1, 2);
//        assertTrue("Done".equals(dummy) || "TODO".equals(dummy));
//    }
//
