package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;
    private OrderService orderService;

    @Autowired
    public UserController(Repos repos) {
        this.userService = new UserService(repos.userRepo, repos.auth,repos.stockrepo);
        this.orderService= new OrderService(repos.orderRepo, repos.storeRepo, repos.auth, repos.userRepo);
        
    }

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("must check if the system get published by admin ...");
    }

    @GetMapping("/generateGuest")
    public ResponseEntity<ApiResponse<String>> generateGuest() {
        try {
            String token = userService.generateGuest();
            return ResponseEntity.ok(new ApiResponse<>(token, null));
        } catch (UIException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Boolean>> register(@RequestParam String token,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam int age) {
        ApiResponse<Boolean> res;
        try {
            userService.register(token, username, password, age);
            return ResponseEntity
                    .ok(new ApiResponse<>(true, null)); // success with data=true
        } catch (UIException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, ex.getMessage()));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestParam String token,
            @RequestParam String username,
            @RequestParam String password
    ) {
        ApiResponse<String> res;
        try {
            String data = userService.login(token, username, password);//data new token for user
            return ResponseEntity.ok(new ApiResponse<>(data, null)); // success: return new token
        } catch (UIException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @DeleteMapping("/destroyGuest")
    public String destroyGuest(@RequestParam String token
    ) {
        ApiResponse<Boolean> res;
        try {
            userService.destroyGuest(token);
            res = new ApiResponse<>(true, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/logout")
    public String logoutUser(@RequestParam String token
    ) {
        ApiResponse<String> res;
        try {
            String newToken = userService.logoutUser(token);
            res = new ApiResponse<>(newToken, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/setAdmin")
    public String setAdmin(@RequestParam String token,
            @RequestParam String adminKey
    ) {
        ApiResponse<Boolean> res;
        try {
            boolean data = userService.setAdmin(token, adminKey, 2);
            res = new ApiResponse<>(data, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addToCart")
    public String addToUserCart(@RequestParam String token,
            @RequestBody ItemStoreDTO itemToAdd
    ) {
        ApiResponse<Boolean> res;
        try {

            res = new ApiResponse<>(userService.addToUserCart(token, itemToAdd), null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    // @PutMapping("/updateProfile")
    // public String updateProfile(@RequestParam String token) {
    //     Response<String> res;
    //     try {
    //         userService.updateProfile(token);
    //         res = new Response<>("done", null);
    //     } catch (UIException ex) {
    //         res = new Response<>(null, ex.getMessage(), ex.getNumber());
    //     } catch (Exception e) {
    //         res = new Response<>(null, e.getMessage(), -1);
    //     }
    //     return res.toJson();
    // }''

    @GetMapping("/getuserdto")
    public String getUserDTO(@RequestParam String token) {
        ApiResponse<UserDTO> res;
        try {
            UserDTO dto = userService.getUserDTO(token);
            res = new ApiResponse<>(dto, null);
        }catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }
    
}
