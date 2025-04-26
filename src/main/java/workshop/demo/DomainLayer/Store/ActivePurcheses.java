package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.User.bidShoppingCart;

public class ActivePurcheses {

    private int storeId;

    private List<BID> activeBid;
    private List<Random> activeRandom;
    private HashMap<Integer,Auction> activeAuction;

    private static AtomicInteger bidIdGen = new AtomicInteger();
    private static AtomicInteger auctionIdGen = new AtomicInteger();
    private static AtomicInteger randomIdGen = new AtomicInteger();

    public int addProductToAuction(int productId, int quantity, long time) {
        int id = auctionIdGen.incrementAndGet();
        Auction auction = new Auction(productId,quantity,time,id,storeId);
        activeAuction.put(id,auction);
        return id;
    }

    public int addProductToBid(int productId, int quantity, long time){
        int id = bidIdGen.incrementAndGet();
        BID bid = new BID(productId,quantity,time,id, storeId);
        activeBid.add(bid);
        return id;
    }

    public SingleBid addUserBidToAuction(int auctionId, int userId,double price) throws Exception{
        if(!activeAuction.containsKey(auctionId)){
            throw new DevException("Id is not found on auction hashmap!");
        }
        return activeAuction.get(auctionId).bid(userId, price);
    }



}
