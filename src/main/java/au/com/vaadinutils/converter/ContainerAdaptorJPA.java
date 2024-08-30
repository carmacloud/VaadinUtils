package au.com.vaadinutils.converter;

import java.util.Collection;
import java.util.HashSet;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;

import au.com.vaadinutils.flow.dao.CrudEntity;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Will be removed once dependent classes are removed.
 */
public class ContainerAdaptorJPA<E extends CrudEntity> implements ContainerAdaptor<E> {

    private final JPAContainer<E> container;

    public ContainerAdaptorJPA(final JPAContainer<E> containerDataSource) {
        container = containerDataSource;
    }

    @Override
    public Item getItem(final Object id) {
        return container.getItem(id);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Property getProperty(final E item, final Object propertyId) {
        return container.getContainerProperty(item.getId(), propertyId);
    }

    @Override
    public E getEntity(final Object id) {
        return container.getItem(id).getEntity();
    }

    @Override
    public Collection<Object> getSortableContainerPropertyIds() {
        final Collection<Object> ids = new HashSet<>();
        ids.addAll(container.getSortableContainerPropertyIds());
        return ids;
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
        return container.getEntityClass();
    }

}
