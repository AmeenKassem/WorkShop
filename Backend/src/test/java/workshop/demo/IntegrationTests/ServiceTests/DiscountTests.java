package workshop.demo.IntegrationTests.ServiceTests;

import java.util.List;

import static org.junit.Assert.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;
import workshop.demo.ApplicationLayer.ActivePurchasesService;
import workshop.demo.ApplicationLayer.DatabaseCleaner;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.External.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.External.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.CouponContext;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.NodeJPARepository;
import workshop.demo.InfrastructureLayer.OfferJpaRepository;
// import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StoreTreeJPARepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // allows non-static @BeforeAll
//@Transactional
public class DiscountTests {

    @Autowired StoreTreeJPARepository tree;
    @Autowired private NodeJPARepository node;
    @Autowired private IStockRepoDB stockRepositoryjpa;
    @Autowired private IStoreRepoDB storeRepositoryjpa;
    @Autowired private GuestJpaRepository guestRepo;
    @Autowired private IOrderRepoDB orderRepository;
    @Autowired private AuthenticationRepo authRepo;
    @Autowired PaymentServiceImp payment;
    @Autowired SupplyServiceImp serviceImp;
    @Autowired private IStoreStockRepo storeStockRepo;
    @Autowired private OfferJpaRepository offerRepo;
    @Autowired SUConnectionRepository sIsuConnectionRepo;
    @Autowired private UserJpaRepository userRepo;
    @Autowired Encoder encoder;
    @Autowired UserSuspensionService suspensionService;
    @Autowired UserService userService;
    @Autowired StockService stockService;
    @Autowired StoreService storeService;
    @Autowired PurchaseService purchaseService;
    @Autowired OrderService orderService;
    @Autowired ReviewService reviewService;
    @Autowired public ActivePurchasesService activePurcheses;

    @Autowired DatabaseCleaner databaseCleaner;

    // ======================== Test Data ========================
    String NOToken;
    String NGToken;
    String GToken;
    String Admin;
    ItemStoreDTO itemStoreDTO;
    int createdStoreId;

    @BeforeAll
    void setup() throws Exception {
        databaseCleaner.wipeDatabase();
var paymentServiceImp = Mockito.mock(PaymentServiceImp.class);
     var   supplyServiceImp = Mockito.mock(SupplyServiceImp.class);

    when(paymentServiceImp.processPayment(any(PaymentDetails.class), anyDouble()))
    .thenReturn(42);
    when(supplyServiceImp.processSupply(any(SupplyDetails.class)))
    .thenReturn(55555);
    purchaseService.setPaymentService(paymentServiceImp);
        purchaseService.setSupplyService(supplyServiceImp);
        GToken = userService.generateGuest();
        userService.register(GToken, "user", "user", 25);
        NGToken = userService.login(GToken, "user", "user");

        String OToken = userService.generateGuest();
        userService.register(OToken, "owner", "owner", 25);
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));

        createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        String[] keywords = { "Laptop", "Lap", "top" };
        int productId = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

        stockService.addItem(createdStoreId, NOToken, productId, 9999999, 2000, Category.Electronics);
        itemStoreDTO = new ItemStoreDTO(productId, 9999999, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");

        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        Admin = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(Admin, "123321", authRepo.getUserId(Admin));
        suspensionService.suspendRegisteredUser(authRepo.getUserId(NOToken),
                2,Admin);
                suspensionService.pauseSuspension(authRepo.getUserId(NOToken), Admin);
    }
