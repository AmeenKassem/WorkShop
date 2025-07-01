package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import workshop.demo.DomainLayer.Store.Store;

public interface IStoreRepoDB extends JpaRepository<Store, Integer> {

    @Modifying
    @Query("UPDATE Store s SET s.active = false WHERE s.storeId = :storeId")
    void deactivateStore(@Param("storeId") int storeId);
    @Modifying
    @Query("UPDATE Store s SET s.active = true WHERE s.storeId = :storeId")
    void activateStore(@Param("storeId") int storeId);
}
