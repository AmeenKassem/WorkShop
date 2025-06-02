package workshop.demo.Controllers;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import workshop.demo.ApplicationLayer.ReviewService;
import workshop.demo.DTOs.ReviewDTO;
import workshop.demo.DomainLayer.Exceptions.UIException;

@RestController
@RequestMapping("/api/Review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(Repos repo) {
        this.reviewService = new ReviewService(repo.reviewRepo, repo.auth, repo.userRepo, repo.storeRepo, repo.stockrepo);
    }

    @PostMapping("/addToProduct")
    public ResponseEntity<?> addReviewToProduct(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam int productId,
            @RequestParam String review) {
        try {
            String decodedReview = URLDecoder.decode(review, StandardCharsets.UTF_8);
            reviewService.AddReviewToProduct(token, storeId, productId, decodedReview);
            return ResponseEntity.ok(new ApiResponse<>("review added to product successfully", null));

        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }

    }

    @PostMapping("/addToStore")
    public ResponseEntity<?> addReviewToStore(@RequestParam String token,
            @RequestParam int storeId,
            @RequestParam String review) {
        try {
            String decodedReview = URLDecoder.decode(review, StandardCharsets.UTF_8);
            reviewService.AddReviewToStore(token, storeId, decodedReview);
            return ResponseEntity.ok(new ApiResponse<>("review added to product successfully", null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getProductReviews")
    public ResponseEntity<?> getProductReviews(@RequestParam int storeId,
            @RequestParam int productId) {

        try {
            List<ReviewDTO> reviews = reviewService.getReviewsForProduct(storeId, productId);
            return ResponseEntity.ok(new ApiResponse<>(reviews, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

    @GetMapping("/getStoreReviews")
    public ResponseEntity<?> getStoreReviews(@RequestParam int storeId) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsForStore(storeId);
            return ResponseEntity.ok(new ApiResponse<>(reviews, null));
        } catch (UIException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, ex.getMessage(), ex.getNumber()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, e.getMessage(), -1));
        }
    }

}
