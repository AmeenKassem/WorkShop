package workshop.demo.PresentationLayer.Presenter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;

import workshop.demo.DTOs.StoreDTO;
import workshop.demo.PresentationLayer.View.HomePage;

public class HomePagePresenter {

    private final RestTemplate restTemplate;
    private HomePage view;

    public HomePagePresenter(HomePage homePage) {
        this.view = homePage;
        this.restTemplate = new RestTemplate();
    }

    public List<StoreDTO> fetchStores() {
        try {
            ResponseEntity<StoreDTO[]> response = restTemplate.getForEntity(
                    "http://localhost:8080/api/stores/allStores", StoreDTO[].class);
            StoreDTO[] body = response.getBody();
            return body != null ? Arrays.asList(body) : Collections.emptyList();
        } catch (Exception e) {
            Notification.show("❌ Failed to load stores: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Div createStoreCard(StoreDTO store) {
        Div card = new Div();
        card.addClassName("store-card");

        H3 name = new H3(store.getName());
        Paragraph rank = new Paragraph("⭐ Rank: " + store.getRank());
        Paragraph category = new Paragraph("🏷️ Category: " + store.getCategory());

        card.add(name, rank, category);

        card.addClickListener(e
                -> UI.getCurrent().navigate("store/" + store.getId()));

        return card;
    }

}
