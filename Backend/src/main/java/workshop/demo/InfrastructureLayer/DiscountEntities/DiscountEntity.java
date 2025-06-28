package workshop.demo.InfrastructureLayer.DiscountEntities;

import jakarta.persistence.*;
import workshop.demo.DomainLayer.Store.Store;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "discount_type")
public abstract class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String name;

    @Column(name = "discount_percent", nullable = false)
    private double percent;
    @Column(name = "discount_condition")
    private String condition;
    @OneToOne(mappedBy = "discountEntity")
    private Store store;

    public String getName() { return name; }
    public double getPercent() { return percent; }
    public String getCondition() { return condition; }

    public void setName(String name) { this.name = name; }
    public void setPercent(double percent) { this.percent = percent; }
    public void setCondition(String condition) { this.condition = condition; }

    public int getId() { return id; }
    protected DiscountEntity() { }
    public void setStore(Store s) { this.store = s; }
}
