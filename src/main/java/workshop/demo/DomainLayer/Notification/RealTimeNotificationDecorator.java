package workshop.demo.DomainLayer.Notification;
import java.util.List;

//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;


public class RealTimeNotificationDecorator extends BaseNotifier {

    private BaseNotifier notifier;

    public RealTimeNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
    }

    public void sendRTMessageToUser(int senderId ,int receiverId, String message, boolean isReceiverOnline) {
        if (isReceiverOnline) {
            notifier.sendMessageToUser(message, senderId, receiverId); // Send immediately if online
        } else {
            // Handle the case when the receiver is offline (e.g., store the message for later delivery)
            // This part can be implemented based on your requirements
        }
    }
}
