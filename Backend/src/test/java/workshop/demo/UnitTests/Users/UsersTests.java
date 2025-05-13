package workshop.demo.UnitTests.Users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.UserRepository;

@Service
@SpringBootTest
public class UsersTests {

    private IAuthRepo auth = new AuthenticationRepo();
    private Encoder enc = new Encoder();
    private String adminKey = "123321";

    @Autowired
    private AdminInitilizer a;

    private IUserRepo userRepo = new UserRepository(enc, a);

    private int goodLogin(String username, String password) throws UIException {
        int userIdFromRegister = userRepo.registerUser(username, password,30);

        int userIdFromLogIn = userRepo.login(username, password);
        return userIdFromLogIn;
    }

    @Test
    public void test_register_and_login() throws UIException {
        int guestId = userRepo.generateGuest();
        int userIdFromRegister = userRepo.registerUser("bhaa", "123123",30);

        int userIdFromLogIn = userRepo.login("bhaa", "123123");

        Assertions.assertEquals(userIdFromRegister, userIdFromLogIn);

        int id2 = userRepo.generateGuest();
        try {
            userRepo.login("bhaa", "11111");
            Assertions.assertTrue(false);
        } catch (UIException ex) {
            Assertions.assertTrue(true);
        } catch (Exception ex) {
            Assertions.assertTrue(false);
        }

    }

    @Test
    public void testOnlineAafterLogin() throws UIException {
        int registeredId = userRepo.registerUser("layan", "123",30);
        int loggedInId = userRepo.login("layan", "123");
        Assertions.assertEquals(registeredId, loggedInId);
        boolean isOnline = userRepo.isOnline(loggedInId);
        Assertions.assertTrue(isOnline, "should be online after login");
    }

    // @Test
    // public void adminTest(){
    //     int userId = goodLogin("bhaa2", "123321");
    //     int userId2 = goodLogin("ghanem", "123321");
    //     //user1 are not admin
    //     Assertions.assertFalse(userRepo.isAdmin(userId));
    //     // System.out.println(a.getPassword());
    //     //wrong admin key
    //     Assertions.assertFalse(userRepo.setUserAsAdmin(userId, adminKey+"2222"));
    //     Assertions.assertFalse(userRepo.isAdmin(userId));
    //     //good admin key
    //     Assertions.assertTrue(userRepo.setUserAsAdmin(userId, adminKey));
    //     Assertions.assertTrue(userRepo.isAdmin(userId));
    //     //user2 still not admin
    //     Assertions.assertFalse((userRepo.isAdmin(userId2)));
    // }
    @Test
    public void onlineTest() throws UIException {
        userRepo.registerUser("ghanem2", "123321",30);

    }

    @Test
    public void registeredTest() {

    }

}
