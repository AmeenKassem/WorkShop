package workshop.demo.DomainLayer.Notification;

import workshop.demo.SocketCommunication.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class BaseNotifier {
    public static final Logger LOGGER = Logger.getLogger(BaseNotifier.class.getName());


    @Autowired
    private SocketHandler socketHandler ;

    public void send(String user, String message) {
        try {
            LOGGER.info("Sending message to " + user + "- " + message);
            socketHandler.sendMessage(user, message);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUserOnline(String user) {
        return socketHandler.hasUserSession(user);
    }
}