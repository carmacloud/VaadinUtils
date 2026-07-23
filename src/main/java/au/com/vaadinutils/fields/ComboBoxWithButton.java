package au.com.vaadinutils.fields;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;

/**
 * Retain, will need UI convert to Flow
 */
public class ComboBoxWithButton extends FieldWithButton<Object> {
    private static final long serialVersionUID = 1L;

    public ComboBoxWithButton(final String caption) {
        super(caption, null);
    }

    public ComboBoxWithButton(final String caption, final Button button) {
        super(caption, button);
    }

    @Override
    protected AbstractField<Object> createField() {
        return new ComboBox();
    }

    public void setNullSelectionAllowed(final boolean nullSelectionAllowed) {
        ((ComboBox) field).setNullSelectionAllowed(nullSelectionAllowed);
    }

    public void setContainerDataSource(final Container newDataSource) {
        ((ComboBox) field).setContainerDataSource(newDataSource);
    }

    public void setItemCaptionPropertyId(final Object propertyId) {
        ((ComboBox) field).setItemCaptionPropertyId(propertyId);
    }

    public void select(final Object itemId) {
        ((ComboBox) field).select(itemId);
    }
}
