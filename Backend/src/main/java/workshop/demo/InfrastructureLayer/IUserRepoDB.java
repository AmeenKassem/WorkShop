package workshop.demo.InfrastructureLayer;

import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepoDB extends JpaRepository<Guest, Integer> {
    
    @Query(value = "SELECT CASE WHEN is_admin = true THEN true ELSE false END FROM guest WHERE id = :userId", nativeQuery = true)
    boolean checkAdmin(@Param("userId") int userId);
    
}
