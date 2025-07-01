package workshop.demo.InfrastructureLayer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.DiscountEntities.DiscountEntity;

@Repository
public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Integer> {

    Optional<DiscountEntity> findByName(String name);

    boolean existsByName(String name);
}
