package workshop.demo.Contrrollers;
package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

import java.util.List;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final UserSuspensionService userSuspensionService;
    private final UserService userService;
    private final OrderService orderService;
    private final UserSuspensionService userSuspensionService;

    @Autowired
    public AdminController(Repos repos) {
        this.userService = new UserService(repos.userRepo, repos.auth, repos.stockrepo);
        this.orderService = new OrderService(repos.orderRepo, repos.storeRepo, repos.auth, repos.userRepo);
        this.userSuspensionService = new UserSuspensionService(repos.UserSuspensionRepo, repos.userRepo, repos.auth);
    }

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("Checking admin access...");
    }
    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("Checking admin access...");
    }

    @GetMapping("/viewSystemPurchaseHistory")
    public String viewSystemPurchaseHistory(@RequestParam String token) {
        ApiResponse<ReceiptDTO[]> res;
        try {
            List<ReceiptDTO> listOrders = orderService.getReceiptDTOsByUser(token);
            ReceiptDTO[] data = listOrders.toArray(new ReceiptDTO[0]);
            res = new ApiResponse<>(data, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }
    // @DeleteMapping("/removeUser")
    // public String removeUser(@RequestParam String token,
    // @RequestParam String userToRemove) {
    // ApiResponse<String> res;
    // try {
    // userService.RemoveUser(token, userToRemove);
    // res = new ApiResponse<>("done", null);
    // } catch (UIException ex) {
    // res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
    // } catch (Exception e) {
    // res = new ApiResponse<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }

    // @GetMapping("/viewSystemInfo")
    // public String viewSystemInfo(@RequestParam String token) {
    // ApiResponse<String> res;
    // try {
    // userService.ViewSystemInfo(token);
    // res = new ApiResponse<>("done", null);
    // } catch (UIException ex) {
    // res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
    // } catch (Exception e) {
    // res = new ApiResponse<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }

   @PostMapping("/suspendUser")
public String suspendUser(@RequestParam Integer userId,
                          @RequestParam int minutes,
                          @RequestParam String token) {
    ApiResponse<Boolean> res;
    try {
        userSuspensionService.suspendRegisteredUser(userId, minutes, token);
        res = new ApiResponse<>(true, null);
    } catch (UIException ex) {
        res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
    } catch (Exception e) {
        res = new ApiResponse<>(null, e.getMessage(), -1);
    }
    return res.toJson();
}

@PostMapping("/pauseSuspension")
public String pauseSuspension(@RequestParam Integer userId,
                              @RequestParam String token) {
    ApiResponse<Boolean> res;
    try {
        userSuspensionService.pauseSuspension(userId, token);
        res = new ApiResponse<>(true, null);
    } catch (UIException ex) {
        res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
    } catch (Exception e) {
        res = new ApiResponse<>(null, e.getMessage(), -1);
    }
    return res.toJson();
}

@PostMapping("/resumeSuspension")
public String resumeSuspension(@RequestParam Integer userId,
                               @RequestParam String token) {
    ApiResponse<Boolean> res;
    try {
        userSuspensionService.resumeSuspension(userId, token);
        res = new ApiResponse<>(true, null);
    } catch (UIException ex) {
        res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
    } catch (Exception e) {
        res = new ApiResponse<>(null, e.getMessage(), -1);
    }
    return res.toJson();
}

    // Additional admin methods can be added here...
}
