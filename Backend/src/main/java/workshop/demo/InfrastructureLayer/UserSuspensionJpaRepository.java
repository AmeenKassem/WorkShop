package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;

import workshop.demo.DomainLayer.UserSuspension.UserSuspension;

public interface UserSuspensionJpaRepository extends JpaRepository<UserSuspension, Integer> {

}
