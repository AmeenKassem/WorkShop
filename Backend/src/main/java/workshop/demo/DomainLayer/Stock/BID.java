package workshop.demo.DomainLayer.Stock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import workshop.demo.ApplicationLayer.LockManager;

// import org.hibernate.validator.internal.util.logging.Log_.logger;

import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class BID {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bidId;

    @OneToMany(mappedBy = "bid", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public Map<Integer, SingleBid> bids;

    private int productId;
    private int quantity;
    private boolean isAccepted;
    private int storeId;

    @Transient
    private SingleBid winner;
    
    @ManyToOne
    @JoinColumn(name = "active_store_id")
    private ActivePurcheses activePurcheses;
    
    @Autowired
    private LockManager lockManager;

    public BID(int productId, int quantity, int id, int storeId) {
        this.productId = productId;
        this.quantity = quantity;
        this.isAccepted = false;
        this.bidId = id;
        this.bids = new HashMap<>();
        this.storeId = storeId;
    }

    public void setActivePurcheses(ActivePurcheses activePurcheses) {
        this.activePurcheses = activePurcheses;
    }

    public BidDTO getDTO() {
        BidDTO bidDTO = new BidDTO();
        bidDTO.productId = productId;
        bidDTO.quantity = quantity;
        bidDTO.isAccepted = isAccepted;
        bidDTO.bidId = bidId;
        bidDTO.winner = winner;
        bidDTO.storeId = storeId;

        SingleBidDTO[] arrayBids = new SingleBidDTO[bids.size()];
        int i = 0;
        for (SingleBid bid : bids.values()) {
            arrayBids[i] = bid.convertToDTO();
            i++;
        }
        bidDTO.bids = arrayBids;
        return bidDTO;
    }

    public SingleBid bid(int userId, double price) throws UIException {
        synchronized (lockManager.getBidLock(bidId)) {
            if (isAccepted)
                throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

            SingleBid bid = new SingleBid(productId, quantity, userId, price, SpecialType.BID, storeId, bidId);
            bids.put(bid.getId(), bid);
            return bid;
        }
    }

    public SingleBid acceptBid(int userBidId) throws DevException, UIException {
        synchronized (lockManager.getBidLock(bidId)) {
            SingleBid curr = null;
            if (isAccepted)
                throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

            for (Integer id : bids.keySet()) {
                if (id == userBidId) {
                    bids.get(id).acceptBid();
                    curr = bids.get(id);
                    if (bids.get(id).isWinner()) {
                        winner = bids.get(id);
                        isAccepted = true;
                        return winner;
                    }
                } else {
                    bids.get(id).rejectBid();
                }
            }
            if (!bids.containsKey(userBidId) || winner == null) {

                throw new DevException("Trying to accept bid for non-existent ID.");
            }

            return curr;
        }
    }

    public boolean rejectBid(int userBidId) throws DevException, UIException {
        synchronized (lockManager.getBidLock(bidId)) {
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
        synchronized (lockManager.getBidLock(bidId)) {
            return !isAccepted;
        }
    }

    public boolean userIsWinner(int userId) {
        return winner != null && winner.getUserId() == userId;
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
