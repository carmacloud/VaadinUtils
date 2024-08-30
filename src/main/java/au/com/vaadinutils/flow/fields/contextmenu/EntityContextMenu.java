package au.com.vaadinutils.flow.fields.contextmenu;

import com.vaadin.flow.component.contextmenu.ContextMenu;

import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.flow.dao.CrudEntity;

public abstract class EntityContextMenu<E> extends ContextMenu {
    private static final long serialVersionUID = 1L;

    private E targetEntity;

    public EntityContextMenu() {
    }

    public E getTargetEntity() {
        return targetEntity;
    }

    protected void setTargetEntity(E targetEntity) {
        this.targetEntity = targetEntity;
    }

    /**
     * Loads the entity from the db (or cache) if possible to ensure that an up to
     * date copy is used
     *
     * @param item the item
     * @return the e
     */
    @SuppressWarnings("unchecked")
    protected E loadEntity(final E item) {
        if (item instanceof CrudEntity) {
            return (E) JpaBaseDao.getGenericDao(item.getClass()).findById(((CrudEntity) item).getId());
        }
        return item;
    }
}
