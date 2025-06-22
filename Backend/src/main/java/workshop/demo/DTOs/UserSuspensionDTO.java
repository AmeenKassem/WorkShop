package workshop.demo.DTOs;

import java.time.LocalDateTime;

public class UserSuspensionDTO {

    private Integer userId;
    private boolean paused;
    private String userName;
    private LocalDateTime suspensionEndTime;
    private long remainingWhenPaused;

    public UserSuspensionDTO() {
    }

    public UserSuspensionDTO(Integer userId, String userName, boolean paused, LocalDateTime suspensionEndTime, long remainingWhenPaused) {
        this.userId = userId;
        this.paused = paused;
        this.userName = userName;
        this.suspensionEndTime = suspensionEndTime;
        this.remainingWhenPaused = remainingWhenPaused;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isPaused() {
        return paused;
    }

    public LocalDateTime getSuspensionEndTime() {
        return suspensionEndTime;
    }

    public long getRemainingWhenPaused() {
        return remainingWhenPaused;
    }

}
