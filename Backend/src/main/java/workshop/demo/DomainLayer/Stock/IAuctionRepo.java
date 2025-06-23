package workshop.demo.DomainLayer.Stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IAuctionRepo extends JpaRepository<Auction,Integer> {
    
}
