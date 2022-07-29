package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Retain, but update UI for Flow
 */
public class TextFieldWithButton extends HorizontalLayout {
    private static final long serialVersionUID = -6761979395677678269L;
    private Button button;
    private TextField field;

    public TextFieldWithButton(final String caption) {
        this(caption, null);
    }

    public TextFieldWithButton(final String caption, final Button button) {
        setSpacing(true);
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

    private TextField createField() {
        return new TextField();
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

    public void setReadOnly(boolean readOnly) {
        field.setReadOnly(readOnly);
    }
}
