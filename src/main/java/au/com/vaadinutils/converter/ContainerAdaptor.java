package au.com.vaadinutils.converter;

import java.util.Collection;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import au.com.vaadinutils.crud.CrudEntity;

/**
 * @deprecated Will be removed once dependent classes are removed.
 */
public interface ContainerAdaptor<T extends CrudEntity> {

    Item getItem(Object id);

    Property<Object> getProperty(T entity, Object id);

    T getEntity(Object id);

    Collection<Object> getSortableContainerPropertyIds();

    void sort(String[] propertyId, boolean[] ascending);

    void removeAllContainerFilters();

    void addContainerFilter(Filter filter);

    Class<T> getEntityClass();

}
