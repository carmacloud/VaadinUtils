package au.com.vaadinutils.fields;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Not needed for migration. Icons or buttons would be a better solution. A
 * Vaadin label that allows actions to be run by adding a LayoutClickListener
 * Retain, needs updating to Flow for UI.
 */
@SuppressWarnings("serial")
public class ClickableLabel extends VerticalLayout {
    private Label label;

    public ClickableLabel() {
        this(null);
        setImmediate(true);
    }

    public ClickableLabel(String value) {
        this(value, ContentMode.HTML);
    }

    public ClickableLabel(String value, ContentMode contentMode) {
        label = new Label(value, contentMode);
        addComponent(label);
        setImmediate(true);
    }

    public void setValue(String value) {
        label.setValue(value);
    }

    @Override
    public void setStyleName(String style) {
        label.setStyleName(style);
    }

    @Override
    public void addStyleName(String style) {
        label.addStyleName(style);
    }

    public void setContentMode(ContentMode contentMode) {
        label.setContentMode(contentMode);

    }

    public String getValue() {
        return label.getValue() != null ? label.getValue() : "";
    }

    /**
     * makes the ClickableLabel a drop in replace ment for a button
     * 
     * @param clickListener
     */
    public void addClickListener(final ClickListener clickListener) {
        addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                clickListener.buttonClick(new ClickEvent(event.getComponent()));

            }
        });

    }

    public Label getLabel() {
        return label;
    }
}
