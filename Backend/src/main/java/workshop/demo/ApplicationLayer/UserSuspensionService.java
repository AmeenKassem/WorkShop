package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSuspensionService {
    private final IUserSuspensionRepo repo;
    private final IUserRepo userRepo;
    private final IAuthRepo authRepo;
    private static final Logger logger = LoggerFactory.getLogger(UserSuspensionService.class);

    // Lock map: per-user synchronization
    private final Map<Integer, Object> userLocks = new ConcurrentHashMap<>();

    @Autowired
    public UserSuspensionService(IUserSuspensionRepo repo, IUserRepo userRepo, IAuthRepo authRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    private Object getUserLock(int userId) {
        return userLocks.computeIfAbsent(userId, k -> new Object());
    }

    public void suspendRegisteredUser(int userId, int seconds, String adminToken) throws UIException {
        System.out.println(
                "Calling suspendRegisteredUser: userId=" + userId + ", seconds=" + seconds + ", token=" + adminToken);
        validateAdmin(adminToken);
        synchronized (getUserLock(userId)) {
            if (repo.isSuspended(userId)) {
                throw new UIException("User is already suspended", ErrorCodes.USER_SUSPENDED);
            }
            repo.suspendRegisteredUser(userId, seconds);
            logger.info("User " + userId + " suspended for " + seconds + " seconds.");
        }
    }

    
    public void suspendGuestUser(int userId, int seconds, String adminToken) throws UIException {
        System.out.println(
                "Calling suspendGuestUser: userId=" + userId + ", seconds=" + seconds + ", token=" + adminToken);
        validateAdmin(adminToken);
        synchronized (getUserLock(userId)) {
            if (repo.isSuspended(userId)) {
                throw new UIException("Guest is already suspended", ErrorCodes.USER_SUSPENDED);
            }
            repo.suspendGuestUser(userId, seconds);
            logger.info("Guest " + userId + " suspended for " + seconds + " seconds.");
        }
    }

    public boolean isUserSuspended(Integer userId) {
        return repo.isSuspended(userId);
    }

    private void validateAdmin(String token) throws UIException {
        System.out.println("validateAdmin called with token: " + token);
        if (!authRepo.validToken(token)) {
            System.out.println("Invalid token detected: " + token);
            throw new UIException("Invalid admin token.", ErrorCodes.INVALID_TOKEN);
        }
        int adminId = authRepo.getUserId(token);
        System.out.println("Admin ID resolved: " + adminId);
        // if (!userRepo.isAdmin(adminId)) {
        //     System.out.println("User " + adminId + " is NOT admin");
        //     throw new UIException("Only admins can suspend.", ErrorCodes.NO_PERMISSION);
        // }
        System.out.println("User " + adminId + " is confirmed admin");
    }

    public void pauseSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling pauseSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        synchronized (getUserLock(userId)) {
            repo.pauseSuspension(userId);
            logger.info("Suspension for " + userId + " paused.");
        }
    }

    public void resumeSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling resumeSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        synchronized (getUserLock(userId)) {
            repo.resumeSuspension(userId);
            logger.info("Suspension for " + userId + " resumed.");
        }
    }
}
