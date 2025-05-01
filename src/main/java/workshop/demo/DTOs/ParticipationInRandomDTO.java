package workshop.demo.DTOs;

public class ParticipationInRandomDTO {
   public int userId;
   public int storeId;
   public int productId;
   //public int numberOfCards;
   public double amountPaid;
   public boolean isWinner ;
   public boolean ended;
   public int randomId; 
   

   public ParticipationInRandomDTO(int productId, int storeId2, int userId2, int randomId, double amountPaid) {
    this.amountPaid=amountPaid;
    userId=userId2 ;
    this.storeId=storeId2;
    this.productId= productId;
   this.randomId=randomId;

   }


   // public CardForRandomDTO addCard() {
   //    numberOfCards++;
   //    return this;
   // }


   public void markAsWinner() {
         isWinner=true;
         ended=true;
   }


   public void markAsLoser() {
      isWinner=false;
       ended=true;
   } 

   public int getRandomId() {
      return randomId;
   }


   public boolean won() {
    return ended && isWinner;
   }

<<<<<<< HEAD:src/main/java/workshop/demo/DTOs/CardForRandomDTO.java
   public int getProductId() {
      return productId;
  }

  public int getStoreId() {
   return storeId;
}

=======
   public int getUserId() {
      return userId;
   }
>>>>>>> syncBhaa:src/main/java/workshop/demo/DTOs/ParticipationInRandomDTO.java
   
}
