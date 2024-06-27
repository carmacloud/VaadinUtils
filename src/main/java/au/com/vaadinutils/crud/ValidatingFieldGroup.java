package au.com.vaadinutils.crud;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.Field;

/**
 * Replaced in V14 migration.
 */
public class ValidatingFieldGroup<E> extends FieldGroup {
    private static final long serialVersionUID = 1L;
    private final Class<E> entityClass;
    private JPAContainer<E> container;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public ValidatingFieldGroup(final JPAContainer<E> container, final Class<E> entityClass) {
        this.entityClass = entityClass;
        this.container = container;
    }

    public ValidatingFieldGroup(final Item item, final Class<E> entityClass) {
        super(item);
        this.entityClass = entityClass;

    }

    public ValidatingFieldGroup(final Class<E> entityClass) {
        this.entityClass = entityClass;

    }

    private boolean groupIsDirty;
    private DirtyListener dirtyListener;

    public DirtyListener getDirtyListener() {
        return dirtyListener;
    }

    public void setDirtyListener(final DirtyListener listener) {
        dirtyListener = listener;
    }

    public boolean getGroupIsDirty() {
        return groupIsDirty;
    }

    public void setGroupIsDirty(final boolean groupIsDirty) {
        this.groupIsDirty = groupIsDirty;
    }

    private final Set<Field<?>> knownFields = new HashSet<>();

    /*
     * Override configureField to add a bean validator to each field.
     */
    @Override
    protected void configureField(final Field<?> field) {

        // Vaadin applies the readonly status from the underlying entity
        // which doesn't allow us to make a single field readonly
        // hence we track it ourselves.
        final boolean readOnly = field.isReadOnly();
        super.configureField(field);

        // If the field was originally readonly then force it back to readonly.
        if (readOnly)
            field.setReadOnly(true);

        if (!knownFields.contains(field)) {
            // only ever add the validator once for a field

            // Add Bean validators if there are annotations
            // Note that this requires a bean validation implementation to
            // be available.
            final BeanValidator validator = new BeanValidator(entityClass, getPropertyId(field).toString());

            field.addValidator(validator);
            if (field.getLocale() != null) {
                validator.setLocale(field.getLocale());
            }

            final ValueChangeListener changeListener = new ValueChangeListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void valueChange(final ValueChangeEvent event) {
                    if (groupIsDirty == false) {
                        groupIsDirty = true;

                        if (dirtyListener != null) {
                            dirtyListener.fieldGroupIsDirty(true);
                        }

                    }

                }
            };
            field.addValueChangeListener(changeListener);
        }
        knownFields.add(field);

    }

    @Override
    public void discard() {
        groupIsDirty = false;
        if (dirtyListener != null) {
            dirtyListener.fieldGroupIsDirty(false);
        }
        super.discard();

    }

    @Override
    public void setItemDataSource(final Item itemDataSource) {
        groupIsDirty = false;
        if (dirtyListener != null) {
            dirtyListener.fieldGroupIsDirty(false);
        }
        super.setItemDataSource(itemDataSource);
    }

    public JPAContainer<E> getContainer() {

        return container;
    }

    // when finished testing delete this method, it's in the super type anyway
    @Override
    public boolean isModified() {
        for (final Field<?> field : getFields()) {
            if (field.isModified()) {
                logger.warn("Dirty: {} {}", field.getCaption(), field.getClass().getSimpleName());
                String value = null;
                if (field.getValue() != null) {
                    value = field.getValue().toString();
                }
                logger.warn("Dirty value: " + StringUtils.abbreviate(value, 40));

                return true;
            }
        }
        return false;
    }

    @Override
    public void bind(final Field<?> field, final Object propertyId) throws BindException {

        boolean isReadonly = false;
        try {
            isReadonly = field.isReadOnly();
            field.setReadOnly(false);
            super.bind(field, propertyId);

        } finally {
            field.setReadOnly(isReadonly);
        }
    }

}
