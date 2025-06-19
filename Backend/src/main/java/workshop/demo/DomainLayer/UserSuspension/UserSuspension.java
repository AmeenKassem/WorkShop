package workshop.demo.DomainLayer.UserSuspension;

import java.time.Duration;
import java.time.LocalDateTime;

public class UserSuspension {

    private final Integer userId;
    private final Duration totalDuration;
    private Duration remainingDuration;
    private LocalDateTime lastStartTime;
    private boolean paused;

    public UserSuspension(Integer userId, Duration duration) {
        this.userId = userId;
        this.totalDuration = duration;
        this.remainingDuration = duration;
        this.lastStartTime = LocalDateTime.now();
        this.paused = false;
    }

    public Integer getUserId() {
        return userId;
    }

    public LocalDateTime getExpectedEndTime() {
        if (paused) {
            return LocalDateTime.now().plus(remainingDuration);
        } else {
            Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
            return LocalDateTime.now().plus(remainingDuration.minus(elapsed));
        }
    }

    public boolean isExpired() {
        if (paused) return false;

        Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
        remainingDuration = remainingDuration.minus(elapsed);
        lastStartTime = LocalDateTime.now();
        return remainingDuration.isZero() || remainingDuration.isNegative();
    }

    public void pause() {
        if (!paused) {
            Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
            remainingDuration = remainingDuration.minus(elapsed);
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            lastStartTime = LocalDateTime.now();
            paused = false;
        }
    }

    public Duration getRemainingDuration() {
        if (paused) {
            return remainingDuration;
        } else {
            Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
            return remainingDuration.minus(elapsed);
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public static void main(String[] args){
        UserSuspension sus = new UserSuspension(1, Duration.ofSeconds(10));
        sus.pause();
        // System.out.println("hii");
        // sus.remainingDuration.
    }
}
