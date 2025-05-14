package workshop.demo.PresentationLayer.Presenter;

import org.springframework.web.client.RestTemplate;

import workshop.demo.PresentationLayer.View.RegisterView;

public class RegisterPresenter {

    private final RegisterView view;
    private final RestTemplate restTemplate = new RestTemplate();

    public RegisterPresenter(RegisterView view) {
        this.view = view;
        //view.addRegisterListener(e -> register());
    }

    public void register() {
    }
}
