package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
// import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.CartItem;

public class Store {
    private static final Logger logger = LoggerFactory.getLogger(Store.class);

    private int storeID;
    private String storeName;
    private String category;
    private boolean active;
    private AtomicInteger[] rank;//rank[x] is the number of people who ranked i+1
    //must add something for messages
    private List<String> messgesInStore;
    private Map<Category, List<item>> stock;//map of category -> item
    private ActivePurcheses activePurchases;

    public Store(int storeID, String storeName, String category) {
        logger.debug("Creating store: ID={}, Name={}, Category={}", storeID, storeName, category);

        this.stock = new ConcurrentHashMap<>();
        this.storeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
        this.messgesInStore = Collections.synchronizedList(new LinkedList<>());
        this.activePurchases = new ActivePurcheses(storeID);
    }

    public int getStoreID() {
        return storeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        logger.debug("Setting active status for store {} to {}", storeID, active);

        this.active = active;
    }

    public item getProductById(int id) {
        logger.debug("Fetching product with ID {}", id);

        for (List<item> items : stock.values()) {
            for (item item : items) {
                if (item.getProductId() == id) {
                    return item;
                }
            }
        }
        logger.error("Product with ID={} not found", id);

        return null; // not found
    }

    public List<item> getAllItemsInStock() {
        logger.debug("Getting all items in stock for store {}", storeID);

        List<item> allItems = new ArrayList<>();
        for (List<item> itemList : stock.values()) {
            allItems.addAll(itemList);
        }
        return allItems;
    }

    public void addItem(item newItem) {
        logger.debug("Adding item with ID {} to stock in category {}", newItem.getProductId(), newItem.getCategory());

        // Retrieve or create the list of items for the given category
        List<item> items = stock.computeIfAbsent(newItem.getCategory(), k -> new ArrayList<>());
        synchronized (items) {
            item existingItem = null;
            for (item i : items) {
                if (i.getProductId() == newItem.getProductId()) {
                    existingItem = i;
                    break;
                }
            }
            if (existingItem != null) {
                existingItem.AddQuantity();
            } else {
                items.add(newItem);
            }
        }

    }

    // remove product -> quantity=0
    public void removeItem(int itemId) throws UIException {
        logger.debug("Removing item ID={}", itemId);

        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            logger.error("Failed to remove item. ID={} not found", itemId);

            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(0);
    }

    // update quantity
    public void changeQuantity(int itemId, int quantity) throws UIException {
        logger.debug("Changing quantity of item ID={} to {}", itemId, quantity);

        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            logger.error("Failed to change quantity. ID={} not found", itemId);

            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(quantity);
    }

    // decrase quantity to buy: -> check if I need synchronized the item???

    public void decreaseQtoBuy(int itemId, int quantity) throws UIException {
        logger.debug("Decreasing quantity for item ID={} by {}", itemId, quantity);

        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            logger.error("Cannot decrease. Item ID={} not found", itemId);

            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.changeQuantity(foundItem.getQuantity() - quantity);
    }

