package workshop.demo.DomainLayer.Notification;
import java.util.List;
import java.util.logging.Logger;


//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;


public class RealTimeNotificationDecorator extends BaseNotifier {

    private BaseNotifier notifier;
    public static final Logger LOGGER = Logger.getLogger(BaseNotifier.class.getName());

    public RealTimeNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
    }

    public void sendRTMessageToUser(String user, String message) {
        if (notifier.isUserOnline(user)) {
            notifier.send(user, message); // Send immediately if online
        } else {
            // Handle the case when the receiver is offline (e.g., store the message for later delivery)
            // This part can be implemented based on your requirements
            LOGGER.warning("User " + user + " is offline. Message not sent: " + message);
        }
    }
}
