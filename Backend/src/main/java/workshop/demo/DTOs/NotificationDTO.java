package workshop.demo.DTOs;

public class NotificationDTO {

    public enum NotificationType {
        NORMAL,
        OFFER
    }

    private String message;
    private String receiverName;
    private NotificationType type;

    public NotificationDTO(String message, String receiverName, NotificationType type) {
        this.message = message;
        this.receiverName = receiverName;
        this.type = type;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public NotificationType getType() {
        return type;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
