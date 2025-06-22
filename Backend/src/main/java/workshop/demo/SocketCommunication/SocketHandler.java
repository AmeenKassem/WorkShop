package workshop.demo.SocketCommunication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import workshop.demo.ApplicationLayer.NotificationService;

@Component
public class SocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, List<WebSocketSession>> sessions;
    private final ApplicationContext applicationContext;

    @Autowired
    public SocketHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.sessions = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
        String query = session.getUri().getQuery();
        String username = null;
        if (query != null && query.startsWith("username=")) {
            username = query.substring("username=".length());
        }

        if (username != null) {
            System.out.println("sending delayed notifications for user: " + username);
            sessions.computeIfAbsent(username, k -> new ArrayList<>()).add(session);
            applicationContext.getBean(NotificationService.class).getDelayedMessages(username);
            // if (messages != null) {
            // 	for (String notification : messages) {
            // 		session.sendMessage(new TextMessage(notification));
            // 	}
            // }
            // }else {
            // 	System.out.println("No username provided in the query.");
            // }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        String query = session.getUri().getQuery();
        String username = null;
        if (query != null && query.startsWith("username=")) {
            username = query.substring("username=".length());
        }

        if (username != null) {
            List<WebSocketSession> userSessions = sessions.get(username);
            if (userSessions != null) {
                userSessions.remove(session);
                if (userSessions.isEmpty()) {
                    sessions.remove(username);
                }
            }
        }
    }

    public void sendMessage(String username, String message) throws InterruptedException, IOException {
        List<WebSocketSession> userSessions = sessions.get(username);
        if (userSessions != null) {
            for (WebSocketSession session : userSessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    public boolean hasUserSession(String username) {
        System.err.println(sessions.get(username) != null);
        return sessions.get(username) != null;
    }
}
