package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class NotificationService {

    INotificationRepo notificationRepo;
    IUserRepo userRepo;

    public NotificationService(INotificationRepo notificationRepo, IUserRepo userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public void sendRTMessageToUser(String message, int senderId, int receiverId) {
        boolean isReceiverOnline = userRepo.isOnline(receiverId);
        notificationRepo.sendRTMessageToUser(message, senderId, receiverId, isReceiverOnline);
    }

    public void sendDMessageToUser(int senderId, int receiverId, String message) {
        boolean isReceiverOnline = userRepo.isOnline(receiverId);
        notificationRepo.sendDMessageToUser(senderId, receiverId, message, isReceiverOnline);
    }

    public void sendRTMessageToAll(List<Integer> receiversIds ,String message, int senderId) {
        for (int receiverId : receiversIds) {
            boolean isReceiverOnline = userRepo.isOnline(receiverId);
            notificationRepo.sendRTMessageToUser(message, senderId, receiverId, isReceiverOnline);
        }
    }

    public void sendDMessageToAll(List<Integer> receiversIds ,String message, int senderId) {
        for (int receiverId : receiversIds) {
            boolean isReceiverOnline = userRepo.isOnline(receiverId);
            notificationRepo.sendDMessageToUser(senderId, receiverId, message, isReceiverOnline);
        }
    }

    public MessageDTO[] getDelayedMessages(int userId) {
        return notificationRepo.getDelayedMessages(userId);
    }

}