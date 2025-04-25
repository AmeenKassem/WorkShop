package workshop.demo.Users;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.IncorrectLogin;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.UserRepository;

@SpringBootTest
public class UsersTests {
    private IAuthRepo auth = new AuthenticationRepo();
    private Encoder enc = new Encoder();
    private IUserRepo userRepo = new UserRepository( enc);


    private int goodLogin(String username){
        return -1;
    }

    @Test
    public void test_register_and_login(){
        int guestId = userRepo.generateGuest();
        int userIdFromRegister = userRepo.registerUser( "bhaa", "123123");

        int userIdFromLogIn = userRepo.login("bhaa", "123123");
        
        Assertions.assertEquals(userIdFromRegister, userIdFromLogIn);
        
        int id2 = userRepo.generateGuest();
        try{
            userRepo.login("bhaa", "11111");
            Assertions.assertTrue(false);
        }catch(IncorrectLogin ex){
            Assertions.assertTrue(true);
        }catch(Exception ex){
            Assertions.assertTrue(false);
        }

    }






    

}
