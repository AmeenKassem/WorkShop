package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import workshop.demo.ApplicationLayer.Response;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

import java.util.List;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    @Autowired
    public StoreController(Repos repos) {
        this.storeService = new StoreService(repos.storeRepo, repos.notificationRepo    , repos.auth, repos.userRepo, repos.orderRepo, repos.sUConnectionRepo, repos.stockrepo, repos.UserSuspensionRepo);
    }

    @PostMapping("/addStore")
    public String addStore(@RequestParam String token,
                           @RequestParam String storeName,
                           @RequestParam String category) {
        Response<Integer> res;
        try {
            int storeId = storeService.addStoreToSystem(token, storeName, category);
            res = new Response<>(storeId, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addOwner")
    public String addOwner(@RequestParam int storeId,
                           @RequestParam String token,
                           @RequestParam int newOwnerId) {
        Response<String> res;
        try {
            storeService.AddOwnershipToStore(storeId, token, newOwnerId);
            res = new Response<>("Owner added successfully", null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deleteOwner")
    public String deleteOwner(@RequestParam int storeId,
                              @RequestParam String token,
                              @RequestParam int ownerToDelete) {
        Response<String> res;
        try {
            storeService.DeleteOwnershipFromStore(storeId, token, ownerToDelete);
            res = new Response<>("Owner deleted successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addManager")
    public String addManager(@RequestParam int storeId,
                             @RequestParam String token,
                             @RequestParam int managerId,
                             @RequestBody List<Permission> permissions) {
        Response<String> res;
        try {
            storeService.AddManagerToStore(storeId, token, managerId, permissions);
            res = new Response<>("Manager added successfully", null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/changePermissions")
    public String changePermissions(@RequestParam String token,
                                    @RequestParam int managerId,
                                    @RequestParam int storeId,
                                    @RequestBody List<Permission> permissions) {
        Response<String> res;
        try {
            storeService.changePermissions(token, managerId, storeId, permissions);
            res = new Response<>("Permissions updated successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deleteManager")
    public String deleteManager(@RequestParam int storeId,
                                @RequestParam String token,
                                @RequestParam int managerId) {
        Response<String> res;
        try {
            storeService.deleteManager(storeId, token, managerId);
            res = new Response<>("Manager deleted successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/viewHistory")
    public String viewStoreHistory(@RequestParam int storeId) {
        Response<List<OrderDTO>> res;
        try {
            List<OrderDTO> history = storeService.veiwStoreHistory(storeId);
            res = new Response<>(history, null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/rankStore")
    public String rankStore(@RequestParam String token,
                            @RequestParam int storeId,
                            @RequestParam int newRank) {
        Response<String> res;
        try {
            storeService.rankStore(token, storeId, newRank);
            res = new Response<>("Store ranked successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getFinalRate")
    public String getFinalRate(@RequestParam int storeId) {
        Response<Integer> res;
        try {
            int rate = storeService.getFinalRateInStore(storeId);
            res = new Response<>(rate, null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deactivate")
    public String deactivateStore(@RequestParam int storeId,
                                  @RequestParam String token) {
        Response<String> res;
        try {
            storeService.deactivateteStore(storeId, token);
            res = new Response<>("Store deactivated successfully", null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/close")
    public String closeStore(@RequestParam int storeId,
                             @RequestParam String token) {
        Response<String> res;
        try {
            storeService.closeStore(storeId, token);
            res = new Response<>("Store closed successfully", null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/viewRoles")
    public String viewRoles(@RequestParam int storeId) {
        Response<List<WorkerDTO>> res;
        try {
            List<WorkerDTO> roles = storeService.ViewRolesAndPermissions(storeId);
            res = new Response<>(roles, null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/storeOrders")
    public String getAllOrdersByStore(@RequestParam int storeId,
                                      @RequestParam String token) {
        Response<List<OrderDTO>> res;
        try {
            List<OrderDTO> orders = storeService.veiwStoreHistory(storeId);
            res = new Response<>(orders, null);
        } catch (UIException ex) {
            res = new Response<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getstreDTO")
    public String getStoreDTO(@RequestParam String token,@RequestParam int storeId) {
        Response<StoreDTO> res;
        try {
            StoreDTO dto = storeService.getStoreDTO(token , storeId);
            res = new Response<>(dto, null);
        } catch (Exception e) {
            res = new Response<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }
}
