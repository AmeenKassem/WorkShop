package workshop.demo.IntegrationTests.ServiceTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import workshop.demo.ApplicationLayer.AdminService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PaymentServiceImp;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.SupplyServiceImp;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
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

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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
   AdminService adminService;
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
       String[] keywords = { "Laptop", "Lap", "top" };
       int productId = stockService.addProduct(NOToken, "Laptop", Category.ELECTRONICS, "Gaming Laptop", keywords);

       assertEquals(1, stockService.addItem(createdStoreId, NOToken, productId, 10, 2000, Category.ELECTRONICS));
       itemStoreDTO = new ItemStoreDTO(1, 10, 2000, Category.ELECTRONICS, 0, createdStoreId, "Laptop");
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
   //Shgal
   @Test
   void add_discount() throws Exception {
       Store store = storeRepository.getStores().get(0);
       Predicate<DiscountScope> cond = scope -> scope.containsCategory("ELECTRONICS");
       Discount discount = new VisibleDiscount("10% off elec", 0.1, cond);
       store.addDiscount(discount);
       List<ItemStoreDTO> items = List.of(itemStoreDTO);
       DiscountScope scope = new DiscountScope(items);
       assertTrue(store.getDiscount().isApplicable(scope));
       assertEquals(2000.0, store.getDiscount().apply(scope), 0.1);
   }
   @Test
   void buy_normal_discount() throws Exception {
       Store storeId = storeRepository.getStores().get(0);
       //Predicate<DiscountScope> cond = scope -> scope.containsCategory("ELECTRONICS");
       CreateDiscountDTO dto = new CreateDiscountDTO("10% off electonics",0.1,
               CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
       //System.out.println("Hm:"+cond.toString());
       storeService.addDiscountToStore(storeId.getStoreID(),NOToken,dto);
       userService.addToUserCart(GToken,itemStoreDTO,4);

       PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
       SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
       ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
       assertEquals(7200,receipts[0].getFinalPrice());
   }
    @Test
    void buy_AND_discount() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond1 = scope -> scope.containsCategory("ELECTRONICS");
        Predicate<DiscountScope> cond2 = scope -> scope.containsCategory("HOME");

        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:HOME");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond1);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond2);

        AndDiscount andDiscount = new AndDiscount("asdf off ELECTRONICS");

       andDiscount.addDiscount(discount1);
        andDiscount.addDiscount(discount2);

        storeId.setDiscount(andDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,3);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(6000,receipts[0].getFinalPrice());
    }
    @Test
    void buy_OR_discount1() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond = scope -> scope.containsCategory("ELECTRONICS");

        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond);


        OrDiscount ORDiscount = new OrDiscount("asdf off ELECTRONICS");
        ORDiscount.addDiscount(discount1);
        ORDiscount.addDiscount(discount2);

        storeId.setDiscount(ORDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(5600,receipts[0].getFinalPrice());
    }
    @Test
    void buy_OR_discount2() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond1 = scope -> scope.containsCategory("ELECTRONICS");
        Predicate<DiscountScope> cond2 = scope -> scope.containsCategory("HOME");

        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond1);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond2);


        OrDiscount ORDiscount = new OrDiscount(" off ELECTRONICS");
        ORDiscount.addDiscount(discount1);
        ORDiscount.addDiscount(discount2);

        storeId.setDiscount(ORDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(7200,receipts[0].getFinalPrice());
    }
    @Test
    void buy_XOR_discount1() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond = scope -> scope.containsCategory("ELECTRONICS");
        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond);


        XorDiscount XORDiscount = new XorDiscount("asdf off ELECTRONICS");
        XORDiscount.addDiscount(discount1);
        XORDiscount.addDiscount(discount2);

        storeId.setDiscount(XORDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(8000,receipts[0].getFinalPrice());
    }
    @Test
    void buy_XOR_discount2() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond1 = scope -> scope.containsCategory("ELECTRONICS");
        Predicate<DiscountScope> cond2 = scope -> scope.containsCategory("HOME");

        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond1);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond2);


        XorDiscount XORDiscount = new XorDiscount("asdf off ELECTRONICS");
        XORDiscount.addDiscount(discount1);
        XORDiscount.addDiscount(discount2);

        storeId.setDiscount(XORDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(7200,receipts[0].getFinalPrice());
    }
    @Test
    void buy_Max_discount() throws Exception {
        Store storeId = storeRepository.getStores().get(0);
        Predicate<DiscountScope> cond = scope -> scope.containsCategory("ELECTRONICS");
        CreateDiscountDTO dto1 = new CreateDiscountDTO("10% off ELECTRONICS",0.10,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");
        CreateDiscountDTO dto2 = new CreateDiscountDTO("20% off ELECTRONICS",0.20,
                CreateDiscountDTO.Type.VISIBLE,"CATEGORY:ELECTRONICS");

        Discount discount1 = new VisibleDiscount("10% off ELECTRONICS",0.10,cond);
        Discount discount2 = new VisibleDiscount("20% off ELECTRONICS",0.20,cond);


        MaxDiscount maxDiscount = new MaxDiscount("asdf off ELECTRONICS");
        maxDiscount.addDiscount(discount1);
        maxDiscount.addDiscount(discount2);

        storeId.setDiscount(maxDiscount);


        userService.addToUserCart(GToken,itemStoreDTO,4);

        PaymentDetails paymentDetails = PaymentDetails.testPayment(); // fill if needed
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails(); // fill if needed
        ReceiptDTO[] receipts = purchaseService.buyGuestCart(GToken, paymentDetails, supplyDetails);
        assertEquals(6400,receipts[0].getFinalPrice());
    }



}

