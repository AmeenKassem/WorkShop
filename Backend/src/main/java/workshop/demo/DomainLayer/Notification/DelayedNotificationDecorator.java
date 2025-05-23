package workshop.demo.DomainLayer.Notification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DelayedNotificationDecorator {

    private static final Logger logger = LoggerFactory.getLogger(DelayedNotificationDecorator.class);

    private BaseNotifier notifier;
    private Map<String, List<String>> delayedMessages;

    public DelayedNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
        delayedMessages = new ConcurrentHashMap<>();
    }

    public void sendDelayedMessageToUser(String username, String message) {
        if (notifier.isUserOnline(username)) {
            notifier.send(username, message); // Send immediately if online
        } else if (delayedMessages.containsKey(username)) {
            delayedMessages.get(username).add(message);
        } else {
            delayedMessages.put(username, List.of(message));
        }
    }

    public String[] getDelayedMessages(String username) {

        if (!delayedMessages.containsKey(username)) {
            return null; // No delayed messages for this user
        } else {
            return delayedMessages.remove(username).toArray(new String[0]); // Return and remove the delayed messages
                                                                            // for this user
        }

    }

}
