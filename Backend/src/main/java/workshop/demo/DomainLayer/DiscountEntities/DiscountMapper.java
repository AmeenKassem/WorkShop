package workshop.demo.DomainLayer.DiscountEntities;

import java.util.List;
import java.util.stream.Collectors;

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

public class DiscountMapper {

    public static DiscountEntity toEntity(Discount discount) {
        if (discount instanceof CompositeDiscount comp) {
            CompositeDiscountEntity composite = new CompositeDiscountEntity();
            composite.setName(comp.getName());
            composite.setCondition("None");
            composite.setPercent(0); // handled by subdiscounts

            if (comp instanceof MaxDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.MAX);
            } else if (comp instanceof AndDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.AND);
            } else if (comp instanceof OrDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.OR);
            } else if (comp instanceof XorDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.XOR);
            } else if (comp instanceof MultiplyDiscount) {
                composite.setLogic(CompositeDiscountEntity.Logic.MULTIPLY);
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
            entity.setCondition(vd.getConditionString());
            entity.setPercent(vd.getPercent());
            return entity;

        } else if (discount instanceof InvisibleDiscount id) {
            InvisibleDiscountEntity entity = new InvisibleDiscountEntity();
            entity.setName(id.getName());
            entity.setCondition(id.getConditionString());
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
