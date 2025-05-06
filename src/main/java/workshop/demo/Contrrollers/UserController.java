package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.ApplicationLayer.Response;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired // inject the service automatically
    public UserController(Repos repos) {
        this.userService = new UserService(repos.userRepo, repos.auth);
    }

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("must check if the system get published by admin ...");
        // You can do logging, auth checks, setup, etc.
        //TODO by @bhaah
    }

    @GetMapping("/generateGuest")
    public String generateGuest() {
        Response<String> res ;
        try {
            String token = userService.generateGuest();
            res= new Response<String>(token, null);
        } catch (UIException ex) {
            res = new Response<String>(null, ex.getMessage(),ex.getNumber());
        }catch(Exception e){
            res = new Response<String>(null,e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/register")
    public String register(@RequestParam String token,
                           @RequestParam String username,
                           @RequestParam String password) {
        try {
            userService.register(token, username, password);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String token,
                        @RequestParam String username,
                        @RequestParam String password) {
        try {
            userService.login(token, username, password);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @DeleteMapping("/destroyGuest")
    public String destroyGuest(@RequestParam String token) {
        try {
            userService.destroyGuest(token);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/logout")
    public String logoutUser(@RequestParam String token) {
        try {
            userService.logoutUser(token);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/setAdmin")
    public String setAdmin(@RequestParam String token,
                           @RequestParam String adminKey) {
        try {
            userService.setAdmin(token, adminKey);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @PostMapping("/addToCart")
    public String addToUserCart(@RequestParam String token,
                                @RequestBody ItemStoreDTO itemToAdd) {
        try {
            userService.addToUserCart(token, itemToAdd);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @PutMapping("/updateProfile")
    public String updateProfile(@RequestParam String token) {
        try {
            userService.updateProfile(token);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @DeleteMapping("/removeUser")
    public String removeUser(@RequestParam String token,
                             @RequestParam String userToRemove) {
        try {
            userService.RemoveUser(token, userToRemove);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @GetMapping("/viewSystemPurchaseHistory")
    public String viewSystemPurchaseHistory(@RequestParam String token) {
        try {
            userService.ViewSystemPurchaseHistory(token);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }

    @GetMapping("/viewSystemInfo")
    public String viewSystemInfo(@RequestParam String token) {
        try {
            userService.ViewSystemInfo(token);
            return "done";
        } catch (Exception e) {
            return "error";
        }
    }
}
