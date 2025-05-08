package workshop.demo.DomainLayer.Notification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.MessageDTO;

public class DelayedNotificationDecorator {

    private static final Logger logger = LoggerFactory.getLogger(DelayedNotificationDecorator.class);

    private BaseNotifier notifier;
    private Map<Integer, List<MessageDTO>> delayedMessages;

    public DelayedNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
        delayedMessages = new ConcurrentHashMap<>();
    }

    public void sendDelayedMessageToUser(int senderId, int receiverId, String message, boolean isReceiverOnline) {
        logger.debug("sendDelayedMessageToUser called: senderId={}, receiverId={}, isReceiverOnline={}",
                senderId, receiverId, isReceiverOnline);

        MessageDTO msg = new MessageDTO(senderId, receiverId, message);
        if (isReceiverOnline) {
            logger.debug("Receiver {} is online, sending message immediately", receiverId);

            notifier.sendMessageToUser(message, senderId, receiverId); // Send immediately if online
        } else if (delayedMessages.containsKey(receiverId)) {
            logger.debug("Receiver {} is offline, appending message to existing list", receiverId);

            delayedMessages.get(receiverId).add(msg);
        } else {
            logger.debug("Receiver {} is offline, creating new message list", receiverId);

            delayedMessages.put(receiverId, List.of(msg));
        }
    }

    public MessageDTO[] getDelayedMessages(int userId) {
        logger.debug("getDelayedMessages called for userId={}", userId);

        if (!delayedMessages.containsKey(userId)) {
            logger.debug("No delayed messages for userId={}", userId);

            return null; // No delayed messages for this user
        } else {
            logger.debug("Returning and removing {} messages for userId={}",
                    delayedMessages.get(userId).size(), userId);
            return delayedMessages.remove(userId).toArray(new MessageDTO[0]); // Return and remove the delayed messages
                                                                              // for this user
        }
    }

}
