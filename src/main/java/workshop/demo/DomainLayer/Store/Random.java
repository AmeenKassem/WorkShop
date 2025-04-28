package workshop.demo.DomainLayer.Store;

import java.util.HashMap;
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
    private CardForRandomDTO winnerId=null;

    private Object lock;

    private static AtomicInteger idGen=new AtomicInteger();
    


    public Random(int productId, int quantity, int numberOfCards, double priceForCard, int id,int storeId) {
        this.productId=productId;
        this.quantity = quantity;
        this.cardsLeft = numberOfCards;
        this.cardPrice = priceForCard;
        this.userCards = new HashMap<>();
        this.storeId = storeId;
        this.id=id;
    }   


    public CardForRandomDTO buyCard(int userId) throws Exception{
        synchronized(lock){
            if(cardsLeft<=0) throw new UIException("there is no cards left...");
            if(userCards.containsKey(userId)) userCards.put(userId, userCards.get(id).addCard());
            else userCards.put(userId, new CardForRandomDTO(productId,storeId,userId));
            return userCards.get(userId);
        }
    }



    public RandomDTO getDTO() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDTO'");
    }
    
}
