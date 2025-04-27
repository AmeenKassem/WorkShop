package workshop.demo.DomainLayer.Stock;

import workshop.demo.DTOs.*;

public interface IStockRepo {

    int addProduct(String name, Category category, String description) throws Exception; // Adds a global product
    String removeProduct(int productID);
    Product findById(int productId);  
    ProductDTO[] getAllProducts();
    public ProductDTO[] searchByName(String name);
    ProductDTO[] searchByCategory(Category category);
}

