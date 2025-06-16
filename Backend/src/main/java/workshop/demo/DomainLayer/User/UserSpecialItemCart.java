package workshop.demo.DomainLayer.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import workshop.demo.DTOs.SpecialType;

@Entity
public class UserSpecialItemCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int specialCartId;
    public int storeId;
    public int specialId;
    public int bidId;
     @Enumerated(EnumType.STRING)
    public SpecialType type;

    @ManyToOne
    @JoinColumn(name = "registered_id")
    public Registered user;

    // Constructor
    public UserSpecialItemCart(int storeId, int specialId, int bidId, SpecialType type) {
        this.storeId = storeId;
        this.specialId = specialId;
        this.bidId = bidId;
        this.type = type;
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
