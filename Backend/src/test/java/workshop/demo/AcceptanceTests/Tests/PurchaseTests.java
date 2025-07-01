package workshop.demo.AcceptanceTests.Tests;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import workshop.demo.External.PaymentServiceImp;
import workshop.demo.External.SupplyServiceImp;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.*;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

@SpringBootTest
@ActiveProfiles("test")
public class PurchaseTests extends AcceptanceTests {

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
    private Registered user2 = new Registered(USER1_ID, USER1_USERNAME, ENCODED_PASSWORD, 19);

    private Store store;
    private Product product;
    private StoreStock storeStock;
    private ActivePurcheses activePurcheses;

    // =======================
    // Constructor
    // =======================
    public PurchaseTests() throws DevException {
    }


    @BeforeEach
    void setup() throws Exception {
        var paymentServiceImp = Mockito.mock(PaymentServiceImp.class);
     var   supplyServiceImp = Mockito.mock(SupplyServiceImp.class);

    when(paymentServiceImp.processPayment(any(PaymentDetails.class), anyDouble()))
    .thenReturn(42);
    when(supplyServiceImp.processSupply(any(SupplyDetails.class)))
    .thenReturn(55555);
    purchaseService.setPaymentService(paymentServiceImp);
        purchaseService.setSupplyService(supplyServiceImp);
        // Setup user2
        user2 = new Registered(USER2_ID, USER2_USERNAME, ENCODED_PASSWORD, 20);
        user2.login();

        // Setup store and product
        store = new Store(STORE_NAME, STORE_CATEGORY);
        product = new Product(PRODUCT_NAME, Category.Electronics, PRODUCT_DESC, KEYWORD);



        // Needed for store
        item normalItem = new item(product.getProductId(), 10, PRODUCT_PRICE, Category.Electronics);
        storeStock = new StoreStock(store.getstoreId());
        storeStock.addItem(normalItem);

        //set to auction and win
        activePurcheses = new ActivePurcheses(store.getstoreId());
        Auction auction = activePurcheses.addProductToAuction(product.getProductId(), 2, 1000, 2.0);
        storeStock.decreaseQuantitytoBuy(normalItem.getProductId(),2);
        auction.bid(user1.getId(), 200.0); // user1 places a bid
        auction.setEndTimeMillis(System.currentTimeMillis() - 1000);
        auction.endAuction();
        System.out.println("Winner User ID: " + auction.getWinner().getUserId());

        //set to bid and accept
        int bidid = activePurcheses.addProductToBid(product.getProductId(),2);
        storeStock.decreaseQuantitytoBuy(normalItem.getProductId(),2);
        SingleBid bid = activePurcheses.addUserBidToBid(bidid, user2.getId(),150);
        activePurcheses.acceptBid(bidid, bid.getId(), List.of(user1.getId()), user1.getId());

        //set to random and win
        Random random = activePurcheses.addProductToRandom(
                product.getProductId(), 2, PRODUCT_PRICE, store.getstoreId(), 1000
        );
        storeStock.decreaseQuantitytoBuy(normalItem.getProductId(), 2);
        var participation = random.participateInRandom(user2.getId(), PRODUCT_PRICE, user2.getUsername());
        random.endRandom(); // properly triggers picking winner
        random.setActive(false);

        // Needed for user
        user2.addToCart(new CartItem(
                new ItemCartDTO(store.getstoreId(), product.getProductId(), 2, PRODUCT_PRICE, product.getName(), STORE_NAME, Category.Electronics)
        ));

        // Add special items to special cart
        user2.addSpecialItemToCart(new UserSpecialItemCart(
                store.getstoreId(), auction.getId(), 0, SpecialType.Auction, product.getProductId()
        ));
        user2.addSpecialItemToCart(new UserSpecialItemCart(
                store.getstoreId(), bidid, bid.getId(), SpecialType.BID, product.getProductId()
        ));
        user2.addSpecialItemToCart(new UserSpecialItemCart(
                store.getstoreId(), random.getRandomId(), -1, SpecialType.Random, product.getProductId()
        ));
        System.out.println(storeStock.getStock().get(product.getProductId()).getQuantity());
    }


    @Test
    void Add_AuctionBidToSpecialCart_Success_and_buy() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        PaymentDetails paymentDetails = PaymentDetails.testPayment();

