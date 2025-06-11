package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.PresentationLayer.View.ManagePolicyView;

public class ManagePolicyPresenter {

    private final ManagePolicyView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public ManagePolicyPresenter(ManagePolicyView view) {
        this.view = view;
    }

    public void addDiscount(int storeId, String name, double percent, CreateDiscountDTO.Type type, String condition) {
    }

}
