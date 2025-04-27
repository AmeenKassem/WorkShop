package workshop.demo.DomainLayer.Purchase;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingBasket;
import workshop.demo.DomainLayer.User.ShoppingCart;
import workshop.demo.InfrastructureLayer.StockRepository;
import workshop.demo.InfrastructureLayer.StoreRepository;

public class Purchase {

    private final ShoppingCart shoppingCart;
    private final StockRepository stockRepository;
    private final StoreRepository storeRepository;

    public Purchase(ShoppingCart shoppingCart, StockRepository stockRepository, StoreRepository storeRepository) {
        this.shoppingCart = shoppingCart;
        this.stockRepository = stockRepository;
        this.storeRepository = storeRepository;
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

    public List<ReceiptDTO> executePurchase(boolean isGuest) throws Exception {
        List<ReceiptDTO> receipts = new ArrayList<>();

        // Step 1: Validate availability (only for guests)
        if (isGuest && !allProductsAvailable()) {
            throw new Exception("Guest purchase failed: Not all products are available.");
        }

        // Step 2: For each basket (store) in the cart
        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            double totalPrice = 0;
            List<String> boughtItems = new ArrayList<>();

            // Get store name
            String storeName = storeRepository.getStoreNameById(basket.getStoreId());

            for (CartItem item : basket.getItems()) {
                Product product = stockRepository.findById(item.getProductId());

                if (product != null && product.getTotalAmount() >= item.getQuantity()) {
                    // Product is available
                    totalPrice += item.getPrice() * item.getQuantity();
                    boughtItems.add(item.getName() + " x" + item.getQuantity());
                    
                    // Reduce stock
                    product.setTotalAmount(product.getTotalAmount() - item.getQuantity());

                    // Simulate payment and supply
                    mockPayment(item);
                    mockSupply(item);

                } else if (!isGuest) {
                    // Registered user: skip unavailable items
                    continue;
                } else {
                    // Guest must have all products available
                    throw new Exception("Unexpected missing product during Guest purchase.");
                }
            }

            // Create a receipt if items were bought
            if (!boughtItems.isEmpty()) {
                RecieptDTO receipt = new ReceiptDTO(storeName, totalPrice, boughtItems);
                receipts.add(receipt);
            }
        }

        return receipts;
    }
}
