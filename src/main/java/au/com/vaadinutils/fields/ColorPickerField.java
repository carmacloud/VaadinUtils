package au.com.vaadinutils.fields;

import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.components.colorpicker.ColorChangeEvent;
import com.vaadin.ui.components.colorpicker.ColorChangeListener;

import au.com.vaadinutils.domain.iColor;
import au.com.vaadinutils.domain.iColorFactory;

/**
 * Replaced in V14 migration.
 */
public class ColorPickerField extends CustomField<iColor> {
    private static final long serialVersionUID = -1573292123807845727L;

    private final ColorPicker colorPicker;

    private final iColorFactory colorFactory;

    public ColorPickerField(final iColorFactory colorFactory) {
        this.colorFactory = colorFactory;
        this.colorPicker = new ColorPicker();
        this.colorPicker.addColorChangeListener(new ColorChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void colorChanged(final ColorChangeEvent event) {
                setValue(ColorPickerField.this.colorFactory.createColor(event.getColor()));
            }
        });

    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        colorPicker.setReadOnly(readOnly);
        super.setReadOnly(readOnly);
    }

    @Override
    protected Component initContent() {
        return this.colorPicker;
    }

    @Override
    public Class<? extends iColor> getType() {
        return iColor.class;
    }

    @Override
    public iColor getInternalValue() {
        return this.colorFactory.createColor(this.colorPicker.getColor());
    }

    @Override
    public void setInternalValue(final iColor newFieldValue) {
        if (newFieldValue != null)
            this.colorPicker.setColor(new com.vaadin.shared.ui.colorpicker.Color(newFieldValue.getRed(),
                    newFieldValue.getGreen(), newFieldValue.getBlue(), newFieldValue.getAlpha()));
        super.setInternalValue(newFieldValue);
    }

}
