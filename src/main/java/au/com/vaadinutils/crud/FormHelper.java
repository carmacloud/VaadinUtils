package au.com.vaadinutils.crud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.lazyquerycontainer.EntityContainer;
import org.vaadin.ui.LegacyComboBox;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import au.com.vaadinutils.converter.ContainerAdaptor;
import au.com.vaadinutils.converter.ContainerAdaptorFactory;
import au.com.vaadinutils.converter.MultiSelectConverter;
import au.com.vaadinutils.crud.GridHeadingPropertySet.Builder;
import au.com.vaadinutils.crud.splitFields.SplitCheckBox;
import au.com.vaadinutils.crud.splitFields.SplitColorPicker;
import au.com.vaadinutils.crud.splitFields.SplitComboBox;
import au.com.vaadinutils.crud.splitFields.SplitDateField;
import au.com.vaadinutils.crud.splitFields.SplitListSelect;
import au.com.vaadinutils.crud.splitFields.SplitPasswordField;
import au.com.vaadinutils.crud.splitFields.SplitTextArea;
import au.com.vaadinutils.crud.splitFields.SplitTextField;
import au.com.vaadinutils.crud.splitFields.SplitTwinColSelect;
import au.com.vaadinutils.crud.splitFields.legacy.LegacySplitComboBox;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.Path;
import au.com.vaadinutils.domain.iColor;
import au.com.vaadinutils.domain.iColorFactory;
import au.com.vaadinutils.fields.AdvancedSearchContentProvider;
import au.com.vaadinutils.fields.ColorPickerField;
import au.com.vaadinutils.fields.ComboBoxWithSearchField;
import au.com.vaadinutils.fields.DataBoundButton;
import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;

/**
 * Replaced in V14 migration.
 */
