package au.com.vaadinutils.converter;

/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.EntityItemProperty;
import com.vaadin.addon.jpacontainer.metadata.EntityClassMetadata;
import com.vaadin.addon.jpacontainer.metadata.MetadataFactory;
import com.vaadin.addon.jpacontainer.metadata.PropertyMetadata;
import com.vaadin.data.Property;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.AbstractSelect;

import au.com.vaadinutils.flow.dao.CrudEntity;

/**
 * Will be removed once dependent classes are removed.
 */
public class MultiSelectConverter<T extends CrudEntity> implements Converter<Collection<Object>, Collection<T>> {

    private static final long serialVersionUID = 1L;
    private final AbstractSelect select;
    private Boolean owningSide;
    private String mappedBy;
    @SuppressWarnings("rawtypes")
    private final Class type;
    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public MultiSelectConverter(final AbstractSelect select, @SuppressWarnings("rawtypes") final Class type) {
        this.select = select;
        this.type = type;
    }

    private ContainerAdaptor<T> getContainer() {
        return ContainerAdaptorFactory.getAdaptor(select.getContainerDataSource());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Object> convertToPresentation(final Collection<T> value,
            final Class<? extends Collection<Object>> targetType, final Locale locale)
            throws com.vaadin.data.util.converter.Converter.ConversionException {
        // Value here is a collection of entities, should be transformed to a
        // collection (set) of identifier
        // TODO, consider creating a cached value

        if (value == null || value.isEmpty()) {
            try {
                // return
                // createNewCollectionForType(getPropertyDataSource().getType());
                return createNewCollectionForType(type);
            } catch (final Exception e) {
                throw new ConversionException(e);
            }
        }

        final HashSet<Object> identifiers = new HashSet<Object>();
        for (final T entity : value) {
            identifiers.add(entity.getId());
        }
        return identifiers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<T> convertToModel(final Collection<Object> value, final Class<? extends Collection<T>> targetType,
            final Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {

        // NOTE, this currently works properly only if equals and hashcode
        // methods have been implemented correctly (both depending on identifier
        // of the entity)
        // TODO create a filter that has a workaround for invalid
        // equals/hashCode

        // formattedValue here is a set of identifiers.
        // We will modify the existing collection of entities to contain
        // corresponding entities
        final Collection<Object> idset = value;

        Collection<T> modelValue = null;
        if (getPropertyDataSource() != null) {
            modelValue = (Collection<T>) getPropertyDataSource().getValue();
        }

        if (modelValue == null) {
            try {
                modelValue = createNewCollectionForType(type);
            } catch (final Exception e) {
                throw new ConversionException(e);
            }
        }

        if (idset == null || idset.isEmpty()) {
            modelValue.clear();
            return modelValue;
        }

        final HashSet<T> orphaned = new HashSet<T>(modelValue);

        // Add those that did not exist do not exist already + remove them from
        // orphaned collection
        for (final Object id : idset) {
            final EntityItem<T> item = (EntityItem<T>) getContainer().getItem(id);
            if (item != null) {
                final T entity = item.getEntity();
                if (!modelValue.contains(entity)) {
                    modelValue.add(entity);
                    addBackReference(entity);
                }
                orphaned.remove(entity);
            } else {
                logger.error("couldn't find id {} in database for type {} entityClass {}", id, type,
                        getContainer().getEntityClass());
            }
        }

        // remove orphanded
        for (final T entity : orphaned) {
            modelValue.remove(entity);
            removeBackReference(entity);
        }

        if (!isOwningSide()) {
            // refresh the item as modifying back references may also have
            // changed the collections, without this we'd get concurrent
            // modification exception.

            // FIXME: when verifying a field using this converter this following
            // line causes a value change event on that field, which causes all
            // kinds of shit which ultimately causes an exception causing the
            // validation to fail with a validation error message.
            // getPropertyDataSource().getItem().refresh();
        }
        return modelValue;
    }

    @SuppressWarnings("rawtypes")
    private EntityItemProperty getPropertyDataSource() {
        if (select.getPropertyDataSource() != null) {
            return (EntityItemProperty) ((TransactionalPropertyWrapper) select.getPropertyDataSource())
                    .getWrappedProperty();
        }

        return null;

    }

    private void removeBackReference(final T entity) {
        if (!isOwningSide()) {
            final Property<Object> itemProperty = getBackReferenceItemProperty(entity);
            final Object property = itemProperty.getValue();
            if (property instanceof Collection) {
                // many to many
                @SuppressWarnings("rawtypes")
                final Collection c = (Collection) property;
                c.remove(getPropertyDataSource().getItem().getEntity());
                itemProperty.setValue(c);
            } else {
                // one to many
                itemProperty.setValue(null);
            }
        }
    }

    private Property<Object> getBackReferenceItemProperty(final T entity) {
        return getContainer().getProperty(entity, mappedBy);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addBackReference(final T entity) {
        if (!isOwningSide()) {
            final Property itemProperty = getBackReferenceItemProperty(entity);
            final Object property = itemProperty.getValue();
            if (property == null || !(property instanceof Collection)) {
                itemProperty.setValue(getPropertyDataSource().getItem().getEntity());
                // one to many
            } else {
                // many to many
                Preconditions.checkArgument(property instanceof Collection, "Expected a Collection got "
                        + itemProperty.getType() + " " + property.getClass().getCanonicalName());
                final Collection c = (Collection) property;
                c.add(getPropertyDataSource().getItem().getEntity());
                itemProperty.setValue(c);
            }

        }
    }

    /**
     * Checks if the manytomany relation is owned by this side of the property. As a
     * side effect detects the name of the owner property if the relation is owned
     * by the other side.
     * 
     * @return false if bidirectional connection and the mapping has a mappedBy
     *         parameter.
     */
    private boolean isOwningSide() {
        if (owningSide == null) {
            final Class<?> entityClass = getPropertyDataSource().getItem().getContainer().getEntityClass();
            final EntityClassMetadata<?> entityClassMetadata = MetadataFactory.getInstance()
                    .getEntityClassMetadata(entityClass);
            final PropertyMetadata property = entityClassMetadata.getProperty(getPropertyDataSource().getPropertyId());
            final ManyToMany annotation = property.getAnnotation(ManyToMany.class);
            if (annotation != null) {
                if (annotation.mappedBy() != null && !annotation.mappedBy().isEmpty()) {
                    owningSide = Boolean.FALSE;
                    mappedBy = annotation.mappedBy();
                    return owningSide;
                }
            } else {
                final OneToMany annotation2 = property.getAnnotation(OneToMany.class);
                if (annotation2 != null) {
                    if (annotation2.mappedBy() != null && !annotation2.mappedBy().isEmpty()) {
                        owningSide = Boolean.FALSE;
                        mappedBy = annotation2.mappedBy();
                        return owningSide;
                    }
                }
            }
            owningSide = Boolean.TRUE;
        }
        return owningSide;
    }

    @SuppressWarnings("rawtypes")
    static Collection createNewCollectionForType(final Class<?> type)
            throws InstantiationException, IllegalAccessException {
        if (type.isInterface()) {
            if (type == Set.class) {
                return new HashSet();
            } else if (type == List.class) {
                return new ArrayList();
            } else {
                throw new RuntimeException("Couldn't instantiate a collection for property.");
            }
        }

        return (Collection) type.newInstance();

    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Collection<T>> getModelType() {
        if (getPropertyDataSource() != null) {
            return getPropertyDataSource().getType();
        }

        return this.type;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Collection<Object>> getPresentationType() {
        if (getPropertyDataSource() != null) {
            return getPropertyDataSource().getType();
        }

        return this.type;

    }

}
