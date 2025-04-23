package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.*;

import workshop.demo.ApplicationLayer.UserService;


@RestController
@RequestMapping("/user")
public class UserController {
    
    public UserService s ;

    public UserController(Repos repos){
        // s = new UserService(repos.userRepo);
    }

}
