package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.CardForRandomDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;

public class Store {

    private int stroeID;
    private String storeName;
    private String category;
    private boolean active;
    private Map<Category, List<item>> stock;//map of category -> item
    private AtomicInteger[] rank;//rank[x] is the number of people who ranked i+1
    //must add something for messages
    private double storeRating;
    private ActivePurcheses activePurchases;

    public Store(int storeID, String storeName, String category) {
        this.stock = new ConcurrentHashMap<>();
        this.stroeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
        this.activePurchases = new ActivePurcheses(storeID);
    }

    public int getStroeID() {
        return stroeID;
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

    public double getStoreRating() {
        return storeRating;
    }

    public void setStoreRating(double storeRating) {
        this.storeRating = storeRating;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    public item getProductById(int id) {
        for (List<item> items : stock.values()) {
            for (item item : items) {
                if (item.getProductId() == id) {
                    return item;
                }
            }
        }
        return null; // not found
    }

    public void addItem(item newItem) {

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
    public void removeItem(int itemId) throws Exception {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new Exception("Item not fount with ID " + itemId);
        }
        foundItem.changeQuantity(0); // Set quantity to 0 — and that's it
    }

    //update quantity
    public void changeQuantity(int itemId, int quantity) throws Exception {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new Exception("Item not fount with ID " + itemId);
        }
        foundItem.changeQuantity(quantity);
    }

    //decrase quantity to buy: -> check if I need synchronized the item???
    public void decreaseQtoBuy(int itemId) throws Exception {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new Exception("Item not fount with ID " + itemId);
        }
        foundItem.changeQuantity(foundItem.getQuantity() - 1);
    }

    // update price
    public void updatePrice(int itemId, int newPrice) throws Exception {
        item foundItem = getItemByProductId(itemId);
        if (foundItem == null) {
            throw new Exception("Item not fount with ID " + itemId);
        } else {
            foundItem.setPrice(newPrice);
        }
    }

    // rank product 
    public void rankProduct(int productId, int newRank) {
        item currenItem = getItemByProductId(productId);
        if (currenItem != null) {
            AtomicInteger[] ranks = currenItem.getRank();
            if (newRank >= 0 && newRank < ranks.length) {
                ranks[newRank - 1].incrementAndGet();  // thread-safe increment
            } else {
                throw new IllegalArgumentException("Invalid rank index: " + newRank);
            }
        } else {
            throw new IllegalArgumentException("Product ID not found: " + productId);
        }
    }

    //FOR STORE AND TESTING
    // Thread-safe method to get an item by its productId
    public item getItemByProductId(int productId) {
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
    // public List<ItemStoreDTO> getItemsByCategory(Category category) throws Exception {
    //     List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
    //     List<item> items = stock.get(category);
    //     if (items == null) {
    //         throw new Exception("there is no such category!");
    //     }
    //     for (item i : items) {
    //         //int regularInt = i.getQuantity().get();
    //         itemStoreDTOList.add(new ItemStoreDTO(i.getQuantity(), i.getPrice(), i.getCategory(), i.getFinalRank()));
    //     }
    //     return itemStoreDTOList;
    // }
    //just for testing
    //public List<item> getItemsByCategoryObject(Category category) throws Exception {
    public List<item> getItemsByCategoryObject(Category category) {
        // Use computeIfAbsent to safely retrieve or create the list for the category
        return stock.computeIfAbsent(category, k -> new ArrayList<>());
    }

    public Map<Category, List<item>> getStock() {
        return stock;
    }

    //display products in store -> also for layan
    public List<ItemStoreDTO> getProductsInStore() {
        List<ItemStoreDTO> itemStoreDTOList = new ArrayList<>();
        for (List<item> items : stock.values()) {
            // Synchronize the list of items to ensure thread-safety when accessing the list
            synchronized (items) {//must check if it needed to be synchronized
                for (item i : items) {
                    itemStoreDTOList.add(new ItemStoreDTO(i.getProductId(), i.getQuantity(), i.getPrice(), i.getCategory(), i.getFinalRank(),stroeID));
                }
            }
        }
        return null;
    }

    //rank store:
    public boolean rankStore(int i) {
        if (i < 1 || i > 5) {
            return false;
        }
        rank[i - 1].incrementAndGet();
        return true;
    }

    public int getFinalRateInStore(int storeId) {
        int totalVotes = 0;
        int WRank = 0;
        for (int i = 0; i < rank.length; i++) {
            int count = rank[i].get(); // votes for rank (i+1)
            totalVotes += count;
            WRank += (i + 1) * count;
        }
        if (totalVotes == 0) {
            return 3;//defult rank

        }
        int avgRank = (int) Math.round((double) WRank / totalVotes);
        return Math.max(1, Math.min(5, avgRank));//to make surre the result is between 1 and 5

    }

    public SingleBid bidOnAuctionProduct(int auctionId, int userId,double price) throws Exception{
        return activePurchases.addUserBidToAuction(auctionId, userId, price);
    }

    public int addProductToAuction(int userid,int productId,int quantity,double startPrice , long time) throws Exception{
        // checkPermessionForSpecialSell(userid);
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToAuction(productId, quantity, time);
    }

    // private void checkPermessionForSpecialSell(int userid) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'checkPermessionForSpecialSell'");
    // }

    public int addProductToBid(int userid,int productId,int quantity) throws Exception{
        // checkPermessionForSpecialSell(userid);
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToBid(productId, quantity);
    }

    public SingleBid bidOnBid(int bidId,int userid,double price) throws Exception {
        return activePurchases.addUserBidToBid(bidId, userid, price);
    }


    public BidDTO[] getAllBids(){
        return activePurchases.getBids();
    }

    private void decreaseFromQuantity(int quantity,int id) throws Exception{
        item item = getItemById(id);
        if(item.getQuantity()<quantity){
            throw new UIException("stock not enought to make this auction .");
        }
        item.changeQuantity(item.getQuantity()-quantity);
        // return item;
    }

    private item getItemById(int productId){
        for (List<item> category : stock.values()) {
            for (item item : category) {
                if(item.getProductId()==productId)
                    return item;
            }
        }
        return null;
    }

    public AuctionDTO[] getAllAuctions() {
       return activePurchases.getAuctions();
    }

    public SingleBid acceptBid(int bidId,int userBidId) throws Exception {
        return activePurchases.acceptBid(userBidId, bidId);
    }

    //====================== random

    public int addProductToRandom(int productId, int quantity, int cardsNumber, double priceForCard) throws Exception {
        decreaseFromQuantity(quantity, productId);
        return activePurchases.addProductToRandom(productId, quantity, cardsNumber, priceForCard);
    }

    public CardForRandomDTO buyCardForRandom(int userId,int randomid) throws Exception{
        return activePurchases.buyCardForRandomDTO(userId, randomid);
    }

    public CardForRandomDTO end(int randomId) throws Exception{
        return activePurchases.endRandom(randomId);
    }

    public RandomDTO[] getRandoms(){
        return activePurchases.getRandoms();
    }

	public double getCardPrice(int randomId) throws DevException {
		return activePurchases.getCardPrice(randomId);
	}

    public boolean rejectBid(int bidId, int userBidId) throws Exception {
        activePurchases.rejectBid(userBidId,bidId);
        return true;
    }

}
