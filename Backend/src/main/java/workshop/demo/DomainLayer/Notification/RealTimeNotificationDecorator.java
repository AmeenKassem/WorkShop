package workshop.demo.DomainLayer.Notification;
// import java.util.List;
//import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;



@Component
public class RealTimeNotificationDecorator {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeNotificationDecorator.class);

    private BaseNotifier notifier;

    @Autowired
    public RealTimeNotificationDecorator(BaseNotifier notifier) {

        this.notifier = notifier;
        logger.debug("RealTimeNotificationDecorator initialized");

    }

    public void sendRTMessageToUser(String user, String message) {
        if (notifier.isUserOnline(user)) {
            notifier.send(user, message); // Send immediately if online
        } else {
            logger.debug("Receiver "+user+" is offline. Message not sent in real-time.");
            System.out.println("user mesh hoooon");
            // Handle the case when the receiver is offline (e.g., store the message for
            // later delivery)
            // This part can be implemented based on your requirements
            //LOGGER.warning("User " + user + " is offline. Message not sent: " + message);
        }
    }
}
