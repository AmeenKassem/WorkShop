package workshop.demo.ApplicationLayer;

import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Purchase.IPaymentService;

public class PaymentServiceImp implements IPaymentService {
    public boolean processPayment(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            throw new UIException("Invalid payment details.", ErrorCodes.PAYMENT_ERROR);
        }
        if (totalPrice <= 0) {
            throw new UIException("Invalid payment amount.", ErrorCodes.PAYMENT_ERROR);
        }
        return true;
    }

    public boolean processRefund(PaymentDetails paymentDetails, double totalPrice) throws UIException {
        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            throw new UIException("Invalid payment details.", ErrorCodes.PAYMENT_ERROR);
        }
        if (totalPrice <= 0) {
            throw new UIException("Invalid refund amount.", ErrorCodes.PAYMENT_ERROR);
        }
        return true;
    }
}
