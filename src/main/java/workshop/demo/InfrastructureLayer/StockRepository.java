package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DomainLayer.Exceptions.ProductNotFoundException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.item;

public class StockRepository implements IStockRepo {

    private HashMap<Category, List<Integer>> categoryToProductId;
    private HashMap<Integer, Product> idToProduct;
    private AtomicInteger idGen;

    public StockRepository() {
        this.categoryToProductId = new HashMap<>();
        this.idToProduct = new HashMap<>();
        this.idGen = new AtomicInteger(1); // Start product IDs from 1
    }

    @Override
    public int addProduct(String name, Category category, String description, String[] keywords) throws Exception {
        int id = idGen.getAndIncrement();
        Product product = new Product(name, id, category, description, keywords);
        idToProduct.put(id, product);

        categoryToProductId.computeIfAbsent(category, k -> new ArrayList<>()).add(id);

        return id;
    }

    @Override
    public Product findById(int productId) {
        return idToProduct.get(productId);
    }

    @Override
    public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter) {
        List<Product> productsToSearch = new ArrayList<>();
        List<ProductDTO> res = new ArrayList<>();

        if (filter.specificCategory()) {
            List<Integer> ids = categoryToProductId.getOrDefault(filter.getCategory(), new ArrayList<>());
            for (int id : ids) {
                Product p = idToProduct.get(id);
                if (p != null) {
                    productsToSearch.add(p);
                }
            }
        } else {
            productsToSearch.addAll(idToProduct.values());
        }

        for (Product product : productsToSearch) {
            if (filter.productIsMatch(product)) {
                res.add(new ProductDTO(
                        product.getProductId(),
                        product.getName(),
                        product.getCategory(),
                        product.getDescription()));
            }
        }

        return res.toArray(new ProductDTO[0]);
    }

    @Override
    public ProductDTO GetProductInfo(int productId) {
        Product product = idToProduct.get(productId); 
        if (product == null) {
            return null;
        }
        return new ProductDTO(
            product.getProductId(),
            product.getName(),
            product.getCategory(),
            product.getDescription()
        );
    }
    

}
