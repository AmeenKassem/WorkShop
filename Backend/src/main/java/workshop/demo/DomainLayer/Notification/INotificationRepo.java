package workshop.demo.DomainLayer.Notification;

// import java.util.List;
public interface INotificationRepo {

    public void sendImmediateMessage(String user, String message);

    public void sendDelayedMessageToUser(String username, String message);

    public String[] getDelayedMessages(String username);

}
