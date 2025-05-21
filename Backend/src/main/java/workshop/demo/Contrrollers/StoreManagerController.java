package workshop.demo.Contrrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.ManagerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/store-managers")
public class StoreManagerController {

    private final StoreService storeService;

    @Autowired
    public StoreManagerController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/change-permissions")
    public ResponseEntity<?> changePermissions(
            @RequestParam String token,
            @RequestBody ManagerDTO managerDTO) {
        try {
            storeService.changePermissions(
                    token,
                    managerDTO.getManagerId(),
                    managerDTO.getStoreId(),
                    List.copyOf(managerDTO.getPermissions()));
            return ResponseEntity.ok(new ApiResponse<>("Permissions updated successfully", null));
        } catch (UIException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, e.getMessage(), e.getNumber()));
        } catch (DevException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(null, e.getMessage(), -2));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeManager(
            @RequestParam String token,
            @RequestParam int storeId,
            @RequestParam int managerId) {
        try {
            storeService.deleteManager(storeId, token, managerId);
            return ResponseEntity.ok(new ApiResponse<>("Manager removed successfully", null));
        } catch (UIException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, e.getMessage(), e.getNumber()));
        } catch (DevException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(null, e.getMessage(), -2));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // each owner in the store can see the store managers and the permition of them
    @GetMapping("/getManagers")
    public ResponseEntity<?> getManagers(@RequestParam String token, @RequestParam int storeId) {
        try {
            List<ManagerDTO> managers = storeService.getManagersOfStore(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(managers, null));
        } catch (UIException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, e.getMessage(), e.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage()));
        }
    }

    @GetMapping("/owned")
    public ResponseEntity<?> getOwnedStores(@RequestParam String token) {
        List<StoreDTO> stores = storeService.getStoresOwnedByUser(token);
        return ResponseEntity.ok(new ApiResponse<>(stores, null));
    }

}
