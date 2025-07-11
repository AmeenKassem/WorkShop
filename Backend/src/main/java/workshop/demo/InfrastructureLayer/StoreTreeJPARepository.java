package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.StoreUserConnection.StoreTreeEntity;

@Repository
public interface StoreTreeJPARepository extends JpaRepository<StoreTreeEntity, Integer> {

    @Query("SELECT DISTINCT s FROM StoreTreeEntity s "
            + "JOIN FETCH s.allNodes n "
            + "LEFT JOIN FETCH n.myAuth")
    List<StoreTreeEntity> findAllWithNodes();

}
