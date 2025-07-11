package workshop.demo.DomainLayer.Stock;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import workshop.demo.DTOs.ParticipationInRandomDTO;

@Entity
public class ParticipationInRandom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int userId;
    private int storeId;
    private int productId;
    private String userName;
    // public int numberOfCards;
    private double amountPaid;
    private boolean isWinner;
    private boolean ended;
    //private int randomId;
    private boolean mustRefund;
    private int transactionIdForPayment = 0;

    @ManyToOne
    @JoinColumn(name = "random_id")
    private Random random;

    public ParticipationInRandom(int productId, int storeId, int userId, int randomId, double amountPaid, String userName) {
        this.amountPaid = amountPaid;
        this.userName = userName;
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
        //this.randomId = randomId;
        this.isWinner = false;
    }

    public ParticipationInRandomDTO toDTO() {
        ParticipationInRandomDTO dto = new ParticipationInRandomDTO();
        dto.userId = this.userId;
        dto.storeId = this.storeId;
        dto.productId = this.productId;
        dto.amountPaid = this.amountPaid;
        dto.isWinner = this.isWinner;
        dto.ended = this.ended;
        dto.randomId = this.random != null ? this.random.getRandomId() : -1;
        dto.mustRefund = this.mustRefund;
        dto.transactionIdForPayment = this.transactionIdForPayment;
        dto.userName = this.userName;
        return dto;
    }

    public ParticipationInRandom() {
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public int getUserId() {
        return userId;
    }

    public void markAsWinner() {
        isWinner = true;
        ended = true;
    }

    public void markAsLoser() {
        isWinner = false;
        ended = true;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getProductId() {
        return productId;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public boolean isEnded() {
        return ended;
    }

    public int getRandomId() {
        return random != null ? random.getRandomId() : -1;
    }

    public boolean isMustRefund() {
        return mustRefund;
    }

    public int getTransactionIdForPayment() {
        return transactionIdForPayment;
    }

    public void setMustRefund(boolean b) {
        mustRefund = b;
    }

    public void setAmountPaid(double d) {
        if (d < 0) {
            throw new IllegalArgumentException("Amount paid cannot be negative");
        }
        this.amountPaid = d;
    }

    public String getUserName() {
        return userName;
    }
}
