package workshop.demo.DTOs;

public class ItemStoreDTO {

    private int id;
    public int quantity;
    public int price;
    public Category category;
    public int rank;

    public ItemStoreDTO(int id, int quantity, int price, Category category, int rank) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.rank = rank;
    }

}
