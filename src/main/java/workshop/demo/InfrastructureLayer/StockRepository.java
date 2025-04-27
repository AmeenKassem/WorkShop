
package workshop.demo.InfrastructureLayer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;
import workshop.demo.DomainLayer.Exceptions.ProductNotFoundException;


//import workshop.demo.DomainLayer.Stock.ProductDTO;
public class StockRepository implements IStockRepo {

    private final Map<Integer, Product> products = new HashMap<>();  // Map of productId -> Product
    private final Map<Category, List<Product>> categoryProducts = new HashMap<>(); // Category -> List of Products
    private static final AtomicInteger counterSId = new AtomicInteger(1);
    private SuperDataStructure data;

    public StockRepository() {
        this.data = new SuperDataStructure();
    }

    public static int generateId() {
        return counterSId.getAndIncrement();
    }


    public synchronized int addProduct(String name,Category category, String description) throws Exception {
        for(Product product : products.values()){
            if(product.getName().equals(name)){
                throw new Exception("Product already exists in the system");
            }
            int id = generateId();
            Product newProduct = new Product(name,id, category, description); 
            products.put(newProduct.getProductId(), newProduct);
            return id;
        }
        return -1; 
    }

    @Override
    public synchronized String removeProduct(int productID) throws ProductNotFoundException {
        if (products.containsKey(productID)) {
            products.remove(productID);
            return "Product " + productID + " removed successfully.";
        } else {
            throw new ProductNotFoundException("Product " + productID + " does not exist.");
        }
    }
    
    @Override
    public Product findById(int productId) {
        return products.get(productId);
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public List<ProductDTO> searchByName(String name) {
        List<Product> matchingProducts = products.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        return matchingProducts.stream()
                .map(product -> new ProductDTO(product.getProductId(), product.getName(), product.getCategory(), product.getDescription()))
                .collect(Collectors.toList());
    }

    // Search by category
    public List<ProductDTO> searchByCategory(Category category) {
        List<Product> matchingProducts = categoryProducts.getOrDefault(category, new ArrayList<>());
        
        return matchingProducts.stream()
                .map(product -> new ProductDTO(product.getProductId(), product.getName(), product.getCategory(), product.getDescription()))
                .collect(Collectors.toList());
    }




}


