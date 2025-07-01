package workshop.demo.IntegrationTests.ServiceTests;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DTOs.SystemAnalyticsDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

@ActiveProfiles("test")
public class dtosTests {

    // @Test
    // public void testPurchaseHistoryDTO_AllGettersSetters() {
    //     // Dummy values for ReceiptProduct
    //     String productName = "Laptop";
    //     String storeName = "BestBuy";
    //     int quantity = 2;
    //     int price = 1500;
    //     int productId = 1;
    //     Category category = Category.Electronics; // assuming enum value

    //     ReceiptProduct rp = new ReceiptProduct(productName, storeName, quantity, price, productId, category);
    //     List<ReceiptProduct> products = new ArrayList<>();
    //     products.add(rp);

    //     // Original values
    //     String buyer = "buyerUser";
    //     String store = "storeName";
    //     String time = "2025-06-03 15:00";
    //     double total = 3000.0;

    //     PurchaseHistoryDTO dto = new PurchaseHistoryDTO(buyer, store, products, time, total);

    //     // Test all getters
    //     assertEquals(buyer, dto.getBuyerUserName());
    //     assertEquals(store, dto.getStoreName());
    //     assertEquals(products, dto.getItems());
    //     assertEquals(time, dto.getTimeStamp());
    //     assertEquals(total, dto.getTotalPrice());

    //     // Test all setters
    //     dto.setBuyerUserName("newBuyer");
    //     assertEquals("newBuyer", dto.getBuyerUserName());

    //     dto.setStoreName("newStore");
    //     assertEquals("newStore", dto.getStoreName());

    //     List<ReceiptProduct> newList = new ArrayList<>();
    //     dto.setItems(newList);
    //     assertEquals(newList, dto.getItems());

    //     dto.setTimeStamp("newTime");
    //     assertEquals("newTime", dto.getTimeStamp());

    //     dto.setTotalPrice(999.99);
    //     assertEquals(999.99, dto.getTotalPrice());
    // }

    @Test
    public void testItemCartDTO_AllSettersGettersAndConstructors() {
        // ========== Using parameterized constructor ==========
        ItemCartDTO dto1 = new ItemCartDTO(1, 101, 3, 299, "Mouse", "TechStore", Category.Electronics);

        assertEquals(1, dto1.getStoreId());
        assertEquals(101, dto1.getProductId());
        assertEquals(3, dto1.getQuantity());
        assertEquals(299, dto1.getPrice());
        assertEquals("Mouse", dto1.getName());
        assertEquals("TechStore", dto1.getStoreName());
        assertEquals(Category.Electronics, dto1.getCategory());

        // ========== Using default constructor + setters ==========
        ItemCartDTO dto2 = new ItemCartDTO();

        dto2.setStoreId(2);
        dto2.setProductId(202);
        dto2.setQuantity(5);
        dto2.setPrice(599);
        dto2.setName("Keyboard");
        dto2.setStoreName("GadgetWorld");
        dto2.setCategory(Category.Home);

        assertEquals(2, dto2.getStoreId());
        assertEquals(202, dto2.getProductId());
        assertEquals(5, dto2.getQuantity());
        assertEquals(599, dto2.getPrice());
        assertEquals("Keyboard", dto2.getName());
        assertEquals("GadgetWorld", dto2.getStoreName());
        assertEquals(Category.Home, dto2.getCategory());
    }

    @Test
    public void testItemStoreDTO_AllConstructorsSettersGetters() {
        // ==== Constructor with all parameters ====
        ItemStoreDTO dto1 = new ItemStoreDTO(10, 5, 200, Category.Electronics, 4, 1, "Laptop", "TechStore");

        assertEquals(10, dto1.getProductId());
        assertEquals(5, dto1.getQuantity());
        assertEquals(200, dto1.getPrice());
        assertEquals(Category.Electronics, dto1.getCategory());
        assertEquals(4, dto1.getRank());
        assertEquals(1, dto1.getStoreId());
        assertEquals("Laptop", dto1.getProductName());
        assertEquals("TechStore", dto1.getStoreName());

        // ==== Default constructor with setters ====
        ItemStoreDTO dto2 = new ItemStoreDTO();

        dto2.setProductId(20);
        dto2.setPrice(999);
        dto2.setCategory(Category.Home);
        dto2.setRank(5);
        dto2.setStoreId(2);
        dto2.setStoreName("GamePlanet");

        // Verifying with getters
        assertEquals(20, dto2.getProductId());
        assertEquals(999, dto2.getPrice());
        assertEquals(Category.Home, dto2.getCategory());
        assertEquals(5, dto2.getRank());
        assertEquals(2, dto2.getStoreId());
        assertEquals("GamePlanet", dto2.getStoreName());
    }

