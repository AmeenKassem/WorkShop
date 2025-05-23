package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.hibernate.validator.internal.util.logging.Log_.logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Stock.item;
import workshop.demo.DomainLayer.Store.ActivePurcheses;

@Repository
@Repository
public class StockRepository implements IStockRepo {

    // private HashMap<Category, List<Integer>> categoryToProductId = new
    // HashMap<>();
    // private HashMap<Integer, Product> idToProduct = new HashMap<>();
    // private HashMap<Category, List<Integer>> categoryToProductId = new
    // HashMap<>();
    // private HashMap<Integer, Product> idToProduct = new HashMap<>();
    private AtomicInteger idGen = new AtomicInteger(1);
    private ConcurrentHashMap<Integer, ActivePurcheses> storeId2ActivePurchases;// must be thread safe
    private ConcurrentHashMap<Integer, ActivePurcheses> storeId2ActivePurchases;// must be thread safe
    private ConcurrentHashMap<Category, List<Product>> allProducts;
    private ConcurrentHashMap<Integer, StoreStock> storeStocks;// storeId, stock of store
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private AISearch ai = new AISearch();

    @Autowired
    @Autowired
    public StockRepository() {
        this.storeId2ActivePurchases = new ConcurrentHashMap<>();// must be thread safe
        this.storeId2ActivePurchases = new ConcurrentHashMap<>();// must be thread safe
        this.allProducts = new ConcurrentHashMap<>();
        this.storeStocks = new ConcurrentHashMap<>();// storeId, stock of store
        this.storeStocks = new ConcurrentHashMap<>();// storeId, stock of store

    }

    private ActivePurcheses getActivePurchases(int storeId) throws UIException {
        if (!storeId2ActivePurchases.containsKey(storeId)) {
            throw new UIException("store not found on active purchases hashmap", ErrorCodes.STORE_NOT_FOUND);
        }
        return storeId2ActivePurchases.get(storeId);
    }