public class FormHelper<E> implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String STANDARD_COMBO_WIDTH = "220";

    ArrayList<AbstractComponent> fieldList = new ArrayList<>();
    private final AbstractLayout form;
    private final ValidatingFieldGroup<E> group;
    private final Set<ValueChangeListener> valueChangeListeners = new LinkedHashSet<>();

    static transient Logger logger = LogManager.getLogger(FormHelper.class);

    public FormHelper(final AbstractLayout form, final ValidatingFieldGroup<E> group) {
        // I'm actually using this without a field group.
        // need to makes some modifications so that we formally support
        // non-group usage.
        // Preconditions.checkNotNull(group,
        // "ValidatingFieldGroup can not be null");
        this.form = form;
        this.group = group;
    }

    /**
     * The added value change listener will get added to every component that's
     * created with the FormHelper
     *
     * @param listener the value change listener
     */
    public void addValueChangeListener(final ValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    @SuppressWarnings("rawtypes")
    private void addValueChangeListeners(final AbstractField c) {
        for (final ValueChangeListener listener : valueChangeListeners) {
            c.addValueChangeListener(listener);
        }
    }

    public <M> TextField bindTextField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final SingularAttribute<E, M> member) {
        final TextField field = bindTextField(form, group, fieldLabel, member.getName());
        this.fieldList.add(field);
        return field;
    }

    public <M> TextField bindTextField(final String fieldLabel, final SingularAttribute<E, M> member) {
        final TextField field = bindTextField(form, group, fieldLabel, member.getName());
        this.fieldList.add(field);
        return field;
    }

    public <M> TextField bindTextFieldWithButton(final String fieldLabel, final SingularAttribute<E, M> member,
            final Button button) {

        final TextField field = bindTextFieldWithButton(form, group, fieldLabel, member.getName(), button);

        this.fieldList.add(field);

        return field;
    }

    public TextField bindTextField(final String fieldLabel, final String fieldName) {
        final TextField field = bindTextField(form, group, fieldLabel, fieldName);
        this.fieldList.add(field);
        return field;
    }

    public <T extends CustomField<?>> T doBinding(final SingularAttribute<?, ?> field, final T customField) {

        doBinding(group, field.getName(), customField);
        this.fieldList.add(customField);
        form.addComponent(customField);
        return customField;

    }

    public <T extends CustomField<?>> T doBinding(final SetAttribute<?, ?> field, final T customField) {

        doBinding(group, field.getName(), customField);
        this.fieldList.add(customField);
        form.addComponent(customField);
        return customField;

    }

    public <T extends CustomField<?>> T doBinding(final ListAttribute<?, ?> field, final T customField) {

        doBinding(group, field.getName(), customField);
        this.fieldList.add(customField);
        form.addComponent(customField);
        return customField;

    }

    public TextField bindTextField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName) {
        final TextField field = new SplitTextField(fieldLabel);
        field.setWidth("100%");
        field.setImmediate(true);
        field.setNullRepresentation("");
        field.setNullSettingAllowed(false);
        field.setId(fieldLabel.replace(" ", ""));
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

    public TextField bindTextFieldWithButton(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName, final Button button) {

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();

        final TextField field = new SplitTextField(fieldLabel);
        field.setWidth("100%");
        field.setImmediate(true);
        field.setNullRepresentation("");
        field.setNullSettingAllowed(false);
        field.setId(fieldLabel.replace(" ", ""));
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);

        layout.addComponent(field);
        layout.addComponent(button);

        layout.setExpandRatio(field, 2);

        form.addComponent(layout);

        return field;
    }

    public void doBinding(final FieldGroup group, final String fieldName,
            @SuppressWarnings("rawtypes") final Field field) {
        if (group != null) {
            group.bind(field, fieldName);
        } else

        {
            logger.warn("field {}  was not bound", fieldName);
        }
    }

    public <M> PasswordField bindPasswordField(final AbstractLayout form, final FieldGroup group,
            final String fieldLabel, final SingularAttribute<E, M> member) {
        final PasswordField field = bindPasswordField(form, group, fieldLabel,
                (member != null ? member.getName() : null));
        this.fieldList.add(field);
        return field;
    }

    public <M> PasswordField bindPasswordField(final String fieldLabel, final SingularAttribute<E, M> member) {
        final PasswordField field = bindPasswordField(form, group, fieldLabel,
                (member != null ? member.getName() : null));
        this.fieldList.add(field);
        return field;
    }

    public PasswordField bindPasswordField(final AbstractLayout form, final FieldGroup group, final String fieldLabel,
            final String fieldName) {
        final PasswordField field = new SplitPasswordField(fieldLabel);
        field.setWidth("100%");
        field.setImmediate(true);
        field.setNullRepresentation("");
        field.setNullSettingAllowed(false);
        field.setId(fieldLabel.replace(" ", ""));
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

    public <M> TextArea bindTextAreaField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final SingularAttribute<E, M> member, final int rows) {
        final TextArea field = bindTextAreaField(form, group, fieldLabel, member.getName(), rows);
        this.fieldList.add(field);
        return field;
    }

    public TextArea bindTextAreaField(final String fieldLabel, final SingularAttribute<? super E, String> attribute,
            final int rows) {
        final TextArea field = bindTextAreaField(form, group, fieldLabel, attribute.getName(), rows);
        this.fieldList.add(field);
        return field;
    }

    public TextArea bindTextAreaField(final String fieldLabel, final String fieldName, final int rows) {
        final TextArea field = bindTextAreaField(form, group, fieldLabel, fieldName, rows);
        this.fieldList.add(field);
        return field;
    }

    public TextArea bindTextAreaField(final String fieldLabel, final String fieldName, final int rows,
            final int maxlength) {
        final TextArea field = bindTextAreaField(form, group, fieldLabel, fieldName, rows);
        field.setMaxLength(maxlength);
        this.fieldList.add(field);
        return field;
    }

    public TextArea bindTextAreaField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName, final int rows) {
        final TextArea field = new SplitTextArea(fieldLabel);
        field.setRows(rows);
        field.setWidth("100%");
        field.setImmediate(true);
        field.setNullRepresentation("");
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

    public DateField bindDateField(final String fieldLabel, final String fieldName) {
        final DateField field = bindDateField(form, group, fieldLabel, fieldName);
        this.fieldList.add(field);
        return field;
    }

    public DateField bindDateField(final String label, final String member, final String dateFormat,
            final Resolution resolution) {
        final DateField field = bindDateField(form, group, label, member, dateFormat, resolution);
        field.setWidth(STANDARD_COMBO_WIDTH);
        this.fieldList.add(field);
        return field;
    }

    public DateField bindDateField(final String label, final SingularAttribute<? super E, Date> member,
            final String dateFormat, final Resolution resolution) {
        final DateField field = bindDateField(form, group, label, member.getName(), dateFormat, resolution);
        field.setWidth(STANDARD_COMBO_WIDTH);
        this.fieldList.add(field);
        return field;
    }

    public <M> DateField bindDateField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final SingularAttribute<E, M> member, final String dateFormat,
            final Resolution resolution) {
        final DateField field = bindDateField(form, group, fieldLabel, member.getName(), dateFormat, resolution);
        this.fieldList.add(field);
        return field;
    }

    public DateField bindDateField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName, final String dateFormat, final Resolution resolution) {
        final DateField field = new SplitDateField(fieldLabel);
        field.setDateFormat(dateFormat);
        field.setResolution(resolution);

        field.setImmediate(true);
        field.setWidth("100%");
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

    public DateField bindDateField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName) {
        return bindDateField(form, group, fieldLabel, fieldName, "yyyy-MM-dd", Resolution.DAY);
    }

    public <M> ComboBox bindEnumField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final SingularAttribute<E, M> member, final Class<?> clazz) {
        final ComboBox field = bindEnumField(form, group, fieldLabel, member.getName(), clazz);
        this.fieldList.add(field);
        return field;
    }

    public ComboBox bindEnumField(final String fieldLabel, final String fieldName, final Class<?> clazz) {
        final ComboBox field = bindEnumField(form, group, fieldLabel, fieldName, clazz);
        this.fieldList.add(field);
        return field;
    }

    public ComboBox bindEnumField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName, final Class<?> clazz) {
        return bindEnumField(new SplitComboBox(fieldLabel), form, group, fieldLabel, fieldName, clazz);
    }

    public ComboBox bindEnumField(final ComboBox comboBox, final AbstractLayout form,
            final ValidatingFieldGroup<E> group, final String fieldLabel, final String fieldName,
            final Class<?> clazz) {
        final ComboBox field = comboBox;
        field.setCaption(fieldLabel);
        field.setContainerDataSource(createContainerFromEnumClass(fieldName, clazz));
        field.setItemCaptionPropertyId(fieldName);
        // field.setCaption(fieldLabel);
        field.setNewItemsAllowed(false);
        field.setNullSelectionAllowed(false);
        field.setTextInputAllowed(true);
        field.setWidth(STANDARD_COMBO_WIDTH);
        field.setPopupWidth("100%");

        field.setImmediate(true);
        field.setId(fieldLabel.replace(" ", ""));
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);

        form.addComponent(field);
        return field;
    }

    public <M> CheckBox bindBooleanField(final String fieldLabel, final SingularAttribute<E, M> member) {
        final CheckBox field = bindBooleanField(form, group, fieldLabel, member.getName());
        this.fieldList.add(field);
        return field;
    }

    public CheckBox bindBooleanField(final String fieldLabel, final String fieldName) {
        final CheckBox field = bindBooleanField(form, group, fieldLabel, fieldName);
        this.fieldList.add(field);
        return field;
    }

    public CheckBox bindBooleanField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final SingularAttribute<E, Boolean> member) {
        final CheckBox field = bindBooleanField(form, group, fieldLabel, member.getName());
        this.fieldList.add(field);
        return field;

    }

    public CheckBox bindBooleanField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName) {
        final CheckBox field = new SplitCheckBox(fieldLabel);
        field.setWidth("100%");
        field.setImmediate(true);
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

    public ColorPickerField bindColorPickerField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final iColorFactory factory, final String fieldLabel, final SingularAttribute<E, iColor> member) {
        final ColorPickerField field = bindColorPickerField(form, group, factory, fieldLabel, member.getName());
        this.fieldList.add(field);
        return field;

    }

    public ColorPickerField bindColorPickerField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final iColorFactory factory, final String fieldLabel, final String fieldName) {
        final ColorPickerField field = new SplitColorPicker(factory, fieldLabel);
        field.setWidth("100%");
        field.setImmediate(true);

        doBinding(group, fieldName, field);

        form.addComponent(field);
        return field;
    }

    public <L> ComboBox bindComboBox(final AbstractLayout form, final String fieldName, final String fieldLabel,
            final Collection<?> options) {
        final ComboBox field = new SplitComboBox(fieldLabel, options);
        field.setNewItemsAllowed(false);
        field.setNullSelectionAllowed(false);
        field.setTextInputAllowed(true);
        field.setWidth(STANDARD_COMBO_WIDTH);
        field.setPopupWidth("100%");
        field.setImmediate(true);
        form.addComponent(field);
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        return field;
    }

    public <L> ComboBox bindComboBox(final AbstractLayout form, final String fieldName, final String fieldLabel,
            final Container options) {
        final ComboBox field = new SplitComboBox(fieldLabel, options);
        field.setNewItemsAllowed(false);
        field.setNullSelectionAllowed(false);
        field.setTextInputAllowed(true);
        field.setWidth(STANDARD_COMBO_WIDTH);
        field.setPopupWidth("100%");
        field.setImmediate(true);
        form.addComponent(field);
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        return field;
    }

    public <L> LegacyComboBox bindLegacyComboBox(final AbstractLayout form, final String fieldName,
            final String fieldLabel, final Collection<?> options) {
        final LegacyComboBox field = new LegacySplitComboBox(fieldLabel, options);
        field.setNewItemsAllowed(false);
        field.setNullSelectionAllowed(false);
        field.setTextInputAllowed(true);
        field.setWidth(STANDARD_COMBO_WIDTH);
        field.setImmediate(true);
        form.addComponent(field);
        addValueChangeListeners(field);
        doBinding(group, fieldName, field);
        return field;
    }

    public <L extends CrudEntity, K> ComboBox bindEntityField(final String fieldLabel,
            final SingularAttribute<E, L> fieldName, final SingularAttribute<? super L, K> listFieldName) {
        return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListFieldName(listFieldName)
                .build();

    }

    @Deprecated
    public <L extends CrudEntity> ComboBox bindEntityField(final String fieldLabel, final String fieldName,
            final Class<L> listClazz, final String listFieldName) {
        return new EntityFieldBuilder<L>().setLabel(fieldLabel).setField(fieldName).setListClass(listClazz)
                .setListFieldName(listFieldName).build();

    }

    /**
     * Deprecated - use EntityFieldBuilder instead
     * 
     * this method is for displaying a combobox which displayes all the values for a
     * many to one relationship and allows the user to select only one.
     * 
     * @param form
     * @param fieldGroup
     * @param fieldLabel
     * @param field
     * @param listClazz
     * @param listFieldName
     * 
     *                      E is the entity F is the set from the entity that
     *                      contains the foriegn entities L is the foriegn entity M
     *                      is the field to display from the foriegn entity
     * 
     * @return
     */

    /**
     * Deprecated - use EntityFieldBuilder instead
     * 
     * @param form
     * @param fieldGroup
     * @param fieldLabel
     * @param field
     * @param listClazz
     * @param listFieldName
     * @return
     */
    @Deprecated
    public <L extends CrudEntity> ComboBox bindEntityField(final AbstractLayout form,
            final ValidatingFieldGroup<E> fieldGroup, final String fieldLabel, final SingularAttribute<E, L> field,
            final Class<L> listClazz, final SingularAttribute<L, ?> listFieldName) {
        return new EntityFieldBuilder<L>().setForm(form).setLabel(fieldLabel).setField(field)
                .setListFieldName(listFieldName).build();

    }

    /**
     * Deprecated - use EntityFieldBuilder instead
     * 
     * @param form
     * @param fieldGroup
     * @param fieldLabel
     * @param fieldName
     * @param listClazz
     * @param listFieldName
     * @return
     */
    @Deprecated
    public <L extends CrudEntity> ComboBox bindEntityField(final AbstractLayout form,
            final ValidatingFieldGroup<E> fieldGroup, final String fieldLabel, final String fieldName,
            final Class<L> listClazz, final String listFieldName) {
        return new EntityFieldBuilder<L>().setForm(form).setLabel(fieldLabel).setField(fieldName)
                .setListClass(listClazz).setListFieldName(listFieldName).build();

    }

    /**
     * Deprecated - use EntityFieldBuilder instead
     * 
     * @param field
     * @param form
     * @param fieldGroup
     * @param fieldLabel
     * @param fieldName
     * @param listClazz
     * @param listFieldName
     * @return
     */
    @Deprecated
    public <L extends CrudEntity> ComboBox bindEntityField(final ComboBox field, final AbstractLayout form,
            final ValidatingFieldGroup<E> fieldGroup, final String fieldLabel, final String fieldName,
            final Class<L> listClazz, final String listFieldName) {
        return new EntityFieldBuilder<L>().setComponent(field).setForm(form).setLabel(fieldLabel).setField(fieldName)
                .setListClass(listClazz).setListFieldName(listFieldName).build();

    }

    /**
     * use this syntax to instance the builder:<br>
     * formHelper.new EntityFieldBuilder<{name of list class}>(); <br>
     * <br>
     * for example<br>
     * <br>
     * 
     * FormHelper&lt;RaffleBook&gt; helper = new
     * FormHelper&lt;RaffleBook&gt;(...);<br>
     * <br>
     * ComboBox field = helper.new EntityFieldBuilder&lt;RaffleAllocation&gt;()<br>
     * .setLabel("Action")<br>
     * .setField(RaffleBook.allocation)<br>
     * .setListFieldName(RaffleAllocation_.name)<br>
     * .build();<br>
     * 
     * @author rsutton
     * 
     * @param <L> the type of the list class
     */
    public class EntityFieldBuilderV2<L extends CrudEntity, C extends Indexed & Filterable> {

        private ComboBoxWithSearchField<L, C> component = null;
        private String label = null;
        private C container = null;
        private Class<? extends L> listClazz;
        private String field;
        private AbstractLayout builderForm;
        private final Builder<L> headingBuilder = new GridHeadingPropertySet.Builder<>();
        private final List<String> sortColumns = new LinkedList<>();
        private AdvancedSearchContentProvider advancedSearchProvider = null;

        private AdvancedSearchListener advancedSearchListener = null;

        public ComboBoxWithSearchField<L, C> build() {
            Preconditions.checkArgument(group == null || field != null, "Field may not be null");
            if (builderForm == null) {
                builderForm = form;
            }
            Preconditions.checkNotNull(builderForm, "Form may not be null");
            component = new ComboBoxWithSearchField<L, C>(label, listClazz, container, headingBuilder,
                    sortColumns.toArray(new String[] {}), advancedSearchProvider, advancedSearchListener);

            if (label != null) {
                component.setId(label.replace(" ", ""));
            }

            component.setImmediate(true);
            addValueChangeListeners(component);
            if (group != null) {
                Collection<? extends Object> ids = null;
                if (group.getContainer() != null) {
                    ids = group.getContainer().getContainerPropertyIds();
                } else if (group.getItemDataSource() != null) {
                    ids = group.getItemDataSource().getItemPropertyIds();
                }

                Preconditions.checkNotNull(ids,
                        "The group must have either a Container or an ItemDataSource attached.");

                Preconditions.checkState(ids.contains(field),
                        field + " is not valid, valid listFieldNames are " + ids.toString());

                doBinding(group, field, component);
            }
            builderForm.addComponent(component);
            return component;
        }

        public EntityFieldBuilderV2<L, C> setAdvancedSearchProvider(
                final AdvancedSearchContentProvider advancedSearchProvider) {
            this.advancedSearchProvider = advancedSearchProvider;
            return this;
        }

        public EntityFieldBuilderV2<L, C> setContainer(final C container) {
            this.container = container;
            return this;
        }

        public EntityFieldBuilderV2<L, C> setForm(final AbstractLayout form) {
            this.builderForm = form;
            return this;
        }

        public EntityFieldBuilderV2<L, C> setLabel(final String label) {
            this.label = label;
            return this;
        }

        public EntityFieldBuilderV2<L, C> setField(final SingularAttribute<? super E, ? extends L> field) {
            this.field = field.getName();
            listClazz = field.getJavaType();
            return this;
        }

        public <K> EntityFieldBuilderV2<L, C> addDisplayField(final SingularAttribute<? super L, K> listField,
                final String caption) {

            headingBuilder.createColumn(StringUtils.defaultString(caption, listField.getName()), listField.getName())
                    .setLockedState(true).addColumn();

            sortColumns.add(listField.getName());

            return this;
        }

        public <K> EntityFieldBuilderV2<L, C> addDisplayFieldExtends(final SingularAttribute<? extends L, K> listField,
                final String caption) {

            headingBuilder.createColumn(StringUtils.defaultString(caption, listField.getName()), listField.getName())
                    .setLockedState(true).addColumn();

            sortColumns.add(listField.getName());

            return this;
        }

        @SuppressWarnings("unchecked")
        public EntityFieldBuilderV2<L, C> addDisplayField(final Path listField, final String caption) {

            if (container instanceof JPAContainer) {
                ((JPAContainer<L>) container).addNestedContainerProperty(listField.getName());
            }
            headingBuilder.createColumn(caption, listField.getName()).setLockedState(true).addColumn();

            sortColumns.add(listField.getName());

            return this;
        }

        public EntityFieldBuilderV2<L, C> setField(final String field) {
            this.field = field;
            return this;
        }

        public EntityFieldBuilderV2<L, C> setListClass(final Class<L> listClazz) {
            Preconditions.checkState(this.listClazz == null,
                    "As you have set the field as a singularAttribute, the listClass is set automatically so there is no need to call setListClass.");
            this.listClazz = listClazz;
            return this;
        }

        public C getContainer() {
            return container;
        }

        public EntityFieldBuilderV2<L, C> setAdvancedSearchListener(
                final AdvancedSearchListener advancedSearchListener) {
            this.advancedSearchListener = advancedSearchListener;
            return this;
        }

    }

    public <L extends CrudEntity> ComboBoxWithSearchField<L, JPAContainer<L>> bindEntityFieldV2(final String fieldLabel,
            final SingularAttribute<? super E, L> fieldName) {

        return new EntityFieldBuilderV2<L, JPAContainer<L>>().setLabel(fieldLabel).setField(fieldName).build();
    }

    public <L extends CrudEntity, K> ComboBoxWithSearchField<L, JPAContainer<L>> bindEntityFieldV2(
            final String fieldLabel, final SingularAttribute<E, L> fieldName,
            final SingularAttribute<? super L, K> listField) {

        return new EntityFieldBuilderV2<L, JPAContainer<L>>().setLabel(fieldLabel).setField(fieldName)
                .addDisplayField(listField, listField.getName()).build();
    }

    public <L extends CrudEntity, K> ComboBoxWithSearchField<L, BeanItemContainer<L>> bindEntityFieldV2(
            final String fieldLabel, final SingularAttribute<E, L> fieldName,
            final SingularAttribute<? super L, K> listField, final BeanItemContainer<L> container) {

        return new EntityFieldBuilderV2<L, BeanItemContainer<L>>().setLabel(fieldLabel).setField(fieldName)
                .addDisplayField(listField, listField.getName()).setContainer(container).build();
    }

    /**
     * use this syntax to instance the builder:<br>
     * formHelper.new EntityFieldBuilder<{name of list class}>(); <br>
     * <br>
     * for example<br>
     * <br>
     * 
     * FormHelper&lt;RaffleBook&gt; helper = new
     * FormHelper&lt;RaffleBook&gt;(...);<br>
     * <br>
     * ComboBox field = helper.new EntityFieldBuilder&lt;RaffleAllocation&gt;()<br>
     * .setLabel("Action")<br>
     * .setField(RaffleBook.allocation)<br>
     * .setListFieldName(RaffleAllocation_.name)<br>
     * .build();<br>
     * 
     * @author rsutton
     * 
     * @param <L> the type of the list class
     */
    public class EntityFieldBuilder<L extends CrudEntity> {

        private ComboBox component = null;
        private String label = null;
        private Container container = null;
        private Class<L> listClazz;
        private String listField;
        // private ValidatingFieldGroup<E> fieldGroup;
        private String field;
        private AbstractLayout builderForm;

        public ComboBox build() {
            Preconditions.checkNotNull(listField, "ListField may not be null");
            Preconditions.checkArgument(group == null || field != null, "Field may not be null");
            if (builderForm == null) {
                builderForm = form;
            }
            Preconditions.checkNotNull(builderForm, "Form may not be null");

            if (component == null) {
                component = new SplitComboBox(label);
            }
            component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
            if (label != null) {
                component.setCaption(label);
                component.setId(label.replace(" ", ""));
            }

            if (container == null) {
                Preconditions.checkNotNull(listClazz, "listClazz may not be null");
                container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();

            }

            // Preconditions.checkState(container.getContainerPropertyIds().contains(listField),
            // listField
            // + " is not valid, valid listFieldNames are " +
            // container.getContainerPropertyIds().toString());

            final ContainerAdaptor<L> adaptor = ContainerAdaptorFactory.getAdaptor(container);
            if (adaptor.getSortableContainerPropertyIds().contains(listField)) {
                adaptor.sort(new String[] { listField }, new boolean[] { true });
            }

            component.setItemCaptionPropertyId(listField);
            component.setContainerDataSource(container);
            final SingleSelectConverter<L> converter = new SingleSelectConverter<>(component);
            component.setConverter(converter);
            component.setNewItemsAllowed(false);
            component.setNullSelectionAllowed(false);
            component.setTextInputAllowed(true);
            component.setWidth(STANDARD_COMBO_WIDTH);
            component.setPopupWidth("100%");
            component.setImmediate(true);
            addValueChangeListeners(component);
            if (group != null) {
                Collection<? extends Object> ids = null;
                if (group.getContainer() != null) {
                    ids = group.getContainer().getContainerPropertyIds();
                } else if (group.getItemDataSource() != null) {
                    ids = group.getItemDataSource().getItemPropertyIds();
                }

                Preconditions.checkNotNull(ids,
                        "The group must have either a Container or an ItemDataSource attached.");

                Preconditions.checkState(ids.contains(field),
                        field + " is not valid, valid listFieldNames are " + ids.toString());

                doBinding(group, field, component);
            }
            builderForm.addComponent(component);
            return component;
        }

        public EntityFieldBuilder<L> useLazyContainer() {
            container = new JpaBaseDao<L, Long>(listClazz).createLazyQueryContainer();
            return this;
        }

        public EntityFieldBuilder<L> setContainer(final JPAContainer<L> container) {
            this.container = container;
            return this;
        }

        public EntityFieldBuilder<L> setContainer(final EntityContainer<L> container) {
            this.container = container;
            return this;
        }

        public EntityFieldBuilder<L> setForm(final AbstractLayout form) {
            this.builderForm = form;
            return this;
        }

        public EntityFieldBuilder<L> setLabel(final String label) {
            this.label = label;
            return this;
        }

        public EntityFieldBuilder<L> setComponent(final ComboBox component) {
            this.component = component;
            return this;
        }

        public EntityFieldBuilder<L> setField(final SingularAttribute<? super E, L> field) {
            this.field = field.getName();
            listClazz = field.getJavaType();
            return this;
        }

        public EntityFieldBuilder<L> setField(final String field, final Class<L> listClazz) {
            this.field = field;
            this.listClazz = listClazz;
            return this;
        }

        public EntityFieldBuilder<L> setListFieldName(final SingularAttribute<? super L, ?> listField) {
            this.listField = listField.getName();
            return this;
        }

        public EntityFieldBuilder<L> setField(final String field) {
            this.field = field;
            return this;
        }

        public EntityFieldBuilder<L> setListFieldName(final String listField) {
            this.listField = listField;
            return this;
        }

        public EntityFieldBuilder<L> setListClass(final Class<L> listClazz) {
            Preconditions.checkState(this.listClazz == null,
                    "As you have set the field as a singularAttribute, the listClass is set automatically so there is no need to call setListClass.");
            this.listClazz = listClazz;
            return this;
        }

        @SuppressWarnings("unchecked")
        public JPAContainer<L> getContainer() {
            return (JPAContainer<L>) container;
        }

    }

    /**
     * use this syntax to instance the builder:<br>
     * formHelper.new EntityFieldBuilder<{name of list class}>(); <br>
     * <br>
     * for example<br>
     * <br>
     * 
     * FormHelper<TblAdvertisementSize> helper = new
     * FormHelper<TblAdvertisementSize>(...);<br>
     * <br>
     * ListSelect sections = helper.new ListSelectBuilder<TblSection>()<br>
     * .setLabel("Sections")<br>
     * .setField(TblAdvertisementSize_.sections)<br>
     * .setListFieldName("name")<br>
     * .setMultiSelect(true)<br>
     * .build(); <br>
     * 
     * @author bhorvath
     * 
     * @param <L> the type of the list class
     */
    public class ListSelectBuilder<L> {
        private SplitListSelect component = null;
        private String label = null;
        private JPAContainer<L> container = null;
        private Class<L> listClazz;
        private String listField;
        private String field;
        private AbstractLayout builderForm;
        private boolean multiSelect = false;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public SplitListSelect build() {
            Preconditions.checkNotNull(label, "label may not be null");
            Preconditions.checkNotNull(listField, "colField Property may not be null");
            if (builderForm == null) {
                builderForm = form;
            }
            Preconditions.checkNotNull(builderForm, "Form may not be null");

            if (component == null) {
                component = new SplitListSelect(label);
            }

            component.setCaption(label);
            component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
            component.setItemCaptionPropertyId(listField);

            if (container == null) {
                Preconditions.checkNotNull(listClazz, "listClazz may not be null");
                container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();
            }

            Preconditions.checkState(container.getContainerPropertyIds().contains(listField), listField
                    + " is not valid, valid listFields are " + container.getContainerPropertyIds().toString());

            if (container.getSortableContainerPropertyIds().contains(listField)) {
                container.sort(new String[] { listField }, new boolean[] { true });
            }

            component.setContainerDataSource(container);

            if (this.multiSelect == true) {
                component.setConverter(new MultiSelectConverter(component, Set.class));
                component.setMultiSelect(true);
            } else {
                final SingleSelectConverter<L> converter = new SingleSelectConverter<>(component);
                component.setConverter(converter);
            }

            component.setWidth("100%");
            component.setImmediate(true);
            component.setNullSelectionAllowed(false);
            component.setId(label.replace(" ", ""));

            if (group != null && field != null) {
                Preconditions.checkState(group.getContainer().getContainerPropertyIds().contains(field),
                        field + " is not valid, valid listFieldNames are "
                                + group.getContainer().getContainerPropertyIds().toString());

                doBinding(group, field, component);
            }
            builderForm.addComponent(component);

            return component;
        }

        public ListSelectBuilder<L> setMultiSelect(final boolean multiSelect) {
            this.multiSelect = multiSelect;
            return this;
        }

        public ListSelectBuilder<L> setLabel(final String label) {
            this.label = label;
            return this;
        }

        public ListSelectBuilder<L> setField(final SingularAttribute<E, L> field) {
            this.field = field.getName();
            listClazz = field.getBindableJavaType();
            return this;
        }

        public ListSelectBuilder<L> setField(final SetAttribute<E, L> field) {
            this.field = field.getName();
            listClazz = field.getBindableJavaType();
            return this;
        }

        /**
         * Sets the field to display from the List entity.
         * 
         * @param colField
         * @return
         */
        public ListSelectBuilder<L> setListFieldName(final SingularAttribute<L, ?> colField) {
            this.listField = colField.getName();
            return this;
        }

        public ListSelectBuilder<L> setListFieldName(final String colField) {
            this.listField = colField;
            return this;
        }

        public ListSelectBuilder<L> setContainer(final JPAContainer<L> container) {
            this.container = container;
            return this;
        }

        public ListSelectBuilder<L> setForm(final AbstractLayout form) {
            this.builderForm = form;
            return this;
        }

        public ListSelectBuilder<L> setComponent(final SplitListSelect component) {
            this.component = component;
            return this;
        }

        public ListSelectBuilder<L> setListClass(final Class<L> listClazz) {
            Preconditions.checkState(this.listClazz == null,
                    "If you set the field as a singularAttribute, the listClass is set automatically.");
            this.listClazz = listClazz;
            return this;
        }

    }

    /**
     * use this syntax to instance the builder:<br>
     * The formhelper must be an Entity with an attribute containing a Set of items
     * that will be selected.
     * 
     * formHelper.new EntityFieldBuilder<{class name of the items in the list}>();
     * <br>
     * <br>
     * If you need to filter the set of available items then you must explicity set
     * the list container and filter it.
     * 
     * for example<br>
     * <br>
     * 
     * FormHelper<TblAdvertisementSize> helper = new
     * FormHelper<TblAdvertisementSize>(...);<br>
     * <br>
     * TwinColSelect sections = helper.new TwinColSelectBuilder<TblSection>()<br>
     * .setLabel("Sections")<br>
     * .setField(TblAdvertisementSize_.sections)<br>
     * .setListFieldName("name")<br>
     * .setLeftColumnCaption("Available") .setRightColumnCaption("Selected")
     * .build(); <br>
     * 
     * @author bhorvath
     * 
     * @param <L> the type of the list class
     */
    public class TwinColSelectBuilder<L extends CrudEntity> {
        private SplitTwinColSelect component = null;
        private String label = null;
        private Container container = null;
        private Class<L> listClazz;
        private String listField;
        private String field;
        private AbstractLayout builderForm;
        private String leftColumnCaption = "Available";
        private String rightColumnCaption = "Selected";

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public SplitTwinColSelect build() {
            Preconditions.checkNotNull(label, "label may not be null");
            Preconditions.checkNotNull(listField, "colField Property may not be null");
            Preconditions.checkArgument(group == null || field != null, "Field may not be null");
            if (builderForm == null) {
                builderForm = form;
            }
            Preconditions.checkNotNull(builderForm, "Form may not be null");

            if (component == null) {
                component = new SplitTwinColSelect(label);
            }

            component.setCaption(label);
            component.setItemCaptionMode(ItemCaptionMode.PROPERTY);
            component.setItemCaptionPropertyId(listField);
            component.setLeftColumnCaption(leftColumnCaption);
            component.setRightColumnCaption(rightColumnCaption);

            if (container == null) {
                Preconditions.checkNotNull(listClazz, "listClazz may not be null");
                container = JpaBaseDao.getGenericDao(listClazz).createVaadinContainer();
            }

            Preconditions.checkState(container.getContainerPropertyIds().contains(listField), listField
                    + " is not valid, valid listFields are " + container.getContainerPropertyIds().toString());

            final ContainerAdaptor<L> adaptor = ContainerAdaptorFactory.getAdaptor(container);
            if (adaptor.getSortableContainerPropertyIds().contains(listField)) {
                adaptor.sort(new String[] { listField }, new boolean[] { true });
            }

            component.setContainerDataSource(container);
            component.setConverter(new MultiSelectConverter(component, Set.class));

            component.setWidth("100%");
            component.setId(label.replace(" ", ""));
            component.setImmediate(true);
            component.setNullSelectionAllowed(true);
            component.setBuffered(true);
            addValueChangeListeners(component);

            if (group != null) {
                Preconditions.checkState(group.getContainer().getContainerPropertyIds().contains(field),
                        field + " is not valid, valid listFieldNames are "
                                + group.getContainer().getContainerPropertyIds().toString());

                doBinding(group, field, component);
            }
            builderForm.addComponent(component);

            return component;
        }

        /**
         * label that will appear next to the component on the screen
         * 
         * @param label
         * @return
         */
        public TwinColSelectBuilder<L> setLabel(final String label) {
            this.label = label;
            return this;
        }

        public TwinColSelectBuilder<L> setLeftColumnCaption(final String leftColumnCaption) {
            this.leftColumnCaption = leftColumnCaption;
            return this;
        }

        public TwinColSelectBuilder<L> setRightColumnCaption(final String rightColumnCaption) {
            this.rightColumnCaption = rightColumnCaption;
            return this;
        }

        /**
         * the set in the parent table that holds the set of children
         * 
         * @param field
         * @return
         */
        public TwinColSelectBuilder<L> setField(final SetAttribute<E, L> field) {
            this.field = field.getName();
            listClazz = field.getBindableJavaType();
            return this;
        }

        public TwinColSelectBuilder<L> setField(final ListAttribute<E, L> field) {
            this.field = field.getName();
            listClazz = field.getBindableJavaType();
            return this;
        }

        public TwinColSelectBuilder<L> setListFieldName(final SingularAttribute<L, ?> colField) {
            this.listField = colField.getName();
            return this;
        }

        public TwinColSelectBuilder<L> setListFieldName(final String colField) {
            this.listField = colField;
            return this;
        }

        /**
         * Set the list container of available items. A container will normally be
         * generated automatically based on the List class <L>. However if you need to
         * filter the list of available items you will need to provide your own
         * container which is filtered.
         * 
         * @param container
         * @return
         */
        public TwinColSelectBuilder<L> setContainer(final JPAContainer<L> container) {
            this.container = container;
            return this;
        }

        public TwinColSelectBuilder<L> setContainer(final EntityContainer<L> container) {
            this.container = container;
            return this;
        }

        public TwinColSelectBuilder<L> setForm(final AbstractLayout form) {
            this.builderForm = form;
            return this;
        }

        public TwinColSelectBuilder<L> setComponent(final SplitTwinColSelect component) {
            this.component = component;
            return this;
        }

        public TwinColSelectBuilder<L> setListClass(final Class<L> listClazz) {
            Preconditions.checkState(this.listClazz == null,
                    "If you set the field as a singularAttribute, the listClass is set automatically.");
            this.listClazz = listClazz;
            return this;
        }

        public TwinColSelectBuilder<L> useLazyContainer() {
            container = new JpaBaseDao<L, Long>(listClazz).createLazyQueryContainer();
            return this;
        }
    }

    public static Container createContainerFromEnumClass(final String fieldName, final Class<?> clazz) {
        final LinkedHashMap<Enum<?>, String> enumMap = new LinkedHashMap<>();
        for (final Object enumConstant : clazz.getEnumConstants()) {
            final String label = StringUtils.capitalize(enumConstant.toString().toLowerCase().replace("_", " "));
            enumMap.put((Enum<?>) enumConstant, label);
        }

        return createContainerFromMap(fieldName, enumMap);
    }

    @SuppressWarnings("unchecked")
    static public IndexedContainer createContainerFromMap(final String fieldName, final Map<?, String> hashMap) {
        final IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(fieldName, String.class, "");

        final Iterator<?> iter = hashMap.keySet().iterator();
        while (iter.hasNext()) {
            final Object itemId = iter.next();
            container.addItem(itemId);
            container.getItem(itemId).getItemProperty(fieldName).setValue(hashMap.get(itemId));
        }

        return container;
    }

    static public <Q extends CrudEntity> Container createContainerFromEntities(final String fieldName,
            final Collection<Q> list) {
        final LinkedHashMap<Q, String> enumMap = new LinkedHashMap<>();

        final List<Q> sortedList = new LinkedList<>();
        sortedList.addAll(list);
        Collections.sort(sortedList, new Comparator<Q>() {
            @Override
            public int compare(final Q arg0, final Q arg1) {
                return arg0.getName().compareToIgnoreCase(arg1.getName());
            }
        });

        for (final Q value : sortedList) {
            enumMap.put(value, value.getName());
        }

        return createContainerFromMap(fieldName, enumMap);
    }

    public ArrayList<AbstractComponent> getFieldList() {
        return this.fieldList;
    }

    public static void showConstraintViolation(final ConstraintViolationException e) {
        // build constraint error
        final StringBuilder sb = new StringBuilder();
        for (final ConstraintViolation<?> violation : e.getConstraintViolations()) {
            sb.append("Error: " + violation.getPropertyPath() + " : " + violation.getMessage() + "\n");

        }
        logger.error(sb.toString());
        VaadinHelper.notificationDialog(sb.toString(), NotificationType.ERROR);
    }

    protected AbstractLayout getForm() {
        return form;
    }

    protected ValidatingFieldGroup<E> getFieldGroup() {
        return this.group;
    }

    public <M> DataBoundButton<M> bindButtonField(final String fieldLabel, final SingularAttribute<E, M> enterScript,
            final Class<M> type) {
        return bindButtonField(fieldLabel, enterScript.getName(), type);

    }

    public <M> DataBoundButton<M> bindButtonField(final String fieldLabel, final String fieldName,
            final Class<M> type) {
        final DataBoundButton<M> field = bindButtonField(form, group, fieldLabel, fieldName, type);
        this.fieldList.add(field);
        return field;
    }

    public <M> DataBoundButton<M> bindButtonField(final AbstractLayout form, final ValidatingFieldGroup<E> group,
            final String fieldLabel, final String fieldName, final Class<M> type) {
        final DataBoundButton<M> field = new DataBoundButton<>(fieldLabel, type);

        field.setImmediate(true);

        doBinding(group, fieldName, field);
        form.addComponent(field);
        return field;
    }

