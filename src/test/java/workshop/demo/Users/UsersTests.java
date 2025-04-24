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
    private IUserRepo userRepo = new UserRepository(auth, enc);


    @Test
    public void test_register_and_login(){
        String guestToken = userRepo.generateGuest();
        userRepo.registerUser(guestToken, "bhaa", "123123");
        System.out.println(enc.encodePassword("123123"));
        System.out.println(enc.encodePassword("123123"));

        String userToken = userRepo.login(guestToken,"bhaa", "123123");
        
        //The username is on the token , and the login is succed
        Assertions.assertEquals("bhaa",auth.getUserName(userToken) );

        String token2 = userRepo.generateGuest();
        try{
            userRepo.login(token2, "bhaa", "11111");
            Assertions.assertTrue(false);
        }catch(IncorrectLogin ex){
            Assertions.assertTrue(true);
        }catch(Exception ex){
            Assertions.assertTrue(false);
        }

    }


    

}
