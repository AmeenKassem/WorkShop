package workshop.demo.DomainLayer.Stock;

import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.Status;

@Entity
public class SingleBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int amount;
    private double price;
    private SpecialType type;
    private int specialId;
    private Status status;
    private int storeId;
    private int userId;
    private int productId;
    public int ownersNum;// number of owners at the time i added my bid
    private int acceptCounter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    private BID bid;

    public SingleBid(int productId, int amount, int userId, double price, SpecialType type, int storeId, int specialId) {
        this.productId = productId;
        this.amount = amount;
        this.userId = userId;
        this.price = price;
        this.type = type;
        this.storeId = storeId;
        this.id = id;
        this.specialId = specialId;
        this.acceptCounter = 0;
        if (type == SpecialType.Auction) {
            status = Status.AUCTION_PENDING;
        } else {
            status = Status.BID_PENDING;
        }
    }

    public SingleBid(){
        
    }

    public void setBid(BID bid) {
        this.bid = bid;
    }   

    public SingleBidDTO convertToDTO() {
        return new SingleBidDTO(
                this.id,
                this.productId,
                this.amount,
                this.price,
                this.type,
                this.specialId,
                this.status,
                this.storeId,
                this.userId,
                this.isWinner(),
                this.isAccepted(),
                this.isEnded()
        );
    }

    public double getBidPrice() {
        return this.price;
    }

    public void markAsWinner() {
        this.status = Status.AUCTION_WON;
    }

    public void markAsLosed() {
        this.status = Status.AUCTION_LOSED;
    }

    public int getId() {
        return this.id;
    }

    public void acceptBid() {
        acceptCounter++;
        if (acceptCounter == ownersNum) {
            this.status = Status.BID_ACCEPTED;
        }
    }

    public void rejectBid() {
        this.status = Status.BID_REJECTED;
    }

    public int getUserId() {
        return this.userId;
    }

    public boolean isWon() {
        return this.status == Status.AUCTION_WON || this.status == Status.BID_ACCEPTED;
    }

    // Optional: add more getters if needed
    public Status getStatus() {
        return this.status;
    }

    public int getAmount() {
        return this.amount;
    }

    public SpecialType getType() {
        return this.type;
    }

    public int getSpecialId() {
        return this.specialId;
    }

    public int getStoreId() {
        return this.storeId;
    }

    public boolean isAccepted() {
        return this.status == Status.BID_ACCEPTED;
    }

    public boolean isWinner() {
        return this.status == Status.AUCTION_WON || this.status == Status.BID_ACCEPTED;
    }

    public boolean isEnded() {
        return status != Status.AUCTION_PENDING && status != Status.BID_PENDING;

    }

    public int productId() {
        return productId;

    }
}
