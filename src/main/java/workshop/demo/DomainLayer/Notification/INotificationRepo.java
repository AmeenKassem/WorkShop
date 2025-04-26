package workshop.demo.DomainLayer.Notification;

import java.util.List;

import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import workshop.demo.DTOs.MessageDTO;

public interface INotificationRepo {

    public void sendMessageToUser(String message, int senderId, int receiverId);
    public void sendMessageToAll(List<Integer> receiversIds,String message, int senderId);
    public List<MessageDTO> getDelayedMessages(int userId);

}
