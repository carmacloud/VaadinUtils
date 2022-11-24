package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Retain, but update UI for Flow
 */
public class TextFieldWithButton extends HorizontalLayout {
    private static final long serialVersionUID = -6761979395677678269L;
    private Button button;
    private TextField field;
    private String label;

    public TextFieldWithButton(final String caption) {
        this(caption, null);
    }

    public TextFieldWithButton(final String label, final Button button) {
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

    public void setButton(Button button) {
        this.button = button;
    }

    public void setFieldWidth(final String width) {
        field.setWidth(width);
    }

    public void addValueChangerListener(
            ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        field.addValueChangeListener(listener);
    }

    public void addButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        button.addClickListener(listener);
    }

    public void setReadOnly(boolean readOnly) {
        field.setReadOnly(readOnly);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
        this.field.setLabel(label);
    }
}
