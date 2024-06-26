package au.com.vaadinutils.crud.splitFields.legacy;

import java.util.Collection;

import org.vaadin.ui.LegacyComboBox;

import com.vaadin.data.Container;
import com.vaadin.ui.Label;

import au.com.vaadinutils.crud.splitFields.SplitField;

/**
 * Will be removed once dependent classes are removed.
 */
public class LegacySplitComboBox extends LegacyComboBox implements SplitField {

    private static final long serialVersionUID = -3156478731788878472L;
    private final Label label;

    public LegacySplitComboBox(final String label) {
        this.label = new Label(label);
        setCaption(label);
    }

    public LegacySplitComboBox(final String fieldLabel, final Container createContainerFromEnumClass) {
        super(fieldLabel, createContainerFromEnumClass);
        this.label = new Label(fieldLabel);
    }

    public LegacySplitComboBox(final String fieldLabel, final Collection<?> options) {
        super(fieldLabel, options);
        this.label = new Label(fieldLabel);
    }

    @Override
    public void setVisible(final boolean visible) {
        label.setVisible(visible);
        super.setVisible(visible);
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public String getCaption() {
        return label.getValue();
    }

    @Override
    public void hideLabel() {
        setCaption(null);
    }

}
