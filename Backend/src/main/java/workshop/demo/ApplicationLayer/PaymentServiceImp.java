package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.IPaymentService;

@Service
public class PaymentServiceImp implements IPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImp.class);

    public boolean processPayment(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        logger.info("processPayment called with totalPrice={}", totalPrice);

        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            logger.error("Payment failed: card number or CVV is missing.");

            throw new UIException("Invalid payment details.", ErrorCodes.PAYMENT_ERROR);
        }
        // changed this beacuse random price is 0
        if (totalPrice < 0) {
            logger.error("Payment failed: invalid amount {}", totalPrice);

            throw new UIException("Invalid payment amount.", ErrorCodes.PAYMENT_ERROR);
        }
        logger.info("Payment processed successfully for card ending with {}");
        return true;
    }

    public boolean processRefund(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        logger.info("processRefund called with totalPrice={}", totalPrice);

        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            logger.error("Refund failed: card number or CVV is missing.");

            throw new UIException("Invalid payment details.", ErrorCodes.PAYMENT_ERROR);
        }
        if (totalPrice <= 0) {
            logger.error("Refund failed: invalid amount {}", totalPrice);

            throw new UIException("Invalid refund amount.", ErrorCodes.PAYMENT_ERROR);
        }
        logger.info("Refund processed successfully for card ending with {}");
        return true;
    }


    public int externalPayment(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        return 1;
    }

    public int externalRefund(int transactionId) throws UIException {
        return 1;
    }
}
