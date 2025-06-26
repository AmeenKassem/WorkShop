package workshop.demo.InfrastructureLayer.DiscountEntities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("COMPOSITE")
public class CompositeDiscountEntity extends DiscountEntity {

    public enum Logic {
        AND, OR, MAX, XOR, MULTIPLY
    }

    @Enumerated(EnumType.STRING)
    private Logic logic;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "composite_sub_discounts",
            joinColumns = @JoinColumn(name = "parent_id"),
            inverseJoinColumns = @JoinColumn(name = "child_id")
    )
    private List<DiscountEntity> subDiscounts = new ArrayList<>();

    public Logic getLogic() { return logic; }
    public void setLogic(Logic logic) { this.logic = logic; }

    public List<DiscountEntity> getSubDiscounts() { return subDiscounts; }
    public void setSubDiscounts(List<DiscountEntity> subDiscounts) { this.subDiscounts = subDiscounts; }
}
