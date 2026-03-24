package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;

/**
 * Adds a {@link Component} in to a {@link Span} to allow a simple
 * {@link Button} like feature.<br>
 * Use the Click Listener from the layout to respond to click events.<br>
 * Note: Only use this for adding items that don't have a click listener
 * interface to enable a clickable content. Otherwise, better off just using an
 * {@link Icon} or similar.
 */
public class ClickableLabel extends Span {

    private static final long serialVersionUID = -6393085595535312254L;
    private Component label;

    public ClickableLabel() {
        this.setWidthFull();
    }

    public ClickableLabel(final Component value) {
        this.setComponent(value);
    }

    public void setComponent(final Component value) {
        this.setId("ClickableLabel" + value.getId().orElse(""));
        this.removeAll();
        this.label = value;
        this.add(this.label);
    }

    public Component getValue() {
        return this.label != null ? this.label : null;
    }

    public Component getComponent() {
        return this.label;
    }

    /**
     * @deprecated Use the tooltip method of the added {@link Component} rather than
     *             the one attached to {@link Span}.
     * @param toolTip
     */
    @Deprecated
    public void setDescription(final String toolTip) {
        this.setTitle(toolTip);
    }
}