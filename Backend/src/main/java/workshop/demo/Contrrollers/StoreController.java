package workshop.demo.Contrrollers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    @Autowired
    public StoreController(Repos repos) {
        this.storeService = new StoreService(repos.storeRepo, repos.notificationRepo, repos.auth, repos.userRepo, repos.orderRepo, repos.sUConnectionRepo, repos.stockrepo, repos.UserSuspensionRepo);
    }

    @PostMapping("/addStore")
    public String addStore(@RequestParam String token,
            @RequestParam String storeName,
            @RequestParam String category) {
        ApiResponse<Integer> res;
        try {
            int storeId = storeService.addStoreToSystem(token, storeName, category);
            res = new ApiResponse<>(storeId, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addOwner")
    public String addOwner(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int newOwnerId) {
        ApiResponse<String> res;
        try {
            storeService.AddOwnershipToStore(storeId, token, newOwnerId);
            res = new ApiResponse<>("Owner added successfully", null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deleteOwner")
    public String deleteOwner(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int ownerToDelete) {
        ApiResponse<String> res;
        try {
            storeService.DeleteOwnershipFromStore(storeId, token, ownerToDelete);
            res = new ApiResponse<>("Owner deleted successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addManager")
    public String addManager(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int managerId,
            @RequestBody List<Permission> permissions) {
        ApiResponse<String> res;
        try {
            storeService.AddManagerToStore(storeId, token, managerId, permissions);
            res = new ApiResponse<>("Manager added successfully", null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/changePermissions")
    public String changePermissions(@RequestParam String token,
            @RequestParam int managerId,
            @RequestParam int storeId,
            @RequestBody List<Permission> permissions) {
        ApiResponse<String> res;
        try {
            storeService.changePermissions(token, managerId, storeId, permissions);
            res = new ApiResponse<>("Permissions updated successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deleteManager")
    public String deleteManager(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int managerId) {
        ApiResponse<String> res;
        try {
            storeService.deleteManager(storeId, token, managerId);
            res = new ApiResponse<>("Manager deleted successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/viewHistory")
    public String viewStoreHistory(@RequestParam int storeId) {
        ApiResponse<List<OrderDTO>> res;
        try {
            List<OrderDTO> history = storeService.veiwStoreHistory(storeId);
            res = new ApiResponse<>(history, null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/rankStore")
    public String rankStore(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam int newRank) {
        ApiResponse<String> res;
        try {
            storeService.rankStore(token, storeId, newRank);
            res = new ApiResponse<>("Store ranked successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getFinalRate")
    public String getFinalRate(@RequestParam int storeId) {
        ApiResponse<Integer> res;
        try {
            int rate = storeService.getFinalRateInStore(storeId);
            res = new ApiResponse<>(rate, null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/deactivate")
    public String deactivateStore(@RequestParam int storeId,
            @RequestParam String token) {
        ApiResponse<String> res;
        try {
            storeService.deactivateteStore(storeId, token);
            res = new ApiResponse<>("Store deactivated successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/close")
    public String closeStore(@RequestParam int storeId,
            @RequestParam String token) {
        ApiResponse<String> res;
        try {
            storeService.closeStore(storeId, token);
            res = new ApiResponse<>("Store closed successfully", null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/viewRoles")
    public String viewRoles(@RequestParam int storeId) {
        ApiResponse<List<WorkerDTO>> res;
        try {
            List<WorkerDTO> roles = storeService.ViewRolesAndPermissions(storeId);
            res = new ApiResponse<>(roles, null);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/storeOrders")
    public String getAllOrdersByStore(@RequestParam int storeId,
            @RequestParam String token) {
        ApiResponse<List<OrderDTO>> res;
        try {
            List<OrderDTO> orders = storeService.veiwStoreHistory(storeId);
            res = new ApiResponse<>(orders, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
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
