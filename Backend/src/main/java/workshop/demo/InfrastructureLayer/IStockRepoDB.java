package workshop.demo.InfrastructureLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import workshop.demo.DomainLayer.Stock.Product;

public interface IStockRepoDB extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :substring, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("substring") String substring);
}
