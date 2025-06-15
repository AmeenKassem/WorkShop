package workshop.demo.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.User.Registered;

@Repository
public interface UserJpaRepository extends JpaRepository<Registered, Integer> {

    
}
