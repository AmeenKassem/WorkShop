package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import workshop.demo.Controllers.ApiResponse;
import workshop.demo.DTOs.PurchaseHistoryDTO;
import workshop.demo.DTOs.ReceiptDTO;
import workshop.demo.PresentationLayer.Handlers.ExceptionHandlers;
import workshop.demo.PresentationLayer.Presenter.AdminPurchaseHistoryPresenter;

import java.nio.charset.StandardCharsets;
import java.util.List;
@Route(value = "admin-purchase-history", layout = MainLayout.class)
@CssImport("./Theme/AdminPurchaseHistory.css")
public class AdminPurchaseHistoryView extends VerticalLayout {

    private final AdminPurchaseHistoryPresenter presenter;
    private final Grid<PurchaseHistoryDTO> grid = new Grid<>(PurchaseHistoryDTO.class, false);
    private final Paragraph noDataMsg = new Paragraph("No purchase history available.");

    public AdminPurchaseHistoryView() {
        this.presenter = new AdminPurchaseHistoryPresenter(this);

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        add(new Hr());
        addClassName("admin-panel-wrapper");
        H2 title = new H2("📊 Admin Command Center – Purchase Logs 👁️");
        title.addClassName("main-header");

        Button refreshButton = new Button("🔄 Refresh", click -> presenter.fetchPurchaseHistory());
        refreshButton.addClassNames("v-button", "primary");
        refreshButton.getStyle().set("margin-bottom", "1rem");
        configGrid();

        add(title, refreshButton, grid, noDataMsg);
        noDataMsg.setVisible(false); 
        presenter.fetchPurchaseHistory();
    }

    private void configGrid() {
        grid.setWidthFull();
        grid.setHeight("600px");

        grid.addColumn(PurchaseHistoryDTO::getBuyerUserName).setHeader("👤 Buyer").setAutoWidth(true);
        grid.addColumn(PurchaseHistoryDTO::getStoreName).setHeader("🏬 Store").setAutoWidth(true);
        grid.addColumn(PurchaseHistoryDTO::getTimeStamp).setHeader("⏰ Timestamp").setAutoWidth(true);
        grid.addColumn(PurchaseHistoryDTO::getTotalPrice).setHeader("💰 Total Price").setAutoWidth(true);

        grid.addClassName("purchase-grid");

        grid.setAllRowsVisible(true);

        grid.addItemClickListener(event -> {
            PurchaseHistoryDTO selected = event.getItem();
            ReceiptDTO receipt = new ReceiptDTO(
                selected.getStoreName(),
                selected.getTimeStamp(),
                selected.getItems(),
                selected.getTotalPrice()
            );
            PurchaseView.showReceiptDialog(new ReceiptDTO[]{receipt});
        });
    }

    public void displayPurchaseHistory(List<PurchaseHistoryDTO> historyList) {
        grid.setVisible(true);
        noDataMsg.setVisible(false);

        if (historyList == null || historyList.isEmpty()) {
            grid.setVisible(false);
            noDataMsg.setVisible(true);
        } else {
            grid.setItems(historyList);
        }
    }
}
