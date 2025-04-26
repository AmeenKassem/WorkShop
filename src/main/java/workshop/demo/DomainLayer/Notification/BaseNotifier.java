package workshop.demo.DomainLayer.Notification;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public class BaseNotifier implements INotificationRepo{

    public BaseNotifier() {
        // Constructor logic if needed
    }

    @Override
    public void sendMessageToUser(String message, int senderId, int receiverId) {
        // Default implementation for sending notification
        System.out.println("Sending notification: " + message);
    }

    @Override
    public void sendMessageToAll(List<Integer> receiversIds, String message, int senderId) {
        // Default implementation for sending notification to all users
        for (Integer userId : receiversIds) {
            sendMessageToUser(message, senderId, userId);
        }
    }
}
