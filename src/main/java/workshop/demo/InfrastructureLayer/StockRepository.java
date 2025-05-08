package workshop.demo.InfrastructureLayer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.*;
import workshop.demo.DomainLayer.Store.ActivePurcheses;

public class StockRepository implements IStockRepo {

    private HashMap<Category, List<Integer>> categoryToProductId = new HashMap<>();
    private HashMap<Integer, Product> idToProduct = new HashMap<>();
    private AtomicInteger idGen = new AtomicInteger(1);
    private HashMap<Integer,ActivePurcheses> storeId2ActivePurchases = new HashMap<>();
    
    
    private ActivePurcheses getActivePurchases(int storeId) throws UIException{
        if(!storeId2ActivePurchases.containsKey(storeId))
        throw new UIException("store not found on active purchases hashmap", ErrorCodes.STORE_NOT_FOUND);
        return storeId2ActivePurchases.get(storeId);
    }

    private void checkQuantity(int productId, int quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkQuantity'");
    }

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

    @Override
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price)
            throws UIException, DevException {
        return getActivePurchases(StoreId).addUserBidToAuction(auctionId, userId, price);
    }

    @Override
    public int addAuctionToStore(int StoreId,  int productId, int quantity, long tome, double startPrice)
            throws UIException, DevException {
        checkQuantity(productId,quantity);
        return getActivePurchases(StoreId).addProductToAuction(productId, quantity, tome);
    }


    @Override
    public AuctionDTO[] getAuctionsOnStore(int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).getAuctions();
    }

    @Override
    public int addProductToBid(int storeId,  int productId, int quantity) throws UIException, DevException {
        checkQuantity(productId, quantity);
        return getActivePurchases(storeId).addProductToBid(productId, quantity);
    }

    @Override
    public SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).addUserBidToBid(bidId, userId, price);
    }

    @Override
    public BidDTO[] getAllBids(int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).getBids();
    }

    @Override
    public boolean rejectBid( int storeId, int bidId, int userBidId) throws UIException,Exception {
        return getActivePurchases(storeId).rejectBid(bidId, userBidId);
    }

    @Override
    public SingleBid acceptBid(int storeId, int bidId, int userBidId) throws UIException, DevException {
        return getActivePurchases(storeId).acceptBid(userBidId, bidId);
    }

    @Override
    public int addProductToRandom( int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
        checkQuantity(productId, quantity);
        return getActivePurchases(storeId).addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    }

    @Override
    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException {
        return getActivePurchases(storeId).participateInRandom(userId, randomId, amountPaid);
    }

    @Override
    public ParticipationInRandomDTO endRandom(int storeId,  int randomId) throws Exception {
        return getActivePurchases(storeId).endRandom(randomId);
    }

    @Override
    public RandomDTO[] getRandomsInStore(int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).getRandoms();
    }

    @Override
    public void addStore(int storeId) {
        storeId2ActivePurchases.put(storeId, new ActivePurcheses(storeId));
        // TODO Rahaf add store stock init
        throw new UnsupportedOperationException("Unimplemented method 'addStore'");
    }
}
