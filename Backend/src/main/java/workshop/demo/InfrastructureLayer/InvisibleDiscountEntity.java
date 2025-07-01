package workshop.demo.InfrastructureLayer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import workshop.demo.DomainLayer.DiscountEntities.DiscountEntity;

@Entity
@DiscriminatorValue("INVISIBLE")
public class InvisibleDiscountEntity extends DiscountEntity {
    // No extra fields required
}
