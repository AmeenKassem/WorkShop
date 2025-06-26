package workshop.demo.InfrastructureLayer.DiscountEntities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Integer> {
    DiscountEntity findByName(String name);
    boolean existsByName(String name);
}
