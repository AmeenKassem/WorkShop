package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
// import workshop.demo.DomainLayer.User.IUserRepo;

@Service
public class NotificationService {

    INotificationRepo notificationRepo;
    // IUserRepo userRepo;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    public NotificationService(INotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
        // this.userRepo = userRepo;
    }

    public void sendRTMessageToUser(String username, String message) throws UIException {
        notificationRepo.sendImmediateMessage(username, message);
    }

    public void sendDMessageToUser(String username, String message) throws UIException {
        notificationRepo.sendDelayedMessageToUser(username, message);
    }

    // public void sendRTMessageToAll(List<Integer> receiversIds ,String message, int senderId) throws UIException {
    //     for (int receiverId : receiversIds) {
    //         boolean isReceiverOnline = userRepo.isOnline(receiverId);
    //         notificationRepo.sendRTMessageToUser(message, senderId, receiverId, isReceiverOnline);
    //     }
    // }
    // public void sendDMessageToAll(List<Integer> receiversIds ,String message, int senderId) throws UIException {
    //     for (int receiverId : receiversIds) {
    //         boolean isReceiverOnline = userRepo.isOnline(receiverId);
    //         notificationRepo.sendDMessageToUser(senderId, receiverId, message, isReceiverOnline);
    //     }
    // }
    public String[] getDelayedMessages(String username) {
        return notificationRepo.getDelayedMessages(username);
    }

}
