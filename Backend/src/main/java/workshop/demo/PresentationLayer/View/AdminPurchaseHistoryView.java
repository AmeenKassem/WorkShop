package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import workshop.demo.Contrrollers.ApiResponse;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Presenter.AdminPurchaseHistoryPresenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Route(value = "admin-purchase-history", layout = MainLayout.class)
public class AdminPurchaseHistoryView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AdminPurchaseHistoryPresenter presenter;

    public AdminPurchaseHistoryView(List<PurchaseHistoryDTO> historyList) {
        this.presenter = new AdminPurchaseHistoryPresenter(this);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(new H2("🧾 System Purchase History"));
        presenter.fetchPurchaseHistory();

    }

    public void displayPurchaseHistory(List<PurchaseHistoryDTO> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            add(new Paragraph("No purchase history available."));
            return;
        }

        Grid<PurchaseHistoryDTO> grid = new Grid<>(PurchaseHistoryDTO.class, false);
        grid.addColumn(PurchaseHistoryDTO::getBuyerUserName).setHeader("Buyer");
        grid.addColumn(PurchaseHistoryDTO::getStoreName).setHeader("Store");
        grid.addColumn(PurchaseHistoryDTO::getTimeStamp).setHeader("Timestamp");
        grid.addColumn(PurchaseHistoryDTO::getTotalPrice).setHeader("Total Price");

        grid.setItems(historyList);

        grid.addItemClickListener(event -> {
            PurchaseHistoryDTO selected = event.getItem();
            ReceiptDTO receipt = new ReceiptDTO();
            receipt.setProductsList(selected.getItems());
            receipt.setStoreName(selected.getStoreName());
            receipt.setFinalPrice(selected.getTotalPrice());
            receipt.setDate(selected.getTimeStamp());
            ReceiptDTO[] wrapper = new ReceiptDTO[]{receipt};
            PurchaseView.showReceiptDialog(wrapper);
        });

        add(grid);
    }
}
