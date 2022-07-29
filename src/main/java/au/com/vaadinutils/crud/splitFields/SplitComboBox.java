package au.com.vaadinutils.crud.splitFields;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;

/**
 * @deprecated Replaced in V14 migration.
 */
public class SplitComboBox extends ComboBox implements SplitField {
    /**
     * 
     */
    private static final long serialVersionUID = -6304106636121695094L;
    private Label label;

    public SplitComboBox(String label) {
        this.label = new Label(label);
        setCaption(label);
    }

    public SplitComboBox(String fieldLabel, Container createContainerFromEnumClass) {
        super(fieldLabel, createContainerFromEnumClass);
        this.label = new Label(fieldLabel);
    }

    public SplitComboBox(String fieldLabel, Collection<?> options) {
        super(fieldLabel, options);
        this.label = new Label(fieldLabel);
    }

    @Override
    public void setVisible(boolean visible) {
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
