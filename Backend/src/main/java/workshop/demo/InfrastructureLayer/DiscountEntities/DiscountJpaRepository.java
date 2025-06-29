package workshop.demo.InfrastructureLayer.DiscountEntities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Integer> {
    Optional<DiscountEntity> findByName(String name);
    boolean existsByName(String name);
}

