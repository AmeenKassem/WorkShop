package workshop.demo.DomainLayer.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class RealTimeNotificationDecorator extends BaseNotifier {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeNotificationDecorator.class);

    private BaseNotifier notifier;

    public RealTimeNotificationDecorator(BaseNotifier notifier) {

        this.notifier = notifier;
        logger.debug("RealTimeNotificationDecorator initialized");

    }

    public void sendRTMessageToUser(int senderId, int receiverId, String message, boolean isReceiverOnline) {
        logger.debug("sendRTMessageToUser called: senderId={}, receiverId={}, isReceiverOnline={}",
                senderId, receiverId, isReceiverOnline);

        if (isReceiverOnline) {
            logger.debug("Receiver {} is online. Sending real-time message.", receiverId);

            notifier.sendMessageToUser(message, senderId, receiverId); // Send immediately if online
        } else {
            logger.debug("Receiver {} is offline. Message not sent in real-time.", receiverId);

            // Handle the case when the receiver is offline (e.g., store the message for
            // later delivery)
            // This part can be implemented based on your requirements
        }
    }
}
