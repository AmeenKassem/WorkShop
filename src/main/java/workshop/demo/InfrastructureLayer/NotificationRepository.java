package workshop.demo.InfrastructureLayer;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import workshop.demo.DomainLayer.Notification.INotificationRepo;

public class NotificationRepository implements INotificationRepo {

    @Override
    public void sendMessageToUser(User u) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToUser'");
    }

    
}
