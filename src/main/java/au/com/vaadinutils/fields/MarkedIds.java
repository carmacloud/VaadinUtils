package au.com.vaadinutils.fields;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Replaced in Vaadin 14 migration.
 */
public class MarkedIds {
    final Set<Object> markedIds = new TreeSet<Object>();
    private boolean trackingSelected = true;

    private final Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();
    private int containerSize;

    public void addSelectionListener(final SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    private void updateSelectionListeners() {
        int count = markedIds.size();
        if (!trackingSelected) {
            count = containerSize - count;
        }
        for (final SelectionListener listener : selectionListeners) {

            listener.selectedItems(count);
        }
    }

    public void clear(final boolean b, final int containerSize) {
        markedIds.clear();
        trackingSelected = b;
        this.containerSize = containerSize;
        updateSelectionListeners();

    }

    public void addAll(final Collection<Long> value) {
        markedIds.addAll(value);
        updateSelectionListeners();

    }

    public void add(final Object itemId) {
        markedIds.add(itemId);
        updateSelectionListeners();

    }

    public void remove(final Object itemId) {
        markedIds.remove(itemId);
        updateSelectionListeners();

    }

    public boolean contains(final Object itemId) {
        return markedIds.contains(itemId);
    }

    public void removeAll(final Collection<Long> ids) {
        markedIds.removeAll(ids);
        updateSelectionListeners();

    }

    public Collection<?> getIds() {
        return markedIds;
    }

    // Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public boolean isTrackingSelected() {
        return trackingSelected;
    }
}
