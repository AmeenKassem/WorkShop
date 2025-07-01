package workshop.demo.AcceptanceTests.Tests;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.DiscountEntities.VisibleDiscountEntity;

@SpringBootTest
@ActiveProfiles("test")
public class DiscountTests extends AcceptanceTests {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseTests.class);

    // =======================
    // Constants
    // =======================
    private static final String USER1_USERNAME = "user1";
    private static final String USER2_USERNAME = "user2";
    private static final String PASSWORD = "pass123";
    private static final String ENCODED_PASSWORD = "encodedPass123";

    private static final int USER1_ID = 1;
    private static final int USER2_ID = 2;

    private static final String STORE_NAME = "CoolStore";
    private static final String STORE_CATEGORY = "Electronics";

    private static final String PRODUCT_NAME = "Phone";
    private static final int PRODUCT_PRICE = 100;
    private static final String PRODUCT_DESC = "SMART PHONE";
    private static final String[] KEYWORD = {"Phone"};
    // =======================
    // Tokens
    // =======================
    private String user1Token = "user1Token";
    private String user2Token = "user2Token";
    // =======================
    // Entities
    // =======================
    private Registered user1 = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 19);
    private Registered user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 19);

    private Store store;
    private Product product;
    private StoreStock storeStock;
    private ActivePurcheses activePurcheses;

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

