package au.com.vaadinutils.flow.fields;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.componentfactory.gridlayout.GridLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.data.converter.StringToBigDecimalConverter;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.flow.converter.LongNoGroupingConverter;
import au.com.vaadinutils.flow.helper.VaadinHelper;

/**
 * A class to enable quickly creating basic field types and optionally adding
 * them to a layout and/or optionally binding them to a supplied binder.<br>
 * <br>
 * Binder being created outside of this class will need to handle when to
 * refresh the {@link DataProvider}s supplied to the combos.<br>
 * They won't refresh on a bean read or write, and the state change listener on
 * {@link Binder} fires every time each field here is validated, which makes for
 * a messy and over the top refresh.
 *
 * @param <E> Extends {@link CrudEntity}.
 */
public class FormHelper<E extends CrudEntity> {

    private final Logger logger = LogManager.getLogger();

    private final Class<E> entityClass;
    private final Component layout;

    // Binder
    private final Binder<E> binder;

    // Store and form items so they can be retrieved and enabled/shown etc.
    private final Map<Component, FormItem> fieldsWithFormItems = new HashMap<>(10);

    /**
     * Use this if you do not require binding and will add components to a layout
     * yourself.
     */
    public FormHelper() {
        this(null, null, null);
    }

    /**
     * Use this if you do not require binding but require components to be added to
     * a layout.
     * 
     * @param layout The layout to add components to.
     */
    public FormHelper(final Component layout) {
        this(null, layout, null);
    }

    /**
     * Use this if you require binding but will add components to a layout yourself
     *
     * @param entityClass The class of the entity being bound to.
     * @param binder      The binder to bind to.
     */
    public FormHelper(final Class<E> entityClass, final Binder<E> binder) {
        this(entityClass, null, binder);
    }

    /**
     * Use this if you require binding and also require components to be added to a
     * layout
     * 
     * @param entityClass The class of the entity being bound to.
     * @param layout      the layout to add components to.
     * @param binder      The binder to bind to.
     */
    public FormHelper(final Class<E> entityClass, final Component layout, final Binder<E> binder) {
        this.entityClass = entityClass;
        this.layout = layout;
        this.binder = binder;
    }

    // Single components
    /**
     * Creates and binds a {@link TextField} for {@link String}, {@link Long},
     * {@link Double} and {@link BigDecimal} attribute types only.<br>
     * The {@link SingularAttribute} parameter is used to determine if converting is
     * needed, and which {@link Converter} is required.<br>
     * 
     * If an attribute type is not matched, the field is created unbound.
     * 
     * @param caption           A {@link String} that will be used to caption the
     *                          field.
     * @param propertyAttribute A {@link SingularAttribute} for binding and
     *                          determining converter types (if required).
     * @param validator         A {@link Validator} of type {@link String} to add
     *                          validation to the field. if null, validation is not
     *                          set.
     * @return A {@link TextField}, bound to the {@link Binder} if attribute matches
     *         the correct type.
     */
    public TextField bindTextField(final String caption, final SingularAttribute<E, ?> propertyAttribute,
            final Validator<String> validator) {
        final String bindingProperty = propertyAttribute.getName();
        checkState(bindingProperty);
        final TextField field = new TextField(caption);
        field.setWidthFull();
        field.setClearButtonVisible(true);
        field.setId(entityClass.getSimpleName() + "-" + bindingProperty + "-" + caption);
        final Class<?> propertyJavaType = propertyAttribute.getType().getJavaType();
        final Converter<String, ?> converter;
        if (propertyJavaType.equals(Long.class)) {
            converter = new LongNoGroupingConverter("Error, number must be a whole number.");
        } else if (propertyJavaType.equals(Double.class)) {
            converter = new StringToDoubleConverter("Error, number format is incorrect.");
        } else if (propertyJavaType.equals(BigDecimal.class)) {
            converter = new StringToBigDecimalConverter("Error, number format is incorrect.");
        } else if (propertyJavaType.equals(String.class)) {
            converter = null;
        } else {
            logger.error(bindingProperty + " is unbound. Type required: " + propertyAttribute.getBindableJavaType());
            return field;
        }
        final BindingBuilder<E, String> bindingBuilderString = binder.forField(field);
        if (converter != null) {
            if (validator != null) {
                bindingBuilderString.withValidator(validator).withNullRepresentation("").withConverter(converter);
                field.setValueChangeMode(ValueChangeMode.EAGER);
            } else {
                bindingBuilderString.withNullRepresentation("").withConverter(converter);
            }
        } else if (validator != null) {
            bindingBuilderString.withValidator(validator).withNullRepresentation("");
            field.setValueChangeMode(ValueChangeMode.EAGER);
        } else {
            bindingBuilderString.withNullRepresentation("");
        }

        bindingBuilderString.bind(bindingProperty);

        addComponentIfRequired(field);

        return field;
    }

