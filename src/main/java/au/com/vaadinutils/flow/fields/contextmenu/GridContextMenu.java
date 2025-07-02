package au.com.vaadinutils.flow.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

public class GridContextMenu<E> extends EntityContextMenu<E> {

    private static final long serialVersionUID = -5882295471669681116L;
    private final Logger logger = LogManager.getLogger();
    private boolean loadCrud = false;
    private SerializablePredicate<E> dynamicContentHandler;

    // Menu clean-up
    protected Registration reg;
    protected final List<Registration> registrations = new ArrayList<>();
    private int count = 0;

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

        reg = addOpenedChangeListener(event -> {
            if (grid != null) {
                grid.select(getTargetEntity());
            }
        });
        registrations.add(reg);
        logger.debug("Adding Registration: " + this.getClass().getSimpleName());
    }

    public void setAsIconContextMenu(final Column<E> actionColumn, final Grid<E> grid, final Div invisibleTarget) {
        super.setTarget(invisibleTarget);
        setOpenOnClick(true);

        grid.addItemClickListener(e -> {
            final E selectedItem = grid.getSelectionModel().getFirstSelectedItem().orElse(null);
            logger.info(selectedItem);
            if (loadCrud) {
                setTargetEntity(loadEntity(selectedItem));
            } else {
                setTargetEntity(selectedItem);
            }
            if (e.getButton() == 0 && e.getColumn().equals(actionColumn)) {
                final int x = e.getClientX();
                final int y = e.getClientY();
                // Move the invisible target to the mouse position
                invisibleTarget.getElement().executeJs("this.style.left = $0 + 'px'; this.style.top = $1 + 'px';", x,
                        y);
                // Simulate a click to open the context menu
                invisibleTarget.getElement().executeJs("this.click();");
            }
        });

        reg = addOpenedChangeListener(event -> {
            if (grid != null) {
                grid.select(getTargetEntity());
            }
        });
        registrations.add(reg);
        logger.warn("Adding Registration: " + this.getClass().getSimpleName());
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
        super.setTarget(grid);

        // Only allow context on the rows, not headers or footers.
        setDynamicContentHandler(record -> {
            return record != null;
        });

        this.loadCrud = loadCrud;

        reg = addOpenedChangeListener(event -> {
            grid.select(getTargetEntity());
        });
        registrations.add(reg);
        logger.warn("Adding Registration: " + this.getClass().getSimpleName());
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
        if (getTarget() instanceof Grid) {
            final Grid<E> grid = (Grid<E>) getTarget();
            final String key = eventDetail.getString("key");

            if (getDynamicContentHandler() != null) {
                final E item = grid.getDataCommunicator().getKeyMapper().get(key);
                if (getDynamicContentHandler().test(item)) {
                    if (item == null) {
                        return true;
                    }
                    if (loadCrud) {
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
            logger.warn("No Registrations removed for: " + this.getClass().getSimpleName());
        } else {
            logger.warn(count + " registrations removed for: " + this.getClass().getSimpleName());
        }
    }
}