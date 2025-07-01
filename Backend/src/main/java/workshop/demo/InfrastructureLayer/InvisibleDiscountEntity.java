package workshop.demo.InfrastructureLayer.DiscountEntities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INVISIBLE")
public class InvisibleDiscountEntity extends DiscountEntity {
    // No extra fields required
}