    /**
     * Creates and binds a {@link Checkbox} for {@link Boolean} attribute types
     * only.
     * 
     * @param caption           A {@link String} that will be used to caption the
     *                          field.
     * @param propertyAttribute A {@link SingularAttribute} for binding a
     *                          {@link Boolean} type only.
     * @return A {@link Checkbox}, bound to the {@link Binder}.
     */
    public Checkbox bindCheckbox(final String caption, final SingularAttribute<E, Boolean> propertyAttribute) {
        final String bindingProperty = propertyAttribute.getName();
        checkState(bindingProperty);
        final Checkbox field = new Checkbox(caption);
        field.setWidthFull();
        binder.forField(field).bind(bindingProperty);
        field.setId(entityClass.getSimpleName() + "-" + bindingProperty + "-" + caption);
        addComponentIfRequired(field);

        return field;
    }

    /**
     * Creates and binds a {@link DatePicker} for {@link Date} attribute types only.
     * 
     * @param caption           A {@link String} that will be used to caption the
     *                          field.
     * @param propertyAttribute A {@link SingularAttribute} for binding a
     *                          {@link Date} type only.
     * @param dateFormat        A {@link String} being the date format that will be
     *                          displayed in the field.
     * @param validator         A {@link Validator} of type {@link LocalDate} to add
     *                          validation to the field. if null, validation is not
     *                          set.
     * @return A {@link DatePicker}, bound to the {@link Binder}.
     */
    public DatePicker bindDatePicker(final String caption, final SingularAttribute<E, Date> propertyAttribute,
            final String dateFormat, final Validator<LocalDate> validator) {
        final String bindingProperty = propertyAttribute.getName();
        checkState(bindingProperty);
        final DatePicker field = new DatePicker(caption);
        field.setI18n(VaadinHelper.setCustomDateFormats(dateFormat));
        field.setWidthFull();
        final BindingBuilder<E, LocalDate> bindingBuilderDate = binder.forField(field);
        if (validator != null) {
            bindingBuilderDate.withValidator(validator);
        }
        bindingBuilderDate.withConverter(new LocalDateToDateConverter()).bind(bindingProperty);
        field.setId(entityClass.getSimpleName() + "-" + bindingProperty + "-" + caption);
        addComponentIfRequired(field);

        return field;
    }

    /**
     * Creates and binds a {@link TextArea} for {@link String} attribute types only.
     * 
     * @param caption           A {@link String} that will be used to caption the
     *                          field.
     * @param propertyAttribute A {@link SingularAttribute} for binding a
     *                          {@link String} type only.
     * @param validator         A {@link Validator} of type {@link String} to add
     *                          validation to the field. if null, validation is not
     *                          set.
     * @return A {@link TextArea}, bound to the {@link Binder}.
     */
    public TextArea bindTextArea(final String caption, final SingularAttribute<E, String> propertyAttribute,
            final Validator<String> validator) {
        final String bindingProperty = propertyAttribute.getName();
        checkState(bindingProperty);
        final TextArea field = new TextArea(caption);
        field.setWidthFull();
        field.setClearButtonVisible(true);
        final BindingBuilder<E, String> bindingBuilderString = binder.forField(field).withNullRepresentation("");
        if (validator != null) {
            bindingBuilderString.withValidator(validator);
            field.setValueChangeMode(ValueChangeMode.EAGER);
        }
        bindingBuilderString.bind(bindingProperty);
        field.setId(entityClass.getSimpleName() + "-" + bindingProperty + "-" + caption);
        addComponentIfRequired(field);

        return field;
    }

