package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.InfrastructureLayer.DiscountEntities.DiscountEntity;

import java.util.Optional;

@Repository
public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Integer> {
    Optional<DiscountEntity> findByName(String name);
    boolean existsByName(String name);
}

