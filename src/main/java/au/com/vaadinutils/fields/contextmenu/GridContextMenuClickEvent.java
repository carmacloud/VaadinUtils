package au.com.vaadinutils.fields.contextmenu;

/**
 * Context menus are changed for V14+
 */
public class GridContextMenuClickEvent<E> {
    private final E entity;
    private final int clientX;
    private final int clientY;

    public GridContextMenuClickEvent(final E entity, final int clientX, final int clientY) {
        this.entity = entity;
        this.clientX = clientX;
        this.clientY = clientY;
    }

    public E getEntity() {
        return entity;
    }

    public int getClientX() {
        return clientX;
    }

    public int getClientY() {
        return clientY;
    }
}
