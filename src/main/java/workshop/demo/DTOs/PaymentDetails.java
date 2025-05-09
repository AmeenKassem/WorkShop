package workshop.demo.DTOs;

public class PaymentDetails {
    public String cardNumber;
    public String cardHolderName;
    public String expirationDate;
    public String cvv;

    public PaymentDetails(String cardNumber, String cardHolderName, String expirationDate, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
    }

    // Returns a dummy payment for testing purposes
    public static PaymentDetails testPayment() {
        return new PaymentDetails("4111111111111111", "Test User", "12/30", "123");
    }
}
