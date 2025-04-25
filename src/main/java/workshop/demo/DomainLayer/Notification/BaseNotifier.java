package workshop.demo.DomainLayer.Notification;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public class BaseNotifier implements INotificationRepo{

    public BaseNotifier() {
        // Constructor logic if needed
    }

    @Override
    public void sendMessageToUser(User user,String message) {
        // Default implementation for sending notification
        System.out.println("Sending notification: " + message);
    }

    @Override
    public void sendMessageToAll(List<User> users, String message) {
        // Default implementation for sending notification to all users
        for (User user : users) {
            sendMessageToUser(user, message);
        }
    }

}