    @Test
    public void testSpecialCartItemDTO_SettersGetters() {
        SpecialCartItemDTO dto = new SpecialCartItemDTO();

        // Use both setter methods
        dto.setIds(10, 200, 3000, SpecialType.Auction);
        dto.setValues("Gaming Mouse", true, false);

        // Verify all getter values
        assertEquals(10, dto.getStoreId());
        assertEquals(200, dto.getSpecialId());
        assertEquals(3000, dto.getBidId());
        assertEquals(SpecialType.Auction, dto.getType());

        assertEquals("Gaming Mouse", dto.getProductName());
        assertTrue(dto.isWinner());
        assertFalse(dto.isEnded());
    }

    // @Test
    // public void testOrderDTO_AllConstructorsSettersGetters() {
    //     // Dummy ReceiptProduct
    //     ReceiptProduct product = new ReceiptProduct("Mouse", "BestBuy", 2, 50, 101, Category.Electronics);
    //     List<ReceiptProduct> productList = new ArrayList<>();
    //     productList.add(product);

    //     // === Using full constructor ===
    //     OrderDTO dto1 = new OrderDTO(1, 10, "2025-06-03", productList, 100.0);

    //     assertEquals(1, dto1.getUserId());
    //     assertEquals(10, dto1.getStoreId());
    //     assertEquals("2025-06-03", dto1.getDate());
    //     assertEquals(productList, dto1.getProductsList());
    //     assertEquals(100.0, dto1.getFinalPrice());

    //     // === Using default constructor + setters ===
    //     OrderDTO dto2 = new OrderDTO();
    //     dto2.setUserId(2);
    //     dto2.setStoreId(20);
    //     dto2.setDate("2025-07-01");
    //     dto2.setProductsList(productList);
    //     dto2.setFinalPrice(200.0);

    //     assertEquals(2, dto2.getUserId());
    //     assertEquals(20, dto2.getStoreId());
    //     assertEquals("2025-07-01", dto2.getDate());
    //     assertEquals(productList, dto2.getProductsList());
    //     assertEquals(200.0, dto2.getFinalPrice());
    // }

    // @Test
    // public void testReceiptDTO_AllConstructorsSettersGetters() {
    //     // Dummy ReceiptProduct
    //     ReceiptProduct product = new ReceiptProduct("Keyboard", "StoreX", 1, 120, 501, Category.Electronics);
    //     List<ReceiptProduct> products = new ArrayList<>();
    //     products.add(product);

    //     // === Test full constructor ===
    //     ReceiptDTO dto1 = new ReceiptDTO("StoreX", "2025-06-03", products, 120.0);

    //     assertEquals("StoreX", dto1.getStoreName());
    //     assertEquals("2025-06-03", dto1.getDate());
    //     assertEquals(products, dto1.getProductsList());
    //     assertEquals(120.0, dto1.getFinalPrice());

    //     // === Test default constructor + setters ===
    //     ReceiptDTO dto2 = new ReceiptDTO();
    //     dto2.setStoreName("StoreY");
    //     dto2.setDate("2025-07-01");
    //     dto2.setProductsList(products);
    //     dto2.setFinalPrice(200.5);

    //     assertEquals("StoreY", dto2.getStoreName());
    //     assertEquals("2025-07-01", dto2.getDate());
    //     assertEquals(products, dto2.getProductsList());
    //     assertEquals(200.5, dto2.getFinalPrice());
    // }

    @Test
    public void testProductDTO_AllConstructorsSettersGetters() {
        // === Full constructor ===
        ProductDTO dto1 = new ProductDTO(101, "Gaming Chair", Category.Home, "Ergonomic and stylish");

        assertEquals(101, dto1.getProductId());
        assertEquals("Gaming Chair", dto1.getName());
        assertEquals(Category.Home, dto1.getCategory());
        assertEquals("Ergonomic and stylish", dto1.getDescription());

        // === Default constructor + setters ===
        ProductDTO dto2 = new ProductDTO();
        dto2.setProductId(202);
        dto2.setName("Mechanical Keyboard");
        dto2.setCategory(Category.Electronics);
        dto2.setDescription("RGB lighting and programmable keys");

        assertEquals(202, dto2.getProductId());
        assertEquals("Mechanical Keyboard", dto2.getName());
        assertEquals(Category.Electronics, dto2.getCategory());
        assertEquals("RGB lighting and programmable keys", dto2.getDescription());
    }

