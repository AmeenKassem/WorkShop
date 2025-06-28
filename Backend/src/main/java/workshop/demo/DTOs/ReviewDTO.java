package workshop.demo.DTOs;

import workshop.demo.DomainLayer.Review.Review;

public class ReviewDTO {

    private int reviewerId;
    private String name;
    private String reviewMsg;

    public ReviewDTO(int reviewerId, String name, String reviewMsg) {
        this.reviewerId = reviewerId;
        this.name = name;
        this.reviewMsg = reviewMsg;
    }

    public ReviewDTO(Review review) {
        this.reviewerId = review.getReviewerId();
        this.name = review.getName();
        this.reviewMsg = review.getReviewMsg();
    }

    public ReviewDTO() {
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public String getName() {
        if(name==null) return "Guest";
        return name;
    }

    public String getReviewMsg() {
        return reviewMsg;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReviewMsg(String reviewMsg) {
        this.reviewMsg = reviewMsg;
    }
}
