package workshop.demo.DomainLayer.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Auction  {

    private int productId;
    
    private int quantity;

    private AuctionStatus status;

    private Timer timer;

    private int auctionId;

    private List<SingleBid> bids;

    private double maxBid;

    private SingleBid winner;

    private int storeId; 

    private AtomicInteger idGen = new AtomicInteger();

    public Auction(int productId, int quantity, long time, int id, int storeId) {
        this.productId=productId;
        this.quantity=quantity;
        this.timer = new Timer();
        bids = new ArrayList<>();
        this.storeId=storeId;
        auctionId = id;
        timer.schedule(new TimerTask() {
            @Override 
            public void run(){
                for (SingleBid singleBid : bids) {
                    if(maxBid==singleBid.getBidPrice()){
                        winner = singleBid;
                        winner.markAsWinner();

                    }else{
                        singleBid.markAsLosed();
                    }
                }
                status= AuctionStatus.FINISH;
            }
        }, storeId);
        status = AuctionStatus.IN_PROGRESS;
    }

    public SingleBid bid(int userId,double price) throws Exception{
        if(price<=maxBid){
            throw new UIException("Cant bid less than current");
        }
        if(status==AuctionStatus.FINISH){
            throw new UIException("This Bid is done!");
        }

        maxBid = price;
        
        SingleBid bid = new SingleBid(productId,quantity,userId,price,SpecialType.Auction,storeId,idGen.incrementAndGet(),auctionId);
        
        bids.add(bid);
        
        return bid;
    }



}
enum AuctionStatus{
    IN_PROGRESS , FINISH
}