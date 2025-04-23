package workshop.demo.DomainLayer.Notification;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public interface INotificationRepo {

    public void sendMessageToUser(User u);
}
