package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.NotificationService;

@RestController
@RequestMapping("/api/test")
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@RequestParam String username, @RequestParam String message) {
        System.out.println("request received");
        try{ 
            
            notificationService.sendDMessageToUser(username, message);
            return ResponseEntity.ok("Notification sent to " + username);

        } catch (Exception e) {
            System.out.println("Error sending notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not connected.");

        }
    }
}
