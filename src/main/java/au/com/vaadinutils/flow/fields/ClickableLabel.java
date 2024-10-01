package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;

/**
 * Adds a {@link Label} in to a {@link Span} to allow a simple {@link Button}
 * like feature.<br>
 * Use the Click Listener from the layout to respond to click events.
 */
public class ClickableLabel extends Span {

    private static final long serialVersionUID = -6393085595535312254L;
    private Component label;

    public ClickableLabel() {
        this.setWidthFull();
    }

    public ClickableLabel(final Component value) {
        setComponent(value);
    }

    public void setComponent(final Component value) {
        setId("ClickableLabel" + value.getId().orElse(""));
        removeAll();
        label = value;
        add(label);
    }

    public Component getValue() {
        return label != null ? label : null;
    }

    public Component getComponent() {
        return label;
    }

    public void setDescription(final String toolTip) {
        getElement().setAttribute("title", toolTip);
    }
}