@BeforeEach
void setup2() throws Exception
{
databaseCleaner.wipeDiscounts();
if (!storeRepositoryjpa.findAll().isEmpty()) {
    createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();
}
else {
    databaseCleaner.wipeDatabase();
var paymentServiceImp = Mockito.mock(PaymentServiceImp.class);
     var   supplyServiceImp = Mockito.mock(SupplyServiceImp.class);

    when(paymentServiceImp.processPayment(any(PaymentDetails.class), anyDouble()))
    .thenReturn(42);
    when(supplyServiceImp.processSupply(any(SupplyDetails.class)))
    .thenReturn(55555);
    purchaseService.setPaymentService(paymentServiceImp);
        purchaseService.setSupplyService(supplyServiceImp);
    GToken = userService.generateGuest();
    userService.register(GToken, "user", "user", 25);
    NGToken = userService.login(GToken, "user", "user");

    String OToken = userService.generateGuest();
    userService.register(OToken, "owner", "owner", 25);
    NOToken = userService.login(OToken, "owner", "owner");

    assertTrue(authRepo.getUserName(NOToken).equals("owner"));

    createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

    String[] keywords = { "Laptop", "Lap", "top" };
    int productId = stockService.addProduct(NOToken, "Laptop", Category.Electronics, "Gaming Laptop", keywords);

    stockService.addItem(createdStoreId, NOToken, productId, 9999999, 2000, Category.Electronics);
    itemStoreDTO = new ItemStoreDTO(productId, 9999999, 2000, Category.Electronics, 0, createdStoreId, "Laptop", "TestStore");

    String token = userService.generateGuest();
    userService.register(token, "adminUser2", "adminPass2", 22);
    Admin = userService.login(token, "adminUser2", "adminPass2");
    userService.setAdmin(Admin, "123321", authRepo.getUserId(Admin));
    suspensionService.suspendRegisteredUser(authRepo.getUserId(NOToken),
            2,Admin);
    suspensionService.pauseSuspension(authRepo.getUserId(NOToken), Admin);

}
}

    @Test
    void single_discount_category_condition() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("10% off ELECTRONICS 1", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE,
                null);
        String[] subDiscountNames = new String[0];

        storeService.addDiscountTest(createdStoreId, NOToken, dto);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void single_discount_total_condition() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("15% off 2", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1", CreateDiscountDTO.Logic.SINGLE, null);

        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(3400, receipts[0].getFinalPrice()); // 15% off
    }

    @Test
    void single_discount_quantity_condition() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("20% off for bulk buys 3", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 3); // Quantity = 3

        ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(4800, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void single_discount_item_condition() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        int itemId = itemStoreDTO.getProductId();
        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific item 4", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);

        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void single_discount_store_condition_matches() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("10% for store 5", 0.1,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + createdStoreId,
                CreateDiscountDTO.Logic.SINGLE, null);

        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void single_discount_always_applies_null_condition() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("5% off everything 6", 0.05,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(1900, receipts[0].getFinalPrice()); // 5% off
    }

    @Test
    void single_discount_invalid_condition_never_applies() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("Invalid condition 7", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "UNSUPPORTED:FOO", CreateDiscountDTO.Logic.SINGLE,
                null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount applied
    }

    @Test
    void buy_SINGLE_discount_fail_wrongCategory() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("10% off HOME 8", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void buy_SINGLE_discount_fail_quantityTooLow() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("5% off on 10+ items 9", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // only 4 items

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void buy_SINGLE_discount_fail_totalTooLow() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("15% off expensive carts 10", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // 8000 total

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void buy_SINGLE_discount_fail_wrongStore() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO dto = new CreateDiscountDTO("15% off expensive carts 11", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "STORE:999999", CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void or_discount_store_condition_applies() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY 12", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good STORE 13", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE,
                null);
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR STORE applies 14", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void or_discount_only_total_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY 15", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good TOTAL16", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>2000", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR total only 17", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void or_discount_both_match_first_applies() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Category First 18", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Total Second 19", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR both 20", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);


        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(2800, receipts[0].getFinalPrice());
    }

    @Test
    void or_discount_none_match() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 21", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 22", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR fail 23", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void or_discount_item_matches_first() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("Item Match 24", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Category Match 25", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR item then cat 26", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(1200, receipts[0].getFinalPrice());
    }

    @Test
    void or_discount_category_matches_only() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL 27", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Category Works 28", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR bad then good 29", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(),
                SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void buy_OR_discount_fail_allWrongCategory() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off HOME 30", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off GROCERY 31", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:GROCERY", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrong categories 32", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Category is ELECTRONICS

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_OR_discount_fail_allWrongStore() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off Store 999 33", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "STORE:999", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off Store  34", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "STORE:888", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrong stores 35", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_OR_discount_fail_allQuantityTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off if qty > 10 36", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off if qty > 15 37", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>15", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - quantity too low 38", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 items

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_OR_discount_fail_totalPriceTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off if total > 10000 39", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off if total > 15000 40", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>15000", CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - price too low 41", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void and_discount_store_and_category_match() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 42", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY 43", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("20% off if both 44", 0.20,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND STORE+CATEGORY 45", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void and_discount_all_conditions_match() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY 46", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 47", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("QUANTITY 48", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d4 = new CreateDiscountDTO("ITEM 49", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);

        // CreateDiscountDTO leaf = new CreateDiscountDTO("30% full match", 0.30,
        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Match 50", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3, d4));

        String[] sub = new String[4];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();
        sub[3] = d4.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(2400, receipts[0].getFinalPrice()); // 40% off
    }

    @Test
    void and_discount_one_condition_fails() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Good CATEGORY 51", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Fail QUANTITY 52", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>100",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("25% inner 53", 0.25,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Partial 54", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d3, d2, d1));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);


        userService.addToUserCart(NGToken, itemStoreDTO, 3);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(6000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void and_discount_only_one_condition() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("QUANTITY 55", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("10% on bulk 56", 0.10,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND One 57", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 3);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(5400, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void and_discount_none_match() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL 58", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9000", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad ITEM 59", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:9999", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("50% off 60", 0.50,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND none 61", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);


        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void buy_AND_discount_fail_wrongCategory() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 62", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off HOME 63", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong    category 64", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // only ELECTRONICS

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_AND_discount_fail_wrongStore() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off STORE:1 65", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "STORE:1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off STORE:99 66", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong  store 67", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Store is only 1

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_AND_discount_fail_quantityTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 68", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off QUANTITY>10 69", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - quantity too   low 70", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);


        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 quantity

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_AND_discount_fail_priceTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 71", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off TOTAL>10000 72", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - total price  too low 73", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void xor_discount_store_only_applies() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 74 ", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 75", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR STORE 76", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));
        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);


        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void xor_discount_only_one_condition_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Fails: TOTAL>9000 77", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9000", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Passes: CATEGORY 78", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR one match 79", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));
        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        userService.addToUserCart(NGToken, itemStoreDTO, 2); // CATEGORY matches

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void xor_discount_both_conditions_match_no_discount_applied() throws
            Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY 80", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 81", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>2000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR both match 82", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both match

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void xor_discount_none_match_no_discount_applied() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 83", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 84", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR none match 85", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(2000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void xor_discount_only_item_condition_applies() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("ITEM 86", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Fails: QUANTITY>100 87", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>100 ",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR item only 89", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only ITEM matches

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void buy_XOR_discount_fail_bothApply() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 90", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS ",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off QUANTITY>1 91", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - both apply 92",
                0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Both conditions true

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        // Neither applies due to XOR conflict
        // // xor
        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_XOR_discount_fail_noneApply() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off CATEGORY:TOYS 93", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off TOTAL>10000 94", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - none apply 95",
                0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Neither condition

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_XOR_discount_fail_wrongStoreInOne() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 96", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off STORE:99 97", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "STORE:2", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - 2nd store  fails 98", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // First applies, second

        // But now let’s make both true to cause XOR failure
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(7200, receipts[0].getFinalPrice());
    }

    @Test
    void buy_XOR_discount_fail_itemMismatch() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ITEM:99 99", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:99", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:100 100", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:100", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - items don't match 101", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our item ID != 99 or


        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void max_discount_store_vs_category_higher_applies() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 102", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY 103", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX STORE vs CAT 104", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void max_discount_selects_highest_applicable() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% QUANTITY 105", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("25% TOTAL 106", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX test best match 107", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3000, receipts[0].getFinalPrice()); // 25% applied
    }

    @Test
    void max_discount_only_one_condition_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 108", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good CATEGORY 109", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX one match 110", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only CATEGORY matches

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1800, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void max_discount_multiple_conditions_same_value() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% CATEGORY 111", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("10% TOTAL 112", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX same value 113", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both apply

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void max_discount_none_match_applies_nothing() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 114", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 115", 0.40,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX fail 116", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Neither applies

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void max_discount_applies_item_based_highest() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("5% CATEGORY 117", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("30% ITEM 118", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX item win 119", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void buy_MAX_discount_fail_noneApply_dueToCategory() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off CATEGORY:TOYS 120", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off CATEGORY:FOOD 121", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:FOOD",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - categoriesdon't match 122", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO,4); // total = 4000 <

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_MAX_discount_fail_dueToTotalPriceThreshold() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("15% off TOTAL>10000 123", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("25% off TOTAL>20000 124", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>20000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - price too low 125", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // total = 4000 <

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(4000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_MAX_discount_fail_dueToStoreMismatch() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off STORE:99 126", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off STORE:100 127", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - wrong storeIDs 128", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our store ID is
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void buy_MAX_discount_fail_dueToItemID() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("5% off ITEM:999 129", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:999", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:777 130", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:777", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - item not incart 131", 0.0,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // itemStoreDTO has a

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void testRemoveDiscountFromStore_Success() throws Exception {
        // Step 1: Add discount
        CreateDiscountDTO dto = new CreateDiscountDTO("10% Off Electronics 132", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);

        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        // Step 2: Add to cart and buy with discount
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000
        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts1[0].getFinalPrice()); // 10% off → 4000 → 3600

        // Step 3: Remove discount
        storeService.removeDiscountFromStore(NOToken, createdStoreId, "10% Off Electronics 132");

        // Step 4: Add to cart again and check full price
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 again
        ReceiptDTO[] receipts2 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts2[0].getFinalPrice()); // Full price after removal
    }

    @Test
    void testRemoveDiscountFromStore_DiscountNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 1, "ThisDiscountDoesNotExist");
        });
    }

    @Test
    void testRemoveDiscountFromStore_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore("bad-token", 1, "any");
        });

    }

    @Test
    void testRemoveDiscountFromStore_UserNotOnline() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(GToken, 1, "any");
        });
    }

    @Test
    void testRemoveDiscountFromStore_UserSuspended() throws Exception {
                        suspensionService.resumeSuspension(authRepo.getUserId(NOToken), Admin);

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 1, "any");
        });
                        suspensionService.pauseSuspension(authRepo.getUserId(NOToken), Admin);

    }

    @Test
    void testRemoveDiscountFromStore_StoreNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 9999, "any");
        });
    }

    @Test
    void testRemoveDiscountFromStore_StoreInactive() throws Exception {
        storeService.closeStore(createdStoreId, Admin);
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, createdStoreId, "any");
        });
    }

    @Test
    void testRemoveDiscountFromStore_NoPermission() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noPerm1", "noPerm1", 20);
        String userToken = userService.login(token, "noPerm1", "noPerm1");

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(userToken, 1, "any");
        });
    }

    @Test
    void testaddDiscountTestToStore_NoPermission() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noPerm", "noPerm", 30);
        String userToken = userService.login(token, "noPerm", "noPerm");

        UIException ex = assertThrows(UIException.class, () -> {

            CreateDiscountDTO dto = new CreateDiscountDTO("10% Off Electronics 133", 0.10,
                    CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);

            storeService.addDiscountTest(createdStoreId, NGToken, dto);

        });

        // Step 4: Assert
        assertEquals("No permission to add discounts",
                ex.getMessage());
    }

    @Test
    void INVISIBLE_single_discount_category_condition() throws Exception {

        CreateDiscountDTO dto = new CreateDiscountDTO("10% off ELECTRONICS 01", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000
        CouponContext.set("10% off ELECTRONICS 01");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void INVISIBLE_single_discount_total_condition() throws Exception {


        CreateDiscountDTO dto = new CreateDiscountDTO("15% off for expensive carts 02", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>3000",
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);


        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
        CouponContext.set("15% off for expensive carts 02");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3400, receipts[0].getFinalPrice()); // 15% off
    }

    @Test
    void INVISIBLE_single_discount_quantity_condition() throws Exception {



        CreateDiscountDTO dto = new CreateDiscountDTO("20% off for bulk buys 03", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>2",
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);


        userService.addToUserCart(NGToken, itemStoreDTO, 3); // Quantity = 3
        CouponContext.set("20% off for bulk buys 03");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4800, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void INVISIBLE_single_discount_item_condition() throws Exception {
        int itemId = itemStoreDTO.getProductId();


        CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific item 04", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);


        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000
        CouponContext.set("25% off for specific item 04");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void INVISIBLE_single_discount_store_condition_matches() throws Exception {
        int storeId = itemStoreDTO.getStoreId();



        CreateDiscountDTO dto = new CreateDiscountDTO("10% for store 05", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:" + createdStoreId,
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("10% for store 05");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void INVISIBLE_single_discount_always_applies_null_condition() throws
            Exception {

        CreateDiscountDTO dto = new CreateDiscountDTO("5% off everything 06", 0.05,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.SINGLE,
                null);


        storeService.addDiscountTest(createdStoreId, NOToken, dto);
        CouponContext.set("5% off everything 06");

        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1900, receipts[0].getFinalPrice()); // 5% off
    }

    @Test
    void INVISIBLE_single_discount_invalid_condition_never_applies() throws
            Exception {

        CreateDiscountDTO dto = new CreateDiscountDTO("Invalid condition 07", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "UNSUPPORTED:FOO" + createdStoreId,
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);



        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
        CouponContext.set("Invalid condition 07");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount applied
    }

    @Test
    void INVISIBLE_buy_SINGLE_discount_fail_wrongCategory() throws Exception {






        CreateDiscountDTO dto = new CreateDiscountDTO("10% off HOME 08", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:HOME" + createdStoreId,
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        userService.addToUserCart(NGToken, itemStoreDTO, 4);
        CouponContext.set("10% off HOME 08");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_buy_SINGLE_discount_fail_quantityTooLow() throws Exception {





        CreateDiscountDTO dto = new CreateDiscountDTO("5% off on 10+ items 09", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>10" + createdStoreId,
                CreateDiscountDTO.Logic.SINGLE, null);
        storeService.addDiscountTest(createdStoreId, NOToken, dto);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // only 4 items
        CouponContext.set("5% off on 10+ items 09");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice()); // No discount
    }

//     

    @Test
    void INVISIBLE_buy_SINGLE_discount_fail_wrongStore() throws Exception {

        CreateDiscountDTO dto = new CreateDiscountDTO(
                "10% off wrong store",
                0.10,
                CreateDiscountDTO.Type.INVISIBLE,
                "STORE:999999" + createdStoreId, // This makes the rule invalid for this store
                CreateDiscountDTO.Logic.SINGLE,
                null
        );

        // Assert that adding this discount fails
        Exception ex = assertThrows(Exception.class, () -> {
            storeService.addDiscountTest(999999, NOToken, dto);
        });
        // or whatever message your service throws for wrong store usage

        // Optional: verify cart flow remains normal
        userService.addToUserCart(NGToken, itemStoreDTO, 4);
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
        assertEquals(8000, receipts[0].getFinalPrice());
    }


    @Test
    void INVISIBLE_or_discount_store_condition_applies() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY 011", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good STORE 012", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR STORE applies 013", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("OR STORE applies 013");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void INVISIBLE_or_discount_only_total_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY 014", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good TOTAL 015", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>2000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR total only 016", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("OR total only 016");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void INVISIBLE_or_discount_both_match_first_applies() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Category First 017", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Total Second 018", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR both 019", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        CouponContext.set("OR both 019");
        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(2800, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_or_discount_none_match() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 020", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>10",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 021", 0.30,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>9999",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR fail 022", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("OR fail 022");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_or_discount_item_matches_first() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("Item Match 023", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Category Match 024", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR item then cat 025", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("OR item then cat 025");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1200, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_or_discount_category_matches_only() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL 026", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Category Works 027", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR bad then good 028", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("OR bad then good 028");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }
    @Test
    void INVISIBLE_buy_OR_discount_fail_allWrongCategory() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off HOME 029", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:HOME",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off GROCERY 030", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:GROCERY",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrongcategories 031", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Category is

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);
        CouponContext.set("OR Discount - wrong categories 031");

        assertEquals(8000, receipts[0].getFinalPrice());

    
        assertThrows(Exception.class, () ->        storeService.collectDiscountDTOs(null, null)
);

 
                       storeService.collectNames(null, null);


        storeService.getAllDiscountsFlattened(createdStoreId, NOToken);
        assertThrows(Exception.class, () ->  storeService.getVisibleDiscountDescriptions(createdStoreId, NOToken));
        storeService.getAllDiscountNames(createdStoreId, NOToken);
        storeService.getFlattenedDiscounts(createdStoreId, NOToken);


    }

    @Test
    void INVISIBLE_buy_OR_discount_fail_allWrongStore() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off Store 999 032", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:999",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off Store 888 033", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:888",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrong stores 034",
                0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        CouponContext.set("OR Discount - wrong stores 034");

        userService.addToUserCart(NGToken, itemStoreDTO, 4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_OR_discount_fail_allQuantityTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off if qty > 10 035", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>10",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off if qty > 15 036", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>15",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - quantity too low 037", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 items
        CouponContext.set("OR Discount - quantity too low 037");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_OR_discount_fail_totalPriceTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off if total > 10000 038",
                0.10,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off if total > 15000 039",
                0.15,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>15000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - price too  low 040", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.OR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, orDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000
        CouponContext.set("OR Discount - price too low 040");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_and_discount_store_and_category_match() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 041", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY 042", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("20% off if both 043", 0.20,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND STORE+CATEGORY 044", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("AND STORE+CATEGORY 044");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
    }

    @Test
    void INVISIBLE_and_discount_all_conditions_match() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY 045", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 046", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("QUANTITY 047", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d4 = new CreateDiscountDTO("ITEM 048", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);

        // CreateDiscountDTO leaf = new CreateDiscountDTO("30% full match", 0.30,
        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Match 049", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3, d4));

        String[] sub = new String[4];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();
        sub[3] = d4.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        CouponContext.set("AND Match 049");

        userService.addToUserCart(NGToken, itemStoreDTO, 2);

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(2400, receipts[0].getFinalPrice()); // 40% off
    }

    @Test
    void INVISIBLE_and_discount_one_condition_fails() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Good CATEGORY 050", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Fail QUANTITY 051", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>100",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("25% inner 052", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Partial 053", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d3, d2, d1));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 3);
        CouponContext.set("AND Partial 053");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(6000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_and_discount_only_one_condition() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("QUANTITY 054", 0.0,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("10% on bulk 055", 0.10,
                CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND One 056", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 3);
        CouponContext.set("AND One 056");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(5400, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void INVISIBLE_and_discount_none_match() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL 057", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>9000",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad ITEM 058", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:9999",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d3 = new CreateDiscountDTO("50% off 059 ", 0.50,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND none 060", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2, d3));

        String[] sub = new String[3];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        sub[2] = d3.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);

        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        CouponContext.set("AND none 060");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_buy_AND_discount_fail_wrongCategory() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 061", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off HOME 062", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:HOME",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong  category 063", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));
        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // only ELECTRONICS
        CouponContext.set("AND Discount - wrong category 063");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_AND_discount_fail_wrongStore() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off STORE:1 064", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off STORE:99 065", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong store 066", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Store is only 1
        CouponContext.set("AND Discount - wrong store 066");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_AND_discount_fail_quantityTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 067", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off QUANTITY>10 067", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>10",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - quantity too low 068", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 quantity
        CouponContext.set("AND Discount - quantity too low 068");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_AND_discount_fail_priceTooLow() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 069", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off TOTAL>10000 070", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - total price too low 071", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.AND,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, andDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000
        CouponContext.set("AND Discount - total price too low 071");

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_xor_discount_store_only_applies() throws Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 072", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 073", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR STORE 074", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("XOR STORE 074");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void INVISIBLE_xor_discount_only_one_condition_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Fails: TOTAL>9000 075", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>9000", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Passes: CATEGORY 076", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR one match 077", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // CATEGORY matches
        CouponContext.set("XOR both match 077");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off

    }

    @Test
    void INVISIBLE_xor_discount_both_conditions_match_no_discount_applied()
            throws Exception {
        // Store store = storeRepository.getStores().get(0);

        CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY 078", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL 079", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>2000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR both match 080", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both match
        CouponContext.set("XOR both match 080");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_xor_discount_none_match_no_discount_applied() throws Exception
    {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 081", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 082", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>9999",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR none match 083", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("XOR none match 083");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(2000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_xor_discount_only_item_condition_applies() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("ITEM 084", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Fails: QUANTITY>100 085", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>100",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR item only 086", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only ITEM matches
        CouponContext.set("XOR item only 086");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
    }

    @Test
    void INVISIBLE_buy_XOR_discount_fail_bothApply() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 087", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off QUANTITY>1 088", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>1",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - both apply 089",
                0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Both conditions true

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("XOR Discount - both apply 089");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        // Neither applies due to XOR conflict
        // // xor
        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_XOR_discount_fail_noneApply() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off CATEGORY:TOYS 090", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off TOTAL>10000 091", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - none apply 092",
                0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Neither condition
        CouponContext.set("XOR Discount - none apply 092");
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_XOR_discount_fail_wrongStoreInOne() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ELECTRONICS 093", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off STORE:99 094", 0.20,
                CreateDiscountDTO.Type.VISIBLE, "STORE:2", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - 2nd store  fails 095", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // First applies, second

        // But now let’s make both true to cause XOR failure
        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("XOR Discount - 2nd store fails 095");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(7200, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_XOR_discount_fail_itemMismatch() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off ITEM:99 096", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:99", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:100 097", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:100", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - items don't match 098", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.XOR,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, xorDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our item ID != 99 or

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("XOR Discount - items don't match 098");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_max_discount_store_vs_category_higher_applies() throws
            Exception {
        int storeId = itemStoreDTO.getStoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("STORE 099", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId,
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY 0100", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX STORE vs CAT 0101", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();
        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("MAX STORE vs CAT 0101");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void INVISIBLE_max_discount_selects_highest_applicable() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% QUANTITY 0102", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("25% TOTAL 0103", 0.25,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX test best match 0103", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
        CouponContext.set("MAX test best match 0103");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3000, receipts[0].getFinalPrice()); // 25% applied
    }

    @Test
    void INVISIBLE_max_discount_only_one_condition_matches() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 0104", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Good CATEGORY 0105", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX one match 0106", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only CATEGORY matches
        CouponContext.set("MAX one match 0106");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1800, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void INVISIBLE_max_discount_multiple_conditions_same_value() throws Exception
    {


        CreateDiscountDTO d1 = new CreateDiscountDTO("10% CATEGORY 0107", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("10% TOTAL 0108", 0.10,
                CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX same value 0109", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both apply
        CouponContext.set("MAX same value 0109");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
    }

    @Test
    void INVISIBLE_max_discount_none_match_applies_nothing() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY 0110", 0.30,
                CreateDiscountDTO.Type.INVISIBLE, "QUANTITY>99",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL 0111", 0.40,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>9999",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX fail 0112", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // Neither applies
        CouponContext.set("MAX fail 0112");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts[0].getFinalPrice()); // No discount
    }

    @Test
    void INVISIBLE_max_discount_applies_item_based_highest() throws Exception {
        int itemId = itemStoreDTO.getProductId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("5% CATEGORY 0113", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("30% ITEM 0114", 0.30,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId,
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX item win 0115", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 1);
        CouponContext.set("MAX item win 0115");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
    }

    @Test
    void INVISIBLE_buy_MAX_discount_fail_noneApply_dueToCategory() throws
            Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off CATEGORY:TOYS 0116", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:TOYS",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off CATEGORY:FOOD 0117", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:FOOD",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - categoriesdon't match 0118", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Category is


        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("MAX Discount - categories don't match 0118");
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_MAX_discount_fail_dueToTotalPriceThreshold() throws
            Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("15% off TOTAL>10000 001", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>10000",
                CreateDiscountDTO.Logic.SINGLE, null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("25% off TOTAL>20000 002", 0.25,
                CreateDiscountDTO.Type.INVISIBLE, "TOTAL>20000",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - price too low 003", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // total = 4000 <

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        CouponContext.set("MAX Discount - price too low 003");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(4000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_MAX_discount_fail_dueToStoreMismatch() throws Exception {

        CreateDiscountDTO d1 = new CreateDiscountDTO("10% off STORE:99 004", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("20% off STORE:100 005", 0.20,
                CreateDiscountDTO.Type.INVISIBLE, "STORE:100",
                CreateDiscountDTO.Logic.SINGLE, null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - wrong store IDs 006", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();


        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our store ID is

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("MAX Discount - wrong store IDs 006");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_buy_MAX_discount_fail_dueToItemID() throws Exception {
        createdStoreId = storeRepositoryjpa.findAll().get(0).getstoreId();

        CreateDiscountDTO d1 = new CreateDiscountDTO("5% off ITEM:999 007", 0.05,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:999", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:777 008", 0.15,
                CreateDiscountDTO.Type.INVISIBLE, "ITEM:777", CreateDiscountDTO.Logic.SINGLE,
                null);

        CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - item not in cart 009", 0.0,
                CreateDiscountDTO.Type.INVISIBLE, null, CreateDiscountDTO.Logic.MAX,
                List.of(d1, d2));

        String[] sub = new String[2];
        sub[0] = d1.getName();
        sub[1] = d2.getName();

        storeService.addDiscountTest(createdStoreId, NOToken, maxDTO);
        userService.addToUserCart(NGToken, itemStoreDTO, 4); // itemStoreDTO has a

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        CouponContext.set("MAX Discount - item not in cart 009");

        ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails,
                supplyDetails);

        assertEquals(8000, receipts[0].getFinalPrice());
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_Success() throws Exception {
        // Step 1: Add discount




        CreateDiscountDTO DTO = new CreateDiscountDTO("10% Off Electronics 0010", 0.10,
                CreateDiscountDTO.Type.INVISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE,null);


        storeService.addDiscountTest(createdStoreId, NOToken, DTO);

        // Step 2: Add to cart and buy with discount
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000
        CouponContext.set("10% Off Electronics 0010");
        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(3600, receipts1[0].getFinalPrice()); // 10% off → 4000 → 3600

        // Step 3: Remove discount
        storeService.removeDiscountFromStore(NOToken, createdStoreId, "10% Off Electronics 0010");

        // Step 4: Add to cart again and check full price
        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 again
        ReceiptDTO[] receipts2 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        CouponContext.set("10% Off Electronics 0010");
        assertEquals(4000, receipts2[0].getFinalPrice()); // Full price after removal
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_DiscountNotFound() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 1, "ThisDiscountDoesNotExist");
        });
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_InvalidToken() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore("bad-token", 1, "any");
        });

    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_UserNotOnline() {
        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(GToken, 1, "any");
        });
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_UserSuspended() throws Exception {
                        suspensionService.resumeSuspension(authRepo.getUserId(NOToken), Admin);

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 1, "any");
        });
        suspensionService.pauseSuspension(authRepo.getUserId(NOToken), Admin);
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_StoreNotFound() {
        Exception ex = assertThrows(Exception.class, () -> {
            storeService.removeDiscountFromStore(NOToken, 9999, "any");
        });
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_StoreInactive() throws Exception {
        storeService.closeStore(createdStoreId, Admin);
        Exception ex = assertThrows(Exception.class, () -> {
            storeService.removeDiscountFromStore(NOToken, createdStoreId, "any");
        });
    }

    @Test
    void INVISIBLE_testRemoveDiscountFromStore_NoPermission() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noPerm2", "noPerm2", 20);
        String userToken = userService.login(token, "noPerm2", "noPerm2");

        UIException ex = assertThrows(UIException.class, () -> {
            storeService.removeDiscountFromStore(userToken, 1, "any");
        });
    }

    @Test
    void INVISIBLE_testaddDiscountTestToStore_NoPermission() throws Exception {
        String token = userService.generateGuest();
        userService.register(token, "noPerm3", "noPerm3", 30);
        String userToken = userService.login(token, "noPerm3", "noPerm3");

        UIException ex = assertThrows(UIException.class, () -> {

            CreateDiscountDTO dto = new CreateDiscountDTO("Invalid condition 0000", 0.10,
                    CreateDiscountDTO.Type.INVISIBLE, "UNSUPPORTED:FOO" + createdStoreId,
                    CreateDiscountDTO.Logic.SINGLE, null);
            storeService.addDiscountTest(createdStoreId, NGToken, dto);


        });

        // Step 4: Assert
        assertEquals("No permission to add discounts",
                ex.getMessage());
    }

    @Test
    void INVISIBLE_testRemoveANDDiscountFromStore_Success() throws Exception {
        // Step 1: Add discount
        CreateDiscountDTO d1 = new CreateDiscountDTO("5% off ITEM:1 .0", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:1 .1", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);

        String[] a = new String[2];
        a[0] = d1.getName();
        a[1] = d2.getName();



        CreateDiscountDTO dto = new CreateDiscountDTO("AND Discount .2", 0.10,
                CreateDiscountDTO.Type.VISIBLE, null,
                CreateDiscountDTO.Logic.AND, List.of(d1, d2) );
        storeService.addDiscountTest(createdStoreId, NOToken, dto);
        storeService.removeDiscountFromStore(NOToken, createdStoreId, "AND Discount .2");

        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts1[0].getFinalPrice());

    }

    @Test
    void INVISIBLE_testRemoveORDiscountFromStore_Success() throws Exception {
        // Step 1: Add discount
        CreateDiscountDTO d1 = new CreateDiscountDTO("5% off ITEM:1 .4", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:1 .5", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);

        String[] a = new String[2];
        a[0] = d1.getName();
        a[1] = d2.getName();

        CreateDiscountDTO dto = new CreateDiscountDTO("AND Discount .6", 0.10,
                CreateDiscountDTO.Type.VISIBLE, null,
                CreateDiscountDTO.Logic.AND, List.of(d1, d2) );
        storeService.addDiscountTest(createdStoreId, NOToken, dto);
        storeService.removeDiscountFromStore(NOToken, createdStoreId, "AND Discount .6");

        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts1[0].getFinalPrice());

    }

    @Test
    void INVISIBLE_testRemoveMaxDiscountFromStore_Success() throws Exception {
        CreateDiscountDTO d1 = new CreateDiscountDTO("5% off ITEM:1 .7", 0.05,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);
        CreateDiscountDTO d2 = new CreateDiscountDTO("15% off ITEM:1 .8", 0.15,
                CreateDiscountDTO.Type.VISIBLE, "ITEM:1", CreateDiscountDTO.Logic.SINGLE,
                null);

        String[] a = new String[2];
        a[0] = d1.getName();
        a[1] = d2.getName();



        CreateDiscountDTO dto = new CreateDiscountDTO("AND Discount .9", 0.10,
                CreateDiscountDTO.Type.VISIBLE, null,
                CreateDiscountDTO.Logic.MAX, List.of(d1, d2) );
        storeService.addDiscountTest(createdStoreId, NOToken, dto);

        storeService.removeDiscountFromStore(NOToken, createdStoreId, "AND Discount .9");


        userService.addToUserCart(NGToken, itemStoreDTO, 2);
        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken,
                PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
        assertEquals(4000, receipts1[0].getFinalPrice());

    }



    //
}