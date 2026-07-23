package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Label;

import au.com.vaadinutils.domain.iColorFactory;
import au.com.vaadinutils.fields.ColorPickerField;

/**
 * Replaced in V14 migration.
 */
public class SplitColorPicker extends ColorPickerField implements SplitField {
    private static final long serialVersionUID = -1573292123807845727L;
    private final Label label;

    public SplitColorPicker(final iColorFactory factory, final String label) {
        super(factory);
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
