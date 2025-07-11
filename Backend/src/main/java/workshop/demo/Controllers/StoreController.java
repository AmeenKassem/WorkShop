package workshop.demo.Controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/addStore")
    public ResponseEntity<?> addStore(@RequestParam String token,
            @RequestParam String storeName,
            @RequestParam String category) {
        try {
            int storeId = storeService.addStoreToSystem(token, storeName, category);
            return ResponseEntity.ok(new ApiResponse<>(storeId, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/respondToOffer")
    public ResponseEntity<?> respondToOffer(
            @RequestParam int storeId,
            @RequestParam String senderName,
            @RequestParam String receiverName,
            @RequestParam boolean answer,
            @RequestParam boolean toBeOwner) {
        try {
            storeService.reciveAnswerToOffer(storeId, senderName, receiverName, answer, toBeOwner);
            return ResponseEntity.ok(new ApiResponse<>("Offer response recorded successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/makeOfferOwner")
    public ResponseEntity<?> addOwner(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam String newOwner) {
        try {
            storeService.MakeofferToAddOwnershipToStore(storeId, token, newOwner);
            return ResponseEntity.ok(new ApiResponse<>("Owner added successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/deleteOwner")
    public ResponseEntity<?> deleteOwner(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int ownerToDelete) {

        try {
            System.out.println("in the controller -> Trying to delete owner with ID = " + ownerToDelete);
            storeService.DeleteOwnershipFromStore(storeId, token, ownerToDelete);
            return ResponseEntity.ok(new ApiResponse<>("Owner deleted successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));

        }

    }

    @PostMapping("/makeOfferManager")
    public ResponseEntity<?> addManager(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam String managerName,
            @RequestBody List<Permission> permissions) {
        try {
            // List<Permission> pers= new ArrayList<>();
            storeService.MakeOfferToAddManagerToStore(storeId, token, managerName, permissions);
            return ResponseEntity.ok(new ApiResponse<>("made an offer successfuly", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/changePermissions")
    public ResponseEntity<?> changePermissions(@RequestParam String token,
            @RequestParam int managerId,
            @RequestParam int storeId,
            @RequestBody List<Permission> permissions) {
        try {
            storeService.changePermissions(token, managerId, storeId, permissions);
            return ResponseEntity.ok(new ApiResponse<>("Permissions updated successfully", null));

        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/deleteManager")
    public ResponseEntity<?> deleteManager(@RequestParam int storeId,
            @RequestParam String token,
            @RequestParam int managerId) {
        try {
            storeService.deleteManager(storeId, token, managerId);
            return ResponseEntity.ok(new ApiResponse<>("Manager deleted successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // @GetMapping("/viewHistory")
    // public ResponseEntity<?> viewStoreHistory(@RequestParam int storeId) {
    // try {
    // List<OrderDTO> history = storeService.veiwStoreHistory(storeId);
    // return ResponseEntity.ok(new ApiResponse<>(history, null));
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse<>(null, e.getMessage(), -1));
    // }
    // }
    @PostMapping("/rankStore")
    public ResponseEntity<?> rankStore(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam int newRank) {
        try {
            storeService.rankStore(token, storeId, newRank);
            return ResponseEntity.ok(new ApiResponse<>("Store ranked successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getFinalRate")
    public ResponseEntity<?> getFinalRate(@RequestParam int storeId) {
        try {
            int rate = storeService.getFinalRateInStore(storeId);
            return ResponseEntity.ok(new ApiResponse<>(rate, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateStore(@RequestParam int storeId,
            @RequestParam String token) {
        try {
            storeService.deactivateteStore(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>("Store deactivated successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/close")
    public ResponseEntity<?> closeStore(@RequestParam int storeId,
            @RequestParam String token) {

        try {
            storeService.closeStore(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>("Store closed successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/viewRolesAndPermissions")
    public ResponseEntity<?> viewRolesAndPermissions(@RequestParam String token, @RequestParam int storeId) {
        try {
            List<WorkerDTO> workers = storeService.ViewRolesAndPermissions(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(workers, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    // @GetMapping("/storeOrders")
    // public ResponseEntity<?> getAllOrdersByStore(@RequestParam int storeId,
    // @RequestParam String token) {
    // try {
    // List<OrderDTO> orders = storeService.veiwStoreHistory(storeId);
    // return ResponseEntity.ok(new ApiResponse<>(orders, null));
    // } catch (UIException ex) {
    // return ResponseEntity.badRequest().body(new ApiResponse<>(null,
    // ex.getMessage(), ex.getNumber()));
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse<>(null, e.getMessage(), -1));
    // }
    // }
    @GetMapping("/allStores")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> getAllStoresToshow() {
        try {
            List<StoreDTO> stores = storeService.getAllStores();
            return ResponseEntity.ok(new ApiResponse<>(stores, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getstoreDTO")
    public ResponseEntity<?> getStoreDTO(@RequestParam String token, @RequestParam int storeId) {
        try {
            StoreDTO dto = storeService.getStoreDTO(token, storeId);
            return ResponseEntity.ok(new ApiResponse<>(dto, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @GetMapping("/myStores")
    public ResponseEntity<?> getMyStores(@RequestParam String token) {
        try {
            List<StoreDTO> stores = storeService.getStoresOwnedByUser(token);
            return ResponseEntity.ok(new ApiResponse<>(stores, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

//    @PostMapping("/addDiscount")
//    public ResponseEntity<?> addDiscountToStore(
//            @RequestParam int storeId,
//            @RequestParam String token,
//            @RequestParam String name, @RequestParam double percent, @RequestParam CreateDiscountDTO.Type type,
//            @RequestParam String condition, @RequestParam CreateDiscountDTO.Logic logic,
//            @RequestParam(required = false) String[] subDiscountsNames) {
//
//        if (subDiscountsNames == null) {
//            subDiscountsNames = new String[0];
//        }else{
//            for(int i=0;i<subDiscountsNames.length;i++){
//                subDiscountsNames[i] = URLDecoder.decode(URLDecoder.decode(subDiscountsNames[i], StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//            }
//        }
//        name = URLDecoder.decode(URLDecoder.decode(name, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//        condition = URLDecoder.decode(URLDecoder.decode(condition, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//
//        try {
//            storeService.addDiscountToStore(storeId, token, name, percent, type, condition, logic, subDiscountsNames); // assumes
//            // permission
//            // check
//            // is
//            // inside
//            // service
//            return ResponseEntity.ok(new ApiResponse<>("Discount added successfully", null));
//        } catch (UIException ex) {
//            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse<>(null, e.getMessage(), -1));
//        }
//    }

    @PostMapping("/removeDiscountByName")
    public ResponseEntity<?> removeDiscountByName(
            @RequestParam String token,
            @RequestParam int storeId,
            @RequestParam String discountName) {
        try {
            discountName = URLDecoder.decode(URLDecoder.decode(discountName, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            storeService.removeDiscountFromStore(token, storeId, discountName);
            return ResponseEntity.ok(new ApiResponse<>("Discount removed", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

@PostMapping("/addPurchasePolicy")
public ResponseEntity<?> addPurchasePolicy(@RequestParam String token,
                                           @RequestParam int storeId,
                                           @RequestParam String policyKey,
                                           @RequestParam int productId,
                                           @RequestParam(required = false) Integer param) {
    try {
        storeService.addPurchasePolicy(token, storeId, policyKey, productId, param);
        return ResponseEntity.ok(new ApiResponse<>("PurchasePolicy added", null));
    } catch (UIException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, exception.getMessage(), exception.getNumber()));
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, ex.getMessage(), -1));
    }
}

@PostMapping("/removePurchasePolicy")
public ResponseEntity<?> removePurchasePolicy(@RequestParam String token,
                                              @RequestParam int storeId,
                                              @RequestParam String policyKey,
                                              @RequestParam int productId,
                                              @RequestParam(required = false) Integer param) {
    try {
        storeService.removePurchasePolicy(token, storeId, policyKey, productId, param == null ? -1 : param);
        return ResponseEntity.ok(new ApiResponse<>("PurchasePolicy removed", null));
    } catch (UIException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(null, exception.getMessage(), exception.getNumber()));
    } catch (Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(null, ex.getMessage(), -1));
    }
}

    @GetMapping("/discountNames")
    public ApiResponse names(@RequestParam int storeId, @RequestParam String token) throws UIException {
        String[] arr = storeService.getAllDiscountNames(storeId, token);
        return new ApiResponse(arr, null);
    }

    @GetMapping("/getStoreDiscounts")
    public ResponseEntity<?> getStoreDiscounts(@RequestParam int storeId, @RequestParam String token) {
        try {
            CreateDiscountDTO discounts = storeService.getFlattenedDiscounts(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>(discounts, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }
    @GetMapping("/getVisibleDiscountDescriptions")
    public ResponseEntity<?> getVisibleDiscountDescriptions(@RequestParam int storeId, @RequestParam String token) {
        try {
            List<String> descriptions = storeService.getVisibleDiscountDescriptions(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>(descriptions, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }
    @GetMapping("/getAllDiscountDetails")
    public ResponseEntity<?> getAllDiscountDetails(@RequestParam int storeId, @RequestParam String token) {
        try {
            List<CreateDiscountDTO> allDiscounts = storeService.getAllDiscountsFlattened(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>(allDiscounts, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }
    @GetMapping("/getStoreDiscountTree")
    public ResponseEntity<?> getStoreDiscountTree(@RequestParam int storeId, @RequestParam String token) {
        try {
            List<CreateDiscountDTO> discounts = storeService.getDiscountTree(storeId, token);
            return ResponseEntity.ok(new ApiResponse<>(discounts, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }
    @PostMapping("/addDiscount")
    public ResponseEntity<?> addDiscountTree(
            @RequestParam int storeId,
            @RequestParam String token,
            @RequestBody CreateDiscountDTO dto
    ) {
        try {
            storeService.addDiscount(storeId, token, dto);
            return ResponseEntity.ok(Map.of("message", "Discount added successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }







}
