package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
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

    public ActivePurcheses(int storeId){
        this.storeId=storeId;
    }
    
    public int addProductToAuction(int productId, int quantity, long time) throws UIException {
        if(quantity<=0 || time<=0) throw new UIException("you cant set value!");
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
    
    public int addProductToBid(int productId, int quantity) throws UIException {
        if(quantity<=0) throw new UIException("you cant set "+quantity+" value!");
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

    public SingleBid acceptBid(int userBidId,int bidId) throws Exception{
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Id is not found on bid hashmap!");
        }
        return activeBid.get(bidId).acceptBid(userBidId);
        
    }

    public void rejectBid(int userBidId,int bidId)throws Exception{
        if (!activeBid.containsKey(bidId)) {
            throw new DevException("Id is not found on bid hashmap!");
        }
        activeBid.get(bidId).rejectBid(userBidId);
    }
    
    // ========== Random ==========
    
    public int addProductToRandom(int productId, int quantity, double productPrice,int storeId, long RandomTime) throws Exception {
        if(quantity<=0) throw new UIException("you cant set "+quantity+" value!");
        if(productPrice<=0) throw new UIException("you cant set "+productPrice+" value!");
        if(RandomTime<=0) throw new UIException("you cant set "+RandomTime+" value!");
        int id = randomIdGen.incrementAndGet();
        Random random = new Random(productId, quantity,productPrice,id,storeId,RandomTime);
        activeRandom.put(id, random);
        return id;
    }

    public ParticipationInRandomDTO participateInRandom(int userId,int randomId,double productPrice) throws Exception{
        if(!activeRandom.containsKey(randomId)) throw new UIException("trying to buy a card from unfound random id...");
        if(productPrice <= 0) throw new UIException("you cant set "+productPrice+" value!");
        if(!activeRandom.get(randomId).isActive()){
             activeRandom.remove(randomId);
             throw new UIException("Random has ended...");
        }
        return activeRandom.get(randomId).participateInRandom(userId, productPrice);
    }
    
    public ParticipationInRandomDTO endRandom(int randomId) throws Exception{
        if(!activeRandom.containsKey(randomId)) throw new DevException("trying to buy a card from unfound random id...");
        return activeRandom.get(randomId).endRandom();
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

    public Random getRandom(int randomId) throws Exception {
        if(!activeRandom.containsKey(randomId)) throw new DevException("trying to buy a card from unfound random id...");
        return activeRandom.get(randomId);
    }

    // public double getCardPrice(int randomId) throws DevException {
    //     if(!activeRandom.containsKey(randomId)) throw new DevException("trying to buy a card from unfound random id...");
    //     return activeRandom.get(randomId).getPrice();
    // }

    public double getProductPrice(int randomId) throws DevException {
        if(!activeRandom.containsKey(randomId)) throw new DevException("trying to buy a card from unfound random id...");
        return activeRandom.get(randomId).getProductPrice();
    }
    


}
