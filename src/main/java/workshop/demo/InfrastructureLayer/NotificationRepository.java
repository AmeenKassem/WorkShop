package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import workshop.demo.DomainLayer.Notification.INotificationRepo;

public class NotificationRepository implements INotificationRepo {

    @Override
    public void sendMessageToUser(String message, int senderId, int receiverId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToUser'");
    }

    @Override
    public void sendMessageToAll(List<Integer> receiversIds, String message, int senderId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToAll'");
    }

    @Override

    
}
