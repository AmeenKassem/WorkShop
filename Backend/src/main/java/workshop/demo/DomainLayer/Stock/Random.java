package workshop.demo.DomainLayer.Stock;

import java.util.HashMap;
// import java.util.Timer;
// import java.util.TimerTask;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import workshop.demo.ApplicationLayer.LockManager;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Random {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int randomId;

    @OneToMany(mappedBy = "random", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @MapKey(name = "userId")
    private Map<Integer, ParticipationInRandom> usersParticipations;

    private int productId;
    private int quantity;
    private double amountLeft;
    private int storeId;
    private double productPrice;
    private boolean isActive = true;
    private boolean canceled;
    private long endTimeMillis;

    @Transient
    private ParticipationInRandom winner;

    @ManyToOne
    @JoinColumn(name = "active_store_id")
    private ActivePurcheses activePurcheses;

    public Random(int productId, int quantity, double productPrice, int storeId, long randomTime) {
        this.productId = productId;
        this.quantity = quantity;
        this.amountLeft = productPrice;
        this.usersParticipations = new HashMap<>();
        this.storeId = storeId;
        this.productPrice = productPrice;
        this.isActive = true;
        // this.timer = new Timer();
        this.endTimeMillis = System.currentTimeMillis() + randomTime;

        this.canceled = false;

        // timer.schedule(new TimerTask() {
        // @Override
        // public void run() {
        // synchronized (lock) {
        // if (!isActive)
        // return;
        // isActive = false;
        // if (amountLeft > 0) {
        // for (ParticipationInRandomDTO participation : usersParticipations.values()) {
        // if (participation != winner) {
        // participation.mustRefund = true;
        // }
        // }
        // }
        // }
        // }
        // }, randomTime);
    }

    public Random() {

    }

    public ParticipationInRandom participateInRandom(int userId, double amountPaid) throws UIException {
        ParticipationInRandom card;
        if (!isActive)
            throw new UIException("Random event is over.", ErrorCodes.RANDOM_FINISHED);
        if (amountPaid > amountLeft)
            throw new UIException("Maximum amount you can pay is: " + amountLeft,
                    ErrorCodes.INVALID_RANDOM_PARAMETERS);
        if (amountPaid <= 0)
            throw new UIException("Amount paid must be positive.", ErrorCodes.INVALID_RANDOM_PARAMETERS);
        if (usersParticipations.containsKey(userId)) {
            card = usersParticipations.get(userId);
            card.setAmountPaid(card.getAmountPaid() + amountPaid);
        } else {
            card = new ParticipationInRandom(productId, storeId, userId, randomId, amountPaid);
        }
        card.setRandom(this);
        usersParticipations.put(userId, card);
        amountLeft -= amountPaid;

        if (amountLeft == 0) {
            endRandom();
            // timer.cancel();
        }
        return usersParticipations.get(userId);
    }

    public void setActivePurchases(ActivePurcheses active) {
        activePurcheses = active;
    }

    public boolean mustEnd() {
        return System.currentTimeMillis() < endTimeMillis;
    }

    public ParticipationInRandom endRandom() {

        isActive = false;
        double rand = new java.util.Random().nextDouble() * productPrice;
        double cumulativeWeight = 0.0;
        for (ParticipationInRandom participation : usersParticipations.values()) {
            cumulativeWeight += participation.getAmountPaid();
            if (rand <= cumulativeWeight) {
                winner = participation;
                break;
            }
        }
        if (winner != null) {
            winner.markAsWinner();
            for (ParticipationInRandom card : usersParticipations.values()) {
                if (card.getUserId() != winner.getUserId())
                    card.markAsLoser();
            }
        }
        return winner;
    }

    public RandomDTO getDTO() {
        RandomDTO randomDTO = new RandomDTO();
        randomDTO.productId = productId;
        randomDTO.quantity = quantity;
        randomDTO.productPrice = productPrice;
        randomDTO.amountLeft = amountLeft;
        randomDTO.id = randomId;
        randomDTO.storeId = storeId;
        randomDTO.winner = winner != null ? winner.toDTO() : null;
        randomDTO.endTimeMillis = getEndTimeMillis();

        ParticipationInRandomDTO[] participations = new ParticipationInRandomDTO[usersParticipations.size()];
        int i = 0;
        for (ParticipationInRandom card : usersParticipations.values()) {
            participations[i] = card.toDTO();
            i++;
        }
        randomDTO.participations = participations;
        return randomDTO;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getAmountLeft() {
        return amountLeft;
    }

    public boolean userIsWinner(int userId) {
        if (winner == null)
            return false;
        return winner.getUserId() == userId;
    }

    public ParticipationInRandom getWinner() {
        return winner;
    }

    public ParticipationInRandom getCard(int userId) {
        return usersParticipations.get(userId);
    }

    public int getProductId() {
        return productId;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCancel(boolean canceled) {
        this.canceled = canceled;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public long getRemainingTimeMillis() {
        long now = System.currentTimeMillis();
        return Math.max(0, endTimeMillis - now);
    }

    public long getRemainingSeconds() {
        return getRemainingTimeMillis() / 1000;
    }

    public int getRandomId() {
        return randomId;
    }

    public long getRestMS() {
        return endTimeMillis - System.currentTimeMillis();
    }

    public int getQuantity() {
        return quantity;
    }

    public List<Integer> getParticipationsUsersIds() {
        return usersParticipations.keySet().stream().toList();
    }

    public void mustRefundAllParticipations() {

        for (ParticipationInRandom participation : usersParticipations.values()) {
            participation.setMustRefund(true);

        }
    }

    public ParticipationInRandomDTO getRandomCardforuser(int userId) {
        ParticipationInRandom participation = usersParticipations.get(userId);
        if (participation != null) {
            return participation.toDTO();
        }
        return null;
    }

    public void setActive(boolean b) {
        this.isActive = b;
    }

    public Map<Integer, ParticipationInRandom> getUsersParticipations() {
        return usersParticipations;
    }

}
