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
    public ResponseEntity<ApiResponse<ReceiptDTO[]>> getReceipts(@RequestParam String token) {
        ApiResponse<ReceiptDTO[]> response;

        try {
            System.out.println("Fetching receipts for user with token: " + token);
            ReceiptDTO[] receipts = orderService.getReceiptDTOsByUser(token).toArray(new
            ReceiptDTO[0]);
            response = new ApiResponse<>(receipts, null);
            return ResponseEntity.ok(response);
        
        } catch (UIException e) {
            response = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            return ResponseEntity.badRequest().body(response);
        } 
        catch (Exception e) {
            response = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(500).body(response);
        }
    }

}
