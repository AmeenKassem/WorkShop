package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.OrderService;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private OrderService orderService;

    public HistoryController(Repos repo) {
        this.orderService = new OrderService(repo.orderRepo, repo.storeRepo);
    }
}
