package workshop.demo.DomainLayer.Store;

import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.Category;

public class item {

    private int productId;
    private AtomicInteger quantity;
    private int price;
    private Category category;
    private AtomicInteger[] rank;//rank[x] is the number of people who ranked i+1

    public item(int produtId, int quantity, int price, Category category) {
        this.productId = produtId;
        this.price = price;
        this.quantity = new AtomicInteger(quantity);;
        this.rank = new AtomicInteger[5];
        this.category = category;
    }

    public int getFinalRank() {
        int fRank = 0;
        for (int i = 0; i < rank.length; i++) {
            fRank += rank[i].get() * (i + 1);
        }
        return fRank;
    }

    public boolean rankP(int i) {
        if (i < 1 || i > 5) {
            return false;
        }
        rank[i].incrementAndGet();
        return true;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void AddQuantity() {
        this.quantity.incrementAndGet();
    }

    public void changeQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public AtomicInteger[] getRank() {
        return rank;
    }

    public int getProductId() {
        return productId;
    }

    public Category getCategory() {
        return category;
    }

    public synchronized int getPrice() {
        return price;
    }

    public synchronized void setPrice(int price) {
        this.price = price;
    }

    //for tests:
    public void setRank(AtomicInteger[] rank) {
        this.rank = rank;
    }

}
