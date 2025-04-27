package workshop.demo.DomainLayer.Stock;

import java.util.List;

import workshop.demo.DTOs.*;
import workshop.demo.DTOs.ProductDTO;

public interface IStockRepo {

    int addProduct(String name, Category category, String description) throws Exception; // Adds a global product
    String removeProduct(int productID);
    Product findById(int productId);  
    List<Product> getAllProducts();
    public List<ProductDTO> searchByName(String name);
    List<ProductDTO> searchByCategory(Category category);
}

