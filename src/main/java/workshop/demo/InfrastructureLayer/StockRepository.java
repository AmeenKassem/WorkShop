package workshop.demo.InfrastructureLayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;

public class StockRepository implements IStockRepo {

    private HashMap<Category, List<Integer>> categoryToProductId = new HashMap<>();
    private HashMap<Integer, Product> idToProduct = new HashMap<>();
    private AtomicInteger idGen = new AtomicInteger(1);

    @Override
    public int addProduct(String name, Category category, String description, String[] keywords) {
        int id = idGen.getAndIncrement();
        Product product = new Product(name, id, category, description, keywords);
        idToProduct.put(id, product);
        categoryToProductId.computeIfAbsent(category, k -> new ArrayList<>()).add(id);
        return id;
    }

    @Override
    public Product findById(int productId) throws UIException {
        Product product = idToProduct.get(productId);
        if (product == null)
            throw new UIException("Product not available.", ErrorCodes.PRODUCT_NOT_FOUND);
        return product;
    }

    @Override
    public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter) {
        List<ProductDTO> result = new ArrayList<>();
        List<Product> products = filter.specificCategory()
                ? categoryToProductId.getOrDefault(filter.getCategory(), new ArrayList<>()).stream()
                .map(idToProduct::get)
                .filter(Objects::nonNull)
                .toList()
                : new ArrayList<>(idToProduct.values());

        for (Product product : products) {
            if (filter.productIsMatch(product)) {
                result.add(new ProductDTO(product.getProductId(), product.getName(), product.getCategory(), product.getDescription()));
            }
        }
        return result.toArray(new ProductDTO[0]);
    }

    @Override
    public ProductDTO GetProductInfo(int productId) {
        Product product = idToProduct.get(productId);
        if (product == null)
            return null;
        return new ProductDTO(product.getProductId(), product.getName(), product.getCategory(), product.getDescription());
    }
}
