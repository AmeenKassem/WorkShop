package workshop.demo.InfrastructureLayer;

import java.util.List;
import workshop.demo.DomainLayer.Notification.BaseNotifier;
import workshop.demo.DomainLayer.Notification.DelayedNotificationDecorator;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Notification.RealTimeNotificationDecorator;

public class NotificationRepository implements INotificationRepo {

    RealTimeNotificationDecorator realTimeNotificationDecorator;
    DelayedNotificationDecorator delayedNotificationDecorator;
    BaseNotifier baseNotifier;

    public NotificationRepository() {
        this.baseNotifier = new BaseNotifier();
        this.realTimeNotificationDecorator = new RealTimeNotificationDecorator(baseNotifier);
        this.delayedNotificationDecorator = new DelayedNotificationDecorator(baseNotifier);
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
