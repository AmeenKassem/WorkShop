package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.MessageDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class NotificationService {

    INotificationRepo notificationRepo;
    IUserRepo userRepo;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(INotificationRepo notificationRepo, IUserRepo userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public void sendRTMessageToUser(String message, int senderId, int receiverId) throws UIException {
        logger.info("sendRTMessageToUser called with senderId={}, receiverId={}", senderId, receiverId);
        boolean isReceiverOnline = userRepo.isOnline(receiverId);
        logger.info("Receiver {} online status: {}", receiverId, isReceiverOnline);
       notificationRepo.sendRTMessageToUser(message, senderId, receiverId, isReceiverOnline);
    }

    public void sendDMessageToUser(int senderId, int receiverId, String message) throws UIException {
        logger.info("sendDMessageToUser called with senderId={}, receiverId={}", senderId, receiverId);
        boolean isReceiverOnline = userRepo.isOnline(receiverId);
        logger.info("Receiver {} online status: {}", receiverId, isReceiverOnline);
        notificationRepo.sendDMessageToUser(senderId, receiverId, message, isReceiverOnline);
    }

    public void sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId) throws UIException {
        logger.info("sendRTMessageToAll called by senderId={}, message={}", senderId, message);
        for (int receiverId : receiversIds) {
            boolean isReceiverOnline = userRepo.isOnline(receiverId);
            logger.info("Receiver {} online status: {}", receiverId, isReceiverOnline);
            notificationRepo.sendRTMessageToUser(message, senderId, receiverId, isReceiverOnline);
        }
    }

    public void sendDMessageToAll(List<Integer> receiversIds, String message, int senderId) throws UIException {
        logger.info("sendDMessageToAll called by senderId={}, message={}", senderId, message);
        for (int receiverId : receiversIds) {
            boolean isReceiverOnline = userRepo.isOnline(receiverId);
            logger.info("Receiver {} online status: {}", receiverId, isReceiverOnline);

            notificationRepo.sendDMessageToUser(senderId, receiverId, message, isReceiverOnline);
        }
    }

    public MessageDTO[] getDelayedMessages(int userId) {
        logger.info("getDelayedMessages called for userId={}", userId);
        return notificationRepo.getDelayedMessages(userId);
    }

}