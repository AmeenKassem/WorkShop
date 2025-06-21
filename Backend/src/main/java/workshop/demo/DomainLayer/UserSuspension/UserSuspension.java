package workshop.demo.DomainLayer.UserSuspension;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_suspensions")
public class UserSuspension {

    @Id
    private Integer userId;
    // private long totalDurationMinutes;   
    private LocalDateTime suspensionEndTime;  
    private long remainingWhenPaused;   
    private LocalDateTime lastStartTime; 
    private boolean paused;

    public UserSuspension(Integer userId, long durationMinutes) {
        this.userId = userId;
        this.lastStartTime = LocalDateTime.now();
        this.suspensionEndTime = this.lastStartTime.plusMinutes(durationMinutes);
        this.remainingWhenPaused = 0;
        this.paused = false;
    }

    public UserSuspension() {}

    public Integer getUserId() {
        return userId;
    }

    public boolean isExpired() {
        return !paused && LocalDateTime.now().isAfter(suspensionEndTime);
    }

    public void pause() {
        if (!paused) {
            remainingWhenPaused = java.time.Duration.between(LocalDateTime.now(), suspensionEndTime).toMinutes();
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            suspensionEndTime = LocalDateTime.now().plusMinutes(remainingWhenPaused);
            paused = false;
        }
    }

    public long getRemainingMinutes() {
        if (paused) {
            return remainingWhenPaused;
        } else {
            return Math.max(java.time.Duration.between(LocalDateTime.now(), suspensionEndTime).toMinutes(), 0);
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public static void main(String[] args){
        // UserSuspension sus = new UserSuspension(1, Duration.ofSeconds(10));
        // sus.pause();
        // System.out.println("hii");
        // sus.remainingDuration.
    }
}
