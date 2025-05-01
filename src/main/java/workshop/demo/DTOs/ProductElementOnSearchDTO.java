package workshop.demo.DTOs;

public class ProductElementOnSearchDTO {
    public int storeId;//if the user click on the product we have to send http request for adding product to user cart
    public int productId;
    public SpecialType type;
    public int specialTypeId;//random,bid or auction . 
    //will be visible to user : 
    public String productName;
    public String productDesc;
    public Category productCategory;
    public double productPrice;
    public String storeName;
    public double rating;
}
