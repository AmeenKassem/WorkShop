package workshop.demo.DomainLayer.Store;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Random {

    private int productId;
    private int quantity; 
    private HashMap<Integer,ParticipationInRandomDTO> usersParticipations ;
    private double amountLeft;
    //private double cardPrice;
    private int id;
    private int storeId;
    private double productPrice;
    //private CardForRandomDTO winner=null;
    private ParticipationInRandomDTO winner=null;
    //private int totalCards;
    private Object lock = new Object();
    private Timer timer;
    private boolean isActive = true;
    private static AtomicInteger idGen=new AtomicInteger();
    
    


    public Random(int productId, int quantity, double productPrice, int id,int storeId, long RandomTime) {
        this.productId=productId;
        this.quantity = quantity;
        //this.amountLeft = numberOfCards;
        //this.cardPrice = priceForCard;
        this.amountLeft = productPrice;
        this.usersParticipations = new HashMap<>();
        this.storeId = storeId;
        //totalCards=numberOfCards;
        this.productPrice=productPrice;
        this.id=id;
        this.isActive = true;
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized(lock){
                if(!isActive) return; // If the random is already ended, do nothing
                isActive = false;
                // Notify the winner and other participants
                // here we give back the money for all participants that didn't win
                for (ParticipationInRandomDTO participation : usersParticipations.values()) {
                    if (participation != winner) {
                        // Refund the amount paid to the participant
                        // You can implement the refund logic here
                    }
                }
                }
                
            }
        }, RandomTime);

    }   

    // public int canBuyQuantity(int quantity){
    //     if(cardsLeft<=0) throw new UIException("there is no cards left...");
    // }

    public ParticipationInRandomDTO participateInRandom(int userId, double amountPaid) throws Exception{
        synchronized(lock){
            if(!isActive) throw new UIException("random is over...");
            if(amountPaid > amountLeft) throw new UIException("max amount can pay is: "+amountLeft);
            if(amountPaid <= 0) throw new UIException("amount paid should be positive...");
            if(!usersParticipations.containsKey(userId)) 
                usersParticipations.put(userId, new ParticipationInRandomDTO(productId,storeId,userId,id,amountPaid));
            else {
                throw new UIException("user already participated in this random...");
            }
            amountLeft -= amountPaid;
            if(amountLeft == 0) {
                endRandom(); // Mark the random as inactive
                timer.cancel(); // Cancel the timer if the random is over
            }
            return usersParticipations.get(userId);
        }
    }

    public ParticipationInRandomDTO endRandom(){
        //int[] cards = new int[totalCards];
        synchronized(lock){
            isActive = false;
            double rand = new java.util.Random().nextDouble() * productPrice;
            double cumulativeWeight = 0.0;
            for (ParticipationInRandomDTO participation : usersParticipations.values()) {
                cumulativeWeight += participation.amountPaid;
                if(rand <= cumulativeWeight) {
                    winner = participation;
                    break;
                }
            }
            // int winnerIndex = (int)(Math.random()*i);
            // int winnerUserId= cards[winnerIndex];
            //winner = usersParticipations.get(winnerUserId);
            winner.markAsWinner();
            for (ParticipationInRandomDTO card : usersParticipations.values()) {
                if(card.userId!=winner.getUserId()) card.markAsLoser();
            }
            return winner;
        }
    }
        



    public RandomDTO getDTO() {
        RandomDTO randomDTO = new RandomDTO();
        randomDTO.productId = productId;
        randomDTO.quantity = quantity;
        //randomDTO.totalCards =totalCards;
        randomDTO.productPrice = productPrice;
        randomDTO.amountLeft = amountLeft;
        randomDTO.id = id;
        randomDTO.storeId = storeId;
        //randomDTO.cardPrice=cardPrice;
        randomDTO.winner = winner;
        ParticipationInRandomDTO[] participations = new ParticipationInRandomDTO[usersParticipations.size()];
        int i=0;
        for (ParticipationInRandomDTO cardForRandomDTO : usersParticipations.values()) {
            participations[i]=cardForRandomDTO;
            i++;
        }
        randomDTO.participations=participations;
        return randomDTO;
    }

    // public double getPrice() {
    //     return cardPrice;
    // }

    public double getProductPrice() {
        return productPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getAmountLeft() {
        return amountLeft;
    }

    
    
}
