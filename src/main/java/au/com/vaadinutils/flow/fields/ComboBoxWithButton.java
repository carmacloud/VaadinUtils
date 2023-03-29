package au.com.vaadinutils.flow.fields;

import java.util.List;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;

public class ComboBoxWithButton<T> extends HorizontalLayout {
    private static final long serialVersionUID = -383329492696973793L;
    private Button button;
    private final ComboBox<T> field;
    private String label;
    private DataProvider<T, String> dataProvider;

    public ComboBoxWithButton(final String caption) {
        this(caption, null);
    }

    public ComboBoxWithButton(final String label, final Button button) {
        this.setWidthFull();
        setSpacing(true);
        if (button != null) {
            this.setButton(button);
        } else {
            this.setButton(new Button());
        }
        field = createField(label);

        setAlignItems(Alignment.END);
        add(field, this.button);
    }

    public void select(final T itemId) {
        field.setValue(itemId);
    }

    private ComboBox<T> createField(final String label) {
        this.label = label;
        return new ComboBox<T>(label);
    }

    public ComboBox<T> getField() {
        return field;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public void setFieldWidth(final String width) {
        field.setWidth(width);
    }

    public void addValueChangeListener(
            ValueChangeListener<? super ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        field.addValueChangeListener(listener);
    }

    public void addButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        button.addClickListener(listener);
    }

    public void setItems(final List<T> items) {
        field.setItems(items);
    }

    public void setItems(@SuppressWarnings("unchecked") T... items) {
        field.setItems(items);
    }

    public void setDataProvider(final DataProvider<T, String> dataProvider) {
        this.dataProvider = dataProvider;
        field.setDataProvider(dataProvider);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
        this.field.setLabel(label);
    }

    public T getValue() {
        return field.getValue();
    }

    public void setValue(final T newFieldValue) {
        field.setValue(newFieldValue);
    }

    public void refresh() {
        this.dataProvider.refreshAll();
    }
}