    public void updatePrice(int itemId, int newPrice) throws UIException {
        logger.debug("Updating price for item ID={} to {}", itemId, newPrice);

        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            logger.error("Failed to update price. ID={} not found", itemId);

            throw new UIException("Item not found with ID " + itemId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        foundItem.setPrice(newPrice);
    }

    // rank product
    public void rankProduct(int productId, int newRank) throws UIException {
        logger.debug("Ranking product ID={} with rank={}", productId, newRank);

        item currenItem = getItemByProductId(productId);
        if (currenItem != null) {
            AtomicInteger[] ranks = currenItem.getRank();
            if (newRank >= 1 && newRank <= ranks.length) {
                ranks[newRank - 1].incrementAndGet(); // thread-safe increment

            } else {
                logger.error("Invalid rank value: {}", newRank);

                throw new UIException("Invalid rank index: " + newRank, ErrorCodes.INVALID_RANK);
            }
        } else {
            logger.error("Failed to rank product. ID={} not found", productId);

            throw new UIException("Product ID not found: " + productId, ErrorCodes.PRODUCT_NOT_FOUND);
        }
    }

    // FOR STORE AND TESTING
    // Thread-safe method to get an item by its productId
    public item getItemByProductId(int productId) {
        logger.debug("Fetching items by category: {}", category);

        for (List<item> items : stock.values()) {
            // Synchronize the list of items to ensure thread-safety when accessing the list
            synchronized (items) {
                for (item i : items) {
                    if (i.getProductId() == productId) {
                        return i;
                    }
                }
            }
        }
        return null;
    }

    // //search product by category //no need for it
    // public List<ItemStoreDTO> getItemsByCategory(Category category) throws
    // UIException {
    // List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
    // List<item> items = stock.get(category);
    // if (items == null) {
    // throw new Exception("there is no such category!");
    // }
    // for (item i : items) {
    // //int regularInt = i.getQuantity().get();
    // itemStoreDTOList.add(new ItemStoreDTO(i.getQuantity(), i.getPrice(),
    // i.getCategory(), i.getFinalRank()));
    // }
    // return itemStoreDTOList;
    // }
    // just for testing
    // public List<item> getItemsByCategoryObject(Category category) throws
    // UIException {
    public List<item> getItemsByCategoryObject(Category category) {
        // Use computeIfAbsent to safely retrieve or create the list for the category
        return stock.computeIfAbsent(category, k -> new ArrayList<>());
    }

    public Map<Category, List<item>> getStock() {
        return stock;
    }

    // display products in store -> also for layan
    public List<ItemStoreDTO> getProductsInStore() {
        logger.debug("Getting all products in store {}", storeID);

        List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
        for (List<item> items : stock.values()) {
            // Synchronize the list of items to ensure thread-safety when accessing the list
            synchronized (items) {// must check if it needed to be synchronized
                for (item i : items) {
                    ItemStoreDTO toAdd = new ItemStoreDTO(i.getProductId(), i.getQuantity(), i.getPrice(),
                            i.getCategory(), i.getFinalRank(), storeID);
                    itemStoreDTOList.add(toAdd);
                }
            }
        }
        return null;
    }

    // rank store:
    public boolean rankStore(int i) {
        if (i < 1 || i > 5) {
            logger.error("Invalid store rank: {}", i);

            return false;
        }
        rank[i - 1].incrementAndGet();
        logger.debug("Ranking store {} with {}", storeID, i);

        return true;
    }

    public int getFinalRateInStore(int storeId) {
        logger.debug("Calculating final rank for store {}", storeId);

        int totalVotes = 0;
        int WRank = 0;
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i].get(); // votes for rank (i+1)
            totalVotes += count;
            WRank += (i + 1) * count;
        }
        if (totalVotes == 0) {
            return 3;// defult rank

        }
        int avgRank = (int) Math.round((double) WRank / totalVotes);
        return Math.max(1, Math.min(5, avgRank));// to make surre the result is between 1 and 5

    }

    public SingleBid bidOnAuctionProduct(int auctionId, int userId, double price) throws DevException, UIException {
        return activePurchases.addUserBidToAuction(auctionId, userId, price);
    }

    public int addProductToAuction(int userid, int productId, int quantity, double startPrice, long time)
            throws UIException {
        // checkPermessionForSpecialSell(userid);
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToAuction(productId, quantity, time);
    }

    // private void checkPermessionForSpecialSell(int userid) {
    // throw new UnsupportedOperationException("Unimplemented method
    // 'checkPermessionForSpecialSell'");
    // }
    public int addProductToBid(int userid, int productId, int quantity) throws DevException, UIException {
        // checkPermessionForSpecialSell(userid);
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToBid(productId, quantity);
    }

    public SingleBid bidOnBid(int bidId, int userid, double price) throws DevException, UIException {
        return activePurchases.addUserBidToBid(bidId, userid, price);
    }

    public BidDTO[] getAllBids() {
        return activePurchases.getBids();
    }

    private void decreaseFromQuantity(int quantity, int id) throws UIException {
        item item = getItemById(id);
        if (item == null) {
            throw new UIException("Item not found with ID " + id, ErrorCodes.PRODUCT_NOT_FOUND);
        }
        if (item.getQuantity() < quantity) {
            throw new UIException("Stock not enough to make this auction.", ErrorCodes.INSUFFICIENT_STOCK);
        }
        item.changeQuantity(item.getQuantity() - quantity);
    }

    private item getItemById(int productId) {
        for (List<item> category : stock.values()) {
            for (item item : category) {
                if (item.getProductId() == productId) {
                    return item;
                }
            }
        }
        return null;
    }

    public AuctionDTO[] getAllAuctions() {
        return activePurchases.getAuctions();
    }

    public SingleBid acceptBid(int bidId, int userBidId) throws DevException, UIException {
        return activePurchases.acceptBid(userBidId, bidId);
    }

    // ====================== random
    public int addProductToRandom(int productId, int quantity, double productPrice, int storeId, long RandomTime)
            throws UIException {
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    }

    public ParticipationInRandomDTO participateInRandom(int userId, int randomid, double amountPaid)
            throws UIException {
        return activePurchases.participateInRandom(userId, randomid, amountPaid);
    }

    public ParticipationInRandomDTO end(int randomId) throws DevException, UIException {
        return activePurchases.endRandom(randomId);
    }

    public RandomDTO[] getRandoms() {
        return activePurchases.getRandoms();
    }

    // public double getCardPrice(int randomId) throws DevException {
    // return activePurchases.getCardPrice(randomId);
    // }
    public double getProductPrice(int randomId) throws DevException {
        return activePurchases.getProductPrice(randomId);
    }

    public boolean rejectBid(int bidId, int userBidId) throws DevException, UIException {
        activePurchases.rejectBid(userBidId, bidId);
        return true;
    }

    public double getStoreRating() {
        return getFinalRateInStore(storeID);
    }

    // public boolean rejectBid(int bidId, int userBidId) throws UIException {
    // activePurchases.rejectBid(userBidId,bidId);
    // return true;
    // }
    // <<<<<<< HEAD
    // =======

    public Random getRandom(int randomId) throws DevException, UIException {
        return activePurchases.getRandom(randomId);
    }

    // >>>>>>> development

    public List<ReceiptProduct> ProcessCartItems(List<ItemCartDTO> cartItems, boolean isGuest, String storeName)
            throws UIException {
        List<ReceiptProduct> boughtItems = new ArrayList<>();
        for (ItemCartDTO dto : cartItems) {
            CartItem item = new CartItem(dto);
            item storeItem = getItemByProductId(item.getProductId());

            if (storeItem == null || storeItem.getQuantity() < item.getQuantity()) {
                if (isGuest) {
                    throw new UIException("Insufficient stock during guest purchase.", ErrorCodes.INSUFFICIENT_STOCK);
                } else {
                    continue;
                }
            }
            decreaseQtoBuy(item.getProductId(), item.getQuantity());
            boughtItems.add(new ReceiptProduct(
                    item.getName(),
                    item.getCategory(),
                    item.getDescription(),
                    storeName,
                    item.getQuantity(),
                    item.getPrice()));
        }
        return boughtItems;
    }

}
