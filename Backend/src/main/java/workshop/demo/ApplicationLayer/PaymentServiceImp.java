package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.IPaymentService;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
public class PaymentServiceImp implements IPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImp.class);

    @Override
    public int processPayment(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        logger.info("processPayment called with totalPrice={}", totalPrice);

        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            logger.error("Payment failed: card number or CVV is missing.");
            return -1;
        }

        if (totalPrice < 0) {
            logger.error("Payment failed: invalid amount {}", totalPrice);
            return -1;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action_type", "pay");
        params.add("amount", String.valueOf((int) totalPrice));
        params.add("currency", "USD");
        params.add("card_number", paymentDetails.cardNumber);

        try {
            String[] parts = paymentDetails.expirationDate.split("/");
            params.add("month", parts[0]);
            params.add("year", "20" + parts[1]);
        } catch (Exception e) {
            throw new UIException("Invalid expiration date format", ErrorCodes.PAYMENT_ERROR);
        }

        params.add("holder", paymentDetails.cardHolderName);
        params.add("cvv", paymentDetails.cvv);
        params.add("id", String.valueOf(paymentDetails.id));

        String result = postToExternalSystem(params);

        try {
            int transactionId = Integer.parseInt(result);
            if (transactionId >= 10000 && transactionId <= 100000) {
                logger.info("Payment successful. Transaction ID: {}", transactionId);
                return transactionId;
            } else {
                logger.warn("Payment failed. Received transaction ID: {}", transactionId);
                return -1;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid response from external payment system: {}", result);
            throw new UIException("External payment failed", ErrorCodes.PAYMENT_ERROR);
        }
    }


    public boolean processRefund(int transactionId) throws UIException {
        logger.info("processRefund called for transactionId={}", transactionId);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("action_type", "cancel_pay");
        params.add("transaction_id", String.valueOf(transactionId));

        String result = postToExternalSystem(params);

        if ("1".equals(result)) {
            logger.info("Refund (cancel_pay) successful for transactionId={}", transactionId);
            return true;
        } else {
            logger.error("Refund (cancel_pay) failed for transactionId={}", transactionId);
            throw new UIException("Refund failed", ErrorCodes.PAYMENT_ERROR);
        }
    }

    //
    // public int externalPayment(PaymentDetails paymentDetails, double totalPrice)
    // throws UIException {
    // return 1;
    // }
    //
    // public int externalRefund(int transactionId) throws UIException {
    // return 1;
    // }
    private String postToExternalSystem(MultiValueMap<String, String> params) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String url = "https://damp-lynna-wsep-1984852e.koyeb.app/";
        return restTemplate.postForObject(url, request, String.class);
    }
}
