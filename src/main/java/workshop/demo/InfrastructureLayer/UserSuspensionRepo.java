package workshop.demo.InfrastructureLayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

public class UserSuspensionRepo implements IUserSuspensionRepo {

    private final Map<Integer, UserSuspension> guestSuspensions = new ConcurrentHashMap<>();
    private final Map<String, UserSuspension> userSuspensions = new ConcurrentHashMap<>();

    @Override
    public void save(UserSuspension suspension) {
        if (suspension.getUsername() != null) {
            userSuspensions.put(suspension.getUsername(), suspension);
        } else if (suspension.getUserId() != null) {
            guestSuspensions.put(suspension.getUserId(), suspension);
        }
    }

    @Override
    public UserSuspension getSuspensionByUserId(int userId) {
        return guestSuspensions.get(userId);
    }

    @Override
    public UserSuspension getSuspensionByUsername(String username) {
        return userSuspensions.get(username);
    }

    @Override
    public void removeSuspension(Integer userId, String username) {
        if (username != null) {
            userSuspensions.remove(username);
        } else if (userId != null) {
            guestSuspensions.remove(userId);
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

