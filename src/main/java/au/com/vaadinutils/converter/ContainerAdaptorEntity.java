package au.com.vaadinutils.converter;

import java.util.Collection;

import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import au.com.vaadinutils.crud.CrudEntity;

/**
 * Will be removed once dependent classes are removed.
 */
public class ContainerAdaptorEntity<E extends CrudEntity> implements ContainerAdaptor<E> {

    private final EntityContainer<E> container;

    public ContainerAdaptorEntity(final EntityContainer<E> container) {
        this.container = container;
    }

    @Override
    public Item getItem(final Object id) {
        return container.getItem(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Property getProperty(final E entity, final Object id) {
        return container.getContainerProperty(entity.getId(), id);
    }

    @Override
    public E getEntity(final Object id) {
        if (container.getItemIds().contains(id)) {
            return container.getEntity(id);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Object> getSortableContainerPropertyIds() {
        return (Collection<Object>) container.getSortableContainerPropertyIds();
    }

    @Override
    public void sort(final String[] propertyId, final boolean[] ascending) {
        container.sort(propertyId, ascending);

    }

    @Override
    public void removeAllContainerFilters() {
        container.removeAllContainerFilters();

    }

    @Override
    public void addContainerFilter(final Filter filter) {
        container.addContainerFilter(filter);

    }

    @Override
    public Class<E> getEntityClass() {
        // TODO:
        return null;
    }
}
