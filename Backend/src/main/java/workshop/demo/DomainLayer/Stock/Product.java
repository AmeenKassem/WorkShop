package workshop.demo.DomainLayer.Stock;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.Category;

public class Product {

    private String name;
    private int productId;
    private String description;
    private Category category;  // Using enum Category
    private List<String> keywords; // List of keywords for the product

    public Product(String name, int id, Category category, String description, String[] keywords) {
        this.name = name;
        this.productId = id;
        this.category = category;
        this.description = description;
    }

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

    public List<String> getKeywords() {
        return keywords;
    }

    public synchronized void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new ArrayList<>();
        }
        this.keywords.add(keyword);
    }

}
