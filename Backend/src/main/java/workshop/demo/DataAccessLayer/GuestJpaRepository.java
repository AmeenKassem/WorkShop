package workshop.demo.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.User.Guest;

@Repository
public interface GuestJpaRepository extends JpaRepository<Guest, Integer> {
}
