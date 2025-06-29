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
    private String condition; // e.g. "CATEGORY:DAIRY", "TOTAL>100", or null
    private Logic logic;// default to simple discount
    private List<CreateDiscountDTO> subDiscounts;
    private String coupon;

    public CreateDiscountDTO(String name, double percent, Type type, String condition, Logic logic, List<CreateDiscountDTO> subDiscounts) {
        this.name = name;
        this.percent = percent;
        this.type = type;
        this.condition = condition;
        this.logic = logic;
        this.subDiscounts = subDiscounts;
    }

    public CreateDiscountDTO() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name){ this.name =name;}

    public double getPercent() {
        return percent;
    }
    public void setPercent(double percent){ this.percent=percent;}

    public Type getType() {
        return type;
    }
    public void setType(Type type){this.type=type;}
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition){ this.condition=condition;}
    public Logic getLogic() {
        return logic;
    }
    public void setLogic(Logic logic){this.logic=logic;}

    public List<CreateDiscountDTO> getSubDiscounts() {
        return subDiscounts;
    }
    public void setSubDiscounts(List<CreateDiscountDTO> subDiscounts){
        this.subDiscounts=subDiscounts;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }
}
