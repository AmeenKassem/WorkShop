
package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;

// import com.vaadin.flow.component.template.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ProductDTO;

@Entity
public class Product {

    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;
    private String description;
    private Category category; // Using enum Category

    @Transient
    private String[] keywords; // List of keywords for the product

    public Product(String name, Category category, String description, String[] keywords) {
        this.name = name;
        // this.productId = id;
        this.category = category;
        this.description = description;
        this.keywords = keywords;
    }

    public Product(){}

    public String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public int getProductId() {
        return productId;
    }

    public synchronized void setProductId(int productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public synchronized void setCategory(Category category) {
        this.category = category;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public synchronized void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public ProductDTO getDTO() {
        return new ProductDTO(getProductId(), getName(), getCategory(),
                getDescription());
    }

   
}
