package workshop.demo.IntegrationTests.ServiceTests;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.CreateDiscountDTO.Logic;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.AndDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountScope;
import workshop.demo.DomainLayer.Store.MaxDiscount;
import workshop.demo.DomainLayer.Store.OrDiscount;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.VisibleDiscount;
import workshop.demo.DomainLayer.Store.XorDiscount;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.NotificationRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;
import workshop.demo.InfrastructureLayer.PurchaseRepository;
import workshop.demo.InfrastructureLayer.SUConnectionRepository;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.UserRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionRepo;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DiscountTests {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserSuspensionRepo suspensionRepo;
    @Autowired
    private AuthenticationRepo authRepo;

    @Autowired
    PaymentServiceImp payment;
    @Autowired
    SupplyServiceImp serviceImp;

    @Autowired
    SUConnectionRepository sIsuConnectionRepo;

    @Autowired
    Encoder encoder;
    @Autowired
    UserRepository userRepo;
    @Autowired
    UserSuspensionService suspensionService;
    @Autowired
    AdminHandler adminService;
    @Autowired
    UserService userService;
    @Autowired
    StockService stockService;
    @Autowired
    StoreService storeService;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    OrderService orderService;

    String NOToken;
    String NGToken;
    ItemStoreDTO itemStoreDTO;
    String GToken;
    String Admin;

    @BeforeEach
    void setup() throws Exception {
        System.out.println("===== SETUP RUNNING =====");

        GToken = userService.generateGuest();
        userService.register(GToken, "user", "user", 25);
        NGToken = userService.login(GToken, "user", "user");

        String OToken = userService.generateGuest();
        userService.register(OToken, "owner", "owner", 25);

        // --- Login ---
        NOToken = userService.login(OToken, "owner", "owner");

        assertTrue(authRepo.getUserName(NOToken).equals("owner"));
        // ======================= STORE CREATION =======================

        int createdStoreId = storeService.addStoreToSystem(NOToken, "TestStore", "ELECTRONICS");

        // ======================= PRODUCT & ITEM ADDITION =======================
        String[] keywords = {"Laptop", "Lap", "top"};
        int productId = stockService.addProduct(NOToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);

        assertEquals(1, stockService.addItem(createdStoreId, NOToken, productId, 10, 2000, Category.ELECTRONICS));
        itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.ELECTRONICS, 0, createdStoreId, "Laptop","TestStore");
        System.out.println(itemStoreDTO.getCategory());
        stockService.setProductToRandom(NOToken, productId, 1, 2000, createdStoreId, 5000);
        stockService.setProductToAuction(NOToken, createdStoreId, productId, 1, 1000, 2);
        assertTrue(stockService.getAllAuctions(NOToken, createdStoreId).length == 1);
        assertTrue(stockService.getAllRandomInStore(NOToken, createdStoreId).length == 1);
        //  assertTrue(stockService.getAllBidsStatus(NOToken, createdStoreId).length == 1);

        String token = userService.generateGuest();
        userService.register(token, "adminUser2", "adminPass2", 22);
        Admin = userService.login(token, "adminUser2", "adminPass2");
        userService.setAdmin(Admin, "123321", authRepo.getUserId(Admin));

        // ======================= SECOND GUEST SETUP =======================
    }

    @AfterEach

    void tearDown() {
        userRepo.clear();
        storeRepository.clear();
        stockRepository.clear();
        orderRepository.clear();
        suspensionRepo.clear();
        purchaseRepository.clear();
        sIsuConnectionRepo.clear();

    }

