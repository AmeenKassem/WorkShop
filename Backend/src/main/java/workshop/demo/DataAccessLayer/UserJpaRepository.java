package workshop.demo.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.User.Registered;

@Repository
public interface UserJpaRepository extends JpaRepository<Registered, Integer> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM registered WHERE username = :username", nativeQuery = true)
    int existsByUsername(@Param("username") String username);

}
