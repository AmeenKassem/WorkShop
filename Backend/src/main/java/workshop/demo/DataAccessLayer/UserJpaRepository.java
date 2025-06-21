package workshop.demo.DataAccessLayer;

import java.util.List;
import java.util.Optional;

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

    @Query("SELECT r FROM Registered r WHERE r.username = :username")
    Optional<Registered> findByUsername(@Param("username") String username);


    @Query("SELECT r FROM Registered r WHERE r.id = :id AND r.username = :username")
    Optional<Registered> findByIdAndUsername(@Param("id") int id, @Param("username") String username);

    List<Registered> findAllBySystemRole(RoleOnSystem role);

    @Query("SELECT COUNT(r) FROM Registered r WHERE r.isOnline = true")
    long countOnlineUsers();

    void deleteByUsername(String username);

    List<Registered> findByIsOnlineTrue();

    // ✅ Explicit find by ID (optional, already exists by default)
    @Query("SELECT r FROM Registered r WHERE r.id = :id")
    Optional<Registered> findByIdExplicit(@Param("id") int id);
}
