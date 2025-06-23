package workshop.demo.Controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.PaymentDetails;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.SupplyDetails;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/purchase")
public class PurcheseContoller {

    @Autowired
    private PurchaseService purchaseService;

    @ModelAttribute
    public void beforeEveryRequest(HttpServletRequest request) {
        System.out.println("PurchaseController");
    }

    // cant use the RequestBody for the PaymentDetails and the SupplyDetails
    // because they are not in the same class
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<ReceiptDTO[]>> buyGuestCart(@RequestParam String token,
            @RequestParam String paymentJson,
            @RequestParam String supplyJson, @RequestParam(required = false) String coupon) {
        ApiResponse<ReceiptDTO[]> res;
        try {
            // CouponContext.set(coupon);
            System.out.println("payment received for guest");
            String decodedPaymentJson = URLDecoder.decode(paymentJson, StandardCharsets.UTF_8);
            String decodedSupplyJson = URLDecoder.decode(supplyJson, StandardCharsets.UTF_8);
            PaymentDetails paymentdetails = PaymentDetails.getPaymentDetailsFromJSON(decodedPaymentJson);
            SupplyDetails supplydetails = SupplyDetails.getSupplyDetailsFromJSON(decodedSupplyJson);

            ReceiptDTO[] receipts = purchaseService.buyGuestCart(token, paymentdetails, supplydetails);
            res = new ApiResponse<>(receipts, null);
            return ResponseEntity.ok(res);
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res);
        } finally {
            // CouponContext.clear();
        }
    }

    @PostMapping("/registered")
    public ResponseEntity<ApiResponse<ReceiptDTO[]>> buyRegisteredCart(@RequestParam String token,
            @RequestParam String paymentJson,
            @RequestParam String supplyJson, @RequestParam(required = false) String coupon) {
        ApiResponse<ReceiptDTO[]> res;
        try {
            // CouponContext.set(coupon);
            System.out.println("payment received for registered");
            String decodedPaymentJson = URLDecoder.decode(paymentJson, StandardCharsets.UTF_8);
            String decodedSupplyJson = URLDecoder.decode(supplyJson, StandardCharsets.UTF_8);
            PaymentDetails paymentdetails = PaymentDetails.getPaymentDetailsFromJSON(decodedPaymentJson);
            SupplyDetails supplydetails = SupplyDetails.getSupplyDetailsFromJSON(decodedSupplyJson);
            // // Create products
            // ReceiptProduct p1 = new ReceiptProduct("Laptop", "TechStore", 1, 1200, 1,
            // Category.Electronics);
            // ReceiptProduct p2 = new ReceiptProduct("Chair", "HomeMart", 2, 150, 2,
            // Category.Furniture);
            // ReceiptProduct p3 = new ReceiptProduct("T-Shirt", "FashionHub", 3, 25, 3,
            // Category.Clothing);

            // // Create ReceiptDTO instances
            // ReceiptDTO r1 = new ReceiptDTO("TechStore", "2025-06-01", List.of(p1), 1200);
            // ReceiptDTO r2 = new ReceiptDTO("HomeMart", "2025-06-02", List.of(p2), 300);
            // ReceiptDTO r3 = new ReceiptDTO("FashionHub", "2025-06-03", List.of(p3), 75);
            // // Create array of ReceiptDTO
            // ReceiptDTO[] receipts = new ReceiptDTO[] { r1, r2, r3 };
            ReceiptDTO[] receipts = purchaseService.buyRegisteredCart(token, paymentdetails, supplydetails);
            res = new ApiResponse<>(receipts, null);
            // Return 200 OK for successful operations
            return ResponseEntity.ok(res);
        } catch (UIException e) {
            res = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            // Return 400 Bad Request for client errors
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            // Return 500 Internal Server Error for server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(res);
        } finally {
            // CouponContext.clear();
        }
    }

    @PostMapping("/participateRandom")
    public ResponseEntity<?> participateInRandom(@RequestParam String token,
            @RequestParam int randomId,
            @RequestParam int storeId,
            @RequestParam double amountPaid,
            @RequestBody PaymentDetails payment) {
        try {
            ParticipationInRandomDTO result = purchaseService.participateInRandom(token, randomId, storeId, amountPaid,
                    payment);
            return ResponseEntity.ok(new ApiResponse<>(result, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @PostMapping("/finalizeSpecialCart")
    public String finalizeSpecialCart(@RequestParam String token,
            @RequestParam String paymentJson,
            @RequestParam String supplyJson) {
        ApiResponse<ReceiptDTO[]> res;

        try {
            System.out.println("Finalizing special cart purchase");
            // Decode the JSON strings
            String decodedPaymentJson = URLDecoder.decode(paymentJson, StandardCharsets.UTF_8);
            String decodedSupplyJson = URLDecoder.decode(supplyJson, StandardCharsets.UTF_8);

            // Use static methods to parse the JSON
            PaymentDetails paymentDetails = PaymentDetails.getPaymentDetailsFromJSON(decodedPaymentJson);
            SupplyDetails supplyDetails = SupplyDetails.getSupplyDetailsFromJSON(decodedSupplyJson);

            // Finalize special cart purchase
            ReceiptDTO[] receipts = purchaseService.finalizeSpecialCart(token,
                    paymentDetails, supplyDetails);

            // // Create products
            // ReceiptProduct p1 = new ReceiptProduct("Laptop", "TechStore", 1, 1200, 1,
            // Category.Electronics);
            // ReceiptProduct p2 = new ReceiptProduct("Chair", "HomeMart", 2, 150, 2,
            // Category.Furniture);
            // ReceiptProduct p3 = new ReceiptProduct("T-Shirt", "FashionHub", 3, 25, 3,
            // Category.Clothing);
            // // Create ReceiptDTO instances
            // ReceiptDTO r1 = new ReceiptDTO("TechStore", "2025-06-01", List.of(p1), 1200);
            // ReceiptDTO r2 = new ReceiptDTO("HomeMart", "2025-06-02", List.of(p2), 300);
            // ReceiptDTO r3 = new ReceiptDTO("FashionHub", "2025-06-03", List.of(p3), 75);
            // // Create array of ReceiptDTO
            // ReceiptDTO[] receipts = new ReceiptDTO[] { r1, r2, r3 };
            res = new ApiResponse<>(receipts, null);
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
