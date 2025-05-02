package workshop.demo.DomainLayer.Purchase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Random;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;

public class Purchase {

    private final ShoppingCart shoppingCart;
    private final IStoreRepo storeRepository;
    private final IOrderRepo orderRepository;
    private final IUserRepo userRepo;
    private final IStockRepo stockRepository;

    public Purchase(ShoppingCart shoppingCart, IStockRepo stockRepository,
            IStoreRepo storeRepository, IOrderRepo orderRepository, IUserRepo userRepo) {
        this.shoppingCart = shoppingCart;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.userRepo = userRepo;
        this.stockRepository = stockRepository;
    }

    // public Purchase(ShoppingCart shoppingCart2, IStockRepo stockRepo, IStoreRepo storeRepo, IPurchaseRepo purchaseRepo,
    //         IUserRepo userRepo2) {
    //     //TODO Auto-generated constructor stub
    // }
    private void mockPayment(CartItem item) {
        System.out.println("Payment successful for: " + item.getProductId());
    }

    private void mockPayment(SingleBid bid) {
        System.out.println("Payment successful for auction bid: "
                + "user=" + bid.getUserId()
                + ", store=" + bid.getStoreId()
                + ", amount=" + bid.getAmount()
                + ", price=" + bid.getBidPrice());
    }

    private void mockSupply(CartItem item) {
        System.out.println("Supply successful for: " + item.getProductId());
    }

    public List<ReceiptDTO> processRegularPurchase(boolean isGuest, int userId) throws Exception {
        List<ReceiptDTO> receipts = new ArrayList<>();

        // If guest, validate availability for all items
        if (isGuest) {
            List<ItemCartDTO> allItems = new ArrayList<>();
            for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
                allItems.addAll(basket.getItems());
            }
            if (!storeRepository.checkAvailability(allItems)) {
                throw new Exception("Guest purchase failed: Not all products are available.");
            }
        }

        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            double totalPrice = 0;
            List<ReceiptProduct> boughtItems = new ArrayList<>();
            String storeName = storeRepository.getStoreNameById(basket.getStoreId());

            Store store = storeRepository.findStoreByID(basket.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + basket.getStoreId());
            }

            for (ItemCartDTO dto : basket.getItems()) {
                CartItem item = new CartItem(dto);
                item storeItem = store.getItemByProductId(item.getProductId());

                if (storeItem != null && storeItem.getQuantity() >= item.getQuantity()) {
                    totalPrice += item.getPrice() * item.getQuantity();

                    boughtItems.add(new ReceiptProduct(
                            item.getName(),
                            item.getCategory(),
                            item.getDescription(),
                            storeName,
                            item.getQuantity(),
                            item.getPrice()
                    ));

                    store.decreaseQtoBuy(item.getProductId(), item.getQuantity());
                    mockPayment(item);
                    mockSupply(item);

                } else if (!isGuest) {
                    continue;
                } else {
                    throw new Exception("Unexpected missing product during Guest purchase.");
                }
            }

            if (!boughtItems.isEmpty()) {
                String date = LocalDate.now().toString();
                int finalPrice = (int) totalPrice;

                ReceiptDTO receipt = new ReceiptDTO(storeName, date, boughtItems, finalPrice);
                receipts.add(receipt);

                orderRepository.setOrderToStore(basket.getStoreId(), userId, receipt, storeName);
            } else {
                throw new DevException("bought items is empty!");
            }
        }

        return receipts;
    }

    //buying a random ticket
    public ParticipationInRandomDTO buyRandomTicket(int userId, int randomId, double amountPaid) throws Exception {
        Random random = storeRepository.getRandomById(randomId);
        ParticipationInRandomDTO participation = random.participateInRandom(userId, amountPaid);
        userRepo.ParticipateInRandom(participation); // Save participation in userRepo

        mockPayment(userId, amountPaid, randomId);
        return participation;
    }

    private void mockPayment(int userId, double amountPaid, int randomId) {
        System.out.println("Mock payment: user " + userId + " paid " + amountPaid + " for random ID " + randomId);
    }

    public void processRandomWinnings(int userId) throws Exception {
        List<ParticipationInRandomDTO> cards = userRepo.getWinningCards(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (ParticipationInRandomDTO card : cards) {
            Store store = storeRepository.findStoreByID(card.storeId);
            if (store == null) {
                throw new Exception("Store not found for ID: " + card.storeId);
            }

            item storeItem = store.getItemByProductId(card.productId);
            if (storeItem == null || storeItem.getQuantity() < 1) {
                throw new Exception("Product unavailable for supply in random win");
            }

            store.decreaseQtoBuy(card.productId, 1);

            Product product = stockRepository.findById(card.productId);
            String storeName = storeRepository.getStoreNameById(card.storeId);

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    1,
                    0 // already paid when buying the random ticket
            );
            storeToProducts.computeIfAbsent(card.storeId, k -> new ArrayList<>()).add(receiptProduct);
            System.out.println("Supplying random-won product " + product.getName() + " to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    public void processAuctionWinnings(int userId) throws Exception {
        List<SingleBid> winningBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : winningBids) {
            if (bid.getType() != SpecialType.Auction) {
                continue;
            }

            Store store = storeRepository.findStoreByID(bid.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + bid.getStoreId());
            }

            item storeItem = store.getItemByProductId(bid.getId());
            if (storeItem == null || storeItem.getQuantity() < bid.getAmount()) {
                throw new Exception("Product unavailable for auction bid supply");
            }

            store.decreaseQtoBuy(bid.getId(), bid.getAmount());

            Product product = stockRepository.findById(bid.getId());
            String storeName = storeRepository.getStoreNameById(bid.getStoreId());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            mockPayment(bid);

            System.out.println("Auction product " + product.getName() + " supplied to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    public void processBids(int userId) throws Exception {
        List<SingleBid> acceptedBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();

        for (SingleBid bid : acceptedBids) {
            if (bid.getType() != SpecialType.BID) {
                continue;
            }

            Store store = storeRepository.findStoreByID(bid.getStoreId());
            if (store == null) {
                throw new Exception("Store not found for ID: " + bid.getStoreId());
            }

            item storeItem = store.getItemByProductId(bid.getId());
            if (storeItem == null || storeItem.getQuantity() < bid.getAmount()) {
                throw new Exception("Unavailable product for bid supply");
            }

            store.decreaseQtoBuy(bid.getId(), bid.getAmount());

            Product product = stockRepository.findById(bid.getId());
            String storeName = storeRepository.getStoreNameById(bid.getStoreId());

            ReceiptProduct receiptProduct = new ReceiptProduct(
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    storeName,
                    bid.getAmount(),
                    (int) bid.getBidPrice()
            );

            storeToProducts.computeIfAbsent(bid.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            mockPayment(bid);

            System.out.println("Bid product " + product.getName() + " supplied to user " + userId);
        }

        createReceiptsPerStore(storeToProducts, userId);
    }

    private void createReceiptsPerStore(Map<Integer, List<ReceiptProduct>> storeToProducts, int userId) throws Exception {
        for (Map.Entry<Integer, List<ReceiptProduct>> entry : storeToProducts.entrySet()) {
            int storeId = entry.getKey();
            String storeName = storeRepository.getStoreNameById(storeId);
            List<ReceiptProduct> products = entry.getValue();
            String date = LocalDate.now().toString();
            double finalPrice = products.stream().mapToInt(ReceiptProduct::getPrice).sum();

            ReceiptDTO receipt = new ReceiptDTO(storeName, date, products, finalPrice);
            orderRepository.setOrderToStore(storeId, userId, receipt, storeName);
        }

    }

}
