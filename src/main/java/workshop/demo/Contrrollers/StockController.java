package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.*;
import workshop.demo.ApplicationLayer.StockService;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService stockService;

    public StockController(Repos repos) {
        this.stockService = new StockService(repos.stockrepo, repos.userRepo, repos.auth, repos.productFilter);
    }
}