    private void checkQuantity(int productId, int quantity, int storeId) throws UIException {
        if (storeStocks.get(storeId).getItemByProductId(productId).getQuantity() < quantity) {
            throw new UIException("there is no quantity to move it to special purchases",
                    ErrorCodes.INSUFFICIENT_ITEM_QUANTITY_TO_RANDOM);
    private void checkQuantity(int productId, int quantity, int storeId) throws UIException {
        if (storeStocks.get(storeId).getItemByProductId(productId).getQuantity() < quantity) {
            throw new UIException("there is no quantity to move it to special purchases",
                    ErrorCodes.INSUFFICIENT_ITEM_QUANTITY_TO_RANDOM);
        }
    }

    @Override
    public int addProduct(String name, Category category, String description, String[] keywords) {
        int id = idGen.getAndIncrement();
        Product product = new Product(name, id, category, description, keywords);
        // add it if not exist:
        for (String key : keywords) {
            ai.addPair(name, key, true);
        }
        for (Category defCategory : allProducts.keySet()) {
            if (defCategory.equals(category))
                continue;
            ai.addPair(name, defCategory.name(), false);
            for (Product defProduct : allProducts.get(defCategory)) {
                ai.addPair(name, defProduct.getName(), false);
                logger.info("ai trained to "+name +","+defProduct.getName()+" are not the same");
            }
            // break;
        }
        allProducts.computeIfAbsent(category, k -> new ArrayList<>()).add(product);
        return id;
    }

    @Override
    public Product findByIdInSystem_throwException(int productId) throws UIException {
    public Product findByIdInSystem_throwException(int productId) throws UIException {
        for (List<Product> productList : allProducts.values()) {
            for (Product product : productList) {
                if (product.getProductId() == productId) {
                    return product;
                }
            }
        }
        return null;
        // throw new UIException("Product not available.",
        // ErrorCodes.PRODUCT_NOT_FOUND);
        // throw new UIException("Product not available.",
        // ErrorCodes.PRODUCT_NOT_FOUND);
    }

    // @Override
    // public ProductDTO[] getMatchesProducts(ProductSearchCriteria filter) {
    // List<ProductDTO> result = new ArrayList<>();
    // List<Product> products = filter.specificCategory()
    // ? allProducts.getOrDefault(filter.getCategory(), new ArrayList<>()).stream()
    // .map(idToProduct::get)
    // .filter(Objects::nonNull)
    // .toList()
    // : new ArrayList<>(idToProduct.values());
    // for (Product product : products) {
    // if (filter.productIsMatch(product)) {
    // result.add(new ProductDTO(product.getProductId(), product.getName(),
    // product.getCategory(), product.getDescription()));
    // }
    // }
    // return result.toArray(new ProductDTO[0]);
    // List<ProductDTO> result = new ArrayList<>();
    // List<Product> products = filter.specificCategory()
    // ? allProducts.getOrDefault(filter.getCategory(), new ArrayList<>()).stream()
    // .map(idToProduct::get)
    // .filter(Objects::nonNull)
    // .toList()
    // : new ArrayList<>(idToProduct.values());
    // for (Product product : products) {
    // if (filter.productIsMatch(product)) {
    // result.add(new ProductDTO(product.getProductId(), product.getName(),
    // product.getCategory(), product.getDescription()));
    // }
    // }
    // return result.toArray(new ProductDTO[0]);
    // }
    // fix it -> bhaa must ckeck it I did not understand it well so I tried this:

    // why do we need that? if it's for review it to user -> it's wrong
    // fix it -> bhaa must ckeck it I did not understand it well so I tried this:

    // why do we need that? if it's for review it to user -> it's wrong
    @Override
    public ProductDTO GetProductInfo(int productId) throws UIException {
        Product product = findByIdInSystem_throwException(productId);
        Product product = findByIdInSystem_throwException(productId);
        if (product == null) {
            return null;
        }
        return new ProductDTO(product.getProductId(), product.getName(), product.getCategory(),
                product.getDescription());
        return new ProductDTO(product.getProductId(), product.getName(), product.getCategory(),
                product.getDescription());
    }

    @Override
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price)
            throws UIException, DevException {
        return getActivePurchases(StoreId).addUserBidToAuction(auctionId, userId, price);
    }

    @Override
    public int addAuctionToStore(int StoreId, int productId, int quantity, long tome, double startPrice)
            throws UIException, DevException {
        checkQuantity(productId, quantity, StoreId);
        checkQuantity(productId, quantity, StoreId);
        int res = getActivePurchases(StoreId).addProductToAuction(productId, quantity, tome);
        decreaseQuantitytoBuy(StoreId, productId, quantity);
        return res;
    }

    @Override
    public AuctionDTO[] getAuctionsOnStore(int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).getAuctions();
    }

    @Override
    public int addProductToBid(int storeId, int productId, int quantity) throws UIException, DevException {
        checkQuantity(productId, quantity, storeId);
        checkQuantity(productId, quantity, storeId);
        int res = getActivePurchases(storeId).addProductToBid(productId, quantity);
        decreaseQuantitytoBuy(storeId, productId, quantity);
        return res;
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
    public boolean rejectBid(int storeId, int bidId, int userBidId) throws UIException, Exception {
        return getActivePurchases(storeId).rejectBid(userBidId, bidId);
        return getActivePurchases(storeId).rejectBid(userBidId, bidId);
    }

    @Override
    public SingleBid acceptBid(int storeId, int bidId, int userBidId) throws UIException, DevException {
        return getActivePurchases(storeId).acceptBid(userBidId, bidId);
    }

    @Override
    public int addProductToRandom(int productId, int quantity, double productPrice, int storeId,
            long RandomTime) throws UIException, DevException {
        checkQuantity(productId, quantity, storeId);
        int res = getActivePurchases(storeId).addProductToRandom(productId, quantity, productPrice, storeId,
                RandomTime);
        checkQuantity(productId, quantity, storeId);
        int res = getActivePurchases(storeId).addProductToRandom(productId, quantity, productPrice, storeId,
                RandomTime);
        this.decreaseQuantitytoBuy(storeId, productId, quantity);
        return res;
    }

    @Override
    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid)
            throws UIException, DevException {
        return getActivePurchases(storeId).participateInRandom(userId, randomId, amountPaid);
    }

    @Override
    public ParticipationInRandomDTO endRandom(int storeId, int randomId) throws Exception {
        return getActivePurchases(storeId).endRandom(randomId);
    }

    @Override
    public RandomDTO[] getRandomsInStore(int storeId) throws UIException, DevException {
        return getActivePurchases(storeId).getRandoms();
    }

    // stock managment:
    // stock managment:
    @Override
    public void addStore(int storeId) {
        storeId2ActivePurchases.put(storeId, new ActivePurcheses(storeId));
        this.storeStocks.put(storeId, new StoreStock(storeId));
    }


    @Override
    public ItemStoreDTO[] getProductsInStore(int storeId) throws UIException, DevException {
        return search(new ProductSearchCriteria(null, null, null, storeId, null, null, null, null));
    }

    @Override
    public item addItem(int storeId, int productId, int quantity, int price, Category category)
            throws UIException, DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        item toAdd = new item(productId, quantity, price, category);
        this.storeStocks.get(storeId).addItem(toAdd);
        return toAdd;
    }

