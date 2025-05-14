package workshop.demo.DTOs;

public class StoreDTO {

    private int id;
    private String storeName;
    private String stroeCategory;
    //please give me the final rank as I implemented it 
    //with rounding so I can show it in 5 stars
    private int finalRank;
    // Getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return storeName;
    }

    public double getRank() {
        return finalRank;
    }

    public String getCategory() {
        return stroeCategory;
    }

}
