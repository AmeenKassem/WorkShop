package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.CardForRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class Random {

    private int productId;
    private int quantity; 
    private HashMap<Integer,CardForRandomDTO> userCards ;
    private int cardsLeft;
    private double cardPrice;
    private int id;
    private int storeId;
    private CardForRandomDTO winner=null;
    private int totalCards;
    private Object lock;

    private static AtomicInteger idGen=new AtomicInteger();

    public Random(int productId, int quantity, int numberOfCards, double priceForCard, int id,int storeId) {
        this.productId=productId;
        this.quantity = quantity;
        this.cardsLeft = numberOfCards;
        this.cardPrice = priceForCard;
        this.userCards = new HashMap<>();
        this.storeId = storeId;
        totalCards=numberOfCards;
        this.id=id;
    }   

    // public int canBuyQuantity(int quantity){
    //     if(cardsLeft<=0) throw new UIException("there is no cards left...");
    // }

    public CardForRandomDTO buyCard(int userId) throws Exception{
        synchronized(lock){
            if(cardsLeft<=0) throw new UIException("there is no cards left...");
            if(!userCards.containsKey(userId)) 
                userCards.put(userId, new CardForRandomDTO(productId,storeId,userId,id));
            cardsLeft--;
            return userCards.get(userId);
        }
    }
    
    public CardForRandomDTO endRandom(){
        int[] cards = new int[totalCards];
        int i=0;
        synchronized(lock){
            for (CardForRandomDTO userCard : userCards.values()) {
                int cardsForUser = userCard.numberOfCards;
                for(int j=i;j<i+cardsForUser;j++){
                    cards[j]=userCard.userId;
                }
                i+=cardsForUser;
            }
            int winnerIndex = (int)(Math.random()*i);
            int winnerUserId= cards[winnerIndex];
            winner = userCards.get(winnerUserId);
            winner.markAsWinner();
            for (CardForRandomDTO card : userCards.values()) {
                if(card.userId!=winnerUserId) card.markAsLoser();
            }
            return winner;
        }
    }
        



    public RandomDTO getDTO() {
        RandomDTO randomDTO = new RandomDTO();
        randomDTO.productId = productId;
        randomDTO.quantity = quantity;
        randomDTO.totalCards =totalCards;
        randomDTO.cardsLeft = cardsLeft;
        randomDTO.id = id;
        randomDTO.storeId = storeId;
        randomDTO.cardPrice=cardPrice;
        randomDTO.winner = winner;
        CardForRandomDTO[] cards = new CardForRandomDTO[userCards.size()];
        int i=0;
        for (CardForRandomDTO cardForRandomDTO : userCards.values()) {
            cards[i]=cardForRandomDTO;
            i++;
        }
        randomDTO.cards=cards;
        return randomDTO;
    }

    public double getPrice() {
        return cardPrice;
    }
    
}
