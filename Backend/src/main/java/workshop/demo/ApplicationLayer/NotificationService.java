package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import workshop.demo.DomainLayer.Notification.BaseNotifier;
import workshop.demo.DomainLayer.Notification.DelayedNotification;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.DelayedNotificationRepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private BaseNotifier notifier;
    // private Map<String, List<String>> delayedMessages;
    @Autowired
    private DelayedNotificationRepository notificationRepo;
    @Autowired
    private UserJpaRepository userRepo;

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

    @Transactional
    public void getDelayedMessages(String username) {
        // return notificationRepo.getDelayedMessages(username);
        List<DelayedNotification> msgs = notificationRepo.findByUsername(username);
        notificationRepo.deleteByUsername(username);
        for (int i = 0; i < msgs.size(); i++) {
            sendDelayedMessageToUser(username, msgs.get(i).getMessage());
        }
    }

    public void sendMessageForUsers(String string, List<Integer> paricpationIds) {
        for (Integer id : paricpationIds) {
            Registered user = userRepo.findById(id).orElseThrow();
            sendDelayedMessageToUser(user.getUsername(), string);
        }
    }
}
