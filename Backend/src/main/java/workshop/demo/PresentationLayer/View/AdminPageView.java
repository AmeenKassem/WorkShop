package workshop.demo.PresentationLayer.View;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import workshop.demo.PresentationLayer.Presenter.AdminPagePresenter;

@Route(value = "admin", layout = MainLayout.class)
public class AdminPageView extends VerticalLayout {

    private final AdminPagePresenter presenter;

    public AdminPageView() {
        presenter = new AdminPagePresenter(this);
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(new H2("Admin Control Panel"));

        // Button viewHistoryBtn = new Button("View Purchase History",
        //         e -> presenter.onViewSystemPurchaseHistory());
        // Button suspendBtn = new Button("Suspend User",
        //         e -> presenter.onSuspendUser());
        // Button pauseSuspensionBtn = new Button("Pause Suspension",
        //         e -> presenter.onPauseSuspension());
        // Button resumeSuspensionBtn = new Button("Resume Suspension",
        //         e -> presenter.onResumeSuspension());
        // Button removeUserBtn = new Button("Remove User",
        //         e -> presenter.onRemoveUser());
        // VerticalLayout actionButtons = new VerticalLayout(
        //         viewHistoryBtn, suspendBtn, pauseSuspensionBtn, resumeSuspensionBtn, removeUserBtn
        // );
        // HorizontalLayout inputFields = new HorizontalLayout();
        // add(inputFields, actionButtons);
        Button viewHistoryBtn = new Button("📊 View Purchase History", e -> UI.getCurrent().navigate("admin-purchase-history"));

        Button manageUsersBtn = new Button("👥 Manage Users", e -> NotificationView.showError("coming soon"));

        VerticalLayout actionButtons = new VerticalLayout(viewHistoryBtn, manageUsersBtn);
        add(actionButtons);
    }
}
