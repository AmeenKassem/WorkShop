package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import workshop.demo.DomainLayer.Notification.BaseNotifier;
import workshop.demo.DomainLayer.Notification.DelayedNotification;
// import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.DelayedNotificationRepository;

@Service
public class NotificationService {

    // @Autowired
    // INotificationRepo notificationRepo;
    // IUserRepo userRepo;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // private static final Logger logger = LoggerFactory.getLogger(DelayedNotificationDecorator.class);
    @Autowired
    private BaseNotifier notifier;
    // private Map<String, List<String>> delayedMessages;
    @Autowired
    private DelayedNotificationRepository notificationRepo;

    public void sendDelayedMessageToUser(String username, String message) {

        System.out.println(notifier.isUserOnline(username));
        if (notifier.isUserOnline(username)) {
            notifier.send(username, message); // Send immediately if online

        } else {
            DelayedNotification noti = new DelayedNotification();
            noti.setMessage(message);
            noti.setUsername(username);
            notificationRepo.save(noti);
        }

    }

    // public void sendRTMessageToUser(String username, String message) throws UIException {
    //     // notificationRepo.sendImmediateMessage(username, message);
    // }
    // public void sendDMessageToUser(String username, String message) throws UIException {
    //     // notificationRepo.sendDelayedMessageToUser(username, message);
    // }
    // public void sendRTMessageToAll(List<Integer> receiversIds ,String message,
    // int senderId) throws UIException {
    // for (int receiverId : receiversIds) {
    // boolean isReceiverOnline = userRepo.isOnline(receiverId);
    // notificationRepo.sendRTMessageToUser(message, senderId, receiverId,
    // isReceiverOnline);
    // }
    // }
    // public void sendDMessageToAll(List<Integer> receiversIds ,String message, int
    // senderId) throws UIException {
    // for (int receiverId : receiversIds) {
    // boolean isReceiverOnline = userRepo.isOnline(receiverId);
    // notificationRepo.sendDMessageToUser(senderId, receiverId, message,
    // isReceiverOnline);
    // }
    // }
    @Transactional
    public void getDelayedMessages(String username) {
        // return notificationRepo.getDelayedMessages(username);
        List<DelayedNotification> msgs = notificationRepo.findByUsername(username);
        notificationRepo.deleteByUsername(username);
        for (int i = 0; i < msgs.size(); i++) {
            sendDelayedMessageToUser(username, msgs.get(i).getMessage());
        }
    }
}
