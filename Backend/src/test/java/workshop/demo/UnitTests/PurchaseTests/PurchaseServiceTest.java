package workshop.demo.UnitTests.PurchaseTests;
// package workshop.demo.PurchaseTests;

// import java.util.List;
// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.argThat;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import workshop.demo.ApplicationLayer.PurchaseService;
// import workshop.demo.DTOs.Category;
// import workshop.demo.DTOs.ItemCartDTO;
// import workshop.demo.DTOs.ParticipationInRandomDTO;
// import workshop.demo.DTOs.PaymentDetails;
// import workshop.demo.DTOs.ReceiptDTO;
// import workshop.demo.DTOs.ReceiptProduct;
// import workshop.demo.DTOs.SingleBid;
// import workshop.demo.DTOs.SpecialType;
// import workshop.demo.DTOs.SupplyDetails;
// import workshop.demo.DomainLayer.Authentication.IAuthRepo;
// import workshop.demo.DomainLayer.Order.IOrderRepo;
// import workshop.demo.DomainLayer.Purchase.IPaymentService;
// import workshop.demo.DomainLayer.Purchase.IPurchaseRepo;
// import workshop.demo.DomainLayer.Purchase.ISupplyService;
// import workshop.demo.DomainLayer.Stock.IStockRepo;
// import workshop.demo.DomainLayer.Stock.Product;
// import workshop.demo.DomainLayer.Stock.item;
// import workshop.demo.DomainLayer.Store.IStoreRepo;
// import workshop.demo.DomainLayer.User.IUserRepo;
// import workshop.demo.DomainLayer.User.ShoppingCart;
// import workshop.demo.InfrastructureLayer.Encoder;
// import workshop.demo.InfrastructureLayer.OrderRepository;
// import workshop.demo.InfrastructureLayer.PurchaseRepository;
// import workshop.demo.InfrastructureLayer.StockRepository;
// import workshop.demo.InfrastructureLayer.StoreRepository;
// import workshop.demo.InfrastructureLayer.UserRepository;
// public class PurchaseServiceTest {
//     private IAuthRepo authRepo;
//     private IStockRepo stockRepo;
//     private IStoreRepo storeRepo;
//     private IUserRepo userRepo;
//     private IPurchaseRepo purchaseRepo;
//     private IOrderRepo orderRepo;
//     private IPaymentService paymentService;
//     private ISupplyService supplyService;
//     private PurchaseService purchaseService;
//     @BeforeEach
//     public void setup() {
//         authRepo = mock(IAuthRepo.class);
//         stockRepo = mock(IStockRepo.class);
//         storeRepo = mock(IStoreRepo.class);
//         userRepo = mock(IUserRepo.class);
//         purchaseRepo = mock(IPurchaseRepo.class);
//         orderRepo = mock(IOrderRepo.class);
//         paymentService = mock(IPaymentService.class);
//         supplyService = mock(ISupplyService.class);
//         purchaseService = new PurchaseService(authRepo, stockRepo, storeRepo, userRepo, purchaseRepo, orderRepo, paymentService, supplyService);
//     }
//     @Test
//     void testParticipateInRandom_success() throws Exception {
//         String token = "valid";
//         int userId = 2;
//         int randomId = 5;
//         int storeId = 3;
//         int productId = 7;
//         double amount = 20.0;
//         PaymentDetails paymentDetails = new PaymentDetails("1", "A", "1", "1");
//         ParticipationInRandomDTO dto = new ParticipationInRandomDTO(productId, storeId, userId, randomId, amount);
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         when(storeRepo.validatedParticipation(userId, randomId, storeId, amount)).thenReturn(dto);
//         doNothing().when(userRepo).ParticipateInRandom(dto);
//         doNothing().when(purchaseRepo).saveRandomParticipation(dto);
//         when(paymentService.processPayment(paymentDetails, amount)).thenReturn(true);
//         ParticipationInRandomDTO result = purchaseService.participateInRandom(token, randomId, storeId, amount, paymentDetails);
//         assertNotNull(result);
//         assertEquals(randomId, result.randomId);
//         assertEquals(userId, result.userId);
//     }
//     @Test
//     void testSubmitBid_success() throws Exception {
//         String token = "layan";
//         int userId = 333;
//         SingleBid bid = new SingleBid(1, 1, userId, 100.0, SpecialType.BID, 2, 1001, 2001);
//         //mock this
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         assertDoesNotThrow(() -> purchaseService.submitBid(token, bid)); //dont throw exception
//         verify(purchaseRepo, times(1)).saveBid(bid); // here im verify that saveBid was called once and already checked it in purchaseTests 
//     }
//     @Test
//     public void testSearchProductInStore_success() throws Exception {
//         String token = "token";
//         int storeId = 1;
//         Product product = new Product("TV", 333, Category.ELECTRONICS, "TV", new String[]{"tv"});
//         item storeItem = new item(333, 5, 1000, Category.ELECTRONICS);
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(stockRepo.findByIdInSystem(333)).thenReturn(product);
//         when(storeRepo.getItemByStoreAndProductId(storeId, 333)).thenReturn(storeItem);
//         when(storeRepo.getStoreNameById(storeId)).thenReturn("BestBuy");
//         String result = purchaseService.searchProductInStore(token, storeId, 333);
//         assertTrue(result.contains("TV"));
//         assertTrue(result.contains("Price: 1000"));
//         assertTrue(result.contains("Store: BestBuy"));
//     }
//     ////////test without mock
//     @Test
//     public void testBuyGuestCart_success() throws Exception {
//         Encoder encoder = mock(Encoder.class);
//         when(encoder.encodePassword(anyString())).thenAnswer(i -> i.getArgument(0));
//         UserRepository realUserRepo = new UserRepository(encoder, null);
//         StoreRepository realStoreRepo = new StoreRepository();
//         StockRepository realStockRepo = new StockRepository();
//         OrderRepository realOrderRepo = new OrderRepository();
//         PurchaseRepository realPurchaseRepo = new PurchaseRepository();
//         paymentService = mock(IPaymentService.class);
//         supplyService = mock(ISupplyService.class);
//         IAuthRepo mockAuthRepo = mock(IAuthRepo.class);
//         when(mockAuthRepo.validToken("token")).thenReturn(true);
//         when(mockAuthRepo.getUserId("token")).thenReturn(1);
//         // Add product to stock and store
//         int productId = realStockRepo.addProduct("Book", Category.HOME, "300 page", new String[]{"book"});
//         int storeId = realStoreRepo.addStoreToSystem(1, "max", "Books");
//         realStoreRepo.addItem(storeId, productId, 10, 50, Category.HOME);
//         int guestId = realUserRepo.generateGuest(); // will be ID 1
//         realUserRepo.addItemToGeustCart(guestId, new ItemCartDTO(storeId, Category.HOME, productId, 1, 50, "Book", "300 page", "max"));
//         PurchaseService realService = new PurchaseService(mockAuthRepo, realStockRepo, realStoreRepo, realUserRepo, realPurchaseRepo, realOrderRepo,
//                 paymentService, supplyService);
//         // Payment and supply details
//         PaymentDetails payment = new PaymentDetails("1234", "layan", "12/24", "123");
//         SupplyDetails supply = new SupplyDetails("123 ", "City", "State", "12345");
//         //the function i want to test
//         ReceiptDTO[] receipts = realService.buyGuestCart("token", payment, supply);
//         // Assert
//         assertEquals(1, receipts.length);
//         assertEquals("max", receipts[0].getStoreName());
//         assertEquals(50, receipts[0].getFinalPrice());
//         List<ReceiptProduct> products = receipts[0].getProductsList();
//         assertEquals(1, products.size());
//         assertEquals("Book", products.get(0).getProductName());
//         assertEquals(50, products.get(0).getPrice());
//         assertEquals(1, products.get(0).getQuantity());
//     }
//     @Test
//     public void testParticipateInRandom_success2() throws Exception {
//         // Real and mock setup
//         Encoder encoder = mock(Encoder.class);
//         when(encoder.encodePassword(anyString())).thenAnswer(i -> i.getArgument(0));
//         UserRepository userRepo = new UserRepository(encoder, null);
//         StoreRepository storeRepo = new StoreRepository();
//         StockRepository stockRepo = new StockRepository();
//         PurchaseRepository purchaseRepo = new PurchaseRepository();
//         OrderRepository orderRepo = new OrderRepository();
//         IPaymentService paymentService = mock(IPaymentService.class);
//         ISupplyService supplyService = mock(ISupplyService.class);
//         IAuthRepo authRepo = mock(IAuthRepo.class);
//         when(authRepo.validToken("token")).thenReturn(true);
//         when(authRepo.getUserId("token")).thenReturn(1);
//         // Register user and add product
//         int userId = userRepo.registerUser("layan", "333");
//         int productId = stockRepo.addProduct("Toy", Category.HOME, "Fun", new String[]{"toy"});
//         int storeId = storeRepo.addStoreToSystem(userId, "myBaby", "toys");
//         storeRepo.addItem(storeId, productId, 10, 50, Category.HOME);
//         // Add random to store
//         long randomTime = System.currentTimeMillis() + 100000;
//         int randomId = stockRepo.addProductToRandom(productId, 5, 20.0, storeId, randomTime);
//         when(paymentService.processPayment(any(), eq(20.0))).thenReturn(true); // Mock payment  
//         PurchaseService service = new PurchaseService(authRepo, stockRepo, storeRepo, userRepo, purchaseRepo, orderRepo, paymentService, supplyService);
//         PaymentDetails payment = new PaymentDetails("1234", "User", "12/25", "123");
//         ParticipationInRandomDTO result = service.participateInRandom("token", randomId, storeId, 20.0, payment);
//         assertEquals(randomId, result.randomId);
//         assertEquals(userId, result.userId);
//         assertEquals(20.0, result.amountPaid);
//     }
//     ////shopping cart empty
//     @Test
//     public void testBuyGuestCartcartEmPty() throws Exception {
//         when(authRepo.validToken("token")).thenReturn(true);
//         when(authRepo.getUserId("token")).thenReturn(3);
//         when(userRepo.getUserCart(3)).thenReturn(new ShoppingCart());  //the shopping cart empty
//         PurchaseService service = new PurchaseService(authRepo, stockRepo, storeRepo, userRepo, purchaseRepo, orderRepo, paymentService, supplyService);
//         PaymentDetails payment = new PaymentDetails("123", "layan", "12/25", "123");
//         SupplyDetails supply = new SupplyDetails("Addr", "City", "State", "123");
//         Exception ex = assertThrows(Exception.class, () -> service.buyGuestCart("token", payment, supply));
//         assertEquals("Shopping cart is empty or not found", ex.getMessage());
//     }
//     @Test
//     public void testFinalizeAuctionWins_outOfStock() throws Exception {
//         int userId = 5;
//         String token = "token";
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         SingleBid bid = new SingleBid(3, 1, userId, 50.0, SpecialType.Auction, 3, 7, 10);
//         when(userRepo.getWinningBids(userId)).thenReturn(List.of(bid));
//         // Mock findById to throw exception to check the case when product is not available
//         when(stockRepo.findByIdInSystem(3)).thenThrow(new Exception("product not avaliable"));
//         PurchaseService service = new PurchaseService(authRepo, stockRepo, storeRepo, userRepo, purchaseRepo, orderRepo, paymentService, supplyService);
//         PaymentDetails payment = new PaymentDetails("c", "n", "d", "cvv");
//         Exception ex = assertThrows(Exception.class, () -> service.finalizeAuctionWins(token, payment));
//         assertEquals("product not avaliable", ex.getMessage());
//     }
//     //dont pass 
//     @Test
//     public void testFinalizeAcceptedBids_success() throws Exception {
//         String token = "token";
//         int userId = 1;
//         int productId = 5;
//         int storeId = 10;
//         double price = 100.0;
//         int quantity = 2;
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         SingleBid bid = new SingleBid(productId, 1, userId, price, SpecialType.BID, productId, storeId, quantity);
//         when(userRepo.getWinningBids(userId)).thenReturn(List.of(bid));
//         Product product = new Product("Laptop", productId, Category.ELECTRONICS, " laptop", new String[]{"laptop"});
//         when(stockRepo.findByIdInSystem(productId)).thenReturn(product);
//         when(storeRepo.getStoreNameById(storeId)).thenReturn("kps");
//         doNothing().when(storeRepo).validateAndDecreaseStock(storeId, productId, quantity);
//         when(paymentService.processPayment(any(), eq(price))).thenReturn(true);
//         PaymentDetails payment = new PaymentDetails("456", "layan", "12/25", "456");
//         purchaseService.finalizeAcceptedBids(token, payment);
//         verify(paymentService, times(1)).processPayment(payment, (int) price); //call payment service once with the price
//         verify(orderRepo, times(1)).setOrderToStore(eq(storeId), eq(userId), argThat(receipt -> {
//             assertEquals("kps", receipt.getStoreName());
//             assertEquals(price, receipt.getFinalPrice());
//             assertEquals(1, receipt.getProductsList().size());
//             ReceiptProduct rp = receipt.getProductsList().get(0);
//             assertEquals("Laptop", rp.getProductName());
//             assertEquals(Category.ELECTRONICS, rp.getCategory());
//             assertEquals(quantity, rp.getQuantity());
//             assertEquals(price, rp.getPrice());
//             return true;
//         }), eq("TechStore"));
//     }
//     @Test
//     public void testBuyRegisteredCart_success() throws Exception {
//         Encoder encoder = mock(Encoder.class);
//         when(encoder.encodePassword(anyString())).thenAnswer(i -> i.getArgument(0));
//         UserRepository realUserRepo = new UserRepository(encoder, null);
//         StoreRepository realStoreRepo = new StoreRepository();
//         StockRepository realStockRepo = new StockRepository();
//         OrderRepository realOrderRepo = new OrderRepository();
//         PurchaseRepository realPurchaseRepo = new PurchaseRepository();
//         paymentService = mock(IPaymentService.class);
//         supplyService = mock(ISupplyService.class);
// //register
//         int userId = realUserRepo.registerUser("layan", "1234"); // userId = 1
//         IAuthRepo mockAuthRepo = mock(IAuthRepo.class);
//         when(mockAuthRepo.validToken("token")).thenReturn(true);
//         when(mockAuthRepo.getUserId("token")).thenReturn(userId);
//         // Add product to stock and store
//         int productId = realStockRepo.addProduct("Laptop", Category.ELECTRONICS, "i7", new String[]{"laptop"});
//         int storeId = realStoreRepo.addStoreToSystem(userId, "ksp", "Electronics");
//         realStoreRepo.addItem(storeId, productId, 5, 999, Category.ELECTRONICS);
//         // Add item toshopping cart
//         ItemCartDTO item = new ItemCartDTO(storeId, Category.ELECTRONICS, productId, 1, 999, "Laptop", "High-end", "TechZone");
//         realUserRepo.addBidToRegularCart(new SingleBid(productId, 1, userId, 999.0, SpecialType.BID, productId, storeId, 1));
//         realUserRepo.getUserCart(userId).addItem(storeId, item);
//         // Build PurchaseService 
//         PurchaseService service = new PurchaseService(mockAuthRepo, realStockRepo, realStoreRepo, realUserRepo, realPurchaseRepo, realOrderRepo,
//                 paymentService, supplyService
//         );
//         PaymentDetails payment = new PaymentDetails("4111", "layan", "12/24", "123");
//         SupplyDetails supply = new SupplyDetails("add", "city", "state", "12345");
//         ReceiptDTO[] receipts = service.buyRegisteredCart("token", payment, supply); //the function i want to test
//         assertEquals(1, receipts.length);
//         assertEquals("ksp", receipts[0].getStoreName());
//         assertEquals(999, receipts[0].getFinalPrice());
//         List<ReceiptProduct> items = receipts[0].getProductsList();
//         assertEquals(1, items.size());
//         assertEquals("Laptop", items.get(0).getProductName());
//         assertEquals(999, items.get(0).getPrice());
//         assertEquals(1, items.get(0).getQuantity());
//     }
//     @Test
//     public void testFinalizeRandomWinnings_success() throws Exception {
//         String token = "token";
//         int userId = 1;
//         int storeId = 10;
//         int productId = 5;
//         int randomId = 7;
//         ParticipationInRandomDTO card = new ParticipationInRandomDTO(productId, storeId, userId, randomId, 20.0);
//         List<ParticipationInRandomDTO> winningCards = List.of(card);
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         when(userRepo.getWinningCards(userId)).thenReturn(winningCards);
//         Product product = new Product("laptop", productId, Category.ELECTRONICS, "i7", new String[]{"phone"});
//         when(stockRepo.findByIdInSystem(productId)).thenReturn(product);
//         when(storeRepo.getStoreNameById(storeId)).thenReturn("ksp");
//         doNothing().when(storeRepo).validateAndDecreaseStock(storeId, productId, 1);
//         when(supplyService.processSupply(any())).thenReturn(true);
//         SupplyDetails supply = new SupplyDetails("Street", "City", "State", "000");
//         assertDoesNotThrow(() -> purchaseService.finalizeRandomWinnings(token, supply));
//         //here im verify that saveBid was called once and its already checked it in purchaseTests also check if the recipt unclude correct data
//         verify(orderRepo, times(1)).setOrderToStore(eq(storeId), eq(userId), argThat(receipt -> {
//             assertEquals("ksp", receipt.getStoreName());
//             assertEquals(0, receipt.getFinalPrice()); // already paid
//             assertEquals(1, receipt.getProductsList().size());
//             ReceiptProduct rp = receipt.getProductsList().get(0);
//             assertEquals("laptop", rp.getProductName());
//             assertEquals(Category.ELECTRONICS, rp.getCategory());
//             assertEquals(1, rp.getQuantity());
//             assertEquals(0, rp.getPrice());
//             return true;
//         }), eq("ksp"));
//     }
//     @Test
//     public void testFinalizeRandomWinningsproductNotFound() throws Exception {
//         String token = "token";
//         int userId = 1;
//         int storeId = 10;
//         int productId = 5;
//         int randomId = 7;
//         ParticipationInRandomDTO card = new ParticipationInRandomDTO(productId, storeId, userId, randomId, 20.0);
//         List<ParticipationInRandomDTO> winningCards = List.of(card);
//         when(authRepo.validToken(token)).thenReturn(true);
//         when(authRepo.getUserId(token)).thenReturn(userId);
//         when(userRepo.isRegistered(userId)).thenReturn(true);
//         when(userRepo.getWinningCards(userId)).thenReturn(winningCards);
//         when(stockRepo.findByIdInSystem(productId)).thenThrow(new Exception("product not avaliable")); // simulate the product avaliable 
//         SupplyDetails supply = new SupplyDetails("Street", "City", "State", "000");
//         Exception ex = assertThrows(Exception.class, () -> purchaseService.finalizeRandomWinnings(token, supply));
//         assertEquals("product not avaliable", ex.getMessage());
//     }
// }
