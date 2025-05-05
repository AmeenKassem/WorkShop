package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.PurchaseService;

@RestController
@RequestMapping("/Purchese")
public class PurcheseContoller {

    private PurchaseService purchaseService;

    public PurcheseContoller(Repos repo) {
        this.purchaseService = new PurchaseService(repo.auth, repo.stockrepo, repo.storeRepo, repo.userRepo, repo.purchaseRepo, repo.orderRepo,repo.paymentService, repo.supplyService);
    }
}
