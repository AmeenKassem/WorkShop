package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

import workshop.demo.PresentationLayer.View.MyCartView;

public class MyCartPresenter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MyCartView mCartView;

    public MyCartPresenter(MyCartView mCartView) {
        this.mCartView = mCartView;

    }

}