@Test
void single_discount_category_condition() throws Exception {
    Store store = storeRepository.getStores().get(0);

    CreateDiscountDTO dto = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
    String[] subDiscountNames = new String[0];
    storeService.addDiscountToStore(1, NOToken, dto.getName(),dto.getPercent(),dto.getType(),dto.getCondition(),
            dto.getLogic(),subDiscountNames);
    userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000

    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
    assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
}
//
//@Test
//void single_discount_total_condition() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("15% off for expensive carts", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(1, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3400, receipts[0].getFinalPrice()); // 15% off
//}
//
//@Test
//void single_discount_quantity_condition() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("20% off for bulk buys", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(1, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 3); // Quantity = 3
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4800, receipts[0].getFinalPrice()); // 20% off
//}
//
//@Test
//void single_discount_item_condition() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int itemId = itemStoreDTO.getProductId();
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("25% off for specific item", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(1, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
//}
//@Test
//void single_discount_store_condition_matches() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int storeId = itemStoreDTO.getStoreId();
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("10% for store", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(storeId, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
//}
//
//@Test
//void single_discount_always_applies_null_condition() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("5% off everything", 0.05,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(1, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1); // Total = 2000
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1900, receipts[0].getFinalPrice()); // 5% off
//}
//
//@Test
//void single_discount_invalid_condition_never_applies() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("Invalid condition", 0.99,
//            CreateDiscountDTO.Type.VISIBLE, "UNSUPPORTED:FOO", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(1, NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4000, receipts[0].getFinalPrice()); // No discount applied
//}
//@Test
//void buy_SINGLE_discount_fail_wrongCategory() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("10% off HOME", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4);
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void buy_SINGLE_discount_fail_quantityTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("5% off on 10+ items", 0.05,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // only 4 items
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void buy_SINGLE_discount_fail_totalTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("15% off expensive carts", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // 8000 total
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void buy_SINGLE_discount_fail_wrongStore() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO dto = new CreateDiscountDTO("10% off wrong store", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:999999", CreateDiscountDTO.Logic.SINGLE, null);
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, dto);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4);
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice()); // No discount
//}
//
@Test
void or_discount_store_condition_applies() throws Exception {
    Store store = storeRepository.getStores().get(0);
    int storeId = itemStoreDTO.getStoreId();

    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY", 0.10,
            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
    CreateDiscountDTO d2 = new CreateDiscountDTO("Good STORE", 0.30,
            CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE, null);
    String[] sub = new String[2];
    sub[0] = d1.getName();
    sub[1] = d2.getName();
    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR STORE applies", 0.0,
            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
    storeService.addDiscountToStore(storeId, NOToken, d1.getName(),d1.getPercent(),d1.getType(),
            d1.getCondition(),d1.getLogic(),new String[0]);
    storeService.addDiscountToStore(storeId, NOToken, d2.getName(),d2.getPercent(),d2.getType(),
            d2.getCondition(),d2.getLogic(),new String[0]);
    storeService.addDiscountToStore(storeId, NOToken, orDTO.getName(),orDTO.getPercent(),orDTO.getType(),
            orDTO.getCondition(),orDTO.getLogic(),sub);
    userService.addToUserCart(NGToken, itemStoreDTO, 1);

    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
    assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
}
//
//@Test
//void or_discount_only_total_matches() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad CATEGORY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Good TOTAL", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>2000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR total only", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
//}
//
//@Test
//void or_discount_both_match_first_applies() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Category First", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Total Second", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR both", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(2800, receipts[0].getFinalPrice());
//}
//
//@Test
//void or_discount_none_match() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL", 0.30,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR fail", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void or_discount_item_matches_first() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int itemId = itemStoreDTO.getProductId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Item Match", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Category Match", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR item then cat", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1200, receipts[0].getFinalPrice());
//}
//
//@Test
//void or_discount_category_matches_only() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Category Works", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR bad then good", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
//}
//@Test
//void buy_OR_discount_fail_allWrongCategory() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off HOME", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off GROCERY", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:GROCERY", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrong categories", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Category is ELECTRONICS
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_OR_discount_fail_allWrongStore() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off Store 999", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:999", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off Store 888", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:888", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - wrong stores", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4);
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_OR_discount_fail_allQuantityTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off if qty > 10", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off if qty > 15", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>15", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - quantity too low", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 items
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_OR_discount_fail_totalPriceTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off if total > 10000", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off if total > 15000", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>15000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO orDTO = new CreateDiscountDTO("OR Discount - price too low", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.OR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, orDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void and_discount_store_and_category_match() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int storeId = itemStoreDTO.getStoreId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("STORE", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d3 = new CreateDiscountDTO("20% off if both", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND STORE+CATEGORY", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(d1, d2, d3));
//
//    storeService.addDiscountToStore(storeId, NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3200, receipts[0].getFinalPrice()); // 20% off
//}
//
//@Test
//void and_discount_all_conditions_match() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int itemId = itemStoreDTO.getProductId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY", 0.10, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL", 0.10, CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d3 = new CreateDiscountDTO("QUANTITY", 0.10, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d4 = new CreateDiscountDTO("ITEM", 0.10, CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
//
// //   CreateDiscountDTO leaf = new CreateDiscountDTO("30% full match", 0.30, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Match", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(d1, d2, d3, d4));
//
//    storeService.addDiscountToStore(1, NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(2400, receipts[0].getFinalPrice()); // 40% off
//}
//
//@Test
//void and_discount_one_condition_fails() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Good CATEGORY", 0.0, CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Fail QUANTITY", 0.0, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO leaf = new CreateDiscountDTO("25% inner", 0.25, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Partial", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(d1, d2, leaf));
//
//    storeService.addDiscountToStore(1, NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 3);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(6000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void and_discount_only_one_condition() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("QUANTITY", 0.0, CreateDiscountDTO.Type.VISIBLE, "QUANTITY>2", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO leaf = new CreateDiscountDTO("10% on bulk", 0.10, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND One", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(d1, leaf));
//
//    storeService.addDiscountToStore(1, NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 3);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(5400, receipts[0].getFinalPrice()); // 10% off
//}
//
//@Test
//void and_discount_none_match() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad TOTAL", 0.0, CreateDiscountDTO.Type.VISIBLE, "TOTAL>9000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Bad ITEM", 0.0, CreateDiscountDTO.Type.VISIBLE, "ITEM:9999", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO leaf = new CreateDiscountDTO("50% off never", 0.50, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND none", 0.0, CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(d1, d2, leaf));
//
//    storeService.addDiscountToStore(1, NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4000, receipts[0].getFinalPrice()); // No discount
//}
//@Test
//void buy_AND_discount_fail_wrongCategory() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off HOME", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:HOME", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong category", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // only ELECTRONICS
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_AND_discount_fail_wrongStore() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off STORE:1", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:1", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off STORE:99", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - wrong store", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Store is only 1
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_AND_discount_fail_quantityTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off QUANTITY>10", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>10", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - quantity too low", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Only 4 quantity
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_AND_discount_fail_priceTooLow() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off TOTAL>10000", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO andDTO = new CreateDiscountDTO("AND Discount - total price too low", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.AND, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, andDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Total = 8000
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void xor_discount_store_only_applies() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int storeId = itemStoreDTO.getStoreId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("STORE", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR STORE", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(storeId, NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1500, receipts[0].getFinalPrice()); // 25% off
//}
//
//@Test
//void xor_discount_only_one_condition_matches() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Fails: TOTAL>9000", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>9000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Passes: CATEGORY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR one match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // CATEGORY matches
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
//}
//
//@Test
//void xor_discount_both_conditions_match_no_discount_applied() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("CATEGORY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("TOTAL", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>2000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR both match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both match
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void xor_discount_none_match_no_discount_applied() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR none match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(2000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void xor_discount_only_item_condition_applies() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int itemId = itemStoreDTO.getProductId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("ITEM", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Fails: QUANTITY>100", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>100", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR item only", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only ITEM matches
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1500, receipts[0].getFinalPrice());     // 25% off
//}
//@Test
//void buy_XOR_discount_fail_bothApply() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off QUANTITY>1", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - both apply", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Both conditions true
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    // Neither applies due to XOR conflict
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_XOR_discount_fail_noneApply() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off CATEGORY:TOYS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:TOYS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off TOTAL>10000", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - none apply", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Neither condition true
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_XOR_discount_fail_wrongStoreInOne() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ELECTRONICS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("20% off STORE:99", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:2", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - 2nd store fails", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // First applies, second false → OK
//
//    // But now let’s make both true to cause XOR failure
//
//
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(7200, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_XOR_discount_fail_itemMismatch() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off ITEM:99", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:99", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off ITEM:100", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:100", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO xorDTO = new CreateDiscountDTO("XOR Discount - items don't match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.XOR, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, xorDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our item ID != 99 or 100
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void max_discount_store_vs_category_higher_applies() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int storeId = itemStoreDTO.getStoreId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("STORE", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:" + storeId, CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("CATEGORY", 0.30,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX STORE vs CAT", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(storeId, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
//}
//
//@Test
//void max_discount_selects_highest_applicable() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("10% QUANTITY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>1", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("25% TOTAL", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>3000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX test best match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Total = 4000
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3000, receipts[0].getFinalPrice()); // 25% applied
//}
//
//@Test
//void max_discount_only_one_condition_matches() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Good CATEGORY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX one match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1); // Only CATEGORY matches
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1800, receipts[0].getFinalPrice()); // 10% off
//}
//
//@Test
//void max_discount_multiple_conditions_same_value() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("10% CATEGORY", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("10% TOTAL", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>1000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX same value", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Both apply
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(3600, receipts[0].getFinalPrice()); // 10% off
//}
//
//@Test
//void max_discount_none_match_applies_nothing() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("Bad QUANTITY", 0.30,
//            CreateDiscountDTO.Type.VISIBLE, "QUANTITY>99", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("Bad TOTAL", 0.40,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>9999", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX fail", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // Neither applies
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(4000, receipts[0].getFinalPrice()); // No discount
//}
//
//@Test
//void max_discount_applies_item_based_highest() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//    int itemId = itemStoreDTO.getProductId();
//
//    CreateDiscountDTO d1 = new CreateDiscountDTO("5% CATEGORY", 0.05,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO d2 = new CreateDiscountDTO("30% ITEM", 0.30,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:" + itemId, CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX item win", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(d1, d2));
//
//    storeService.addDiscountToStore(1, NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 1);
//
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//    assertEquals(1400, receipts[0].getFinalPrice()); // 30% off
//}
//@Test
//void buy_MAX_discount_fail_noneApply_dueToCategory() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off CATEGORY:TOYS", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:TOYS", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("20% off CATEGORY:FOOD", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "CATEGORY:FOOD", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - categories don't match", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Category is ELECTRONICS
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_MAX_discount_fail_dueToTotalPriceThreshold() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("15% off TOTAL>10000", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>10000", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("25% off TOTAL>20000", 0.25,
//            CreateDiscountDTO.Type.VISIBLE, "TOTAL>20000", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - price too low", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 2); // total = 4000 < thresholds
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(4000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_MAX_discount_fail_dueToStoreMismatch() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("10% off STORE:99", 0.10,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:99", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("20% off STORE:100", 0.20,
//            CreateDiscountDTO.Type.VISIBLE, "STORE:100", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - wrong store IDs", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // Our store ID is likely 1
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//@Test
//void buy_MAX_discount_fail_dueToItemID() throws Exception {
//    Store store = storeRepository.getStores().get(0);
//
//    CreateDiscountDTO sub1 = new CreateDiscountDTO("5% off ITEM:999", 0.05,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:999", CreateDiscountDTO.Logic.SINGLE, null);
//    CreateDiscountDTO sub2 = new CreateDiscountDTO("15% off ITEM:777", 0.15,
//            CreateDiscountDTO.Type.VISIBLE, "ITEM:777", CreateDiscountDTO.Logic.SINGLE, null);
//
//    CreateDiscountDTO maxDTO = new CreateDiscountDTO("MAX Discount - item not in cart", 0.0,
//            CreateDiscountDTO.Type.VISIBLE, null, CreateDiscountDTO.Logic.MAX, List.of(sub1, sub2));
//
//    storeService.addDiscountToStore(store.getStoreID(), NOToken, maxDTO);
//    userService.addToUserCart(NGToken, itemStoreDTO, 4); // itemStoreDTO has a different productId
//
//    PaymentDetails paymentDetails = PaymentDetails.testPayment();
//    SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
//    ReceiptDTO[] receipts = purchaseService.buyGuestCart(NGToken, paymentDetails, supplyDetails);
//
//    assertEquals(8000, receipts[0].getFinalPrice());
//}
//
//    @Test
//    void testRemoveDiscountFromStore_Success() throws Exception {
//        // Step 1: Add discount
//        CreateDiscountDTO dto = new CreateDiscountDTO("10% Off Electronics", 0.10,
//                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//        storeService.addDiscountToStore(1, NOToken, dto);
//
//        // Step 2: Add to cart and buy with discount
//        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 = 4000
//        ReceiptDTO[] receipts1 = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//        assertEquals(3600, receipts1[0].getFinalPrice()); // 10% off → 4000 → 3600
//
//        // Step 3: Remove discount
//        storeService.removeDiscountFromStore(NOToken, 1, "10% Off Electronics");
//
//        // Step 4: Add to cart again and check full price
//        userService.addToUserCart(NGToken, itemStoreDTO, 2); // 2 * 2000 again
//        ReceiptDTO[] receipts2 = purchaseService.buyGuestCart(NGToken, PaymentDetails.testPayment(), SupplyDetails.getTestDetails());
//        assertEquals(4000, receipts2[0].getFinalPrice()); // Full price after removal
//    }
//    @Test
//    void testRemoveDiscountFromStore_DiscountNotFound() {
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(NOToken, 1, "ThisDiscountDoesNotExist");
//        });
//    }
//    @Test
//    void testRemoveDiscountFromStore_InvalidToken() {
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore("bad-token", 1, "any");
//        });
//
//
//    }
//    @Test
//    void testRemoveDiscountFromStore_UserNotOnline() {
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(GToken, 1, "any");
//        });
//    }
//
//
//    @Test
//    void testRemoveDiscountFromStore_UserSuspended() throws Exception {
//        suspensionRepo.suspendRegisteredUser(authRepo.getUserId(NOToken),2);
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(NOToken, 1, "any");
//        });
//    }
//    @Test
//    void testRemoveDiscountFromStore_StoreNotFound() {
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(NOToken, 9999, "any");
//        });
//    }
//    @Test
//    void testRemoveDiscountFromStore_StoreInactive() throws Exception {
//        storeService.closeStore(1, Admin);
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(NOToken, 1, "any");
//        });
//    }
//
//    @Test
//    void testRemoveDiscountFromStore_NoPermission() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "noPerm", "noPerm", 20);
//        String userToken = userService.login(token, "noPerm", "noPerm");
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.removeDiscountFromStore(userToken, 1, "any");
//        });
//    }
//
//    @Test
//    void testAddDiscountToStore_NoPermission() throws Exception {
//        String token = userService.generateGuest();
//        userService.register(token, "noPerm", "noPerm", 30);
//        String userToken = userService.login(token, "noPerm", "noPerm");
//
//        CreateDiscountDTO dto = new CreateDiscountDTO("5% off", 0.05,
//                CreateDiscountDTO.Type.VISIBLE, "CATEGORY:ELECTRONICS", CreateDiscountDTO.Logic.SINGLE, null);
//
//        UIException ex = assertThrows(UIException.class, () -> {
//            storeService.addDiscountToStore(1, userToken, dto);
//        });
//
//        // Step 4: Assert
//        assertEquals("You do not have permission to add discounts to this store", ex.getMessage());
//    }
//
//
//
//
//
//
//
//
//
}
//
//
