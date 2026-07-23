package au.com.vaadinutils.fields;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.data.Buffered;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.crud.AdvancedSearchListener;
import au.com.vaadinutils.crud.FormHelper;
import au.com.vaadinutils.crud.GridHeadingPropertySet.Builder;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.flow.dao.CrudEntity;

/**
 * Replaced in V14 migration.
 */
public class ComboBoxWithSearchField<T extends CrudEntity, C extends Indexed & Filterable> extends CustomField<T> {
    private static final String NOT_SELECTED = "None Selected";
    private static final long serialVersionUID = 1L;

    private T sourceValue;

    private T currentValue;
    final private TextField valueLabel = new TextField();
    private final PopupButton select;
    private boolean allowNull = true;
    private Class<? extends T> type;
    private String caption;
    private final ComboBoxWithSearchFieldChooserWindow<T, C> popup;
    private CssLayout holder;

    Logger logger = LogManager.getLogger();
    private String nullLabel = NOT_SELECTED;
    private Button clear;

    public ComboBoxWithSearchField(final String caption, final Class<? extends T> type, final C container,
            final Builder<T> headingBuilder, final String[] sortColumns) {
        this(caption, type, container, headingBuilder, sortColumns, null, null);
    }

    public ComboBoxWithSearchField(final String caption, final Class<? extends T> type, final C container,
            final Builder<T> headingBuilder, final String[] sortColumns,
            final AdvancedSearchContentProvider advancedSearchProvider,
            final AdvancedSearchListener advancedSearchListener) {
        this.type = type;
        setImmediate(true);
        setCaption(caption);
        setSizeFull();
        addValidator(getValidator());
        this.caption = caption;

        holder = new CssLayout();
        select = new PopupButtonCustom("", holder);
        select.setId(caption + "Combo");

        popup = new ComboBoxWithSearchFieldChooserWindow<T, C>(getSelectionListener(), type, caption, container,
                headingBuilder, sortColumns, advancedSearchProvider, advancedSearchListener);

        select.setContent(popup.getPopupContent2());

        clear = new Button("X");
        clear.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                getSelectionListener().selected(null);

            }
        });

        clear.setVisible(false);

    }

    /**
     * handy when you want to start with the chooser open
     */
    public void showPopUp() {
        popup.select(currentValue);
        select.setPopupVisible(true);
    }

    public void setPopupWidth(final String width) {
        select.getContent().setWidth(width);
    }

    public class PopupButtonCustom extends PopupButton {
        private static final long serialVersionUID = 1L;

        /**
         * allow aligning on an arbitrary component for the popup
         * 
         * @param caption
         * @param alignto
         */
        PopupButtonCustom(final String caption, final Component alignto) {
            super(caption);
            setPopupPositionComponent(alignto);
        }

        @Override
        protected void setPopupPositionComponent(final Component component) {
            super.setPopupPositionComponent(component);
        }
    }

    private Validator getValidator() {
        return new Validator() {

            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final Object value) throws InvalidValueException {
                if (currentValue == null && allowNull == false) {
                    throw new InvalidValueException("You must select a " + caption);
                }

            }
        };
    }

    @Override
    protected Component initContent() {

        holder.addStyleName("v-component-group");
        holder.setSizeFull();
        holder.setWidth("100%");

        select.setClosePopupOnOutsideClick(true);

        select.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                popup.select(currentValue);
            }

        });

        holder.addComponent(valueLabel);
        holder.addComponent(select);
        holder.addComponent(clear);
        holder.setWidth(FormHelper.STANDARD_COMBO_WIDTH);
        valueLabel.setWidth("100%");

        return holder;
    }

    private ChooserListener getSelectionListener() {
        return new ChooserListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void selected(final Object id) {
                if (id == null) {
                    currentValue = null;

                } else {
                    if (id instanceof Long) {
                        currentValue = JpaBaseDao.getGenericDao(type).findById((Long) id);
                    } else {
                        currentValue = (T) id;
                    }
                }
                setValueLabel(currentValue);
                select.setPopupVisible(false);

                fireValueChange(false);
            }
        };
    }

    private void setValueLabel(final T value) {
        valueLabel.setReadOnly(false);
        if (value == null) {
            valueLabel.setStyleName(ValoTheme.LABEL_FAILURE);
            valueLabel.setValue(nullLabel);
            valueLabel.setDescription(null);
        } else {
            valueLabel.setStyleName(ValoTheme.LABEL_SUCCESS);
            valueLabel.setValue(value.toString());

        }

        valueLabel.setReadOnly(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void commit() throws Buffered.SourceException, InvalidValueException {
        super.commit();
        sourceValue = (T) getConvertedValue();
    }

    @Override
    public boolean isModified() {
        if (sourceValue == null && currentValue == null) {
            return false;
        }
        if (sourceValue == null && getConvertedValue() != null)
            return true;
        return !sourceValue.equals(getConvertedValue());
    }

    @Override
    public void setReadOnly(final boolean b) {
        select.setReadOnly(b);
        super.setReadOnly(b);
        select.setVisible(!b);
    }

    @Override
    protected void setInternalValue(final T newValue) {
        super.setInternalValue(newValue);

        setValueLabel(newValue);
        currentValue = newValue;
        sourceValue = newValue;

    }

    @Override
    protected T getInternalValue() {
        return currentValue;
    }

    @Override
    public Object getConvertedValue() {

        return currentValue;
    }

    public Long getLongValue() {
        if (super.getValue() == null) {
            return null;
        }
        return super.getValue().getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getType() {
        return (Class<T>) type;
    }

    public void setAllowNull(final boolean b) {
        allowNull = b;
        clear.setVisible(b);

    }

    @Override
    public void validate() throws InvalidValueException {
        super.validate();
    }

    public void setInputPrompt(final String string) {
        valueLabel.setInputPrompt(string);
        nullLabel = string;

    }

    public void setNullSelectionAllowed(final boolean b, final String prompt) {
        popup.setNullSelectionAllowed(b, prompt);

    }

    public void setContainerFilters(final Filter filter) {
        popup.setContainerFilters(filter);

    }

    public boolean containerContains(final Object id) {
        return popup.containerContains(id);
    }

    public void showAdvancedSearch(final boolean show) {
        popup.showAdvancedSearch(show);

    }

    public void triggerFilter() {
        popup.triggerFilter();
    }

}
