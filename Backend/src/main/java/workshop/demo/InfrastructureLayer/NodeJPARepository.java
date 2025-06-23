package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.NodeKey;

@Repository
public interface NodeJPARepository extends JpaRepository<Node, NodeKey> {

    @Modifying
    @Query("DELETE FROM Node n WHERE n.key.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") int storeId);
}
