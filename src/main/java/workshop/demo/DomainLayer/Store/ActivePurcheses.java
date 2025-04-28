package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.User.bidShoppingCart;

public class ActivePurcheses {

    private int storeId;

    // Changed Lists to HashMaps
    private HashMap<Integer, BID> activeBid = new HashMap<>();
    private HashMap<Integer, Random> activeRandom = new HashMap<>();
    private HashMap<Integer, Auction> activeAuction = new HashMap<>();
    
    // ID Generators
    private static AtomicInteger bidIdGen = new AtomicInteger();
    private static AtomicInteger auctionIdGen = new AtomicInteger();
    private static AtomicInteger randomIdGen = new AtomicInteger();
    
    // ========== Auction ==========
    
    public int addProductToAuction(int productId, int quantity, long time) {
        int id = auctionIdGen.incrementAndGet();
        Auction auction = new Auction(productId, quantity, time, id, storeId);
        activeAuction.put(id, auction);
        return id;
    }
    
    public SingleBid addUserBidToAuction(int auctionId, int userId, double price) throws Exception {
        if (!activeAuction.containsKey(auctionId)) {
            throw new DevException("Id is not found on auction hashmap!");
        }
        return activeAuction.get(auctionId).bid(userId, price);
    }
    
    public AuctionDTO[] getAuctions() {
        AuctionDTO[] auctionDTOs = new AuctionDTO[activeAuction.size()];
        int i = 0;
        for (Auction auction : activeAuction.values()) {
            auctionDTOs[i] = auction.getDTO();
            i++;
        }
        return auctionDTOs;
    }
    
    // ========== BID ==========
    
    public int addProductToBid(int productId, int quantity) {
        int id = bidIdGen.incrementAndGet();
        BID bid = new BID(productId, quantity,  id, storeId);
        activeBid.put(id, bid);
        return id;
    }
    
    public SingleBid addUserBidToBid(int bidId, int userId, double price) throws Exception {
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Id is not found on bid hashmap!");
        }
        return activeBid.get(bidId).bid(userId, price);
    }
    
    public BidDTO[] getBids() {
        BidDTO[] bidDTOs = new BidDTO[activeBid.size()];
        int i = 0;
        for (BID bid : activeBid.values()) {
            bidDTOs[i] = bid.getDTO();
            i++;
        }
        return bidDTOs;
    }

    public boolean acceptBid(int userBidId,int bidId) throws Exception{
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Id is not found on bid hashmap!");
        }
        activeBid.get(bidId).acceptBid(userBidId);
        return true;
    }

    public void rejectBid(int userBidId,int bidId)throws Exception{
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Id is not found on bid hashmap!");
        }
        activeBid.get(bidId).rejectBid(userBidId);
    }
    
    // ========== Random ==========
    
    public int addProductToRandom(int productId, int quantity, int numberOfCards,double priceForCard) {
        int id = randomIdGen.incrementAndGet();
        Random random = new Random(productId, quantity, numberOfCards,priceForCard,id,storeId);
        activeRandom.put(id, random);
        return id;
    }
    
    public RandomDTO[] getRandoms() {
        RandomDTO[] randomDTOs = new RandomDTO[activeRandom.size()];
        int i = 0;
        for (Random random : activeRandom.values()) {
            randomDTOs[i] = random.getDTO();
            i++;
        }
        return randomDTOs;
    }
    

}
