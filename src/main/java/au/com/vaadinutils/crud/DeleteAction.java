package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

/**
 * @deprecated Replaced in V14 migration.
 */
public interface DeleteAction<E> {
    public void delete(final EntityItem<E> entity) throws Exception;
}
