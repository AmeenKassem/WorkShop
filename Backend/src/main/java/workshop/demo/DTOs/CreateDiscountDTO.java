package workshop.demo.DTOs;

import java.util.List;

public class CreateDiscountDTO {

    public enum Type {
        VISIBLE, INVISIBLE
    }

    public enum Logic {
        SINGLE, AND, OR, MAX, XOR,MULTIPLY
    }

    private String name;
    private double percent;
    private Type type;
    private String condition; // e.g. "CATEGORY:DAIRY", "TOTAL>100", or null : "CATEGORY:DIARY ^ TOTAL>100 OR ..."
    private Logic logic ;// default to simple discount
    private List<CreateDiscountDTO> subDiscounts;

   
    public CreateDiscountDTO(String name, double percent, Type type, String condition, Logic logic, List<CreateDiscountDTO> subDiscounts) {
    this.name = name;
    this.percent = percent;
    this.type = type;
    this.condition = condition;
    this.logic = logic ;
    this.subDiscounts = subDiscounts;
}
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
