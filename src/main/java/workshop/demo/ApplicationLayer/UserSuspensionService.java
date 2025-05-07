package workshop.demo.ApplicationLayer;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

public class UserSuspensionService {

    private final IUserSuspensionRepo repo;
    private final ScheduledExecutorService scheduler;
    private final IUserRepo userRepo;
    private final IAuthRepo authRepo;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    
    
    


    public UserSuspensionService(IUserSuspensionRepo repo, IUserRepo userRepo, IAuthRepo authRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        startScheduler();
    }

    public void suspendRegisteredUser(String username, int minutes, String adminToken) {
        validateAdmin(adminToken);

        if (repo.getSuspensionByUsername(username) != null) {
            logger.info("User " + username + " is already suspended."); // change to execption later
            return;
        }

        Duration duration = Duration.ofMinutes(minutes);
        UserSuspension suspension = new UserSuspension(null, username, duration);
        repo.save(suspension);
        logger.info("User " + username + " suspended for " + minutes + " minutes.");
    }

    public void suspendGuestUser(int guestId, int minutes, String adminToken) {
        validateAdmin(adminToken);

        if (repo.getSuspensionByUserId(guestId) != null) {
            logger.info("Guest " + guestId + " is already suspended."); // change later
            return;
        }

        Duration duration = Duration.ofMinutes(minutes);
        UserSuspension suspension = new UserSuspension(guestId, null, duration);
        repo.save(suspension);
        logger.info("Guest " + guestId + " suspended for " + minutes + " minutes.");
    }

    public void pauseUser(int userId, String username) {
        UserSuspension suspension = getSuspension(userId, username);
        if (suspension != null) {
            suspension.pause();
            logger.info("Suspension paused.");
        }
    }

    public void resumeUser(int userId, String username) {
        UserSuspension suspension = getSuspension(userId, username);
        if (suspension != null) {
            suspension.resume();
            logger.info("Suspension resumed.");
        }
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            for (UserSuspension s : repo.getAllSuspensions()) {
                if (s.isExpired()) {
                    repo.removeSuspension(s.getUserId(), s.getUsername());
                    logger.info("Suspension for " + (s.getUsername() != null ? s.getUsername() : s.getUserId()) + " has expired and was removed.");
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void validateAdmin(String token) {
        return;
    //    if (!authRepo.validToken(token)) throw new RuntimeException("Invalid admin token.");
      //  int adminId = authRepo.getUserId(token);
       // if (!userRepo.isAdmin(adminId)) throw new RuntimeException("Only admins can suspend.");
    }

    private UserSuspension getSuspension(Integer userId, String username) {
        if (username != null) {
            return repo.getSuspensionByUsername(username);
        } else {
            return repo.getSuspensionByUserId(userId);
        }
    }
    public boolean isUserSuspended(Integer userId, String username) {
        UserSuspension suspension = getSuspension(userId, username);
        return suspension != null && !suspension.isExpired();
    }
}
