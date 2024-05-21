package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class TextFieldWithButton extends HorizontalLayout {
    private static final long serialVersionUID = -6761979395677678269L;
    private Button button;
    private final TextField field;
    private String label;

    public TextFieldWithButton(final String label) {
        this(label, null);
    }

    public TextFieldWithButton(final String label, final Button button) {
        this.setWidthFull();
        setSpacing(false);
        if (button != null) {
            this.setButton(button);
        } else {
            this.setButton(new Button());
        }
        field = createField(label);

        setAlignItems(Alignment.END);
        add(field, this.button);
    }

    private TextField createField(final String label) {
        this.label = label;
        return new TextField(label);
    }

    public TextField getField() {
        return field;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(final Button button) {
        this.button = button;
    }

    public void setFieldWidth(final String width) {
        field.setWidth(width);
    }

    public void addValueChangerListener(
            final ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        field.addValueChangeListener(listener);
    }

    public void addButtonClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        button.addClickListener(listener);
    }

    public void setReadOnly(final boolean readOnly) {
        field.setReadOnly(readOnly);
        button.setEnabled(!readOnly);
        button.setVisible(!readOnly);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
        this.field.setLabel(label);
    }

    public void addTextFieldValueChangeListener(
            final ValueChangeListener<ComponentValueChangeEvent<TextField, String>> listener) {
        this.field.addValueChangeListener(listener);
    }
}