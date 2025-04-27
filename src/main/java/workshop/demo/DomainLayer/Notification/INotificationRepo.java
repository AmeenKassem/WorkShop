package workshop.demo.DomainLayer.Notification;

import java.util.List;

import workshop.demo.DTOs.MessageDTO;

//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

public interface INotificationRepo {

    public void sendRTMessageToUser(String message, int senderId, int receiverId, boolean isReceiverOnline);
    public void sendDMessageToUser(int senderId, int receiverId, String message, boolean isReceiverOnline);
    //public void sendRTMessageToAll(List<Integer> receiversIds,String message, int senderId, boolean isReceiverOnline);
    //public void sendDMessageToAll(List<Integer> receiversIds,String message, int senderId, boolean isReceiverOnline);
    public MessageDTO[] getDelayedMessages(int userId);

}
