package au.com.vaadinutils.crud;

import java.util.Collection;

/**
 * @deprecated Replaced in V14 migration.
 */
public interface ChildCrudEventHandler<E extends CrudEntity> {
    public void entitiesAdded(Collection<E> entities);

    public void entitiesUpdated(Collection<E> entities);

    public void entitiesDeleted(Collection<E> entities);
}
