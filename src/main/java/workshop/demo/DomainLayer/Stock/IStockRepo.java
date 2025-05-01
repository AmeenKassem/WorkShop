package workshop.demo.DomainLayer.Stock;

import java.util.List;

import java.util.List;

import workshop.demo.DTOs.*;


public interface IStockRepo {

    // int addProduct(String name, Category category, String description) throws Exception; // Adds a global product
    // String removeProduct(int productID);
    // Product findById(int productId);  
    // ProductDTO[] getAllProducts();
    // ProductDTO[] FilterByName(String name,ProductDTO[] productsToFilter);
    // ProductDTO[] FilterByCategory(Category category,ProductDTO[] productsToFilter);
    // ProductDTO[] FilterByKeyword(String keywordFilter,ProductDTO[] productsToFilter);

    ProductDTO[] Filter(Filters filter);
}

