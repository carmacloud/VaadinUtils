package au.com.vaadinutils.flow.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.contextmenu.ContextMenu;

import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.dao.JpaBaseDao;

public abstract class EntityContextMenu<E> extends ContextMenu {
    private static final long serialVersionUID = 1L;

    private List<ContextMenuEvent> events = new ArrayList<>();
    private E targetEntity;
    
    public EntityContextMenu() {
    }

    protected E getTargetEntity() {
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

    protected void fireEvents() {
        for (ContextMenuEvent event : events) {
            event.preContextMenuOpen();
        }
    }

    public void addEvent(final ContextMenuEvent event) {
        events.add(event);
    }
}
