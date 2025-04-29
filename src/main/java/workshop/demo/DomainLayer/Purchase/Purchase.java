package workshop.demo.DomainLayer.Purchase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;
import workshop.demo.InfrastructureLayer.OrderRepository;

public class Purchase {

    private final ShoppingCart shoppingCart;
    private final StockRepository stockRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    public Purchase(ShoppingCart shoppingCart, StockRepository stockRepository,
                    StoreRepository storeRepository, OrderRepository orderRepository) {
        this.shoppingCart = shoppingCart;
        this.stockRepository = stockRepository;
        this.storeRepository = storeRepository;
        this.orderRepository = orderRepository;
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

    private void mockSupply(CartItem item) {
        System.out.println("Supply successful for: " + item.getProductId());
    }

    public List<ReceiptDTO> executePurchase(boolean isGuest, int userId) throws Exception {
        List<ReceiptDTO> receipts = new ArrayList<>();

        // Validate availability (only for guests)
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
                    // Product is available
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

                // Save to order history
                orderRepository.setOrderToStore(basket.getStoreId(), userId, receipt, storeName);
            }
        }

        return receipts;
    }
}
