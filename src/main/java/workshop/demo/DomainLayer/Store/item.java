package workshop.demo.DomainLayer.Store;

import java.lang.reflect.Array;

public class item {

    private int produtId;
    private int quantity;
    private int price;
    private int[] rank;//rank[x] is the number of people who ranked i+1

    public item(int produtId, int quantity, int price) {
        this.price = price;
        this.quantity = quantity;
        this.price = price;
        this.rank = new int[5];
    }

    public double getFinalRank() {
        double fRank = 0;
        for (int i = 0; i < rank.length; i++) {
            fRank += rank[0] * (i + 1);
        }
        return fRank;
    }

    public boolean rankP(int i) {
        if (i < 1 || i > 5) {
            return false;
        }
        rank[i]++;
        return true;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int[] getRank() {
        return rank;
    }

    public void setRank(int[] rank) {
        this.rank = rank;
    }

    public int getProdutId() {
        return produtId;
    }

}
