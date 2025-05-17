package workshop.demo.Contrrollers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/Review")
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(Repos repo) {
        this.reviewService = new ReviewService(repo.reviewRepo, repo.auth, repo.userRepo, repo.storeRepo);
    }

    @PostMapping("/addToProduct")
    public String addReviewToProduct(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam int productId,
            @RequestParam String review) {
        ApiResponse<String> res;
        try {
            reviewService.AddReviewToProduct(token, storeId, productId, review);
            res = new ApiResponse<>("review added to product successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @PostMapping("/addToStore")
    public String addReviewToStore(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam String review) {
        ApiResponse<String> res;
        try {
            reviewService.AddReviewToStore(token, storeId, review);
            res = new ApiResponse<>("review added to store successfully", null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getProductReviews")
    public String getProductReviews(@RequestParam int storeId,
            @RequestParam int productId) {
        ApiResponse<List<String>> res;
        try {
            List<String> reviews = reviewService.getReviewsForProduct(storeId, productId);
            res = new ApiResponse<>(reviews, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

    @GetMapping("/getStoreReviews")
    public String getStoreReviews(@RequestParam int storeId) {
        ApiResponse<List<String>> res;
        try {
            List<String> reviews = reviewService.getReviewsForStore(storeId);
            res = new ApiResponse<>(reviews, null);
        } catch (UIException ex) {
            res = new ApiResponse<>(null, ex.getMessage(), ex.getNumber());
        } catch (Exception e) {
            res = new ApiResponse<>(null, e.getMessage(), -1);
        }
        return res.toJson();
    }

}
