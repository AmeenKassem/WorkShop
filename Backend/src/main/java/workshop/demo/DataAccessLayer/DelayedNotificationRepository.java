package workshop.demo.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Notification.DelayedNotification;

@Repository
public interface DelayedNotificationRepository extends JpaRepository<DelayedNotification, Long> {

    List<DelayedNotification> findByUsername(String username);

    void deleteByUsername(String username);
}
