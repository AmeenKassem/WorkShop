package workshop.demo.DTOs;

public class UserSpecialItemCart {

    public int storeId;
    public int specialId;
    public int bidId;
    public SpecialType type;
    public  String Pname;

    // Constructor
    public UserSpecialItemCart(int storeId, int specialId, int bidId, SpecialType type,String Pname) {
        this.storeId = storeId;
        this.specialId = specialId;
        this.bidId = bidId;
        this.type = type;
        this.Pname=Pname;
    }

    public UserSpecialItemCart() {
    }

    // equals() method - compares only storeId, specialId, and type
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserSpecialItemCart that = (UserSpecialItemCart) obj;
        return storeId == that.storeId
                && specialId == that.specialId
                && type == that.type;
    }
}
