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
    // public int numberOfCards;
    private double amountPaid;
    private boolean isWinner;
    private boolean ended;
    private int randomId;
    private boolean mustRefund;
    private int transactionIdForPayment = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "random_id")
    private Random random;

    public ParticipationInRandom(int productId, int storeId, int userId, int randomId, double amountPaid) {
        this.amountPaid = amountPaid;
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
        this.randomId = randomId;
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
        dto.randomId = this.randomId;
        dto.mustRefund = this.mustRefund;
        dto.transactionIdForPayment = this.transactionIdForPayment;
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
        return randomId;
    }

    public boolean isMustRefund() {
        return mustRefund;
    }

    public int getTransactionIdForPayment() {
        return transactionIdForPayment;
    }
}
