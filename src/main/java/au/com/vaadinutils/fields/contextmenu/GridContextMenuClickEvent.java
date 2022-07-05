package au.com.vaadinutils.fields.contextmenu;

/**
 * @deprecated context menus are changed for V14+
 */
public class GridContextMenuClickEvent<E> {
    private E entity;
    private int clientX;
    private int clientY;

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
