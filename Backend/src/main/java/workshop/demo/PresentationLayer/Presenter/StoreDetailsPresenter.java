package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

public class StoreDetailsPresenter {

    private final RestTemplate restTemplate;

    public StoreDetailsPresenter() {
        this.restTemplate = new RestTemplate();
    }

    //public StoreDTO fetchStoreById(int storeId) {}
    //should make anothr function in the controller to give me all the products of the store with all it's details
}
