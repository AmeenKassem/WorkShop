package workshop.demo.DomainLayer.Purchase;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.RecieptDTO;
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
        this.storeRepository = storeRepository; 
        this.shoppingCart = shoppingCart;
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

    // Mock supply function
    private void mockSupply(CartItem item) {
        System.out.println("Supply successful for: " + item.getProductId());
    }

        public List<RecieptDTO> executePurchase(boolean isGuest) throws Exception {
        List<RecieptDTO> receipts = new ArrayList<>();

        // Step 1: Validate availability
        if (isGuest) {
            if (!allProductsAvailable()) {
                throw new Exception("Guest purchase failed: Not all products are available.");
            }
        }

        // Step 2: Go over each basket
        for (ShoppingBasket basket : shoppingCart.getBaskets().values()) {
            double totalPrice = 0;
            String storeName = storeRepository.getStoreNameById(storeId); // get from repo
            String storeName = basket.getStoreName(); // I assume you have store name in Basket

            List<String> boughtItems = new ArrayList<>();

            for (CartItem item : basket.getItems()) {
                Product product = stockRepository.findById(item.getProductId());

                if (product != null && product.getTotalAmount() >= item.getQuantity()) {
                    // Product is available
                    totalPrice += item.getPrice() * item.getQuantity();
                    boughtItems.add(item.getName() + " x" + item.getQuantity());
                    product.setTotalAmount(product.getTotalAmount() - item.getQuantity());
            
                    mockPayment(item);
                    mockSupply(item);

                } else if (!isGuest) {
                    // Product not available and registered user → skip
                    continue;
                } else {
                    throw new Exception("Unexpected missing product during Guest purchase.");
                }
            }

            // If anything was bought, create a receipt
            if (!boughtItems.isEmpty()) {
                RecieptDTO receipt = new RecieptDTO(storeName, totalPrice, boughtItems);  //missing implementation of RecieptDTO
                receipts.add(receipt);
            }
        }

        return receipts;
    }


}
