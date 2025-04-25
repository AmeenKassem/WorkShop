package workshop.demo.DomainLayer.Notification;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;


public class RealTimeNotificationDecorator {

    private BaseNotifier notifier;

    public RealTimeNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
    }
    
    public void sendMessageToUser(User u, String message) {
        try {
            // Logic to send real-time notification to a user
            notifier.sendMessageToUser(u, message);
        } catch (Exception e) {
            // Handle any exceptions that may occur during notification sending
            System.err.println("Error sending real-time notification: " + e.getMessage());
        }
    }
}
