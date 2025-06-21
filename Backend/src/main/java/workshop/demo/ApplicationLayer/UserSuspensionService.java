package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.UserSuspensionDTO;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DataAccessLayer.UserSuspensionJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.Registered;
// import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import workshop.demo.DataAccessLayer.UserJpaRepository;

@Service
public class UserSuspensionService {
    private final UserJpaRepository userRepo;
    private final IAuthRepo authRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
    private static final Logger logger = LoggerFactory.getLogger(UserSuspensionService.class);
    private final ConcurrentHashMap<Integer, Object> userLocks = new ConcurrentHashMap<>();

    @Autowired
    public UserSuspensionService(UserSuspensionJpaRepository userSuspensionJpaRepository, UserJpaRepository userRepo,
            IAuthRepo authRepo) {
        this.suspensionJpaRepo = userSuspensionJpaRepository;
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public boolean isUserSuspended(int userId) {
        return suspensionJpaRepo.findById(userId)
                .map(suspension -> !suspension.isExpired() && !suspension.isPaused())
                .orElse(false);
    }

    public void suspendRegisteredUser(int userId, int minutes, String adminToken) throws UIException {
        System.out.println(
                "Calling suspendRegisteredUser: userId=" + userId + ", minutes=" + minutes + ", token=" + adminToken);
        validateAdmin(adminToken);

        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            if (isUserSuspended(userId)) {
                throw new UIException("User is already suspended", ErrorCodes.USER_SUSPENDED);
            }
            UserSuspension suspension = new UserSuspension(userId, minutes);
            suspensionJpaRepo.save(suspension);
            logger.info("User " + userId + " suspended for " + minutes + " minutes.");
        }
    }

    public void suspendGuestUser(int userId, int seconds, String adminToken) throws UIException {
        System.out.println(
                "Calling suspendGuestUser: userId=" + userId + ", seconds=" + seconds + ", token=" + adminToken);
        validateAdmin(adminToken);

        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            if (isUserSuspended(userId)) {
                throw new UIException("Guest is already suspended", ErrorCodes.USER_SUSPENDED);
            }
            long durationMinutes = Math.max(1, seconds / 60L); // Ensure at least one minute
            UserSuspension suspension = new UserSuspension(userId, durationMinutes);
            suspensionJpaRepo.save(suspension);
            logger.info("Guest " + userId + " suspended for " + durationMinutes + " minutes.");
        }
    }

    private void validateAdmin(String token) throws UIException {
        System.out.println("validateAdmin called with token: " + token);
        if (!authRepo.validToken(token)) {
            System.out.println("Invalid token detected: " + token);
            throw new UIException("Invalid admin token.", ErrorCodes.INVALID_TOKEN);
        }
        int adminId = authRepo.getUserId(token);
        System.out.println("Admin ID resolved: " + adminId);
        Registered user = userRepo.findById(adminId)
                .orElseThrow(() -> new UIException("user is not registered", ErrorCodes.USER_NOT_FOUND));
        if (!user.isAdmin()) {
            System.out.println("User " + adminId + " is NOT admin");
            throw new UIException("Only admins can suspend.", ErrorCodes.NO_PERMISSION);
        }
        System.out.println("User " + adminId + " is confirmed admin");
    }

    public void pauseSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling pauseSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        UserSuspension suspension = suspensionJpaRepo.findById(userId)
                .orElseThrow(() -> new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND));
        suspension.pause();
        suspensionJpaRepo.save(suspension);
        logger.info("Suspension for " + userId + " paused.");
    }

    public void resumeSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling resumeSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        UserSuspension suspension = suspensionJpaRepo.findById(userId)
                .orElseThrow(() -> new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND));
        suspension.resume();
        suspensionJpaRepo.save(suspension);
        logger.info("Suspension for " + userId + " resumed.");
    }

    public void cancelSuspension(Integer userId, String adminToken) throws UIException {
        System.out.println("Calling cancelSuspension: userId=" + userId + ", token=" + adminToken);
        validateAdmin(adminToken);
        UserSuspension suspension = suspensionJpaRepo.findById(userId)
                .orElseThrow(() -> new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND));
        suspensionJpaRepo.delete(suspension);
        logger.info("Suspension for " + userId + " cancelled.");
    }

    public List<UserSuspensionDTO> viewAllSuspensions(String adminToken) throws UIException {
        validateAdmin(adminToken);

        return suspensionJpaRepo.findAll()
                .stream()
                .map(s -> new UserSuspensionDTO(
                        s.getUserId(),
                        s.isPaused(),
                        s.getSuspensionEndTime(),
                        s.getRemainingWhenPaused()))
                .collect(Collectors.toList());
    }
}
