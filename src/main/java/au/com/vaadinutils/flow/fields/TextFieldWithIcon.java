package au.com.vaadinutils.flow.fields;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Not currently used, but might be a neater alternative to
 * {@link TextFieldWithButton}
 */
public class TextFieldWithIcon extends HorizontalLayout {
    private static final long serialVersionUID = -6761979395677678269L;
    private Icon button;
    private final TextField field;
    private String label;

    public TextFieldWithIcon(final String label, final Icon icon) {
        Preconditions.checkNotNull(icon, "Icon must be set.");
        this.setWidthFull();
        setSpacing(true);
        this.setIcon(icon);
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

    public Icon getIcon() {
        return button;
    }

    public void setIcon(final Icon button) {
        this.button = button;
    }

    public void setFieldWidth(final String width) {
        field.setWidth(width);
    }

    public void addValueChangerListener(
            final ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        field.addValueChangeListener(listener);
    }

    public void addButtonClickListener(final ComponentEventListener<ClickEvent<Icon>> listener) {
        button.addClickListener(listener);
    }

    public void setReadOnly(final boolean readOnly) {
        field.setReadOnly(readOnly);
        button.setVisible(!readOnly);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
        this.field.setLabel(label);
    }
}