package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.*;

import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;


@RestController
@RequestMapping("/user")
public class UserController {
    
    public UserService s ;
    
    public AuthenticationRepo auth;

    public UserController(Repos repos){
        // s = new UserService(repos.userRepo);
        auth = repos.auth;
    }

    @PostMapping("/reg")
    public String register(@PathVariable String user){
        
        return "done";
    }

    @PostMapping("/log")
    public String login(@PathVariable String token){
        return "done";

    }
}
