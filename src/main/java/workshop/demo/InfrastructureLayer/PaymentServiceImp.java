package workshop.demo.InfrastructureLayer;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DomainLayer.Purchase.IPaymentService;

public class PaymentServiceImp implements IPaymentService{
    public boolean processPayment(PaymentDetails paymentDetails, double totalPrice) throws Exception {
        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            throw new Exception("invalid payment details.");
        }
        if (totalPrice <= 0) {
            throw new Exception("invalid payment amount.");
        }
        return true;
    }
    
    
    public boolean processRefund(PaymentDetails paymentDetails, double totalPrice) throws Exception {
        if (paymentDetails.cardNumber == null || paymentDetails.cvv == null) {
            throw new Exception("invalid payment details.");
        }
        if (totalPrice <= 0) {
            throw new Exception("invalid refund amount.");
        }
        return true;
    } 
}
