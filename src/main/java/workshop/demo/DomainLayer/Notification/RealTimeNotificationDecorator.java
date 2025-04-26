package workshop.demo.DomainLayer.Notification;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;


public class RealTimeNotificationDecorator extends BaseNotifier {

    private BaseNotifier notifier;

    public RealTimeNotificationDecorator(BaseNotifier notifier) {
        this.notifier = notifier;
    }

    public
}
