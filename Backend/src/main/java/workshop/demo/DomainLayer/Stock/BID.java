package workshop.demo.DomainLayer.Stock;

import java.util.HashMap;
import java.util.List;
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
import jakarta.persistence.MapKey;
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
    @MapKey(name = "userId")
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

    // @Transient
    // Object lock = new Object();

    @Transient
    @Autowired
    private IActivePurchasesRepo activePurchasesRepository;

    public BID(int productId, int quantity, int storeId) {
        this.productId = productId;
        this.quantity = quantity;
        this.isAccepted = false;
        this.bids = new HashMap<>();
        this.storeId = storeId;
    }

    public BID() {
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

        if (isAccepted)
            throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

        SingleBid bid = new SingleBid(productId, quantity, userId, price, SpecialType.BID, storeId, bidId);
        bid.setBid(this);
        bids.put(userId, bid);
        return bid;

    }

    public SingleBid acceptBid(int userToAcceptForId, List<Integer> ownersIds, int userId)
            throws DevException, UIException {
                
        SingleBid curr = null;
        if (isAccepted)
            throw new UIException("This bid is already closed!", ErrorCodes.BID_FINISHED);

        if (bids.containsKey(userToAcceptForId)) {
            curr = bids.get(userToAcceptForId);
            // Accept the bid
            curr.acceptBid(ownersIds, userId);
            if (curr.isWinner()) {
                isAccepted = true;
                winner = curr;
                for (SingleBid bid : bids.values()) {
                    if (bid.getId() != curr.getId()) {
                        bid.markAsBIDLosed();
                    }
                }
            }

        } else {

            throw new DevException("Trying to accept bid for non-existent ID.");
        }

        return curr;

    }

    public boolean rejectBid(int userToRejectForId) throws DevException, UIException {

        if (isAccepted)
            throw new UIException("The bid is already closed!", ErrorCodes.BID_FINISHED);
        if (!bids.containsKey(userToRejectForId))
            throw new DevException("Trying to reject bid with non-existent ID.");
        bids.get(userToRejectForId).rejectBid();
        //bids.remove(userBidId);
        return true;

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

    public int getBidId() {
        return bidId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public List<Integer> getLosersIdsIfAccepted() {
        if (isAccepted) {
            return bids.values().stream()
                    .filter(bid -> !bid.isAccepted())
                    .map(SingleBid::getUserId)
                    .toList();
        }
        return List.of();
    }

    public void setActivePurchases(ActivePurcheses activePurcheses2) {
        if (activePurcheses2 == null) {
            throw new IllegalArgumentException("ActivePurchases cannot be null");
        }
        this.activePurcheses = activePurcheses2;
    }

    public int getQuantity() {
        return quantity;
    }
}