    @Override
    public void removeItem(int storeId, int productId) throws UIException, DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        this.storeStocks.get(storeId).removeItem(productId);

    }

    @Override
    public void decreaseQuantitytoBuy(int storeId, int productId, int quantity) throws UIException, DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        this.storeStocks.get(storeId).decreaseQuantitytoBuy(productId, quantity);

    }

    @Override
    public boolean updateQuantity(int storeId, int productId, int newQuantity) throws UIException, DevException {
    public boolean updateQuantity(int storeId, int productId, int newQuantity) throws UIException, DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        this.storeStocks.get(storeId).changeQuantity(productId, newQuantity);
        return true;
        return true;
    }

    @Override
    public boolean updatePrice(int storeId, int productId, int newPrice) throws UIException, DevException {
    public boolean updatePrice(int storeId, int productId, int newPrice) throws UIException, DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        this.storeStocks.get(storeId).updatePrice(productId, newPrice);
        return true;
        return true;
    }

    @Override
    public void rankProduct(int storeId, int productId, int newRank) throws DevException, UIException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        this.storeStocks.get(storeId).rankProduct(productId, newRank);
    }


    @Override
    public boolean checkAvailability(List<ItemCartDTO> cartItems) {
        for (ItemCartDTO itemDTO : cartItems) {
            StoreStock stock = storeStocks.get(itemDTO.storeId);
            if (stock == null) {
                return false;
            }
            StoreStock stock = storeStocks.get(itemDTO.storeId);
            if (stock == null) {
                return false;
            }
            item storeItem = stock.getItemByProductId(itemDTO.productId);
            if (storeItem == null || storeItem.getQuantity() < itemDTO.quantity) {
                return false;
            }
        }
        return true;
    }

    @Override
    public item getItemByStoreAndProductId(int storeId, int productId) throws DevException {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        return storeStocks.get(storeId).getItemByProductId(productId);
    }

    // to ask Bahaa:
    // to ask Bahaa:
    // public double getProductPrice(int storeId, int randomId) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getProductPrice'");
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getProductPrice'");
    // }
    @Override
    public void validateAndDecreaseStock(int storeId, int productId, int amount) throws DevException, UIException {
        item storeItem = getItemByStoreAndProductId(storeId, productId);
        if (storeItem == null || storeItem.getQuantity() < amount) {
            throw new DevException("unvaliable");
        }
        decreaseQuantitytoBuy(storeId, productId, amount);
    }

    public double calculateTotalPrice(List<ReceiptProduct> items) {
        double total = 0;
        for (ReceiptProduct item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public ParticipationInRandomDTO validatedParticipation(int userId, int randomId, int storeId, double amountPaid)
    public ParticipationInRandomDTO validatedParticipation(int userId, int randomId, int storeId, double amountPaid)
            throws DevException, UIException {
        if (storeStocks.get(storeId) == null) {
        if (storeStocks.get(storeId) == null) {
            throw new DevException("Store stock not initialized for storeId in repo: " + storeId);
        }
        // double requiredPrice = getProductPrice(storeId, randomId);// total price
        // if (amountPaid > requiredPrice) {
        // throw new DevException("Insufficient payment");
        // }
        return this.storeId2ActivePurchases.get(storeId).participateInRandom(userId, randomId, amountPaid);
    }

    public double getProductPrice(int storeId, int randomId) throws DevException {
    public double getProductPrice(int storeId, int randomId) throws DevException {
        return this.storeId2ActivePurchases.get(storeId).getProductPrice(randomId);
    }


    public List<ReceiptProduct> processCartItemsForStore(int storeId, List<ItemCartDTO> cartItems, boolean isGuest)
            throws Exception {
        StoreStock storeStock = storeStocks.get(storeId);
        if (storeStock == null) {
            throw new DevException("Store stock not initialized for storeId: " + storeId);
        }
        return storeStock.ProcessCartItems(cartItems, isGuest, storeId);
    }

    // @Override
    // public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria,
    // ProductDTO[] matchesProducts)
    // throws UIException, DevException {

    // List<ItemStoreDTO> itemList = new LinkedList<>();
    // if (criteria.getStoreId() == -1) {// search in all stores
    // for (StoreStock stock : this.storeStocks.values()) {
    // List<item> lilStock = stock.getAllItemsInStock();
    // for (item item1 : lilStock) {
    // for (ProductDTO pro : matchesProducts) {
    // if (item1.getProductId() == pro.getProductId() &&
    // criteria.matchesForStore(item1)) {
    // ItemStoreDTO toAdd = new ItemStoreDTO(
    // item1.getProductId(),
    // item1.getQuantity(),
    // item1.getPrice(),
    // item1.getCategory(),
    // item1.getFinalRank(),
    // stock.getStoreStockId());
    // itemList.add(toAdd);
    // break; // found matching product -> no need to check other products
    // }
    // }
    // }
    // }

    // } else {// search in a spicific store
    // StoreStock stock = this.storeStocks.get(criteria.getStoreId());
    // if (stock == null) {
    // throw new UIException("stock does not exist", ErrorCodes.STOCK_NOT_FOUND);
    // }
    // for (ProductDTO pro : matchesProducts) {
    // item item1 = stock.getItemByProductId(pro.getProductId());
    // if (item1 != null && criteria.matchesForStore(item1)) {
    // ItemStoreDTO toAdd = new ItemStoreDTO(item1.getProductId(),
    // item1.getQuantity(), item1.getPrice(),
    // item1.getCategory(), item1.getFinalRank(), stock.getStoreStockId());
    // itemList.add(toAdd);
    // }
    // }

    // }

    // return itemList.toArray(new ItemStoreDTO[0]);
    // }

    @Override
    public void checkProductExists_ThrowException(int productId) throws UIException {
        if (findByIdInSystem_throwException(productId) == null) {
            throw new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND);
        }
        StoreStock storeStock = storeStocks.get(storeId);
        if (storeStock == null) {
            throw new DevException("Store stock not initialized for storeId: " + storeId);
        }
        return storeStock.ProcessCartItems(cartItems, isGuest, storeId);
    }

    // @Override
    // public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria,
    // ProductDTO[] matchesProducts)
    // throws UIException, DevException {

    // List<ItemStoreDTO> itemList = new LinkedList<>();
    // if (criteria.getStoreId() == -1) {// search in all stores
    // for (StoreStock stock : this.storeStocks.values()) {
    // List<item> lilStock = stock.getAllItemsInStock();
    // for (item item1 : lilStock) {
    // for (ProductDTO pro : matchesProducts) {
    // if (item1.getProductId() == pro.getProductId() &&
    // criteria.matchesForStore(item1)) {
    // ItemStoreDTO toAdd = new ItemStoreDTO(
    // item1.getProductId(),
    // item1.getQuantity(),
    // item1.getPrice(),
    // item1.getCategory(),
    // item1.getFinalRank(),
    // stock.getStoreStockId());
    // itemList.add(toAdd);
    // break; // found matching product -> no need to check other products
    // }
    // }
    // }
    // }

    // } else {// search in a spicific store
    // StoreStock stock = this.storeStocks.get(criteria.getStoreId());
    // if (stock == null) {
    // throw new UIException("stock does not exist", ErrorCodes.STOCK_NOT_FOUND);
    // }
    // for (ProductDTO pro : matchesProducts) {
    // item item1 = stock.getItemByProductId(pro.getProductId());
    // if (item1 != null && criteria.matchesForStore(item1)) {
    // ItemStoreDTO toAdd = new ItemStoreDTO(item1.getProductId(),
    // item1.getQuantity(), item1.getPrice(),
    // item1.getCategory(), item1.getFinalRank(), stock.getStoreStockId());
    // itemList.add(toAdd);
    // }
    // }

    // }

    // return itemList.toArray(new ItemStoreDTO[0]);
    // }

    @Override
    public void checkProductExists_ThrowException(int productId) throws UIException {
        if (findByIdInSystem_throwException(productId) == null) {
            throw new UIException("Product not found", ErrorCodes.PRODUCT_NOT_FOUND);
        }
    }

    @Override
    public ParticipationInRandomDTO getRandomCardIfWinner(int storeId, int specialId, int userId) {
        try {
            return getActivePurchases(storeId).getRandomCardIfWinner(specialId, userId);
        } catch (UIException ex) {
            return null;
        }
    }

    @Override
    public SingleBid getBidIfWinner(int storeId, int specialId, int bidId, SpecialType type) {
        try {
            return getActivePurchases(storeId).getBidIfWinner(specialId, bidId, type);
        } catch (UIException ex) {
            return null;
        }
    }

    @Override
    public SingleBid getBid(int storeId, int specialId, int bidId, SpecialType type) throws UIException {
        return getActivePurchases(storeId).getBidWithId(specialId, bidId, type);
    }

    @Override
    public String GetProductNameForBid(int storeId, int specialId, SpecialType type) throws UIException {
        int productId = getActivePurchases(storeId).getProductIdForSpecial(specialId, type);
        return GetProductInfo(productId).getName();
    }

    @Override
    public ParticipationInRandomDTO getRandomCard(int storeId, int specialId, int randomId) throws UIException {
        return getActivePurchases(storeId).getCardWithId(specialId, randomId);
    }

    public void clear() {
        idGen.set(1);
        for (ActivePurcheses activePurchases : storeId2ActivePurchases.values()) {
            activePurchases.clear();
        }
        if (storeId2ActivePurchases != null) {
            storeId2ActivePurchases.clear();
        }

        if (allProducts != null) {
            allProducts.clear();
        }

        if (storeStocks != null) {
            storeStocks.clear();
        }
    }

    // @Override
    // public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria,
    // ProductDTO[] matchesProducts)
    // throws Exception {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getMatchesItems'");
    // }

    @Override
    public ItemStoreDTO[] search(ProductSearchCriteria criteria) throws UIException {
        List<Product> matchesCategoryProduct = getProductsFilteredByCategory(criteria);
        List<ItemStoreDTO> result = new ArrayList<>();
        for (Product product : matchesCategoryProduct) {
            if (!criteria.productIsMatch(product))
                continue;
            List<StoreStock> matchesStores = getMatchesStore(criteria);
            for (StoreStock store : matchesStores) {
                item item = store.getItemByProductId(product.getProductId());
                if (criteria.matchesForStore(item)) {
                    ItemStoreDTO toAdd = convertToItemStoreDTO(item, product, store.getStoreStockId(),
                            product.getProductId());
                    result.add(toAdd);
                }
            }
        }

        return criteria.sortOnArray(result);

        // return result.toArray(new ItemStoreDTO[0]);

    }

    private ItemStoreDTO convertToItemStoreDTO(item item, Product product, int storeId, int productId) {
        return new ItemStoreDTO(product.getProductId(), item.getQuantity(), item.getPrice(), product.getCategory(),
                item.getFinalRank(), storeId, product.getName(), productId);
    }

    private List<StoreStock> getMatchesStore(ProductSearchCriteria criteria) {
        List<StoreStock> stores = new ArrayList<>();
        if (criteria.specificStore()) {
            stores.add(storeStocks.get(criteria.getStoreId()));
        } else {
            stores.addAll(storeStocks.values());
        }
        return stores;
    }

    private List<Product> getProductsFilteredByCategory(ProductSearchCriteria criteria) {
        List<Product> matchesCategoryProduct = new ArrayList<>();
        if (criteria.specificCategory()) {
            matchesCategoryProduct = allProducts.get(criteria.getCategory());
        } else {
            for (List<Product> productByCategory : allProducts.values()) {
                matchesCategoryProduct.addAll(productByCategory);
            }
        }
        return matchesCategoryProduct;
    }

    @Override
    public ProductDTO[] getAllProducts() {
        List<ProductDTO> allProductDTOs = new ArrayList<>();
        for (List<Product> productList : allProducts.values()) {
            for (Product product : productList) {
                allProductDTOs.add(new ProductDTO(
                        product.getProductId(),
                        product.getName(),
                        product.getCategory(),
                        product.getDescription()));
            }
        }
        return allProductDTOs.toArray(new ProductDTO[0]);
    }

}
