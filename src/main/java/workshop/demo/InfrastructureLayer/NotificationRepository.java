package workshop.demo.InfrastructureLayer;


import workshop.demo.DTOs.MessageDTO;
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
    public void sendRTMessageToUser(String message, int senderId, int receiverId, boolean isReceiverOnline) {
        realTimeNotificationDecorator.sendRTMessageToUser(senderId, receiverId, message, isReceiverOnline);
    }

    @Override
    public void sendDMessageToUser(int senderId, int receiverId, String message, boolean isReceiverOnline) {
        delayedNotificationDecorator.sendDelayedMessageToUser(senderId, receiverId, message, isReceiverOnline);
    }

    @Override
    public MessageDTO[] getDelayedMessages(int userId) {
        return delayedNotificationDecorator.getDelayedMessages(userId);
    }
    
}
