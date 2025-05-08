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

    public void suspendRegisteredUser(String username, int minutes, String adminToken) throws UIException {
       // validateAdmin(adminToken);
        repo.suspendRegisteredUser(username, minutes);
        logger.info("User " + username + " suspended for " + minutes + " minutes.");
    }

    public void suspendGuestUser(int guestId, int minutes, String adminToken) throws UIException {
       // validateAdmin(adminToken);
        repo.suspendGuestUser(guestId, minutes);
        logger.info("Guest " + guestId + " suspended for " + minutes + " minutes.");
    }

    public boolean isUserSuspended(Integer userId, String username) {
        return repo.isSuspended(userId, username);
    }

    private void validateAdmin(String token) throws UIException {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid admin token.", ErrorCodes.INVALID_TOKEN);
        }
        int adminId = authRepo.getUserId(token);
        if (!userRepo.isAdmin(adminId)) {
            throw new UIException("Only admins can suspend.", ErrorCodes.NO_PERMISSION);
        }
    }
    public void pauseSuspension(Integer userId, String username, String adminToken) throws UIException {
       // validateAdmin(adminToken);
        repo.pauseSuspension(userId, username);
        logger.info("Suspension for " + (username != null ? username : userId) + " paused.");
    }
    
    public void resumeSuspension(Integer userId, String username, String adminToken) throws UIException {
     //   validateAdmin(adminToken);
        repo.resumeSuspension(userId, username);
        logger.info("Suspension for " + (username != null ? username : userId) + " resumed.");
    }
}
