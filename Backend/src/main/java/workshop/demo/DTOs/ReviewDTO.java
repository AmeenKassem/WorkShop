package workshop.demo.DTOs;

public class ReviewDTO {

    private int reviewerId;
    private String name;
    private String reviewMsg;

    public ReviewDTO(int reviewerId, String name, String reviewMsg) {
        this.reviewerId = reviewerId;
        this.name = name;
        this.reviewMsg = reviewMsg;
    }

    public ReviewDTO() {
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public String getName() {
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
