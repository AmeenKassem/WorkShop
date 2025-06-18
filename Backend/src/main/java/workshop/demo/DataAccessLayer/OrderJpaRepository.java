package workshop.demo.DataAccessLayer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import workshop.demo.DomainLayer.Order.Order;

public interface OrderJpaRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o WHERE o.storeName = :storeName")
    List<Order> findOrdersByStoreName(@Param("storeName") String storeName);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    List<Order> findOrdersByUserId(@Param("userId") int userId);
}

