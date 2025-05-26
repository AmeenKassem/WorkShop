package workshop.demo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/purchase")
public class PurcheseContoller {

    private PurchaseService purchaseService;

    @Autowired
    public PurcheseContoller(Repos repo) {
        this.purchaseService = new PurchaseService(repo.auth, repo.stockrepo, repo.storeRepo, repo.userRepo,
                repo.purchaseRepo, repo.orderRepo, repo.paymentService, repo.supplyService, repo.UserSuspensionRepo);
    }

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("PurchaseController");
    }

    // cant use the RequestBody for the PaymentDetails and the SupplyDetails
    // because they are not in the same class
    @PostMapping("/guest")
    public ResponseEntity<?> buyGuestCart(@RequestParam String token,
            @RequestParam String paymentJson,
            @RequestParam String supplyJson) {
        ApiResponse<ReceiptDTO[]> res;
        try {
            System.out.println("payment received for guest");
            PaymentDetails paymentdetails = PaymentDetails.getPaymentDetailsFromJSON(paymentJson);
            SupplyDetails supplydetails = SupplyDetails.getSupplyDetailsFromJSON(supplyJson);

            ReceiptDTO[] receipts = purchaseService.buyGuestCart(token, paymentdetails, supplydetails);
            res = new ApiResponse<>(receipts, null);
            // Return 200 OK for successful operations
            return ResponseEntity.ok(res.toJson());
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            // Return 400 Bad Request for client errors
            return ResponseEntity.badRequest().body(res.toJson());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            // Return 500 Internal Server Error for server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res.toJson());
        }
    }

    @PostMapping("/registered")
    public ResponseEntity<?> buyRegisteredCart(@RequestParam String token,
            @RequestParam String paymentJson,
            @RequestParam String supplyJson) {
        ApiResponse<ReceiptDTO[]> res;
        try {
            System.out.println("payment received for registered");
            PaymentDetails paymentdetails = PaymentDetails.getPaymentDetailsFromJSON(paymentJson);
            SupplyDetails supplydetails = SupplyDetails.getSupplyDetailsFromJSON(supplyJson);

            ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(token, paymentdetails, supplydetails);
            res = new ApiResponse<>(receipts, null);
            // Return 200 OK for successful operations
            return ResponseEntity.ok(res.toJson());
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            // Return 400 Bad Request for client errors
            return ResponseEntity.badRequest().body(res.toJson());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            // Return 500 Internal Server Error for server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res.toJson());
        }
    }

    @PostMapping("/participateRandom")
    public String participateInRandom(@RequestParam String token,
            @RequestParam int randomId,
            @RequestParam int storeId,
            @RequestParam double amountPaid,
            @RequestBody PaymentDetails payment) {
        ApiResponse<ParticipationInRandomDTO> res;
        try {
            ParticipationInRandomDTO result = purchaseService.participateInRandom(token, randomId, storeId, amountPaid,
payment);
            res = new ApiResponse<>(result, null);
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/finalizeRandomWinnings")
    public String finalizeRandomWinnings(@RequestParam String token,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zipCode) {
        ApiResponse<String> res;
        try {
            SupplyDetails supply = new SupplyDetails(address, city, state, zipCode);
            // TODO change the test payment and the supply
            purchaseService.finalizeSpecialCart(token, PaymentDetails.testPayment(), supply);
            res = new ApiResponse<>("Done", null);
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    // @PostMapping("/finalizeAcceptedBids")
    // public String finalizeAcceptedBids(@RequestParam String token,
    // @RequestBody PaymentDetails payment) {
    // Response<String> res;
    // try {
    // purchaseService.finalizeAcceptedBids(token, payment);
    // res = new Response<>("Done", null);
    // } catch (UIException e) {
    // res = new Response<>(null, e.getMessage(), e.getNumber());
    // } catch (Exception e) {
    // res = new Response<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }

    // @PostMapping("/submitBid")
    // public String submitBid(@RequestParam String token,
    // @RequestBody SingleBid bid) {
    // Response<String> res;
    // try {
    // purchaseService.submitBid(token, bid);
    // res = new Response<>("done", null);
    // } catch (UIException e) {
    // res = new Response<>(null, e.getMessage(), e.getNumber());
    // } catch (Exception e) {
    // res = new Response<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }

    // @GetMapping("/searchProductInStore")
    // public String searchProductInStore(@RequestParam String token,
    // @RequestParam int storeId,
    // @RequestParam int productId) {
    // Response<String> res;
    // try {
    // String result = purchaseService.searchProductInStore(token, storeId,
    // productId);
    // res = new Response<>(result, null);
    // } catch (UIException e) {
    // res = new Response<>(null, e.getMessage(), e.getNumber());
    // } catch (Exception e) {
    // res = new Response<>(null, e.getMessage(), -1);
    // }
    // return res.toJson();
    // }

}
