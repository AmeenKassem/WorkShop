package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class OfferKey {

    private int storeId;
    private int senderId;
    private int receiverId;

    public OfferKey() {
    }

    public OfferKey(int storeId, int senderId, int receiverId) {
        this.storeId = storeId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    // Getters
    public int getStoreId() {
        return storeId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OfferKey)) {
            return false;
        }
        OfferKey key = (OfferKey) o;
        return storeId == key.storeId
                && senderId == key.senderId
                && receiverId == key.receiverId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, senderId, receiverId);
    }
}
