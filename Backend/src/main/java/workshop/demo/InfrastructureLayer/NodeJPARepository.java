package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.NodeKey;

@Repository
public interface NodeJPARepository extends JpaRepository<Node, NodeKey> {

}
