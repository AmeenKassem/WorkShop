package workshop.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
// import workshop.demo.ApplicationLayer.AdminHandler;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.ApplicationLayer.UserSuspensionService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.SystemAnalyticsDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;
import workshop.demo.PresentationLayer.Requests.AddToCartRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserSuspensionService userSuspensionService;
    // private final AdminHandler adminHandler;

    @Autowired
    public UserController(
            IUserRepo userRepo,
            IStoreRepo storeRepo,
            IOrderRepo orderRepo,
            IAuthRepo auth,                      // the JWT token provider
            IStockRepo stockrepo,
            AdminInitilizer adminInitializer,
            IUserSuspensionRepo userSuspensionRepo,
            UserJpaRepository regRepo,
            GuestJpaRepository guest
    ) throws Exception {
        // this.adminHandler = new AdminHandler(orderRepo, storeRepo, userRepo, auth);
        this.userSuspensionService = new UserSuspensionService(userSuspensionRepo, userRepo, auth);
        this.userService = new UserService(regRepo,userRepo, auth, stockrepo, adminInitializer,  storeRepo,guest);
    }
    // @ModelAttribute
    // public void beforeEveryRequest(HttpServletRequest request) {
    // System.out.println("must check if the system get published by admin ...");
    // }
    @GetMapping("/generateGuest")
    public ResponseEntity<?> generateGuest() {
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

    // @PostMapping("/login")
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> login(
            @RequestParam String token,
            @RequestParam String username,
            @RequestParam String password) {
        try {
            String data = userService.login(token, username, password);// data new token for user
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
    public ResponseEntity<?> destroyGuest(@RequestParam String token) {
        try {
            userService.destroyGuest(token);
            return ResponseEntity.ok(new ApiResponse<>(true, null));
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

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam String token) {
        try {
            String newToken = userService.logoutUser(token);
            return ResponseEntity.ok(new ApiResponse<>(newToken, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/setAdmin")
    public ResponseEntity<?> setAdmin(
            @RequestParam String token,
            @RequestParam String adminKey) {
        try {
            boolean data = userService.setAdmin(token, adminKey, 2);
            return ResponseEntity.ok(new ApiResponse<>(data, null));
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

    @PostMapping("/addToCart")
    public ResponseEntity<?> addToUserCart(
            @RequestParam String token,
            @RequestBody AddToCartRequest request) {
        try {
            boolean data = userService.addToUserCart(token, request.getItem(), request.getQuantity());
            return ResponseEntity.ok(new ApiResponse<>(data, null));
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

    @PostMapping("/ModifyCart")
    public ResponseEntity<ApiResponse<Boolean>> modifyCart(
            @RequestParam String token,
            @RequestParam int productId,
            @RequestParam int quantity) {
        ApiResponse<Boolean> res;
        try {
            boolean result = userService.ModifyCartAddQToBuy(token, productId, quantity);
            res = new ApiResponse<>(result, null);
            //System.out.println("ModifyCart result: " + result);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping("/removeFromCart")
    public ResponseEntity<ApiResponse<Boolean>> removeFromCart(
            @RequestParam String token,
            @RequestParam int productId) {
        ApiResponse<Boolean> res;
        try {
            boolean result = userService.removeItemFromCart(token, productId);
            res = new ApiResponse<>(result, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // @PutMapping("/updateProfile")
    // public String updateProfile(@RequestParam String token) {
    // Response<String> res;
    // try {
    // userService.updateProfile(token);
    // res = new Response<>("done", null);
    // } catch (UIException ex) {
    // res = new Response<>(null, ex.getMessage(), ex.getNumber());
    // } catch (Exception e) {
    // res = new Response<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }''
    @GetMapping(value = "/getUserDTO", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> getUserDTO(@RequestParam String token) {
        try {
            UserDTO dto = userService.getUserDTO(token);
            return ResponseEntity.ok(new ApiResponse<>(dto, null));
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

    @GetMapping("/getspecialcart")
    public ResponseEntity<?> getSpecialCart(@RequestParam String token) {
        ApiResponse<SpecialCartItemDTO[]> res;
        try {
            SpecialCartItemDTO[] cartItems = userService.getSpecialCart(token);

            // SpecialCartItemDTO special1 = new SpecialCartItemDTO();
            // special1.setIds(201, 1001, 0, SpecialType.Random);
            // special1.setValues("Mystery Box", false, false);

            // SpecialCartItemDTO special2 = new SpecialCartItemDTO();
            // special2.setIds(202, 1002, 0, SpecialType.Auction);
            // special2.setValues("Rare Coin", true, true);

            // SpecialCartItemDTO special3 = new SpecialCartItemDTO();
            // special3.setIds(203, 1003, 10001, SpecialType.BID);
            // special3.setValues("Gaming Chair", false, true);
            // SpecialCartItemDTO[] cartItems = new SpecialCartItemDTO[] { special1,
            // special2, special3 };
            res = new ApiResponse<>(cartItems, null);
            return ResponseEntity.ok(res);

        } catch (UIException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res);
        }
    }

    @GetMapping("/getregularcart")
    public ResponseEntity<?> getRegularCart(@RequestParam String token) {
        try {
            // ItemCartDTO item1 = new ItemCartDTO(1, 2, 101, 10, "Bananas", "Fresh
            // bananas", Category.Beauty);
            // ItemCartDTO item2 = new ItemCartDTO(2, 1, 102, 199, "Bluetooth Speaker",
            // "Loud and portable",
            // Category.Clothing);
            // ItemCartDTO item3 = new ItemCartDTO(3, 3, 103, 15, "Pillow", "Soft pillow",
            // Category.Home);
            ItemCartDTO[] cartItems = userService.getRegularCart(token);
            // ItemCartDTO[] cartItems = new ItemCartDTO[] { item1, item2, item3 };
            ApiResponse<ItemCartDTO[]> res = new ApiResponse<>(cartItems, null);
            return ResponseEntity.ok(res); // ✅ Return 200 OK
        } catch (UIException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            ApiResponse<ItemCartDTO[]> res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // ------------admin activites:
    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("Checking admin access...");
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
    public ResponseEntity<ApiResponse<Boolean>> suspendUser(@RequestParam Integer userId,
            @RequestParam int minutes,
            @RequestParam String token) {
        ApiResponse<Boolean> res;
        try {
            userSuspensionService.suspendRegisteredUser(userId, minutes, token);
            res = new ApiResponse<>(true, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping("/pauseSuspension")
    public ResponseEntity<ApiResponse<Boolean>> pauseSuspension(@RequestParam Integer userId,
            @RequestParam String token) {

        ApiResponse<Boolean> res;
        try {
            userSuspensionService.pauseSuspension(userId, token);
            res = new ApiResponse<>(true, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping("/resumeSuspension")
    public ResponseEntity<ApiResponse<Boolean>> resumeSuspension(@RequestParam Integer userId,
            @RequestParam String token) {

        ApiResponse<Boolean> res;
        try {
            userSuspensionService.resumeSuspension(userId, token);
            res = new ApiResponse<>(true, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    // // Additional admin methods can be added here...
    // @GetMapping("/purchaseHistory")
    // public ResponseEntity<List<PurchaseHistoryDTO>> getSystemPurchaseHistory(@RequestParam String token) {
    //     try {
    //         List<PurchaseHistoryDTO> history = adminHandler.viewPurchaseHistory(token);
    //         return ResponseEntity.ok(history);
    //     } catch (UIException ex) {
    //         // Return an error response if token is invalid or user is not admin
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                 .body(null);
    //     } catch (Exception ex) {
    //         // Handle other exceptions as needed
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    //     }
    // }

    // @GetMapping("/analytics")
    // public ResponseEntity<SystemAnalyticsDTO> getSystemAnalytics(@RequestParam String token) {
    //     try {
    //         SystemAnalyticsDTO analytics = adminHandler.getSystemAnalytics(token);
    //         return ResponseEntity.ok(analytics);
    //     } catch (UIException ex) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    //     } catch (Exception ex) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    //     }
    // }

    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(@RequestParam String token) {
        ApiResponse<List<UserDTO>> res;
        try {
            List<UserDTO> users = userService.getAllUsers(token);
            res = new ApiResponse<>(users, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }

    }

}
