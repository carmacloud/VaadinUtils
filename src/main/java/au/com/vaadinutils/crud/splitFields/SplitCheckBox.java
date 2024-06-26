package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;

/**
 * Replaced in V14 migration.
 */
public class SplitCheckBox extends CheckBox implements SplitField {
    private static final long serialVersionUID = -1573292123807845727L;
    private final Label label;

    public SplitCheckBox(final String label) {
        this.label = new Label(label);
        setCaption(label);

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
