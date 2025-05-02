package workshop.demo.DomainLayer.Purchase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import workshop.demo.DTOs.CardForRandomDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;

public class Purchase {

    private final ShoppingCart shoppingCart;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final IUserRepo userRepo;
    private final IStockRepo stockRepository;

    public Purchase(ShoppingCart shoppingCart, IStockRepo stockRepository,
                    StoreRepository storeRepository, OrderRepository orderRepository, IUserRepo userRepo) {
        this.shoppingCart = shoppingCart;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
        this.userRepo = userRepo;
        this.stockRepository = stockRepository;
    }


    private boolean allProductsAvailable() {
        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            for (CartItem item : basket.getItems()) {
                Product product = stockRepository.findById(item.getProductId());
                if (product == null || product.getTotalAmount() < item.getQuantity()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void mockPayment(CartItem item) {
        System.out.println("Payment successful for: " + item.getProductId());
    }

    private void mockPayment(SingleBid bid) {
        System.out.println("Payment successful for auction bid: " +
            "user=" + bid.getUserId() +
            ", store=" + bid.getStoreId() +
            ", amount=" + bid.getAmount() +
            ", price=" + bid.getBidPrice());
    }


    private void mockSupply(CartItem item) {
        System.out.println("Supply successful for: " + item.getProductId());
    }

    public List<ReceiptDTO> processRegularPurchase(boolean isGuest, int userId) throws Exception {
        List<ReceiptDTO> receipts = new ArrayList<>();

        if (isGuest && !allProductsAvailable()) {
            throw new Exception("Guest purchase failed: Not all products are available.");
        }

        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            double totalPrice = 0;
            List<ReceiptProduct> boughtItems = new ArrayList<>();
            String storeName = storeRepository.getStoreNameById(basket.getStoreId());

            for (CartItem item : basket.getItems()) {
                Product product = stockRepository.findById(item.getProductId());

                if (product != null && product.getTotalAmount() >= item.getQuantity()) {
                    totalPrice += item.getPrice() * item.getQuantity();
                    boughtItems.add(new ReceiptProduct(
                        item.getName(),
                        item.getCategory(),
                        item.getDescription(),
                        storeName,
                        item.getQuantity(),
                        item.getPrice()
                    ));

                    product.setTotalAmount(product.getTotalAmount() - item.getQuantity());
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

    
    public void processRandomWinnings(int userId) throws Exception {
        List<CardForRandomDTO> cards = userRepo.getWinningCards(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
    
        for (CardForRandomDTO card : cards) {
            Product product = stockRepository.findById(card.getProductId());
            if (product == null || product.getTotalAmount() < 1) {
                throw new Exception("Product unavailable for supply in random win");
            }
    
            product.setTotalAmount(product.getTotalAmount() - 1);
    
            String storeName = storeRepository.getStoreNameById(card.getStoreId());
    
            ReceiptProduct receiptProduct = new ReceiptProduct(
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                storeName,
                1,
                0 // price is 0 for won products
            );
    
            storeToProducts.computeIfAbsent(card.getStoreId(), k -> new ArrayList<>()).add(receiptProduct);
            System.out.println("Supplying won product " + product.getName() + " to user " + userId);
        }
    
        createReceiptsPerStore(storeToProducts, userId);
    }
    
    public void processAuctionWinnings(int userId) throws Exception {
        List<SingleBid> winningBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
    
        for (SingleBid bid : winningBids) {
            if (bid.getType() != SpecialType.Auction) continue;
    
            Product product = stockRepository.findById(bid.getId());
            if (product == null || product.getTotalAmount() < bid.getAmount()) {
                throw new Exception("Product unavailable for auction bid supply");
            }
    
            System.out.println("Charging user " + userId + " amount: " + bid.getBidPrice());
            mockPayment(bid);
    
            product.setTotalAmount(product.getTotalAmount() - bid.getAmount());
    
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
            System.out.println("Auction product " + product.getName() + " supplied to user " + userId);
        }
    
        createReceiptsPerStore(storeToProducts, userId);
    }
    
    public void processBids(int userId) throws Exception {
        List<SingleBid> acceptedBids = userRepo.getWinningBids(userId);
        Map<Integer, List<ReceiptProduct>> storeToProducts = new HashMap<>();
    
        for (SingleBid bid : acceptedBids) {
            if (bid.getType() != SpecialType.BID) continue;
    
            Product product = stockRepository.findById(bid.getId());
            if (product == null || product.getTotalAmount() < bid.getAmount()) {
                throw new Exception("Unavailable product for bid supply");
            }
    
            product.setTotalAmount(product.getTotalAmount() - bid.getAmount());
    
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


