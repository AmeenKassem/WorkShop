package workshop.demo.InfrastructureLayer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;

public class UserSuspensionRepo implements IUserSuspensionRepo {

    private final Map<Integer, UserSuspension> Suspensions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public UserSuspensionRepo() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        startScheduler();
    }

    public void checkUserSuspensoin_ThrowExceptionIfSuspeneded(int userId){
        if(isSuspended(userId));
    }

    @Override
    public void suspendRegisteredUser(Integer userId, int minutes) throws UIException {
        if (Suspensions.containsKey(userId)) {
            throw new UIException("User " + userId + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofMinutes(minutes);
        Suspensions.put(userId, new UserSuspension(userId, duration));
    }

    @Override
    public void suspendGuestUser(int userId, int minutes) throws UIException {
        if (Suspensions.containsKey(userId)) {
            throw new UIException("Guest " + userId + " is already suspended.", ErrorCodes.SUSPENSION_ALREADY_EXISTS);
        }
        Duration duration = Duration.ofMinutes(minutes);
        Suspensions.put(userId, new UserSuspension(userId, duration));
    }

    @Override
    public boolean isSuspended(Integer userId) {
        UserSuspension suspension = Suspensions.get(userId);
        return suspension != null && !suspension.isExpired() && !suspension.isPaused();
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
        Suspensions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }, 0, 10, TimeUnit.SECONDS);
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
        List<UserSuspension> all = new ArrayList<>();
        all.addAll(Suspensions.values());
        return all;
    }
}
