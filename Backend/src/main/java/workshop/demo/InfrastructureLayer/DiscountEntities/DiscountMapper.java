package workshop.demo.InfrastructureLayer.DiscountEntities;

import workshop.demo.DomainLayer.Store.*;
import java.util.List;
import java.util.stream.Collectors;

public class DiscountMapper {

    public static DiscountEntity toEntity(Discount discount) {
        if (discount instanceof CompositeDiscount comp) {
            CompositeDiscountEntity composite = new CompositeDiscountEntity();
            composite.setName(comp.getName());
            composite.setCondition("None");
            composite.setPercent(0); // handled by subdiscounts

            if (comp instanceof MaxDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.MAX);
            } else {
                throw new IllegalArgumentException("Unsupported composite logic: " + comp.getClass().getSimpleName());
            }

            List<DiscountEntity> children = comp.getDiscounts()
                    .stream()
                    .map(DiscountMapper::toEntity)
                    .collect(Collectors.toList());

            composite.setSubDiscounts(children);
            return composite;

        } else if (discount instanceof VisibleDiscount vd) {
            VisibleDiscountEntity entity = new VisibleDiscountEntity();
            entity.setName(vd.getName());
            entity.setCondition(vd.getCondition().toString());
            entity.setPercent(vd.getPercent());
            return entity;

        } else if (discount instanceof InvisibleDiscount id) {
            InvisibleDiscountEntity entity = new InvisibleDiscountEntity();
            entity.setName(id.getName());
            entity.setCondition(id.getCondition().toString());
            entity.setPercent(id.getPercent());
            return entity;
        }

        throw new IllegalArgumentException("Unknown Discount type: " + discount.getClass());
    }

    public static Discount toDomain(DiscountEntity entity) {
        if (entity instanceof VisibleDiscountEntity vd) {
            return new VisibleDiscount(
                    vd.getName(),
                    vd.getPercent(),
                    DiscountConditions.fromString(vd.getCondition())
            );

        } else if (entity instanceof InvisibleDiscountEntity id) {
            return new InvisibleDiscount(
                    id.getName(),
                    id.getPercent(),
                    DiscountConditions.fromString(id.getCondition())
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
