package workshop.demo.DomainLayer.Stock;

import java.util.List;

public interface IStockRepo {

    public List<ProductDTO> viewProductsInStore(int storeID);

    String addProduct(Product product);  // Adds a global product
    boolean removeProduct(String productId);  
    Product findById(String productId);  
    List<Product> getAllProducts();  

    boolean updateRating(String productId, double newRating);  
    boolean updateDescription(String productId, String newDescription);  

}
