package workshop.demo.DomainLayer.Notification;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.stereotype.Component;

import workshop.demo.DTOs.MessageDTO;

public class DelayedNotificationDecorator {

    private BaseNotifier notifier;
    private Map<Integer, List<MessageDTO>> delayedMessages; 

    public DelayedNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
        delayedMessages = new ConcurrentHashMap<>();
    }

    public void sendDelayedMessageToUser(int senderId, int receiverId, String message, boolean isReceiverOnline) {
        
        MessageDTO msg = new MessageDTO(senderId, receiverId, message);
        if (isReceiverOnline) {
            notifier.sendMessageToUser(message, senderId, receiverId); // Send immediately if online
        } else if(delayedMessages.containsKey(receiverId)) {
            delayedMessages.get(receiverId).add(msg);
        } else {
            delayedMessages.put(receiverId, List.of(msg));
        }
    }

    public MessageDTO[] getDelayedMessages(int userId) {

        if(!delayedMessages.containsKey(userId)) {
            return null; // No delayed messages for this user
        }else{
            return delayedMessages.remove(userId).toArray(new MessageDTO[0]); // Return and remove the delayed messages for this user
        }
    }

}