        ReceiptDTO[] re = purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);

        print(re);
        assertNotNull(re, "Receipts should not be null");
        assertTrue(re.length > 0, "Should have at least one receipt");

        ReceiptDTO receipt = re[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertTrue(receipt.getFinalPrice() > 0);
        assertFalse(receipt.getProductsList().isEmpty(), "Receipt should have products");

        ReceiptProduct rp1 = receipt.getProductsList().get(1);
        assertEquals(product.getProductId(), rp1.getProductId());
        assertEquals(product.getName(), rp1.getProductName());
        assertEquals(200, rp1.getPrice());


    }




    @Test
    void Add_BidProductToSpecialCart_Success_And_Buy() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        PaymentDetails paymentDetails = PaymentDetails.testPayment();

        ReceiptDTO[] re = purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);

        print(re);
        assertNotNull(re, "Receipts should not be null");
        assertTrue(re.length > 0, "Should have at least one receipt");

        ReceiptDTO receipt = re[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertTrue(receipt.getFinalPrice() > 0);
        assertFalse(receipt.getProductsList().isEmpty(), "Receipt should have products");

        ReceiptProduct rp = receipt.getProductsList().get(0);
        assertEquals(product.getProductId(), rp.getProductId());
        assertEquals(product.getName(), rp.getProductName());
        assertEquals(150, rp.getPrice());


    }
    @Test
    void ParticipateInRandom_Success_And_Buy() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();
        PaymentDetails paymentDetails = PaymentDetails.testPayment();

        ReceiptDTO[] re = purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);

        print(re);
        assertNotNull(re, "Receipts should not be null");
        assertTrue(re.length > 0, "Should have at least one receipt");

        ReceiptDTO receipt = re[0];
        assertEquals(store.getStoreName(), receipt.getStoreName());
        assertTrue(receipt.getFinalPrice() > 0);
        assertFalse(receipt.getProductsList().isEmpty(), "Receipt should have products");

        ReceiptProduct rp2 = receipt.getProductsList().get(2);
        assertEquals(product.getProductId(), rp2.getProductId());
        assertEquals(product.getName(), rp2.getProductName());
        assertEquals(100, rp2.getPrice());
    }

    @Test
    void FinalizeSpecialCart_EmptyCart_Failure() throws Exception {
        user2.clearSpecialCart(); // assuming this method exists
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);
        assertNotNull(receipts);
        assertEquals(0, receipts.length, "Should have no receipts if special cart is empty");
    }


    @Test
    void FinalizeSpecialCart_MixedSpecialTypes_Success() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.of(product));
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);

        print(receipts);
        assertNotNull(receipts);
        assertEquals(1, receipts.length);
    }

    @Test
    void FinalizeSpecialCart_UserSuspended_Failure() throws Exception {
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.of(new UserSuspension(user2.getId(), System.currentTimeMillis() + 10000)));
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        UIException ex = assertThrows(UIException.class, () ->
                purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails));
        assertTrue(ex.getMessage().contains("Suspended"));
    }

    @Test
    void FinalizeSpecialCart_BadStock_Failure() throws Exception {
        doNothing().when(mockAuthRepo).checkAuth_ThrowTimeOutException(user2Token, logger);
        when(mockAuthRepo.getUserId(user2Token)).thenReturn(user2.getId());
        when(mockSusRepo.findById(user2.getId())).thenReturn(Optional.empty());
        when(mockUserRepo.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(mockActivePurchases.findById(store.getstoreId())).thenReturn(Optional.of(activePurcheses));
        when(mockStockRepo1.findById(0)).thenReturn(Optional.empty());
        when(mockStoreRepo.findById(store.getstoreId())).thenReturn(Optional.of(store));

        PaymentDetails paymentDetails = PaymentDetails.testPayment();
        SupplyDetails supplyDetails = SupplyDetails.getTestDetails();

        assertThrows(Exception.class, () -> {
            purchaseService.finalizeSpecialCart(user2Token, paymentDetails, supplyDetails);
        });
    }



    @Test
    void Add_BidProductToSpecialCart_Success() throws UIException, DevException {

    }
    @Test
    void Add_BidProduct_Failure_InvalidToken() throws UIException {
    }

    @Test
    void Add_BidProduct_Failure_UserSuspended() throws Exception {
    }

    @Test
    void Add_BidProduct_Failure_StoreNotFound() throws Exception {
    }

    @Test
    void Add_BidProduct_Failure_BidNotFound() throws Exception {
    }

    @Test
    void Add_AuctionBidToSpecialCart_Success() throws UIException, DevException {
    }



    @Test
    void Add_AuctionBid_Failure_InvalidToken() throws UIException {
    }

    @Test
    void Add_AuctionBid_Failure_UserSuspended() throws Exception {

    }

    @Test
    void Add_AuctionBid_Failure_StoreNotFound() throws Exception {
    }

    @Test
    void Add_AuctionBid_Failure_AuctionNotFound() throws Exception {
    }

    @Test
    void Set_ProductToRandom_Success() throws Exception {

    }
    @Test
    void Set_ProductToRandom_Failure_InvalidToken() throws UIException {
    }

    @Test
    void Set_ProductToRandom_Failure_UserSuspended() throws Exception {
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