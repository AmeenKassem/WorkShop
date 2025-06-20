package workshop.demo.DomainLayer.UserSuspension;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_suspensions")
public class UserSuspension {

    @Id
    private Integer userId;
    // private long totalDurationMinutes;   
    private long suspensionEndMinutes;      
    private long remainingAtPauseMinutes;   
    private LocalDateTime lastStartTime; 
    private boolean paused;

    public UserSuspension(Integer userId, long durationMinutes) {
        this.userId = userId;
        // this.totalDurationMinutes = durationMinutes;
        this.suspensionEndMinutes = (System.currentTimeMillis() / 60000) + durationMinutes;
        this.paused = false;
        this.lastStartTime = LocalDateTime.now();
    }

    public UserSuspension() {}

    public Integer getUserId() {
        return userId;
    }

    public long getSuspensionEndMinutes() {
        return suspensionEndMinutes;
    }

    public boolean isExpired() {
        return !paused && (System.currentTimeMillis() / 60000) >= suspensionEndMinutes;
    }

    public void pause() {
        if (!paused) {
            remainingAtPauseMinutes = suspensionEndMinutes - (System.currentTimeMillis() / 60000);
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            suspensionEndMinutes = (System.currentTimeMillis() / 60000) + remainingAtPauseMinutes;
            paused = false;
        }
    }

    public long getRemainingMinutes() {
        if (paused) {
            return remainingAtPauseMinutes;
        } else {
            return Math.max(suspensionEndMinutes - (System.currentTimeMillis() / 60000), 0);
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
