package workshop.demo.Contrrollers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.Response;


import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/store")
public class StoreController {

    private StoreService storeService;
        private OrderService orderService;


    public StoreController(Repos repo) {
        this.storeService = new StoreService(repo.storeRepo, repo.notificationRepo, repo.auth, repo.userRepo, repo.orderRepo, repo.sUConnectionRepo,repo.stockrepo);
    }


    @GetMapping("/stam")
    public String stam(){
        return "aaa";
    }
    @GetMapping("/storeOrders")
public String getAllOrdersByStore(@RequestParam int storeId,
                                  @RequestParam String token) {
    Response<List<OrderDTO>> res;
    try {
        // You might want to validate admin here
        List<OrderDTO> orders = orderService.getAllOrderByStore(storeId);
        res = new Response<>(orders, null);
    } catch (UIException ex) {
        res = new Response<>(null, ex.getMessage(), ex.getNumber());
    } catch (Exception e) {
        res = new Response<>(null, e.getMessage(), -1);
    }
    return res.toJson();
}

}
