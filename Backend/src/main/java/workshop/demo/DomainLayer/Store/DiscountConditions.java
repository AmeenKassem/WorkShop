package workshop.demo.DomainLayer.Store;

import java.util.function.Predicate;

public class DiscountConditions {

    public static Predicate<DiscountScope> fromString(String condition) {
        if (condition == null || condition.isBlank())
            return scope -> true; // no condition = always applicable

        if (condition.startsWith("CATEGORY:")) {
            String category = condition.substring("CATEGORY:".length()).trim();
            return scope -> scope.containsCategory(category);
        }

        if (condition.startsWith("TOTAL>")) {
            double threshold = Double.parseDouble(condition.substring("TOTAL>".length()).trim());
            return scope -> scope.getTotalPrice() > threshold;
        }

        if (condition.startsWith("QUANTITY>")) {
            String[] parts = condition.split(">");
            int minQty = Integer.parseInt(parts[1].trim());
            return scope -> scope.getTotalQuantity() >= minQty;
        }

        if (condition.startsWith("ITEM:")) {
            int itemId = Integer.parseInt(condition.substring("ITEM:".length()).trim());
            return scope -> scope.containsItem(itemId);
        }
        if (condition.startsWith("STORE:")) {
    int storeId = Integer.parseInt(condition.substring("STORE:".length()).trim());
    return scope -> scope.containsStore(storeId);
}


        // fallback: never apply
        return scope -> false;
    }
}