package workshop.demo.InfrastructureLayer;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Store.PolicyManager;

@Repository
public interface PolicyManagerRepository extends JpaRepository<PolicyManager, Integer> {
}
