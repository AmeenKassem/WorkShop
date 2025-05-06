package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.StoreService;

@RestController
@RequestMapping("/store")
public class StoreController {

    private StoreService storeService;

    public StoreController(Repos repo) {
        this.storeService = new StoreService(repo.storeRepo, repo.notificationRepo, repo.auth, repo.userRepo, repo.orderRepo, repo.sUConnectionRepo);
    }

}
