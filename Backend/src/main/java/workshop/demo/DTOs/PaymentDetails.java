package workshop.demo.DTOs;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    public PaymentDetails() {
    }

    // Returns a dummy payment for testing purposes
    public static PaymentDetails testPayment() {
        return new PaymentDetails("4111111111111111", "Test User", "12/30", "123");
    }
    //added this for tests

    public static PaymentDetails test_fail_Payment() throws Exception {
        throw new Exception("Payment failed");
    }
     

    public static PaymentDetails getPaymentDetailsFromJSON(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, PaymentDetails.class);
    }
}