    @Test
    public void testSystemAnalyticsDTO_AllConstructorsSettersGetters() {
        Map<LocalDate, Integer> logins = Map.of(LocalDate.now(), 5);
        Map<LocalDate, Integer> logouts = Map.of(LocalDate.now(), 3);
        Map<LocalDate, Integer> registers = Map.of(LocalDate.now(), 2);
        Map<LocalDate, Integer> purchases = Map.of(LocalDate.now(), 7);

        SystemAnalyticsDTO dto1 = new SystemAnalyticsDTO(logins, logouts, registers, purchases);

        assertEquals(5, dto1.getLoginsPerDay().get(LocalDate.now()));
        assertEquals(3, dto1.getLogoutsPerDay().get(LocalDate.now()));
        assertEquals(2, dto1.getRegisterPerDay().get(LocalDate.now()));
        assertEquals(7, dto1.getPurchasesPerDay().get(LocalDate.now()));

        SystemAnalyticsDTO dto2 = new SystemAnalyticsDTO(logins, logouts, registers, purchases);
        dto2.setLoginsPerDay(Map.of(LocalDate.now(), 1));
        dto2.setLogoutsPerDay(Map.of(LocalDate.now(), 2));
        dto2.setRegisterPerDay(Map.of(LocalDate.now(), 3));
        dto2.setPurchasesPerDay(Map.of(LocalDate.now(), 4));

        assertEquals(1, dto2.getLoginsPerDay().get(LocalDate.now()));
        assertEquals(2, dto2.getLogoutsPerDay().get(LocalDate.now()));
        assertEquals(3, dto2.getRegisterPerDay().get(LocalDate.now()));
        assertEquals(4, dto2.getPurchasesPerDay().get(LocalDate.now()));
    }

    @Test
    public void testUserDTO_AllConstructorsSettersGetters() {
        UserDTO dto1 = new UserDTO(1, "alice", 25, true, false);

        assertEquals(1, dto1.getId());
        assertEquals("alice", dto1.getUsername());
        assertEquals(25, dto1.getAge());
        assertTrue(dto1.getIsOnline());
        assertFalse(dto1.getIsAdmin());

        UserDTO dto2 = new UserDTO();
        assertEquals(0, dto2.getId());
        assertNull(dto2.getUsername());
        assertEquals(0, dto2.getAge());
        assertNull(dto2.getIsOnline());
        assertNull(dto2.getIsAdmin());
    }

    @Test
    public void testStoreDTO_AllConstructorsSettersGetters() {
        StoreDTO dto1 = new StoreDTO(10, "SuperStore", "ELECTRONICS", true, 5);

        assertEquals(10, dto1.getStoreId());
        assertEquals("SuperStore", dto1.getStoreName());
        assertEquals("ELECTRONICS", dto1.getCategory());
        assertTrue(dto1.isActive());
        assertEquals(5, dto1.getFinalRating());

        StoreDTO dto2 = new StoreDTO();
        assertEquals(0, dto2.getStoreId());
        assertNull(dto2.getStoreName());
        assertNull(dto2.getCategory());
        assertFalse(dto2.isActive());
        assertEquals(0, dto2.getFinalRating());
    }

    @Test
    public void testNotificationDTO_AllConstructorsSettersGetters() {
        NotificationDTO dto1 = new NotificationDTO("You have an offer", "bob", NotificationDTO.NotificationType.OFFER, true, "admin", 101);

        assertEquals("You have an offer", dto1.getMessage());
        assertEquals("bob", dto1.getReceiverName());
        assertEquals(NotificationDTO.NotificationType.OFFER, dto1.getType());
        assertTrue(dto1.getToBeOwner());
        assertEquals("admin", dto1.getSenderName());
        assertEquals(101, dto1.getStoreId());

        dto1.setMessage("New message");
        dto1.setReceiverName("charlie");
        dto1.setType(NotificationDTO.NotificationType.NORMAL);

        assertEquals("New message", dto1.getMessage());
        assertEquals("charlie", dto1.getReceiverName());
        assertEquals(NotificationDTO.NotificationType.NORMAL, dto1.getType());
    }

    @Test
    public void testReviewDTO_AllConstructorsSettersGetters() {
        ReviewDTO dto1 = new ReviewDTO(123, "Dave", "Great product!");

        assertEquals(123, dto1.getReviewerId());
        assertEquals("Dave", dto1.getName());
        assertEquals("Great product!", dto1.getReviewMsg());

        dto1.setReviewerId(456);
        dto1.setName("Eve");
        dto1.setReviewMsg("Needs improvement.");

        assertEquals(456, dto1.getReviewerId());
        assertEquals("Eve", dto1.getName());
        assertEquals("Needs improvement.", dto1.getReviewMsg());
    }

