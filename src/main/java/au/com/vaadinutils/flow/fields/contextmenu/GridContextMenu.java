package au.com.vaadinutils.flow.fields.contextmenu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.function.SerializablePredicate;

import elemental.json.JsonObject;

public class GridContextMenu<E> extends EntityContextMenu<E> {

    private static final long serialVersionUID = 1L;
    private boolean loadCrud = false;
    private SerializablePredicate<E> dynamicContentHandler;

    public GridContextMenu() {
    }

    /**
     * 
     * @param target A {@link Component} that will open the context menu on a left
     *               click.
     * @param grid   The {@link Grid} that the context menu is attached to. Used to
     *               have the row selected from an event.
     * @param source The bean on the underlying row of the grid.
     */
    public void setAsComponentContextMenu(final Component target, final Grid<E> grid, final E source) {
        super.setTarget(target);
        setOpenOnClick(true);

        if (loadCrud) {
            setTargetEntity(loadEntity(source));
        } else {
            setTargetEntity(source);
        }

        addOpenedChangeListener(event -> {
            if (grid != null) {
                grid.select(getTargetEntity());
            }
        });
    }

    /**
     * Assigns this as the context menu of given Grid. Allows context menu to appear
     * only on rows in the Grid.
     * 
     * @param loadCrud If set to false, do not load the crud from the DB.<br>
     *                 (Meant for grids backed by a Stored Procedure)
     * @param grid     The {@link Grid} the menu is to be attached to.
     */
    public void setAsGridContextMenu(final Grid<E> grid, boolean loadCrud) {
        super.setTarget(grid);

        // Only allow context on the rows, not headers or footers.
        setDynamicContentHandler(record -> {
            return record != null;
        });

        this.loadCrud = loadCrud;

        addOpenedChangeListener(event -> {
            grid.select(getTargetEntity());
        });

        grid.addCellFocusListener(listener -> {
            final E item = listener.getItem().orElse(null);
            if (item == null) {
                return;
            }
            if (loadCrud) {
                setTargetEntity(loadEntity(item));
            } else {
                setTargetEntity(item);
            }
        });
    }

    public SerializablePredicate<E> getDynamicContentHandler() {
        return dynamicContentHandler;
    }

    public void setDynamicContentHandler(SerializablePredicate<E> dynamicContentHandler) {
        this.dynamicContentHandler = dynamicContentHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean onBeforeOpenMenu(JsonObject eventDetail) {
        if (getTarget() instanceof Grid) {
            Grid<E> grid = (Grid<E>) getTarget();
            String key = eventDetail.getString("key");

            if (getDynamicContentHandler() != null) {
                final E item = grid.getDataCommunicator().getKeyMapper().get(key);
                return getDynamicContentHandler().test(item);
            }
        }

        return super.onBeforeOpenMenu(eventDetail);
    }
}
