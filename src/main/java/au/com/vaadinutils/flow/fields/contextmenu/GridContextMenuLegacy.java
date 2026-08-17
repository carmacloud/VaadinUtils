package au.com.vaadinutils.flow.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import au.com.vaadinutils.flow.user.GridExtender;
import elemental.json.JsonObject;

/**
 * Retained for use with AdminNotesHelper and it's extended classes.
 * 
 * @param <E>
 */
public class GridContextMenuLegacy<E> extends EntityContextMenu<E> {

    private static final long serialVersionUID = -5882295471669681116L;

    private final Logger logger = LogManager.getLogger();
    private AtomicBoolean loadCrud = new AtomicBoolean(false);
    private SerializablePredicate<E> dynamicContentHandler;

    // Used to determine which menu is opened or hidden.
    private ContextType contextType;
    private int buttonClicked = -1;
    private AtomicReference<Column<E>> columnClicked = new AtomicReference<Grid.Column<E>>();

    // Menu clean-up
    protected Registration reg;
    protected final List<Registration> registrations = new ArrayList<>();
    private int count = 0;

    /**
     * Assigns this as the context menu of given Grid. This allows left-click to
     * open the menu. The context menu only appears on rows in the Grid.
     * 
     * @param grid     The {@link Grid} the menu is to be attached to.
     * @param loadCrud If set to false, do not load the crud from the DB.<br>
     *                 (Meant for grids backed by a Stored Procedure)
     */
    public void setAsComponentContextMenu(final Grid<E> grid, final boolean loadCrud) {
        this.contextType = ContextType.LEFT_CLICK;
        setOpenOnClick(true);

        defaultContextActions(grid, loadCrud);
    }

    /**
     * Assigns this as the context menu of given Grid. Allows context menu to appear
     * only on rows in the Grid.
     * 
     * @param loadCrud If set to false, do not load the crud from the DB.<br>
     *                 (Meant for grids backed by a Stored Procedure)
     * @param grid     The {@link Grid} the menu is to be attached to.
     */
    public void setAsGridContextMenu(final Grid<E> grid, final boolean loadCrud) {
        this.contextType = ContextType.GRID;

        defaultContextActions(grid, loadCrud);
    }

    private void defaultContextActions(final Grid<E> grid, final boolean loadCrud) {
        super.setTarget(grid);
        this.loadCrud.set(loadCrud);

        grid.addItemClickListener(e -> {
            buttonClicked = e.getButton();
            columnClicked.set(e.getColumn());
        });

        // Only allow context on the rows, not headers or footers.
        setDynamicContentHandler(record -> {
            return record != null;
        });

        reg = addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                grid.select(getTargetEntity());
            }
        });

        registrations.add(reg);
        logger.debug("Adding Registration: " + this.getClass().getSimpleName());
    }

    public SerializablePredicate<E> getDynamicContentHandler() {
        return dynamicContentHandler;
    }

    public void setDynamicContentHandler(final SerializablePredicate<E> dynamicContentHandler) {
        this.dynamicContentHandler = dynamicContentHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean onBeforeOpenMenu(final JsonObject eventDetail) {
        // Depending on which method is used to show a menu, decide which column is
        // selected and button clicked to show or hide a menu.
        boolean showMenu = true;
        final String column = Optional.ofNullable(columnClicked.get()).map(col -> col.getKey()).orElse(null);
        switch (contextType) {
        case GRID:
            showMenu = buttonClicked != 0 && !GridExtender.ACTION_MENU.equals(column);
            break;
        case LEFT_CLICK:
            showMenu = buttonClicked == 0 && GridExtender.ACTION_MENU.equals(column);
        default:
            break;
        }

        // Reset these to 'not selected'. Will only be set on a left-click.
        buttonClicked = -1;
        columnClicked.set(null);

        if (!showMenu) {
            return false;
        }
        if (getTarget() instanceof Grid) {
            final Grid<E> grid = (Grid<E>) getTarget();
            final String key = eventDetail.getString("key");

            if (getDynamicContentHandler() != null) {
                final E item = grid.getDataCommunicator().getKeyMapper().get(key);
                if (getDynamicContentHandler().test(item)) {
                    if (item == null) {
                        return true;
                    }

                    if (loadCrud.get()) {
                        setTargetEntity(loadEntity(item));
                    } else {
                        setTargetEntity(item);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }

        return super.onBeforeOpenMenu(eventDetail);
    }

    @Override
    public void removeRegistrations() {
        count = 0;
        registrations.forEach(reg -> {
            reg.remove();
            count++;
        });
        if (count <= 1) {
            logger.debug("No Registrations removed for: " + this.getClass().getSimpleName());
        } else {
            logger.warn(count + " registrations removed for: " + this.getClass().getSimpleName());
        }
    }
}