//    public CKEditorEmailField bindEditorField(AbstractLayout form, ValidatingFieldGroup<E> group, String fieldName,
//            boolean readonly, ConfigModifier configModifier) {
//        SplitEditorField field = new SplitEditorField(readonly, configModifier);
//        field.setWidth("100%");
//        field.setImmediate(true);
//        addValueChangeListeners(field);
//        doBinding(group, fieldName, field);
//        form.addComponent(field);
//        return field;
//    }

    public <EN extends Enum<EN>> ComboBox bindEnumField(final String fieldLabel,
            final SingularAttribute<E, EN> fieldName) {
        return bindEnumField(fieldLabel, fieldName.getName(), fieldName.getBindableJavaType());

    }

    public <J extends CrudEntity> FormHelper<E>.EntityFieldBuilder<J> getEntityFieldBuilder(final Class<J> j) {
        return new EntityFieldBuilder<>();

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public <J extends CrudEntity> FormHelper<?>.ListSelectBuilder<J> getListSelectBuilder(
            final AbstractLayout form, final Class<J> j) {
        final FormHelper<?> helper = new FormHelper(form, null);
        return helper.new ListSelectBuilder<J>().setListClass(j);

    }

    /**
     * prefer to use the non static method getEntityFieldBuilder(Class<J> j)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public <J extends CrudEntity> FormHelper<?>.EntityFieldBuilder<J> getEntityFieldBuilder(
            final AbstractLayout form, final Class<J> j) {
        final FormHelper<?> helper = new FormHelper(form, null);
        return helper.new EntityFieldBuilder<J>().setListClass(j);

    }

    public <J extends CrudEntity> FormHelper<E>.TwinColSelectBuilder<J> getTwinColSelectBuilder(final Class<J> j) {
        return new TwinColSelectBuilder<>();
    }

    public void addComponent(final Component component) {
        form.addComponent(component);

    }

    public Slider bindSliderField(final String fieldLabel, final SingularAttribute<E, ? extends Number> fieldName,
            final int min, final int max) {
        final Slider field = new Slider(fieldLabel, min, max);
        field.setWidth("100%");
        field.setImmediate(true);

        field.setId(fieldLabel.replace(" ", ""));
        addValueChangeListeners(field);
        doBinding(group, fieldName.getName(), field);
        form.addComponent(field);
        return field;
    }

}
