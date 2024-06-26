package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;

/**
 * Replaced in V14 migration.
 */
public class SplitPasswordField extends PasswordField implements SplitField {
    private static final long serialVersionUID = 7753660388792217050L;
    private final Label label;

    public SplitPasswordField(final String label) {
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
