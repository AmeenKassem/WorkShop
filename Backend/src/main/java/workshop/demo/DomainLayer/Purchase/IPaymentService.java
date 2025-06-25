package workshop.demo.DomainLayer.Purchase;
import workshop.demo.DTOs.PaymentDetails;

public interface IPaymentService {
    int processPayment(PaymentDetails paymentDetails, double totalPrice) throws Exception;
    boolean processRefund(int transactionId) throws Exception;
  //  int externalPayment(PaymentDetails paymentDetails, double totalPrice) throws Exception;
   // int externalRefund(int transactionId) throws Exception;
}
