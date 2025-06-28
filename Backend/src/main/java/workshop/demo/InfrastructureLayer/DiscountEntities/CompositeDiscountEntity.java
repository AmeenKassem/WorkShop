package workshop.demo.InfrastructureLayer.DiscountEntities;

import jakarta.persistence.*;
import workshop.demo.DomainLayer.Store.*;

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

    public Logic getLogic() {
        return logic;
    }

    public void setLogic(Logic logic) {
        this.logic = logic;
    }

    public List<DiscountEntity> getSubDiscounts() {
        return subDiscounts;
    }

    public void setSubDiscounts(List<DiscountEntity> subDiscounts) {
        this.subDiscounts = subDiscounts;
    }

    public static Discount toDomain(DiscountEntity entity) {
        if (entity instanceof VisibleDiscountEntity vd) {
            return new VisibleDiscount(
                    vd.getName(),
                    vd.getPercent(),
                    DiscountConditions.fromString(vd.getCondition()),
                    vd.getCondition()
            );

        } else if (entity instanceof InvisibleDiscountEntity id) {
            return new InvisibleDiscount(
                    id.getName(),
                    id.getPercent(),
                    DiscountConditions.fromString(id.getCondition()),
                    id.getCondition()
            );

        } else if (entity instanceof CompositeDiscountEntity comp) {
            CompositeDiscount result;

            switch (comp.getLogic()) {
                case MAX -> result = new MaxDiscount(comp.getName());
                case AND -> result = new AndDiscount(comp.getName());
                case OR -> result = new OrDiscount(comp.getName());
                case XOR -> result = new XorDiscount(comp.getName());
                case MULTIPLY -> result = new MultiplyDiscount(comp.getName());
                default -> throw new IllegalArgumentException("Unsupported logic: " + comp.getLogic());
            }

            for (DiscountEntity sub : comp.getSubDiscounts()) {
                result.addDiscount(toDomain(sub));
            }

            return result;
        }

        throw new IllegalArgumentException("Unknown DiscountEntity type: " + entity.getClass());
    }
}
