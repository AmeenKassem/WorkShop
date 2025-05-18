package workshop.demo.Contrrollers;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private OrderService orderService;

    public HistoryController(Repos repo) {
        this.orderService = new OrderService(repo.orderRepo, repo.storeRepo, repo.auth, repo.userRepo);
    }

    @GetMapping("/getreceipts")
    public ResponseEntity<?> getReceipts(@RequestParam String token) {
        ApiResponse<ReceiptDTO[]> response;

        try {
            System.out.println("Fetching receipts for user with token: " + token);
            ReceiptProduct product1 = new ReceiptProduct(
                    "Wireless Mouse",
                    Category.ELECTRONICS,
                    "Ergonomic wireless mouse with USB receiver",
                    "TechStore",
                    2,
                    2500 // price in cents or smallest unit
            );

            ReceiptProduct product2 = new ReceiptProduct(
                    "Fantasy Novel",
                    Category.HOME,
                    "An epic fantasy novel with dragons and magic",
                    "BookWorld",
                    1,
                    1500);

            List<ReceiptProduct> products = Arrays.asList(product1, product2);
            ReceiptDTO receipt = new ReceiptDTO("TechStore", "2023-10-01", products, 4000);
            ReceiptDTO[] receipts = new ReceiptDTO[] { receipt, receipt };
            // ReceiptDTO[] receipts = orderService.getReceiptDTOsByUser(token).toArray(new
            // ReceiptDTO[0]);
            response = new ApiResponse<>(receipts, null);
            return ResponseEntity.ok(response.toJson());
        }
        // } catch (UIException e) {
        //     response = new ApiResponse<>(null, e.getMessage(), e.getNumber());
        //     return ResponseEntity.badRequest().body(response.toJson());
        // } 
        catch (Exception e) {
            response = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(500).body(response.toJson());
        }
    }

}
