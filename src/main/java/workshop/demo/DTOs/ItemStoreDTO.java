package workshop.demo.DTOs;

import workshop.demo.DomainLayer.Stock.Product.Category;

public class ItemStoreDTO {

    public int quantity;
    public int price;
    public Category category;
    public int rank;

    public ItemStoreDTO(int quantity, int price, Category category, int rank) {
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.rank = rank;
    }

}
