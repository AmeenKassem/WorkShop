package workshop.demo.PresentationLayer.Components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import workshop.demo.DTOs.CreateDiscountDTO;

import java.util.ArrayList;
import java.util.List;

public class DiscountEditorComponent extends VerticalLayout {

    private final TextField nameField = new TextField("Name");
    private final NumberField percentField = new NumberField("Percent (0-100)");
    private final ComboBox<CreateDiscountDTO.Type> typeBox = new ComboBox<>("Type");
    private final ComboBox<CreateDiscountDTO.Logic> logicBox = new ComboBox<>("Logic");
    private final TextField conditionField = new TextField("Condition");

    private final VerticalLayout subDiscountsLayout = new VerticalLayout();

    public DiscountEditorComponent() {
        percentField.setMin(0);
        percentField.setMax(100);
        percentField.setValue(0.0);

        typeBox.setItems(CreateDiscountDTO.Type.values());
        logicBox.setItems(CreateDiscountDTO.Logic.values());
        logicBox.setValue(CreateDiscountDTO.Logic.SINGLE);

        logicBox.addValueChangeListener(event -> {
            if (event.getValue() != CreateDiscountDTO.Logic.SINGLE) {
                showSubDiscountControls();
            } else {
                subDiscountsLayout.removeAll();
            }
        });

        add(nameField, percentField, typeBox, logicBox, conditionField, subDiscountsLayout);
    }

    private void showSubDiscountControls() {
        subDiscountsLayout.removeAll();
        Button addSub = new Button("+ Add Sub-discount", e -> {
            DiscountEditorComponent sub = new DiscountEditorComponent();
            sub.getStyle().set("margin-left", "15px");
            subDiscountsLayout.add(sub);
        });
        subDiscountsLayout.add(addSub);
    }

    public CreateDiscountDTO buildDTO() {
        CreateDiscountDTO dto = new CreateDiscountDTO();
        dto.setName(nameField.getValue());
        dto.setPercent(percentField.getValue() / 100.0);
        dto.setType(typeBox.getValue());
        dto.setLogic(logicBox.getValue());
        dto.setCondition(conditionField.getValue());

        if (dto.getLogic() != CreateDiscountDTO.Logic.SINGLE) {
            List<CreateDiscountDTO> children = new ArrayList<>();
            for (Component comp : subDiscountsLayout.getChildren().toList()) {
                if (comp instanceof DiscountEditorComponent editor) {
                    children.add(editor.buildDTO());
                }
            }
            dto.setSubDiscounts(children);
        }

        return dto;
    }
}
