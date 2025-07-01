package workshop.demo.DomainLayer.DiscountEntities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import workshop.demo.DomainLayer.Store.AndDiscount;
import workshop.demo.DomainLayer.Store.CompositeDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountConditions;
import workshop.demo.DomainLayer.Store.InvisibleDiscount;
import workshop.demo.DomainLayer.Store.MaxDiscount;
import workshop.demo.DomainLayer.Store.MultiplyDiscount;
import workshop.demo.DomainLayer.Store.OrDiscount;
import workshop.demo.DomainLayer.Store.VisibleDiscount;
import workshop.demo.DomainLayer.Store.XorDiscount;
import workshop.demo.InfrastructureLayer.InvisibleDiscountEntity;

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
                case MAX ->
                    result = new MaxDiscount(comp.getName());
                case AND ->
                    result = new AndDiscount(comp.getName());
                case OR ->
                    result = new OrDiscount(comp.getName());
                case XOR ->
                    result = new XorDiscount(comp.getName());
                case MULTIPLY ->
                    result = new MultiplyDiscount(comp.getName());
                default ->
                    throw new IllegalArgumentException("Unsupported logic: " + comp.getLogic());
            }

            for (DiscountEntity sub : comp.getSubDiscounts()) {
                result.addDiscount(toDomain(sub));
            }

            return result;
        }

        throw new IllegalArgumentException("Unknown DiscountEntity type: " + entity.getClass());
    }
}
