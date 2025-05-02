package workshop.demo.DomainLayer.Stock;

import java.util.List;

import workshop.demo.DTOs.*;


public interface IStockRepo {

    int addProduct(String name, Category category, String description,String[] keywords) throws Exception; // Adds a global product
    // String removeProduct(int productID);
    Product findById(int productId);  
    public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter);

}

