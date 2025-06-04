package workshop.demo.InfrastructureLayer;

import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepoDB extends JpaRepository<Guest, Integer> {
    
}
