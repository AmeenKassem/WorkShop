package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Exceptions.ProductNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;

public class StockRepository implements IStockRepo {

    private final IStoreRepo IStoreRepo;
    private final Map<Integer, Product> products = new HashMap<>();
    private final Map<Category, List<Product>> categoryProducts = new HashMap<>();
    private static final AtomicInteger counterSId = new AtomicInteger(1);

    public StockRepository(IStoreRepo IStoreRepo) {
        this.IStoreRepo = IStoreRepo; 
    }

    public static int generateId() {
        return counterSId.getAndIncrement();
    }

    public synchronized int addProduct(String name, Category category, String description) throws Exception {
        for (Product product : products.values()) {
            if (product.getName().equals(name)) {
                throw new Exception("Product already exists in the system");
            }
        }
        int id = generateId();
        Product newProduct = new Product(name, id, category, description);
        products.put(newProduct.getProductId(), newProduct);

        categoryProducts.computeIfAbsent(category, k -> new ArrayList<>()).add(newProduct);

        return id;
    }

    @Override
    public synchronized String removeProduct(int productID) throws ProductNotFoundException {
        if (products.containsKey(productID)) {
            Product removed = products.remove(productID);
            categoryProducts.getOrDefault(removed.getCategory(), new ArrayList<>()).remove(removed);
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
    public ProductDTO[] getAllProducts() {
        return products.values().stream()
                .map(product -> new ProductDTO(
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                product.getDescription()
        ))
                .toArray(ProductDTO[]::new);
    }

    public ProductDTO[] searchByName(String name) {
        return products.values().stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .map(product -> new ProductDTO(
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                product.getDescription()
        ))
                .toArray(ProductDTO[]::new);
    }

    public ProductDTO[] searchByCategory(Category category) {
        List<Product> matchingProducts = categoryProducts.getOrDefault(category, new ArrayList<>());

        return matchingProducts.stream()
                .map(product -> new ProductDTO(
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                product.getDescription()
        ))
                .toArray(ProductDTO[]::new);
    }

    public ProductDTO[] searchByKeyword(String keyword) {
        return products.values().stream()
                .filter(product -> product.getKeywords() != null && product.getKeywords().contains(keyword))
                .map(product -> new ProductDTO(
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                product.getDescription()
        ))
                .toArray(ProductDTO[]::new);
    }

    @Override
    public List<ItemStoreDTO> getItemsByStoreId(int storeId) throws Exception {
        Store store = IStoreRepo.findStoreByID(storeId);

        if (store == null) {
            throw new Exception("Store with ID " + storeId + " does not exist");
        }
        return store.getProductsInStore();
    }

    public double getStoreRating(int storeId) {
        Store store = IStoreRepo.findStoreByID(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store with ID " + storeId + " not found");
        }
        return store.getStoreRating(); //not implemented yet
    }

    public List<ItemStoreDTO> getItemsByProductId(int productId) throws Exception {
        List<ItemStoreDTO> itemsForProduct = new ArrayList<>();

        for (Store store : IStoreRepo.getStores()) {

            List<ItemStoreDTO> itemsInStore = store.getProductsInStore();

            for (ItemStoreDTO item : itemsInStore) {
                if (item.getId() == productId) {
                    itemsForProduct.add(item);
                }
            }
        }

        if (itemsForProduct.isEmpty()) {
            throw new Exception("No items found for product ID: " + productId);
        }

        return itemsForProduct;
    }
}
