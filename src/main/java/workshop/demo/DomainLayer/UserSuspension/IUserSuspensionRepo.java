package workshop.demo.DomainLayer.UserSuspension;

import java.util.List;

public interface IUserSuspensionRepo {

    void save(UserSuspension suspension);

    UserSuspension getSuspensionByUserId(int userId);

    UserSuspension getSuspensionByUsername(String username);

    void removeSuspension(Integer userId, String username);

    List<UserSuspension> getAllSuspensions();
}
