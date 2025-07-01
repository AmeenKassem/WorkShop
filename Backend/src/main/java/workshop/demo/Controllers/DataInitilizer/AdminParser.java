package workshop.demo.Controllers.DataInitilizer;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AdminParser extends ManagerDataInit {
    protected void admin(List<String> construction) throws Exception {
        List<String> toSend = construction.subList(1, construction.size());
        switch (construction.get(0).toLowerCase()) {
            case "init":
                adminInit(toSend);
                break;
            case "suspend":
                suspendUser(toSend);
                break;
            case "pause":
                pauseSuspension(toSend);
                break;
            case "resume":
                resumeSuspension(toSend);
                break;
            case "cancel":
                cancelSuspension(toSend);
                break;
            default:
                log("syntax error on line " + line + " :Unkown (" + construction.get(0) + ") for admin");
                error = true;
                break;

        }
    }

    protected  void adminInit(List<String> construction) throws Exception {
        settings.markInitialized(construction.get(0), construction.get(1), construction.get(2));
        log("admin initilize the system successfully!");
    }

    private void suspendUser(List<String> toSend) {
        if (toSend.size() != 3) {
            log("syntax error on line " + line + " : admin suspend <username> <minutes>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        int minutes = Integer.parseInt(toSend.get(1));

        String adminToken = tokens.get(toSend.get(2));  // assumes admin is 'ana'
        if (adminToken == null) {
            log("admin token not found. Make sure admin is logged in before suspend.");
            error = true;
            return;
        }

        Integer userId = ids.get(username);
        if (userId == null) {
            log("cannot suspend: user " + username + " not found or not logged in.");
            error = true;
            return;
        }
        try {
            suspensionService.suspendRegisteredUser(userId, minutes, adminToken);
            log("admin suspended user " + username + " for " + minutes + " minutes");
        } catch (Exception e) {
            log("error suspending user " + username + " on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void pauseSuspension(List<String> toSend) {
        if (toSend.size() != 2) {
            log("syntax error on line " + line + " : admin pause <username>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        String adminToken = tokens.get(toSend.get(1));

        Integer userId = ids.get(username);
        if (userId == null || adminToken == null) {
            log("pause failed: invalid user/admin token.");
            error = true;
            return;
        }

        try {
            suspensionService.pauseSuspension(userId, adminToken);
            log("admin paused suspension for user " + username);
        } catch (Exception e) {
            log("error pausing user " + username + " on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void resumeSuspension(List<String> toSend) {
        if (toSend.size() != 2) {
            log("syntax error on line " + line + " : admin resume <username>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        String adminToken = tokens.get(toSend.get(1));

        Integer userId = ids.get(username);
        if (userId == null || adminToken == null) {
            log("resume failed: invalid user/admin token.");
            error = true;
            return;
        }

        try {
            suspensionService.resumeSuspension(userId, adminToken);
            log("admin resumed suspension for user " + username);
        } catch (Exception e) {
            log("error resuming user " + username + " on line " + line + ": " + e.getMessage());
            error = true;
        }
    }

    private void cancelSuspension(List<String> toSend) {
        if (toSend.size() != 2) {
            log("syntax error on line " + line + " : admin cancel <username>");
            error = true;
            return;
        }

        String username = toSend.get(0);
        String adminToken = tokens.get(toSend.get(1));

        Integer userId = ids.get(username);
        if (userId == null || adminToken == null) {
            log("cancel failed: invalid user/admin token.");
            error = true;
            return;
        }

        try {
            suspensionService.cancelSuspension(userId, adminToken);
            log("admin cancelled suspension for user " + username);
        } catch (Exception e) {
            log("error cancelling user " + username + " on line " + line + ": " + e.getMessage());
            error = true;
        }
    }


}
