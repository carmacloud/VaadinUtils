package au.com.vaadinutils.crud;

import com.vaadin.addon.jpacontainer.EntityItem;

/**
 * @deprecated Replaced in V14 migration.
 */
public interface RowChangeListener<ENTITY> {
    /**
     * Called when a user attempts to change the current row. Return false to stop
     * the user selecting a new row.
     * 
     * @param variables
     * @param source
     * @param rowChangeCallback
     * @return
     */
    void allowRowChange(RowChangeCallback rowChangeCallback);

    /**
     * Called to inform the listener that a new row has been selected and it the new
     * row contains the given item.
     * 
     * @param item
     * @return
     */
    void rowChanged(EntityItem<ENTITY> entityItem);
}
