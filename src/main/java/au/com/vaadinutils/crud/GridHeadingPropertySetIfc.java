package au.com.vaadinutils.crud;

import com.vaadin.ui.Grid;

/**
 * Replaced in V14 migration.
 */
public interface GridHeadingPropertySetIfc<E> {

    void applySettingsToColumns();

    void setDeferLoadSettings(boolean b);

    void applyToGrid(Class<E> type, Grid grid, String uniqueId);

}