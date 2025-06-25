package workshop.demo.DTOs;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentDetails {

    public String cardNumber;
    public String cardHolderName;
    public String expirationDate;
    public String cvv;
    public int id;

    public PaymentDetails(String cardNumber, String cardHolderName, String expirationDate, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
    }
    public PaymentDetails(String cardNumber, String cardHolderName, String expirationDate, String cvv,int id) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.id=id;
    }

    public PaymentDetails() {
    }

    // Returns a dummy payment for testing purposes
    public static PaymentDetails testPayment() {
        return new PaymentDetails("4111111111111111", "Test User", "12/30", "123");
    }
    //added this for tests

    public static PaymentDetails test_fail_Payment() throws Exception {
        return new PaymentDetails(null, "Test User", "12/30", null);
    }
     

    public static PaymentDetails getPaymentDetailsFromJSON(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, PaymentDetails.class);
    }
}
