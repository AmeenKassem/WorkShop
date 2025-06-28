package workshop.demo.InfrastructureLayer.DiscountEntities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("VISIBLE")
public class VisibleDiscountEntity extends DiscountEntity {
    // No extra fields required
}
