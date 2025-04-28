package workshop.demo.DTOs;

public class CardForRandomDTO {
   public int userId;
   public int storeId;
   public int productId;
   public int numberOfCards;
   public boolean isWinner ;
   public boolean ended;
   

   public CardForRandomDTO(int productId2, int storeId2, int userId2) {
    userId=userId2 ;
    this.storeId=storeId2;
    this.productId= productId2;
}


   public CardForRandomDTO addCard() {
      numberOfCards++;
      return this;
   }


   public void markAsWinner() {
         isWinner=true;
         ended=true;
   }


   public void markAsLoser() {
      isWinner=false;
       ended=true;
   } 
   
}
