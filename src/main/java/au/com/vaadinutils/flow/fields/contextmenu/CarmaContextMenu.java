package au.com.vaadinutils.flow.fields.contextmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.shared.Registration;

import au.com.vaadinutils.flow.user.GridExtender;

public class CarmaContextMenu<E> extends GridContextMenu<E> implements ListenerCleanup {

    private static final long serialVersionUID = 3312354883849040494L;
    private final Logger logger = LogManager.getLogger();

    private AtomicReference<Column<E>> columnClicked = new AtomicReference<Grid.Column<E>>();
    private E selectedRow;

    // Menu clean-up
    protected Registration reg;
    protected final List<Registration> registrations = new ArrayList<>();
    private int count = 0;

    public CarmaContextMenu(final Grid<E> target) {
        super(target);
        logger.debug("Creating context menu '" + getClass().getSimpleName() + "'");

        setDynamicContentHandler(record -> {
            selectedRow = record;
            return record != null;
        });

        target.addItemClickListener(e -> {
            columnClicked.set(e.getColumn());
            selectedRow = e.getItem();
        });

        addOpenedChangeListener(e -> {
            if (e.isOpened()
                    && (columnClicked.get() != null && GridExtender.ACTION_MENU.equals(columnClicked.get().getKey()))) {
                target.select(selectedRow);
            }
        });
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

    public E getTargetEntity() {
        return this.selectedRow;
    }
}