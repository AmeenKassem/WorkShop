package workshop.demo.DTOs;

import java.util.List;

public class CreateDiscountDTO {

    public enum Type {
        VISIBLE, INVISIBLE
    }

    public enum Logic {
        SINGLE, AND, OR, MAX, XOR
    }

    private String name;
    private double percent;
    private Type type;
    private String condition; // e.g. "CATEGORY:DAIRY", "TOTAL>100", or null
    private Logic logic = Logic.SINGLE; // default to simple discount
    private List<CreateDiscountDTO> subDiscounts;

    public String getName() {
        return name;
    }

    public double getPercent() {
        return percent;
    }

    public Type getType() {
        return type;
    }

    public String getCondition() {
        return condition;
    }

    public Logic getLogic() {
        return logic;
    }

    public List<CreateDiscountDTO> getSubDiscounts() {
        return subDiscounts;
    }
}