    /**
     * Returns a {@link ComboBoxBuilder} for binding a {@link ComboBox} using either
     * a {@link DataProvider} or backed by a {@link List} of entities.<br>
     * Note: if {@link DataProvider} not supplied, an error is generated.
     * 
     * @param <J>       Extends {@link CrudEntity}, ensuring the field is type safe.
     * @param itemClass The entity class.
     * @return A {@link ComboBoxBuilder} to allow creation of a {@link ComboBox}.
     */
    public <J extends CrudEntity> ComboBoxBuilder<J> getComboBoxBuilder(final Class<J> itemClass) {
        return new ComboBoxBuilder<>();
    }

    /**
     * {@link ComboBoxBuilder} class.<br>
     * Create a {@link ComboBox} using {@link #getComboBoxBuilder(Class)} method
     * rather, than creating from here.
     *
     * @param <L> Extends {@link CrudEntity}.
     */
    public class ComboBoxBuilder<L extends CrudEntity> {
        private ComboBox<L> component = null;
        private String caption = null;
        private DataProvider<L, String> dataProvider;
        private String property;
        private ItemLabelGenerator<L> itemLabelGenerator;
        private Integer popupWidth = null;
        private Validator<L> validator = null;

        private ComboBoxBuilder() {
        }

        public ComboBox<L> build() {
            checkState(property);

            if (component == null) {
                component = new ComboBox<>(caption);
                component.setWidthFull();

                if (dataProvider != null) {
                    component.setDataProvider(dataProvider);
                } else {
                    throw new NullPointerException("You must provide a DataProvider.");
                }

                if (itemLabelGenerator != null) {
                    component.setItemLabelGenerator(itemLabelGenerator);
                }

                if (popupWidth != null) {
                    component.getStyle().set("--vaadin-combo-box-overlay-width", popupWidth + "px");
                }

                if (property != null) {
                    final BindingBuilder<E, L> bindingBuilder2 = binder.forField(component);
                    if (validator != null) {
                        bindingBuilder2.withValidator(validator);
                    }
                    bindingBuilder2.bind(property);
                }

                addComponentIfRequired(component);
            }

            return component;
        }

        public ComboBoxBuilder<L> setLabel(final String caption) {
            this.caption = caption;
            return this;
        }

        public ComboBoxBuilder<L> setDataProvider(final DataProvider<L, String> dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }

        public ComboBoxBuilder<L> setProperty(final String property) {
            this.property = property;
            return this;
        }

        public ComboBoxBuilder<L> setProperty(final SingularAttribute<E, L> attribute) {
            this.property = attribute.getName();
            return this;
        }

        public ComboBoxBuilder<L> setItemLabelGenerator(final ItemLabelGenerator<L> labelGenerator) {
            this.itemLabelGenerator = labelGenerator;
            return this;
        }

        public ComboBoxBuilder<L> setPopupWidth(final Integer popupWidth) {
            this.popupWidth = popupWidth;
            return this;
        }

        public ComboBoxBuilder<L> setValidator(final Validator<L> validator) {
            this.validator = validator;
            return this;
        }
    }

    // Composite Fields
    /**
     * Creates and binds a {@link TextFieldWithButton} for {@link String} attribute
     * types only.
     * 
     * @param caption           A {@link String} that will be used to caption the
     *                          field.
     * @param button            A {@link Button} to add to the field. Note, if none
     *                          supplied, {@link TextFieldWithButton} will create a
     *                          default button with no caption. This button can be
     *                          accessed via the fields interface to add
     *                          functionality.
     * @param propertyAttribute A {@link SingularAttribute} for binding a
     *                          {@link String} type only.
     * @param validator         A {@link Validator} of type {@link String} to add
     *                          validation to the field. if null, validation is not
     *                          set.
     * @return A {@link TextFieldWithButton}, the field bound to the {@link Binder}
     *         (but not the button).
     */
    public TextFieldWithButton bindTextFieldWithButton(final String caption, final Button button,
            final SingularAttribute<E, String> propertyAttribute, final Validator<String> validator) {
        final String bindingProperty = propertyAttribute.getName();
        checkState(bindingProperty);
        final TextFieldWithButton field = new TextFieldWithButton(caption, button);
        field.setFieldWidth("100%");
        field.getField().setClearButtonVisible(true);
        final BindingBuilder<E, String> bindingBuilderString = binder.forField(field.getField());
        if (validator != null) {
            bindingBuilderString.withValidator(validator);
            field.getField().setValueChangeMode(ValueChangeMode.EAGER);
        }
        bindingBuilderString.withNullRepresentation("");
        bindingBuilderString.bind(bindingProperty);

        field.setId(entityClass.getSimpleName() + "-" + bindingProperty + "-" + caption);
        addComponentIfRequired(field);
        return field;
    }

