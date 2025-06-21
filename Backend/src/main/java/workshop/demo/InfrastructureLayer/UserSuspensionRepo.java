package workshop.demo.InfrastructureLayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

@Repository
public class UserSuspensionRepo implements IUserSuspensionRepo {

    private final Map<Integer, UserSuspension> Suspensions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    @Autowired
    public UserSuspensionRepo() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startScheduler();
    }

    public void checkUserSuspensoin_ThrowExceptionIfSuspeneded(int userId) throws UIException {
        if (isSuspended(userId)) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
    }

    @Override
    public void suspendRegisteredUser(Integer userId, int seconds) throws UIException {
        if (Suspensions.containsKey(userId)) {
            throw new UIException("User " + userId + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofSeconds(seconds);
        Suspensions.put(userId, new UserSuspension(userId, seconds));
    }

    @Override
    public void suspendGuestUser(int userId, int seconds) throws UIException {
        if (Suspensions.containsKey(userId)) {
            throw new UIException("Guest " + userId + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofSeconds(seconds);
        Suspensions.put(userId, new UserSuspension(userId, seconds));
    }

    @Override
    public boolean isSuspended(Integer userId) {
        UserSuspension suspension = Suspensions.get(userId);
        return suspension != null && !suspension.isExpired() && !suspension.isPaused();
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            Suspensions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 0, 10, TimeUnit.SECONDS); // checks every 10 seconds
    }

    @Override
    public void pauseSuspension(Integer userId) throws UIException {
        UserSuspension suspension = getSuspension(userId);
        if (suspension == null) {
            throw new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND);
        }
        suspension.pause();
    }

    @Override
    public void resumeSuspension(Integer userId) throws UIException {
        UserSuspension suspension = getSuspension(userId);
        if (suspension == null) {
            throw new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND);
        }
        suspension.resume();
    }

    private UserSuspension getSuspension(Integer userId) {
        return Suspensions.get(userId);
    }

    @Override
    public List<UserSuspension> getAllSuspensions() {
        return new ArrayList<>(Suspensions.values());
    }
    public void clear() {
    Suspensions.clear();

    // Optional: shut down scheduler if tasks are scheduled per test
        scheduler.shutdownNow(); // or scheduler.shutdown();
    }
}

