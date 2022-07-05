package au.com.vaadinutils.fields.contextmenu;

import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.GridContextClickEvent;

/**
 * @deprecated context menus are changed for V14+
 */
public class GridContextMenu<E> extends EntityContextMenu<E> {
    private static final long serialVersionUID = 1L;

    private Grid grid;
    private boolean loadCrud;

    /**
     * Assigns this as the context menu of given table. Allows context menu to
     * appear only on certain parts of the table.
     * 
     * @param onRow    show context menu when row is clicked
     * @param onHeader show context menu when header is clicked
     * @param onFooter show context menu when footer is clicked
     * @param loadCrud If set to false, do not load the crud from the DB.<br>
     *                 (Meant for grids backed by a Stored Procedure)
     * @param table
     */
    public void setAsGridContextMenu(final Grid grid, final boolean onRow, final boolean onHeader,
            final boolean onFooter, boolean loadCrud) {
        this.grid = grid;
        this.loadCrud = loadCrud;
        extend(grid);
        setOpenAutomatically(false);

        grid.addContextClickListener(new ContextClickListener() {
            private static final long serialVersionUID = -2197393292360426242L;

            @Override
            public void contextClick(ContextClickEvent event) {
                if (!(event instanceof GridContextClickEvent)) {
                    return;
                }

                final GridContextClickEvent e = (GridContextClickEvent) event;
                switch (e.getSection()) {
                case BODY:
                    if (onRow) {
                        openContext(e);
                    }
                    break;
                case FOOTER:
                    if (onFooter) {
                        openContext(e);
                    }
                    break;
                case HEADER:
                    if (onHeader) {
                        openContext(e);
                    }
                    break;
                default:
                    break;
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void openContext(final GridContextClickEvent event) {
        openContext(new GridContextMenuClickEvent<>((E) event.getItemId(), event.getClientX(), event.getClientY()));
    }

    public void openContext(final GridContextMenuClickEvent<E> event) {
        try {
            final E itemId = event.getEntity();
            if (itemId == null) {
                return;
            }

            // Make sure we have an up to date copy of the entity from the db (if not using
            // a stored proc in the Grid)
            if (loadCrud) {
                targetEntity = loadEntity(itemId);
            } else {
                targetEntity = itemId;
            }
            grid.select(itemId);

            fireEvents();
            open(event.getClientX(), event.getClientY());
        } catch (IllegalArgumentException e) {
            // This usually means we have tried to select something that doesn't
            // exist in the grid. This can happen when trying to open a context
            // menu on old items while the grid is still refreshing with new
            // items.
        }
    }
}
