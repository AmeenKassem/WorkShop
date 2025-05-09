package workshop.demo.DomainLayer.Store;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.MessageDTO;

public class Store {

    private int storeID;
    private String storeName;
    private String category;
    private boolean active;
    private AtomicInteger[] rank;//rank[x] is the number of people who ranked i+1
    //must add something for messages
    private List<MessageDTO> messgesInStore;

    public Store(int storeID, String storeName, String category) {
        this.storeID = storeID;
        this.storeName = storeName;
        this.category = category;
        this.active = true;
        this.rank = new AtomicInteger[5];
        for (int i = 0; i < 5; i++) {
            rank[i] = new AtomicInteger(0);
        }
        this.messgesInStore = Collections.synchronizedList(new LinkedList<>());
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
        this.active = active;
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

    // //must be deleted ALL OF THAT:
    // public SingleBid bidOnAuctionProduct(int auctionId, int userId, double price) throws DevException, UIException {
    //     return activePurchases.addUserBidToAuction(auctionId, userId, price);
    // }
    // public int addProductToAuction(int userid, int productId, int quantity, double startPrice, long time) throws UIException {
    //     // checkPermessionForSpecialSell(userid);
    //     decreaseFromQuantity(quantity, productId);
    //     return activePurchases.addProductToAuction(productId, quantity, time);
    // }
    // // private void checkPermessionForSpecialSell(int userid) {
    // //     throw new UnsupportedOperationException("Unimplemented method 'checkPermessionForSpecialSell'");
    // // }
    // public int addProductToBid(int userid, int productId, int quantity) throws DevException, UIException {
    //     // checkPermessionForSpecialSell(userid);
    //     decreaseFromQuantity(quantity, productId);
    //     return activePurchases.addProductToBid(productId, quantity);
    // }
    // public SingleBid bidOnBid(int bidId, int userid, double price) throws DevException, UIException {
    //     return activePurchases.addUserBidToBid(bidId, userid, price);
    // }
    // public BidDTO[] getAllBids() {
    //     return activePurchases.getBids();
    // }
    // public AuctionDTO[] getAllAuctions() {
    //     return activePurchases.getAuctions();
    // }
    // public SingleBid acceptBid(int bidId, int userBidId) throws DevException, UIException {
    //     return activePurchases.acceptBid(userBidId, bidId);
    // }
    // //====================== random
    // public int addProductToRandom(int productId, int quantity, double productPrice, int storeId, long RandomTime) throws UIException {
    //     decreaseFromQuantity(quantity, productId);
    //     return activePurchases.addProductToRandom(productId, quantity, productPrice, storeId, RandomTime);
    // }
    // public ParticipationInRandomDTO participateInRandom(int userId, int randomid, double amountPaid) throws UIException {
    //     return activePurchases.participateInRandom(userId, randomid, amountPaid);
    // }
    // public ParticipationInRandomDTO end(int randomId) throws DevException, UIException {
    //     return activePurchases.endRandom(randomId);
    // }
    // public RandomDTO[] getRandoms() {
    //     return activePurchases.getRandoms();
    // }
    // public double getProductPrice(int randomId) throws DevException {
    //     return activePurchases.getProductPrice(randomId);
    // }
    // public boolean rejectBid(int bidId, int userBidId) throws DevException, UIException {
    //     activePurchases.rejectBid(userBidId, bidId);
    //     return true;
    // }
    // public double getStoreRating() {
    //     return getFinalRateInStore(storeID);
    // }
    // public Random getRandom(int randomId) throws DevException, UIException {
    //     return activePurchases.getRandom(randomId);
    // }
}
