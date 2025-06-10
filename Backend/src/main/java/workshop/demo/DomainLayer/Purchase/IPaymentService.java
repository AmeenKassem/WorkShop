package workshop.demo.DomainLayer.Purchase;
import workshop.demo.DTOs.PaymentDetails;

public interface IPaymentService {
    boolean processPayment(PaymentDetails paymentDetails, double totalPrice) throws Exception;
    boolean processRefund(PaymentDetails paymentDetails, double totalPrice) throws Exception;
    int externalPayment(PaymentDetails paymentDetails, double totalPrice) throws Exception;
    int externalRefund(int transactionId) throws Exception;
}
