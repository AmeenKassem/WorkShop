package workshop.demo.Contrrollers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.Response;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/users")
public class UserController {

    private  UserService userService;
    private OrderService orderService;

    @Autowired
    public UserController(Repos repos) {
        this.userService = new UserService(repos.userRepo, repos.auth);
        this.orderService= new OrderService(repos.orderRepo, repos.storeRepo, repos.auth, repos.userRepo);
        
    }

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("must check if the system get published by admin ...");
    }

    @GetMapping("/generateGuest")
    public String generateGuest() {
        Response<String> res;
        try {
            String token = userService.generateGuest();
            res = new Response<>(token, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/register")
    public String register(@RequestParam String token,
                           @RequestParam String username,
                           @RequestParam String password) {
        Response<Boolean> res;
        try {
            userService.register(token, username, password);
            res = new Response<>(true, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/login")
    public String login(@RequestParam String token,
                        @RequestParam String username,
                        @RequestParam String password) {
        Response<String> res;
        try {
            String data = userService.login(token, username, password);
            res = new Response<>(data, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @DeleteMapping("/destroyGuest")
    public String destroyGuest(@RequestParam String token) {
        Response<Boolean> res;
        try {
            userService.destroyGuest(token);
            res = new Response<>(true, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/logout")
    public String logoutUser(@RequestParam String token) {
        Response<String> res;
        try {
            String newToken = userService.logoutUser(token);
            res = new Response<>(newToken, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/setAdmin")
    public String setAdmin(@RequestParam String token,
                           @RequestParam String adminKey) {
        Response<Boolean> res;
        try {
            boolean data = userService.setAdmin(token, adminKey);
            res = new Response<>(data, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addToCart")
    public String addToUserCart(@RequestParam String token,
                                @RequestParam String itemToAddJson) {
        Response<Boolean> res;
        try {
            ItemStoreDTO itemToAdd = ItemStoreDTO.fromJSON(itemToAddJson);
            res = new Response<>(userService.addToUserCart(token, itemToAdd), null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
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
    // }

    // @DeleteMapping("/removeUser")
    // public String removeUser(@RequestParam String token,
    //                          @RequestParam String userToRemove) {
    //     Response<String> res;
    //     try {
    //         userService.RemoveUser(token, userToRemove);
    //         res = new Response<>("done", null);
    //     } catch (UIException ex) {
    //         res = new Response<>(null, ex.getMessage(), ex.getNumber());
    //     } catch (Exception e) {
    //         res = new Response<>(null, e.getMessage(), -1);
    //     }
    //     return res.toJson();
    // }

    @GetMapping("/viewSystemPurchaseHistory")
    public String viewSystemPurchaseHistory(@RequestParam String token) {
        Response<ReceiptDTO[]> res;
        try {
            // userService.ViewSystemPurchaseHistory(token);
            List<ReceiptDTO> listOrders = orderService.getReceiptDTOsByUser(token);
            ReceiptDTO[] data = new ReceiptDTO[listOrders.size()];
            for(int i=0;i<data.length;i++){
                data[i]=listOrders.get(i);
            }
            res = new Response<>(data, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    // @GetMapping("/viewSystemInfo")
    // public String viewSystemInfo(@RequestParam String token) {
    //     Response<String> res;
    //     try {
    //         userService.ViewSystemInfo(token);
    //         res = new Response<>("done", null);
    //     } catch (UIException ex) {
    //         res = new Response<>(null, ex.getMessage(), ex.getNumber());
    //     } catch (Exception e) {
    //         res = new Response<>(null, e.getMessage(), -1);
    //     }
    //     return res.toJson();
    // }
}