    @Test
    public void testPaymentDetails_AllMethods() throws Exception {
        PaymentDetails dto1 = new PaymentDetails("4111111111111111", "John Doe", "12/30", "123");

        assertEquals("4111111111111111", dto1.cardNumber);
        assertEquals("John Doe", dto1.cardHolderName);
        assertEquals("12/30", dto1.expirationDate);
        assertEquals("123", dto1.cvv);

        PaymentDetails dto2 = new PaymentDetails();
        dto2.cardNumber = "2222333344445555";
        dto2.cardHolderName = "Jane Smith";
        dto2.expirationDate = "01/29";
        dto2.cvv = "456";

        assertEquals("2222333344445555", dto2.cardNumber);
        assertEquals("Jane Smith", dto2.cardHolderName);
        assertEquals("01/29", dto2.expirationDate);
        assertEquals("456", dto2.cvv);

        PaymentDetails dto3 = PaymentDetails.testPayment();
        assertNotNull(dto3);

        PaymentDetails dto4 = PaymentDetails.getPaymentDetailsFromJSON(
                "{\"cardNumber\":\"1234567890123456\",\"cardHolderName\":\"Tester\",\"expirationDate\":\"11/29\",\"cvv\":\"321\"}"
        );
        assertEquals("Tester", dto4.cardHolderName);
    }

    @Test
    public void testSupplyDetails_AllMethods() throws Exception {
        SupplyDetails dto1 = new SupplyDetails("123 Main", "City", "ST", "12345");

        assertEquals("123 Main", dto1.address);
        assertEquals("City", dto1.city);
        assertEquals("ST", dto1.state);
        assertEquals("12345", dto1.zipCode);

        SupplyDetails dto2 = new SupplyDetails();
        dto2.address = "456 Ave";
        dto2.city = "Town";
        dto2.state = "TS";
        dto2.zipCode = "54321";

        assertEquals("456 Ave", dto2.address);
        assertEquals("Town", dto2.city);
        assertEquals("TS", dto2.state);
        assertEquals("54321", dto2.zipCode);

        SupplyDetails dto3 = SupplyDetails.getTestDetails();
        assertEquals("123 Test Street", dto3.address);

        SupplyDetails dto4 = SupplyDetails.getSupplyDetailsFromJSON(
                "{\"address\":\"789 Road\",\"city\":\"Place\",\"state\":\"PR\",\"zipCode\":\"99999\"}"
        );
        assertEquals("789 Road", dto4.address);
    }

    // @Test
    // public void testReceiptProduct_AllMethods() {
    //     ReceiptProduct dto = new ReceiptProduct("Monitor", "TechShop", 2, 250, 1001, Category.Electronics);

    //     assertEquals("Monitor", dto.getProductName());
    //     assertEquals("TechShop", dto.getStorename());
    //     assertEquals(2, dto.getQuantity());
    //     assertEquals(250, dto.getPrice());
    //     assertEquals(1001, dto.getProductId());
    //     assertEquals(Category.Electronics, dto.getCategory());

    //     dto.setPrice(300);
    //     dto.setstoreName("NewStore");
    //     dto.setProductId(2002);

    //     assertEquals(300, dto.getPrice());
    //     assertEquals("NewStore", dto.getStorename());
    //     assertEquals(2002, dto.getProductId());
    // }

    @Test
    public void testWorkerDTO_AllMethods() {
        Permission[] perms = new Permission[]{};
        WorkerDTO dto = new WorkerDTO(1, "workerUser", true, false, "StoreX", perms, true);

        assertEquals(1, dto.getWorkerId());
        assertEquals("workerUser", dto.getUsername());
        assertTrue(dto.isManager());
        assertFalse(dto.isOwner());
        assertEquals("StoreX", dto.getStoreName());
        assertEquals(perms, dto.getPermessions());
        assertTrue(dto.isSetByMe());

        WorkerDTO dto2 = new WorkerDTO();
        assertNull(dto2.getUsername());
    }

    @Test
    public void testParticipationInRandomDTO_AllMethods() {
        ParticipationInRandomDTO dto = new ParticipationInRandomDTO(200, 20, 2, 3000, 49.99);

        assertEquals(2, dto.getUserId());
        assertEquals(20, dto.getStoreId());
        assertEquals(200, dto.getProductId());
        assertEquals(3000, dto.getRandomId());

        assertFalse(dto.won());

        dto.markAsWinner();
        assertTrue(dto.won());

        dto.markAsLoser();
        assertFalse(dto.won());
    }

}
