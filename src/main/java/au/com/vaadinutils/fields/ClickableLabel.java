package au.com.vaadinutils.fields;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Migrated
 */
@SuppressWarnings("serial")
public class ClickableLabel extends VerticalLayout {
    private final Label label;

    public ClickableLabel() {
        this(null);
        setImmediate(true);
    }

    public ClickableLabel(final String value) {
        this(value, ContentMode.HTML);
    }

    public ClickableLabel(final String value, final ContentMode contentMode) {
        label = new Label(value, contentMode);
        addComponent(label);
        setImmediate(true);
    }

    public void setValue(final String value) {
        label.setValue(value);
    }

    @Override
    public void setStyleName(final String style) {
        label.setStyleName(style);
    }

    @Override
    public void addStyleName(final String style) {
        label.addStyleName(style);
    }

    public void setContentMode(final ContentMode contentMode) {
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
            public void layoutClick(final LayoutClickEvent event) {
                clickListener.buttonClick(new ClickEvent(event.getComponent()));

            }
        });

    }

    public Label getLabel() {
        return label;
    }
}
