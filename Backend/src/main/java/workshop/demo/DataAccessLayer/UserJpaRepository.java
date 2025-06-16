package workshop.demo.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.RoleOnSystem;

@Repository
public interface UserJpaRepository extends JpaRepository<Registered, Integer> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM registered WHERE username = :username", nativeQuery = true)
    int existsByUsername(@Param("username") String username);

    @Query("SELECT r FROM Registered r WHERE r.username = :username")
    List<Registered> findRegisteredUsersByUsername(@Param("username") String username);

    // @Query("SELECT COUNT(u) > 0 FROM Registered u WHERE u.id = :id AND u.systemRole = 'Admin'")
    // int isAdmin(@Param("id") int id);

}
