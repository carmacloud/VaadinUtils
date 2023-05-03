package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Adds a {@link Label} in to a {@link VerticalLayout} to allow a simple
 * {@link Button} like feature.<br>
 * Use the Click Listener from the layout to respond to click events.
 */
@SuppressWarnings("serial")
public class ClickableLabel extends VerticalLayout {
    private Component label;

    public ClickableLabel() {
    }

    public ClickableLabel(Component value) {
        setComponent(value);
    }

    public void setComponent(Component value) {
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
}
