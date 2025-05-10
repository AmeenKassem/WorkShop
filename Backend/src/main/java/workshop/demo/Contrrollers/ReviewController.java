package workshop.demo.Contrrollers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.ReviewService;

@RestController
@RequestMapping("/Review")
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(Repos repo) {
        this.reviewService = new ReviewService(repo.reviewRepo, repo.auth, repo.userRepo, repo.storeRepo);
    }
}
