package workshop.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/getreceipts")
    public ResponseEntity<ApiResponse<ReceiptDTO[]>> getReceipts(@RequestParam String token) {
        ApiResponse<ReceiptDTO[]> response;

        try {
            System.out.println("Fetching receipts for user with token: " + token);
            ReceiptDTO[] receipts = orderService.getReceiptDTOsByUser(token).toArray(new ReceiptDTO[0]);
            for (ReceiptDTO receiptDTO : receipts) {
                System.out.println(receiptDTO.getFinalPrice()+"-final price for "+receiptDTO.getStoreName());
            }
            response = new ApiResponse<>(receipts, null);
            return ResponseEntity.ok(response);

        } catch (UIException e) {
            response = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getOrdersByStore")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStore(@RequestParam int storeId) {
        ApiResponse<List<OrderDTO>> res;
        try {
            List<OrderDTO> orders = orderService.getAllOrderByStore(storeId);
            res = new ApiResponse<>(orders, null);
            return ResponseEntity.ok(res);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
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


    @GetMapping("/viewAllPurchaseHistory")
    public ResponseEntity<ApiResponse<List<PurchaseHistoryDTO>>> viewAllPurchaseHistory(@RequestParam String token) {
        ApiResponse<List<PurchaseHistoryDTO>> response;
        try {
            List<PurchaseHistoryDTO> history = orderService.viewPurchaseHistory(token);
            response = new ApiResponse<>(history, null);
            return ResponseEntity.ok(response);
        } catch (UIException e) {
            response = new ApiResponse<>(null, e.getMessage(), e.getNumber());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response = new ApiResponse<>(null, e.getMessage(), -1);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
