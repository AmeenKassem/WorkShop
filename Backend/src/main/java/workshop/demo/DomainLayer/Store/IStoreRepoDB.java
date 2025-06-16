package workshop.demo.DomainLayer.Store;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IStoreRepoDB extends JpaRepository<Store,Integer> {
    
}
