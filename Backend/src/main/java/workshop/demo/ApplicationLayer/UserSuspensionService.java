package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.UserSuspensionDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.Registered;
// import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserSuspensionService {

    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;

    @Autowired
    private NotificationService notifier;

    private static final Logger logger = LoggerFactory.getLogger(UserSuspensionService.class);
    private final ConcurrentHashMap<Integer, Object> userLocks = new ConcurrentHashMap<>();

    public boolean isUserSuspended(int userId) {
        return suspensionJpaRepo.findById(userId)
                .map(suspension -> !suspension.isExpired() && !suspension.isPaused())
                .orElse(false);
    }

    public void suspendRegisteredUser(int userId, int minutes, String adminToken) throws UIException {
        System.out.println(
                "Calling suspendRegisteredUser: userId=" + userId + ", minutes=" + minutes + ", token=" + adminToken);
        validateAdmin(adminToken);
        Registered user = userRepo.findById(userId).orElse(null);

        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            if (isUserSuspended(userId)) {
                throw new UIException("User is already suspended", ErrorCodes.USER_SUSPENDED);
            }
            UserSuspension suspension = new UserSuspension(userId, minutes);
            suspensionJpaRepo.save(suspension);
            notifier.sendDelayedMessageToUser(user.getUsername(), "You have been suspended :(");
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

    public LocalDateTime getSuspensionEndTimeIfAny(String token) throws UIException {
        int userId = authRepo.getUserId(token);
        return suspensionJpaRepo.findById(userId)
                .filter(s -> !s.isExpired() && !s.isPaused())
                .map(UserSuspension::getSuspensionEndTime)
                .orElse(null);
    }

    public List<UserSuspensionDTO> viewAllSuspensions(String adminToken) throws UIException {
        validateAdmin(adminToken);

        return suspensionJpaRepo.findAll()
                .stream()
                .map(s -> {
                    String username = userRepo.findById(s.getUserId())
                            .map(Registered::getUsername)
                            .orElse("Unknown");
                    return new UserSuspensionDTO(
                            s.getUserId(),
                            username,
                            s.isPaused(),
                            s.getSuspensionEndTime(),
                            s.getRemainingWhenPaused()
                    );
                })
                .collect(Collectors.toList());
    }
}
