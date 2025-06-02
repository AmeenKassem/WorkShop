package workshop.demo.DTOs;

public class ParticipationInRandomDTO {

    public int userId;
    public int storeId;
    public int productId;
    //public int numberOfCards;
    public double amountPaid;
    public boolean isWinner;
    public boolean ended;
    public int randomId;

    public ParticipationInRandomDTO(int productId, int storeId, int userId, int randomId, double amountPaid) {
        this.amountPaid = amountPaid;
        this.userId = userId;
        this.storeId = storeId;
        this.productId = productId;
        this.randomId = randomId;

    }

    public ParticipationInRandomDTO() {
    }

    public void markAsWinner() {
        isWinner = true;
        ended = true;
    }

    public void markAsLoser() {
        isWinner = false;
        ended = true;
    }

    public int getRandomId() {
        return randomId;
    }

    public boolean won() {
        return ended && isWinner;
    }

    public int getProductId() {
        return productId;
    }

    public int getStoreId() {
        return storeId;
    }

    public int getUserId() {
        return userId;
    }

}
