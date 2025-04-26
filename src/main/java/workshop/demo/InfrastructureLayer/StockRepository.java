package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;

public class StockRepository implements IStockRepo {


    //id must be int and have a generator 
    //must contains <category,List<Product>>
    //remove rating and quantity 
    //all functions must be deal with DTOs and primitive (params and return values)
    //think about the usecases that has to synchronized
    //add list of store ids .
    private final Map<String, Product> products = new HashMap<>(); // store products with their productId as the key.

    @Override
    public String addProduct(Product product) {
        if (products.containsKey(product.getProductId())) {
            return "Product already exists.";
        }
        products.put(product.getProductId(), product);
        return "Product added successfully.";
    }

    @Override
    public boolean removeProduct(String productId) {
        if (productId == null || !products.containsKey(productId)) {
            return false;
        }
        products.remove(productId);
        return true;
    }
    
    @Override
    public Product findById(String productId) {
        return products.get(productId);
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public boolean updateRating(String productId, double newRating) {
        Product product = products.get(productId);
        if (product == null || newRating < 0 || newRating > 5) return false;

        product.setRating(newRating);
        return true;
    }

    @Override
    public boolean updateDescription(String productId, String newDescription) {
        Product product = products.get(productId);
        if (product == null) return false;

        product.setDescription(newDescription);
        return true;
    }

    @Override
    public ProductDTO[] searchByName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchByName'");
    }

    @Override
    public ProductDTO[] searchByName(String name, Category category) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'searchByName'");
    }

}
