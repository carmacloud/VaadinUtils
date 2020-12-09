package au.com.vaadinutils.fields;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

/**
 * Retain, but update UI for Flow
 */
public class TextFieldWithButton extends FieldWithButton<String> {
    private static final long serialVersionUID = 1L;

    public TextFieldWithButton(final String caption) {
        super(caption, null);
    }

    public TextFieldWithButton(final String caption, final Button button) {
        super(caption, button);
    }

    @Override
    protected AbstractField<String> createField() {
        return new TextField();
    }

    public void setNullRepresentation(final String nullRepresentation) {
        ((TextField) field).setNullRepresentation(nullRepresentation);
    }

    public void setNullSettingAllowed(final boolean nullSettingAllowed) {
        ((TextField) field).setNullSettingAllowed(nullSettingAllowed);
    }
}
