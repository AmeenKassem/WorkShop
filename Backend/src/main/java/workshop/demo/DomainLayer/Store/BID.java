package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

// import org.hibernate.validator.internal.util.logging.Log_.logger;

import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class BID {
    private int productId;
    private int quantity;
    private boolean isAccepted;
    private int bidId;
    private SingleBid winner;
    private int storeId;

    private final Object lock = new Object();
    private static AtomicInteger idGen = new AtomicInteger();
    public HashMap<Integer, SingleBid> bids;

    public BID(int productId, int quantity, int id, int storeId) {
        this.productId = productId;
        this.quantity = quantity;
        this.isAccepted = false;
        this.bidId = id;
        this.bids = new HashMap<>();
        this.storeId = storeId;
    }

    public BidDTO getDTO() {
        BidDTO bidDTO = new BidDTO();
        bidDTO.productId = productId;
        bidDTO.quantity = quantity;
        bidDTO.isAccepted = isAccepted;
        bidDTO.bidId = bidId;
        bidDTO.winner = winner;
        bidDTO.storeId = storeId;

        SingleBid[] arrayBids = new SingleBid[bids.size()];
        int i = 0;
        for (SingleBid bid : bids.values()) {
            arrayBids[i] = bid;
            i++;
        }
        bidDTO.bids = arrayBids;
        return bidDTO;
    }

    public SingleBid bid(int userId, double price) throws UIException {
        synchronized (lock) {
            if (isAccepted)
                throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

            SingleBid bid = new SingleBid(productId, quantity, userId, price, SpecialType.BID, storeId,
                    idGen.incrementAndGet(), bidId);
            bids.put(bid.getId(), bid);
            return bid;
        }
    }

    public SingleBid acceptBid(int userBidId) throws DevException ,UIException {
        synchronized (lock) {
            if (isAccepted)
                throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

            for (Integer id : bids.keySet()) {
                if (id == userBidId) {
                    bids.get(id).acceptBid();
                    winner = bids.get(id);
                } else {
                    bids.get(id).rejectBid();
                }
            }
            if (!bids.containsKey(userBidId) || winner == null) {
                
                throw new DevException("Trying to accept bid for non-existent ID.");
            }
            isAccepted = true;
            return winner;
        }
    }

    public boolean rejectBid(int userBidId) throws DevException ,UIException {
        synchronized (lock) {
            if (isAccepted)
                throw new UIException("The bid is already closed!", ErrorCodes.BID_FINISHED);
            if (!bids.containsKey(userBidId))
                throw new DevException("Trying to reject bid with non-existent ID.");
            bids.get(userBidId).rejectBid();
            bids.remove(userBidId);
            return true;
        }
    }

    public boolean isOpen() {
        synchronized (lock) {
            return !isAccepted;
        }
    }

    public boolean userIsWinner(int userId) {
        return winner!=null && winner.getUserId()==userId;
    }

    public SingleBid getWinner() {
        return winner;
    }

    public boolean bidIsWinner(int bidId2) {
        
        return getBid(bidId2).isAccepted();
    }

   // was infinite loop PLEASE CHANGE IT TO THIS
   public SingleBid getBid(int bidId) {
    return bids.get(bidId);
}
    public int getProductId() {
        return productId;
    }
}
