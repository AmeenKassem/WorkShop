package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.OfferKey;

@Repository
public interface OfferJpaRepository extends JpaRepository<Offer, OfferKey> {

    boolean existsByIdSenderIdAndIdReceiverId(int senderId, int receiverId);

    void deleteByIdStoreId(int storeId);

}
