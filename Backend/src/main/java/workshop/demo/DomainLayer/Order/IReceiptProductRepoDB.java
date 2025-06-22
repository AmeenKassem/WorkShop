package workshop.demo.DomainLayer.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import workshop.demo.DTOs.ReceiptProduct;

public interface IReceiptProductRepoDB extends JpaRepository<ReceiptProduct, Integer> {

}
