package workshop.demo.PresentationLayer.Requests;

import workshop.demo.DTOs.ItemStoreDTO;

public class AddToCartRequest {

    private ItemStoreDTO item;
    private int quantity;

    public AddToCartRequest() {
    }

    public AddToCartRequest(ItemStoreDTO item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public ItemStoreDTO getItem() {
        return item;
    }

    public void setItem(ItemStoreDTO item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
