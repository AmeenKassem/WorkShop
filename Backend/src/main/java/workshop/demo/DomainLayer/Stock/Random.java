package workshop.demo.DomainLayer.Stock;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.flow.component.template.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

@Entity
public class Random {


    private int productId;
    private int quantity;
    private HashMap<Integer, ParticipationInRandomDTO> usersParticipations;
    private double amountLeft;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int randomId;

    private int storeId;
    private double productPrice;

    private ParticipationInRandomDTO winner;
    private final Object lock = new Object();
    private Timer timer;
    private boolean isActive = true;
    private boolean canceled;
    private long endTimeMillis;

    @ManyToOne
    @JoinColumn(name = "active_store_id")
    private ActivePurcheses activePurcheses;


    public Random(int productId, int quantity, double productPrice, int id, int storeId, long randomTime) {
        this.productId = productId;
        this.quantity = quantity;
        this.amountLeft = productPrice;
        this.usersParticipations = new HashMap<>();
        this.storeId = storeId;
        this.productPrice = productPrice;
        this.randomId = id;
        this.isActive = true;
        this.timer = new Timer();
        this.endTimeMillis = System.currentTimeMillis() + randomTime;

        this.canceled =false;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (!isActive)
                        return;
                    isActive = false;
                    if (amountLeft > 0) {
                        for (ParticipationInRandomDTO participation : usersParticipations.values()) {
                            if (participation != winner) {
                               participation.mustRefund = true;
                            }
                        }
                    }
                }
            }
        }, randomTime);
    }

    public ParticipationInRandomDTO participateInRandom(int userId, double amountPaid) throws UIException {
        synchronized (lock) {
            if (!isActive)
                throw new UIException("Random event is over.", ErrorCodes.RANDOM_FINISHED);
            if (amountPaid > amountLeft)
                throw new UIException("Maximum amount you can pay is: " + amountLeft,
                        ErrorCodes.INVALID_RANDOM_PARAMETERS);
            if (amountPaid <= 0)
                throw new UIException("Amount paid must be positive.", ErrorCodes.INVALID_RANDOM_PARAMETERS);
            if (usersParticipations.containsKey(userId))
                throw new UIException("User has already participated in this random event.",
                        ErrorCodes.DUPLICATE_RANDOM_ENTRY);

            usersParticipations.put(userId, new ParticipationInRandomDTO(productId, storeId, userId, randomId, amountPaid));
            amountLeft -= amountPaid;

            if (amountLeft == 0) {
                endRandom();
                timer.cancel();
            }
            return usersParticipations.get(userId);
        }
    }

    public void setActivePurchases(ActivePurcheses active){
        activePurcheses=active;
    }

    public ParticipationInRandomDTO endRandom() {
        synchronized (lock) {
            isActive = false;
            double rand = new java.util.Random().nextDouble() * productPrice;
            double cumulativeWeight = 0.0;
            for (ParticipationInRandomDTO participation : usersParticipations.values()) {
                cumulativeWeight += participation.amountPaid;
                if (rand <= cumulativeWeight) {
                    winner = participation;
                    break;
                }
            }
            if (winner != null) {
                winner.markAsWinner();
                for (ParticipationInRandomDTO card : usersParticipations.values()) {
                    if (card.userId != winner.getUserId())
                        card.markAsLoser();
                }
            }
            return winner;
        }
    }

    public RandomDTO getDTO() {
        RandomDTO randomDTO = new RandomDTO();
        randomDTO.productId = productId;
        randomDTO.quantity = quantity;
        randomDTO.productPrice = productPrice;
        randomDTO.amountLeft = amountLeft;
        randomDTO.id = randomId;
        randomDTO.storeId = storeId;
        randomDTO.winner = winner;
        randomDTO.endTimeMillis = getEndTimeMillis();

        ParticipationInRandomDTO[] participations = new ParticipationInRandomDTO[usersParticipations.size()];
        int i = 0;
        for (ParticipationInRandomDTO card : usersParticipations.values()) {
            participations[i] = card;
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
        if(winner == null) return false;
        return winner.userId == userId;
    }

    public ParticipationInRandomDTO getWinner() {
        return winner;
    }

    public ParticipationInRandomDTO getCard(int userId) {
        return usersParticipations.get(userId);
    }

    public int getProductId() {
        return productId;
    }
    public boolean isCanceled(){
        return canceled;
    }
    public void setCancel(boolean canceled){
        this.canceled=canceled;
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

}
