package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;

/**
 * Replaced in V14 migration.
 */
public class SplitListSelect extends ListSelect implements SplitField {
    private static final long serialVersionUID = -7088825671520977496L;

    private final Label label;

    public SplitListSelect(final String label) {
        this.label = new Label(label);
        setCaption(label);
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public void hideLabel() {
        setCaption(null);
    }

    @Override
    public String getCaption() {
        return label.getValue();
    }
}
