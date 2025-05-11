package workshop.demo.DomainLayer.UserSuspension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class UserSuspension {

    private final Integer userId;
    private final Duration totalDuration;
    private Duration remainingDuration;
    private LocalDateTime lastStartTime;
    private boolean paused;

    private final ReentrantLock lock = new ReentrantLock(); // lock for thread-safety

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
        lock.lock();
        try {
            if (paused) {
                return LocalDateTime.now().plus(remainingDuration);
            } else {
                Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
                return LocalDateTime.now().plus(remainingDuration.minus(elapsed));
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isExpired() {
        lock.lock();
        try {
            if (paused) return false;

            Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
            remainingDuration = remainingDuration.minus(elapsed);
            lastStartTime = LocalDateTime.now();
            return remainingDuration.isZero() || remainingDuration.isNegative();
        } finally {
            lock.unlock();
        }
    }

    public void pause() {
        lock.lock();
        try {
            if (!paused) {
                Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
                remainingDuration = remainingDuration.minus(elapsed);
                paused = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public void resume() {
        lock.lock();
        try {
            if (paused) {
                lastStartTime = LocalDateTime.now();
                paused = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public Duration getRemainingDuration() {
        lock.lock();
        try {
            if (paused) {
                return remainingDuration;
            } else {
                Duration elapsed = Duration.between(lastStartTime, LocalDateTime.now());
                return remainingDuration.minus(elapsed);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isPaused() {
        lock.lock();
        try {
            return paused;
        } finally {
            lock.unlock();
        }
    }
}