//         user1 = new Registered(0, USER1_USERNAME, ENCODED_PASSWORD, 19);
//         user2 = new Registered(0, USER2_USERNAME, ENCODED_PASSWORD, 20);

        //  Call save to trigger ID injection
        user1 = mockUserRepo.save(user1);
        user2 = mockUserRepo.save(user2);
        user1.login();
        user2.login();

        // === Store setup ===
        store = new Store(STORE_NAME, STORE_CATEGORY);

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

        user2.addToCart(new CartItem(
                new ItemCartDTO(store.getstoreId(), product.getProductId(), 2, PRODUCT_PRICE, product.getName(), STORE_NAME, Category.Electronics)
        ));

    }


    //SINGLE DISCOUNTS
    @Test
    void testBuyRegisteredCart_WithDiscount_Success_Item() throws Exception {
        //add the discount
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific item", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + product.getProductId(), CreateDiscountDTO.Logic.SINGLE, null);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);
        //assert add discount
        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(1, ((MultiplyDiscount) multi).getDiscounts().size());
        assertEquals("25% off for specific item", ((MultiplyDiscount) multi).getDiscounts().get(0).getName());
        //buy the cart

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        ReceiptDTO receipt = receipts[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertEquals(150.0, receipt.getFinalPrice(), 0.01); // 200 - 25% = 150

        // can even assert items
        assertEquals(1, receipt.getProductsList().size());
        ReceiptProduct prod = receipt.getProductsList().get(0);
        assertEquals(product.getName(), prod.getProductName());
        assertEquals(2, prod.getQuantity());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_CATEGORY() throws Exception {
        //add the discount
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific category", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);
        //assert add discount
        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(1, ((MultiplyDiscount) multi).getDiscounts().size());
        assertEquals("25% off for specific category", ((MultiplyDiscount) multi).getDiscounts().get(0).getName());
        //buy the cart

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        ReceiptDTO receipt = receipts[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertEquals(150.0, receipt.getFinalPrice(), 0.01); // 200 - 25% = 150

        // can even assert items
        assertEquals(1, receipt.getProductsList().size());
        ReceiptProduct prod = receipt.getProductsList().get(0);
        assertEquals(product.getName(), prod.getProductName());
        assertEquals(2, prod.getQuantity());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_TOTAL() throws Exception {
        // add the discount
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for total over 50", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(1, ((MultiplyDiscount) multi).getDiscounts().size());
        assertEquals("25% off for total over 50", ((MultiplyDiscount) multi).getDiscounts().get(0).getName());

        // buy the cart
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        ReceiptDTO receipt = receipts[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertEquals(150.0, receipt.getFinalPrice(), 0.01);

        assertEquals(1, receipt.getProductsList().size());
        ReceiptProduct prod = receipt.getProductsList().get(0);
        assertEquals(product.getName(), prod.getProductName());
        assertEquals(2, prod.getQuantity());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_QUANTITY() throws Exception {
        // add the discount
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for quantity over 1", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE, null);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(1, ((MultiplyDiscount) multi).getDiscounts().size());
        assertEquals("25% off for quantity over 1", ((MultiplyDiscount) multi).getDiscounts().get(0).getName());

        // buy the cart
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        ReceiptDTO receipt = receipts[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertEquals(150.0, receipt.getFinalPrice(), 0.01);

        assertEquals(1, receipt.getProductsList().size());
        ReceiptProduct prod = receipt.getProductsList().get(0);
        assertEquals(product.getName(), prod.getProductName());
        assertEquals(2, prod.getQuantity());
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Success_STORE() throws Exception {
        // add the discount
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific store", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + store.getstoreId(), CreateDiscountDTO.Logic.SINGLE, null);
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(1, ((MultiplyDiscount) multi).getDiscounts().size());
        assertEquals("25% off for specific store", ((MultiplyDiscount) multi).getDiscounts().get(0).getName());

        // buy the cart
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        ReceiptDTO receipt = receipts[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertEquals(150.0, receipt.getFinalPrice(), 0.01);

        assertEquals(1, receipt.getProductsList().size());
        ReceiptProduct prod = receipt.getProductsList().get(0);
        assertEquals(product.getName(), prod.getProductName());
        assertEquals(2, prod.getQuantity());
    }

    @Test
    void testBuyRegisteredCart_WithComposite_AND_Discount() throws Exception {
        // AND discount combining CATEGORY and TOTAL
        CreateDiscountDTO dto = new CreateDiscountDTO("AND discount", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.AND,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));

        // add discount
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        // prepare mocks for buying
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        // assertions
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0); // ensure some discount was applied under AND
    }

    @Test
    void testBuyRegisteredCart_WithComposite_OR_Discount() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("OR discount", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.OR,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0); // discounted by OR
    }

    @Test
    void testBuyRegisteredCart_WithComposite_XOR_Discount() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("XOR discount", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.XOR,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>201",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0); // discounted by XOR choice
    }

    @Test
    void testBuyRegisteredCart_WithComposite_MAX_Discount() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MAX discount", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MAX,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);

        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0); // best of discounts applied
    }

    @Test
    void testBuyRegisteredCart_WithComposite_MULTIPLY_Discount() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MULTIPLY discount", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MULTIPLY,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);

        assertNotNull(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0); // MULTIPLY applies sequentially
    }

    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Failure_CATEGORY() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for category Clothing", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(),store.getstoreId(),Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // no discount
    }
    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Failure_TOTAL() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for total over 500", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>500", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(),store.getstoreId(),Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // no discount
    }
    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Failure_QUANTITY() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for quantity over 10", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(),store.getstoreId(),Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // no discount
    }
    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Failure_STORE() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for wrong store", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "STORE:999", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(),store.getstoreId(),Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // no discount
    }
    @Test
    void testBuyRegisteredCart_WithDiscountCondition_Failure_Item() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for wrong store", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:999", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(),store.getstoreId(),Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenReturn(new VisibleDiscountEntity());

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // no discount
    }

    @Test
    void testBuyRegisteredCart_WithComposite_AND_Discount_Failure_Category() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("AND discount fails on category", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.AND,
                List.of(
                        new CreateDiscountDTO("25% off for wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // AND fails
    }
    @Test
    void testBuyRegisteredCart_WithComposite_AND_Discount_Failure_Total() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("AND discount fails on total", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.AND,
                List.of(
                        new CreateDiscountDTO("25% off for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for total > 1000", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        // same mocks...
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // AND fails
    }
    @Test
    void testBuyRegisteredCart_WithComposite_OR_Discount_Failure_AllFail() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("OR discount fails", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.OR,
                List.of(
                        new CreateDiscountDTO("25% off wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for high total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }

    @Test
    void testBuyRegisteredCart_WithComposite_OR_Discount_Failure_Fallback() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("OR fallback failure", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.OR,
                List.of(
                        new CreateDiscountDTO("25% off unknown store", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "STORE:9999",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% off for high quantity", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>20",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithComposite_XOR_Discount_Failure_AllFail() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("XOR discount fails both", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.XOR,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% too high total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithComposite_XOR_Discount_Failure_BothSucceed() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("XOR fails on both true", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.XOR,
                List.of(
                        new CreateDiscountDTO("25% for category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        // Both conditions match, typical XOR will not apply combined
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01); // XOR did not apply
    }
    @Test
    void testBuyRegisteredCart_WithComposite_MAX_Discount_Failure_AllFail() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MAX discount fails", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MAX,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% too high total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithComposite_MAX_Discount_Failure_Mixed() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MAX mixed fail", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MAX,
                List.of(
                        new CreateDiscountDTO("25% wrong store", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "STORE:9999",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% wrong quantity", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>20",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithComposite_MULTIPLY_Discount_Failure_Category() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MULTIPLY fails category", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MULTIPLY,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% for total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(180.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithComposite_MULTIPLY_Discount_Failure_All() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("MULTIPLY fails both", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "", CreateDiscountDTO.Logic.MULTIPLY,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing",
                                CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% too high total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000",
                                CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }

    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Success_ITEM() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible item discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:" + product.getProductId(), CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals("25% invisible item discount", ((MultiplyDiscount)multi).getDiscounts().get(0).getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible item discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(150.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Success_Category() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible category discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals("25% invisible category discount", ((MultiplyDiscount)multi).getDiscounts().get(0).getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible category discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(150.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Success_Store() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible store discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:" + store.getstoreId(), CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals("25% invisible store discount", ((MultiplyDiscount)multi).getDiscounts().get(0).getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible store discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(150.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Success_Total() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible total discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals("25% invisible total discount", ((MultiplyDiscount)multi).getDiscounts().get(0).getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible total discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(150.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Success_Quantity() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible quantity discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals("25% invisible quantity discount", ((MultiplyDiscount)multi).getDiscounts().get(0).getName());

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible quantity discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(150.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Failure_ITEM() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible wrong item discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:9999", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible wrong item discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Failure_Category() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible wrong category discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:Clothing", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible wrong category discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Failure_Store() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible wrong store discount", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:9999", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible wrong store discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Failure_Total() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible total discount fails", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible total discount fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisibleDiscount_Failure_Quantity() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("25% invisible quantity discount fails", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>100", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("25% invisible quantity discount fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_AND_Discount_Success() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible AND discount", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.AND,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible AND discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_OR_Discount_Success() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible OR discount", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.OR,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        CouponContext.set("Invisible OR discount");
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_XOR_Discount_Success() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible XOR discount", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.XOR,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));

        CouponContext.set("Invisible XOR discount");
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_MAX_Discount_Success() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible MAX discount", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.MAX,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible MAX discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 200.0);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_MULTIPLY_Discount_Success() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible MULTIPLY discount", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.MULTIPLY,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible MULTIPLY discount");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertTrue(receipts[0].getFinalPrice() < 150.0); // multi compounding
    }

    @Test
    void testBuyRegisteredCart_WithInvisible_AND_Discount_Failure() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible AND fails", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.AND,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible AND fails");
        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_OR_Discount_Failure() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible OR fails", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.OR,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% too high total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible OR fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_XOR_Discount_Failure() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible XOR fails", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.XOR,
                List.of(
                        new CreateDiscountDTO("25% category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible XOR fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_MAX_Discount_Failure() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible MAX fails", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.MAX,
                List.of(
                        new CreateDiscountDTO("25% wrong store", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "STORE:9999", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% wrong quantity", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible MAX fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(200.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testBuyRegisteredCart_WithInvisible_MULTIPLY_Discount_Failure() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Invisible MULTIPLY fails", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "", CreateDiscountDTO.Logic.MULTIPLY,
                List.of(
                        new CreateDiscountDTO("25% wrong category", 0.25,
                                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Clothing", CreateDiscountDTO.Logic.SINGLE, null),
                        new CreateDiscountDTO("10% total", 0.10,
                                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null)
                ));
        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(USER2_ID);
        when(mockUserRepo.findById(USER2_ID)).thenReturn(Optional.of(user2));
        when(mockStoreStock.findById(store.getstoreId())).thenReturn(Optional.of(storeStock));
        CouponContext.set("Invisible MULTIPLY fails");

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(user2Token, paymentDetails, supplyDetails);
        print(receipts);
        assertEquals(1, receipts.length);
        assertEquals(180.0, receipts[0].getFinalPrice(), 0.01);
    }
    @Test
    void testAddMultipleSequentialDiscounts() throws Exception {
        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off Electronics", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO dto2 = new CreateDiscountDTO("15% off total", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto1);
        storeService.addDiscount(store.getstoreId(), user1Token, dto2);

        Discount multi = store.getDiscount();
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
        assertEquals(2, ((MultiplyDiscount)multi).getDiscounts().size());
    }
    @Test
    void testAddMixVisibleInvisibleDiscounts() throws Exception {
        CreateDiscountDTO visible = new CreateDiscountDTO("10% Electronics", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO invisible = new CreateDiscountDTO("20% Total", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>50", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, visible);
        storeService.addDiscount(store.getstoreId(), user1Token, invisible);

        Discount multi = store.getDiscount();
        assertEquals(2, ((MultiplyDiscount)multi).getDiscounts().size());
    }

    @Test
    void testAddDiscountWithNullCondition() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Null condition", 0.1,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);
        Discount multi = store.getDiscount();
        assertNotNull(multi);
        assertEquals("MANUALLY_COMBINED_STORE0", multi.getName());
    }
    @Test
    void testAddSameDiscountTwice() throws Exception {
        CreateDiscountDTO dto = new CreateDiscountDTO("Duplicate", 0.2,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:Electronics", CreateDiscountDTO.Logic.SINGLE, null);

        when(mockAuthRepo.getUserId(user1Token)).thenReturn(user1.getId());
        when(mockUserRepo.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));
        when(suConnectionRepo.hasPermission(user1.getId(), store.getstoreId(), Permission.MANAGE_STORE_POLICY)).thenReturn(true);
        when(mockdiscountrepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        storeService.addDiscount(store.getstoreId(), user1Token, dto);
        storeService.addDiscount(store.getstoreId(), user1Token, dto);

        Discount multi = store.getDiscount();
        assertEquals(2, ((MultiplyDiscount)multi).getDiscounts().size());
    }

    void print(ReceiptDTO[] re) {
        System.out.println("=== RECEIPTS ARRAY ===");
        for (int i = 0; i < re.length; i++) {
            ReceiptDTO receipt = re[i];
            System.out.println("Receipt #" + (i + 1));
            System.out.println("  Date: " + receipt.getDate());
            System.out.println("  Store Name: " + receipt.getStoreName());
            System.out.println("  Final Price: " + receipt.getFinalPrice());
            System.out.println("  Payment Tx ID: " + receipt.getPaymentTransactionId());
            System.out.println("  Supply Tx ID: " + receipt.getSupplyTransactionId());
            System.out.println("  Products:");
            if (receipt.getProductsList() != null) {
                for (ReceiptProduct p : receipt.getProductsList()) {
                    System.out.println(
                            "-> ProductID: " + p.getProductId() +
                                    ", Name: " + p.getProductName() +
                                    ", Category: " + p.getCategory() +
                                    ", Quantity: " + p.getQuantity() +
                                    ", Price: " + p.getPrice() +
                                    ", StoreName: " + p.getStorename() +
                                    ", StoreID: " + p.getStoreId() +
                                    ", isSpecial: " + p.isSpecial() +
                                    ", Order: " + p.getOrder()
                    );
                }
            } else {
                System.out.println("    (No products)");
            }
            System.out.println("--------------------");
        }
        System.out.println("====================");
    }
}
