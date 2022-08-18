package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.shared.Registration;

public class LazyComboBoxWithButton<E> extends HorizontalLayout {
    private static final long serialVersionUID = 1L;
    private Button button;
    private ComboBox<E> field;
    private final DataProvider<E, String> dataProvider;
    private  Registration fieldRegistartion;
    private Registration buttonRegistration;

    public LazyComboBoxWithButton(final DataProvider<E, String> dataProvider, final int componentWidth,
            final String caption, final Button button) {
        this.setId("LazyComboBoxWithButton");
        this.dataProvider = dataProvider;
        
        if (button != null) {
            this.button = button;
        } else {
            this.button = new Button();
        }

        this.setWidth(componentWidth + "px");
        setSizeUndefined();
        setSpacing(true);
        field = createField();
        field.setDataProvider(dataProvider);
        field.setWidth(componentWidth + "px");
        add(new Span(caption), this.field, this.button);

        setFlexGrow(1, field);
        setAlignItems(Alignment.CENTER);
    }

    private ComboBox<E> createField() {
        final ComboBox<E> comboBox = new ComboBox<E>();

        return comboBox;
    }

    public ComboBox<E> getField() {
        return field;
    }

    public Button getButton() {
        return button;
    }

    public E getValue() {
        return field.getValue();
    }

    public void setValue(final E newFieldValue) {
        field.setValue(newFieldValue);
    }

    public void addFieldValueChangeListener(
            final ValueChangeListener<? super ComponentValueChangeEvent<ComboBox<E>, E>> listener) {
        fieldRegistartion = field.addValueChangeListener(listener);
    }

    public void removeFieldValueChangeListener() {
        fieldRegistartion.remove();
    }
    
    public void addButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        buttonRegistration= button.addClickListener(listener);
    }
    
    public void removeButtonClickListener() {
        buttonRegistration.remove();
    }
    
    public void refresh() {
        this.dataProvider.refreshAll();
    }
}