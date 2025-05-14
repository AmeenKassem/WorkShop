package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

import workshop.demo.PresentationLayer.View.MyStoresView;

public class MyStoresPresenter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MyStoresView view;

    public MyStoresPresenter(MyStoresView view) {
        this.view = view;
    }

}