    /**
     * Returns a {@link ComboBoxWithButtonBuilder} for binding the field in a
     * {@link ComboBoxWithButton} using either a {@link DataProvider} or backed by a
     * {@link List} of entities.<br>
     * Note: if {@link DataProvider} not supplied, an error is generated.
     * 
     * @param <J>       Extends {@link CrudEntity}, ensuring the field is type safe.
     * @param itemClass The entity class.
     * @return A {@link ComboBoxWithButtonBuilder} to allow creation of a
     *         {@link ComboBoxWithButton}.
     */
    public <J extends CrudEntity> ComboBoxWithButtonBuilder<J> getComboBoxWithButtonBuilder(final Class<J> itemClass) {
        return new ComboBoxWithButtonBuilder<>();
    }

    /**
     * {@link ComboBoxWithButton} class. Create a {@link ComboBoxWithButton} using
     * {@link #getComboBoxWithButtonBuilder(Class)} method, rather than creating
     * from here.
     *
     * @param <L> Extends {@link CrudEntity}.
     */
    public class ComboBoxWithButtonBuilder<L extends CrudEntity> {

        private ComboBoxWithButton<L> component = null;
        private String caption = null;
        private DataProvider<L, String> dataProvider;
        private String property;
        private ItemLabelGenerator<L> itemLabelGenerator;
        private Integer popupWidth = null;
        private Button button;
        private Validator<L> validator = null;

        private ComboBoxWithButtonBuilder() {
        }

        public ComboBoxWithButton<L> build() {
            checkState(property);

            if (component == null) {
                component = new ComboBoxWithButton<>(caption, button);

                component.setFieldWidth("100%");

                if (dataProvider != null) {
                    component.setDataProvider(dataProvider);
                } else {
                    throw new NullPointerException("You must provide a DataProvider.");
                }

                if (itemLabelGenerator != null) {
                    component.getField().setItemLabelGenerator(itemLabelGenerator);
                }

                if (popupWidth != null) {
                    component.getField().getStyle().set("--vaadin-combo-box-overlay-width", popupWidth + "px");
                }

                if (property != null) {
                    final BindingBuilder<E, L> bindingBuilder2 = binder.forField(component.getField());
                    if (validator != null) {
                        bindingBuilder2.withValidator(validator);
                    }
                    bindingBuilder2.bind(property);
                    component.setId(entityClass.getSimpleName() + "-" + property + "-" + caption);
                }

                addComponentIfRequired(component);
            }

            return component;
        }

