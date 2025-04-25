package workshop.demo.DomainLayer.Notification;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public interface INotificationRepo {

    public void sendMessageToUser(User u, String message);
    public void sendMessageToAll(List<User> users,String message);
    
}
