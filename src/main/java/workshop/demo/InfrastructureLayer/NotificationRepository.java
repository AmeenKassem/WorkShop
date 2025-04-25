package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import workshop.demo.DomainLayer.Notification.INotificationRepo;

public class NotificationRepository implements INotificationRepo {

    @Override
    public void sendMessageToUser(User u, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToUser'");
    }

    @Override
    public void sendMessageToAll(List<User> users, String message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToAll'");
    }

    
}
