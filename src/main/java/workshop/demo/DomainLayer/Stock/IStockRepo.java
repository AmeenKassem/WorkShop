package workshop.demo.DomainLayer.Stock;
import java.util.List;

import workshop.demo.DTOs.*;
// import workshop.demo.DTOs.ProductDTO;

public interface IStockRepo {

    String addProduct(Product product);  // Adds a global product
    boolean removeProduct(String productId);  
    Product findById(String productId);  
    List<Product> getAllProducts();  


    //There is no need...
    boolean updateRating(String productId, double newRating);  
    boolean updateDescription(String productId, String newDescription);  
    //...

    // added by bhaa

    /**
     * This function will return all the products that contains the @param name .
     * 
     * @param name
     * @return
     */
    public ProductDTO[] searchByName(String name);

    /**
     * This function will return all the products that contains the @param name in the @param category.
     * 
     * @param name
     * @param category
     * @return
     */
    public ProductDTO[] searchByName(String name,Category category); 
    



    



}