        public ComboBoxWithButtonBuilder<L> setLabel(final String caption) {
            this.caption = caption;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setDataProvider(final DataProvider<L, String> dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setProperty(final String property) {
            this.property = property;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setProperty(final SingularAttribute<E, L> attribute) {
            this.property = attribute.getName();
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setItemLabelGenerator(final ItemLabelGenerator<L> labelGenerator) {
            this.itemLabelGenerator = labelGenerator;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setPopupWidth(final Integer popupWidth) {
            this.popupWidth = popupWidth;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setButton(final Button button) {
            this.button = button;
            return this;
        }

        public ComboBoxWithButtonBuilder<L> setValidator(final Validator<L> validator) {
            this.validator = validator;
            return this;
        }
    }

    // Common methods
    private void checkState(final String bindingProperty) {
        if (bindingProperty != null) {
            Preconditions.checkNotNull(binder, "A binding property must be supplied to allow binding to fields");
        }
    }

    /**
     * Add components created outside of this and add to the layout.<br>
     * If layout is not supplied through the constructor, an error is thrown.
     * 
     * @param field The {@link Component} to add to the layout.
     */
    public void addComponent(final Component field) {
        Preconditions.checkNotNull(layout, "A layout has not been set");
        addComponentIfRequired(field);
    }

    /*
     * Adds the component to the layout (if layout is supplied through a
     * constructor. Note: Adding to a FormLayout is achieved by detecting the layout
     * supplied through the constructor is actually a FormLayout. If so, the
     * captions are used to add as a separate caption on the FormItem and removed
     * from the component. Otherwise the component is just added directly to the
     * form, allowing it to decide the flex and layout. Extra Note: GridLayout does
     * not extend HasComponents, so need to trap separately to add components to it.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addComponentIfRequired(final Component field) {
        if (layout != null) {
            if (layout instanceof FormLayout) {
                final String caption;
                if (field instanceof HasLabel) {
                    final HasLabel field2 = (HasLabel) field;
                    caption = field2.getLabel();
                    field2.setLabel(null);
                } else {
                    if (field.getClass().equals(ComboBoxWithButton.class)) {
                        final ComboBoxWithButton<E> field3 = (ComboBoxWithButton) field;
                        caption = field3.getLabel();
                        field3.setLabel(null);
                        field3.setAlignItems(Alignment.CENTER);
                    } else if (field.getClass().equals(TextFieldWithButton.class)) {
                        final TextFieldWithButton field4 = (TextFieldWithButton) field;
                        caption = field4.getLabel();
                        field4.setLabel(null);
                        field4.setAlignItems(Alignment.CENTER);
                    } else {
                        caption = null;
                    }
                }
                final FormItem formItem = ((FormLayout) layout).addFormItem(field, caption);
                fieldsWithFormItems.put(field, formItem);
            } else if (layout instanceof GridLayout) {
                ((GridLayout) layout).addComponent(field);
            } else {
                ((HasComponents) layout).add(field);
            }
        }
    }

    /**
     * Convenience method to use the binder to bind fields created elsewhere.<br>
     * Useful for fields wrapped in a CustomField or where just binding only is
     * required.
     * 
     * @param field             The {@link Component} to bind.
     * @param propertyAttribute A {@link SingularAttribute} of E, ?
     * @param validator         A {@link Validator} of Object to allow adding to
     *                          fields other than of String type. (Even though
     *                          realistically, we'd never need to validate a
     *                          checkbox.). Can be null.
     * @return The bound {@link Component} with optional validation.
     */
    public Component bind(final Component field, final SingularAttribute<E, ?> propertyAttribute,
            final Validator<Object> validator) {
        return bind(field, propertyAttribute.getName(), validator);
    }

    /**
     * Convenience method to use the binder to bind fields created elsewhere.<br>
     * Useful for fields wrapped in a CustomField or where just binding only is
     * required.
     * 
     * @param field     The {@link Component} to bind.
     * @param property  A {@link String} property.
     * @param validator A {@link Validator} of Object to allow adding to fields
     *                  other than of String type. (Even though realistically, we'd
     *                  never need to validate a checkbox.). Can be null.
     * @returnThe bound {@link Component} with optional validation.
     */
    @SuppressWarnings("unchecked")
    public Component bind(final Component field, final String property, final Validator<Object> validator) {
        BindingBuilder<E, ?> bindingBuilder = null;
        if (binder != null && property != null) {
            if (field instanceof TextField) {
                bindingBuilder = binder.forField((TextField) field);
            } else if (field instanceof Checkbox) {
                bindingBuilder = binder.forField((Checkbox) field);
            } else if (field instanceof TextArea) {
                bindingBuilder = binder.forField((TextArea) field);
            } else if (field instanceof TextFieldWithButton) {
                bindingBuilder = binder.forField(((TextFieldWithButton) field).getField());
            } else if (field instanceof ComboBox) {
                bindingBuilder = binder.forField((ComboBox<E>) field);
            } else if (field instanceof ComboBoxWithButton) {
                bindingBuilder = binder.forField(((ComboBoxWithButton<E>) field).getField());
            } else if (field instanceof DatePicker) {
                bindingBuilder = binder.forField((DatePicker) field);
            }

            Preconditions.checkState(bindingBuilder != null,
                    "Field type not processed for binding '" + field.getClass() + "'");

            if (validator != null) {
                bindingBuilder.withValidator(validator);
            }
            bindingBuilder.bind(property);
        }
        return field;
    }

    /**
     * 
     * @return A {@link Map} of {@link Component}/ {@link FormItem} pairs. useful if
     *         you need to enable/disable the form item.
     */
    public Map<Component, FormItem> getFieldsWithFormItems() {
        return this.fieldsWithFormItems;
    }
}