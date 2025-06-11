package workshop.demo.InfrastructureLayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Notification.DelayedNotificationDecorator;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Notification.RealTimeNotificationDecorator;

@Repository
public class NotificationRepository implements INotificationRepo {

    private final RealTimeNotificationDecorator realTimeNotificationDecorator;
    private final DelayedNotificationDecorator delayedNotificationDecorator;

    @Autowired
    public NotificationRepository(
            RealTimeNotificationDecorator realTimeNotificationDecorator,
            DelayedNotificationDecorator delayedNotificationDecorator
    ) {
        this.realTimeNotificationDecorator = realTimeNotificationDecorator;
        this.delayedNotificationDecorator = delayedNotificationDecorator;
    }

    @Override
    public void sendImmediateMessage(String user, String message) {
        realTimeNotificationDecorator.sendRTMessageToUser(user, message);
    }

    @Override
    public void sendDelayedMessageToUser(String username, String message) {
        delayedNotificationDecorator.sendDelayedMessageToUser(username, message);
    }

    @Override
    public String[] getDelayedMessages(String username) {
        return delayedNotificationDecorator.getDelayedMessages(username);
    }
}
