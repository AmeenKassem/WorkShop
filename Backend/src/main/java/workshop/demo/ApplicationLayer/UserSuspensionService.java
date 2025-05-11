package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

public class UserSuspensionService {
    private final IUserSuspensionRepo repo;
    private final IUserRepo userRepo;
    private final IAuthRepo authRepo;
    private static final Logger logger = LoggerFactory.getLogger(UserSuspensionService.class);

    public UserSuspensionService(IUserSuspensionRepo repo, IUserRepo userRepo, IAuthRepo authRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public void suspendRegisteredUser(int userId, int minutes, String adminToken) throws UIException {
        System.out.println("Calling suspendRegisteredUser: userId=" + userId + ", minutes=" + minutes + ", token=" + adminToken);
        validateAdmin(adminToken);
        repo.suspendRegisteredUser(userId, minutes);
        logger.info("User " + userId + " suspended for " + minutes + " minutes.");
    }

    public void suspendGuestUser(int userId, int minutes, String adminToken) throws UIException {
        System.out.println("Calling suspendGuestUser: userId=" + userId + ", minutes=" + minutes + ", token=" + adminToken);
        validateAdmin(adminToken);
        repo.suspendGuestUser(userId, minutes);
        logger.info("Guest " + userId + " suspended for " + minutes + " minutes.");
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
        if (!userRepo.isAdmin(adminId)) {
            System.out.println("User " + adminId + " is NOT admin");
            throw new UIException("Only admins can suspend.", ErrorCodes.NO_PERMISSION);
        }
        System.out.println("User " + adminId + " is confirmed admin");
    }

    public void pauseSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling pauseSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        repo.pauseSuspension(userId);
        logger.info("Suspension for " + userId + " paused.");
    }

    public void resumeSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling resumeSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        repo.resumeSuspension(userId);
        logger.info("Suspension for " + userId + " resumed.");
    }
}
