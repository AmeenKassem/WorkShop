package workshop.demo.InfrastructureLayer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class UserSuspensionRepo implements IUserSuspensionRepo {

    private final Map<Integer, UserSuspension> guestSuspensions = new ConcurrentHashMap<>();
    private final Map<String, UserSuspension> userSuspensions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public UserSuspensionRepo() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startScheduler();
    }

    @Override
    public void suspendRegisteredUser(String username, int minutes) throws UIException {
        if (userSuspensions.containsKey(username)) {
            throw new UIException("User " + username + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofMinutes(minutes);
        userSuspensions.put(username, new UserSuspension(null, username, duration));
    }

    @Override
    public void suspendGuestUser(int guestId, int minutes) throws UIException {
        if (guestSuspensions.containsKey(guestId)) {
            throw new UIException("Guest " + guestId + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofMinutes(minutes);
        guestSuspensions.put(guestId, new UserSuspension(guestId, null, duration));
    }

    @Override
    public boolean isSuspended(Integer userId, String username) {
        UserSuspension suspension = (username != null) 
            ? userSuspensions.get(username) 
            : guestSuspensions.get(userId);
        
        return suspension != null && !suspension.isExpired() && !suspension.isPaused();
    }
    

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            userSuspensions.entrySet().removeIf(entry -> entry.getValue().isExpired());
            guestSuspensions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 0, 10, TimeUnit.SECONDS);
    }
    @Override
public void pauseSuspension(Integer userId, String username) throws UIException {
    UserSuspension suspension = getSuspension(userId, username);
    if (suspension == null) {
        throw new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND);
    }
    suspension.pause();
}

@Override
public void resumeSuspension(Integer userId, String username) throws UIException {
    UserSuspension suspension = getSuspension(userId, username);
    if (suspension == null) {
        throw new UIException("Suspension not found.", ErrorCodes.SUSPENSION_NOT_FOUND);
    }
    suspension.resume();
}

private UserSuspension getSuspension(Integer userId, String username) {
    if (username != null) {
        return userSuspensions.get(username);
    } else {
        return guestSuspensions.get(userId);
    }
}

    @Override
    public List<UserSuspension> getAllSuspensions() {
        List<UserSuspension> all = new ArrayList<>();
        all.addAll(guestSuspensions.values());
        all.addAll(userSuspensions.values());
        return all;
    }
}
