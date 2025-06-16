package workshop.demo.DataAccessLayer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.User.Guest;

@Repository
public interface GuestJpaRepository extends JpaRepository<Guest, Integer> {

}
