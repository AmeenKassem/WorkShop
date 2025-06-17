package workshop.demo.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.StoreUserConnection.StoreTreeEntity;

@Repository
public interface StoreTreeJPARepository extends JpaRepository<StoreTreeEntity, Integer> {

}
