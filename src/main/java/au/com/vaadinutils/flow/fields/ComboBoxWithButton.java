package au.com.vaadinutils.flow.fields;

import java.util.List;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ComboBoxWithButton<T> extends HorizontalLayout {
    private static final long serialVersionUID = -383329492696973793L;
    private Button button;
    private final ComboBox<T> field;

    public ComboBoxWithButton(final String caption) {
        this(caption, null);
    }

    public ComboBoxWithButton(final String caption, final Button button) {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(false);
        if (button != null) {
            this.setButton(button);
        } else {
            this.setButton(new Button());
        }
        field = createField();
        final Span label = new Span(caption);
        label.getElement().getStyle().set("font-size", "x-small");

        setAlignItems(Alignment.CENTER);
        add(field, this.button);
    }

    public void select(final T itemId) {
        field.setValue(itemId);
    }

    private ComboBox<T> createField() {
        return new ComboBox<T>();
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

    public void addValueChangerListener(
            ValueChangeListener<? super ComponentValueChangeEvent<ComboBox<T>, T>> listener) {
        field.addValueChangeListener(listener);
    }

    public void addButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        button.addClickListener(listener);
    }

    public void setItems(final List<T> items) {
        field.setItems(items);
    }
}
