package workshop.demo.DomainLayer.Notification;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;


public class DelayedNotificationDecorator {

    private BaseNotifier notifier;
    private Map<Integer, List<String>> delayedMessages = new ConcurrentHashMap<>();

    public DelayedNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;

    }

    public void sendMessageToUser(User u, String message) {
        
        // if(user.isOnline()){
        //     try {
        //         // Logic to send real-time notification to the user
        //         notifier.sendMessageToUser(u, message);
        //     } catch (Exception e) {
        //         // Handle any exceptions that may occur during notification sending
        //         System.err.println("Error sending real-time notification: " + e.getMessage());
        //     }
        // } else {
        //     // Store the message for delayed delivery
        //     delayedMessages.computeIfAbsent(u.getId(), k -> new ArrayList<>()).add(message);
        //     System.out.println("User is offline. Message stored for later delivery: " + message);
        // }
    }

    public void sendMessageToAll(List<User> users, String message) {
        // for (User user : users) {
        //     sendMessageToUser(user, message);
        // }
    }

    
}
