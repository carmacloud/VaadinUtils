package au.com.vaadinutils.flow.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import au.com.vaadinutils.flow.helper.VaadinHelper;

/**
 * Plugin in methods for storing and retrieving column widths, order and
 * visibility. Also optionally allows an action column to be included that shows
 * a context menu is present.<br>
 * To use, call after adding columns, supplying the grid and a unique Id. This
 * class will do the rest.<br>
 * For the grid itself, the columns to be processed will need to have an
 * explicit key set, e.g.
 * 
 * <pre>{@code
 * grid.addColumn(<ValueProvider>).setHeader("ColumnHeader").setKey("UniqueColumnName");
 * }</pre>
 *
 * The calls to add the action icon and set context menu must always be before
 * adding columns to the grid, otherwise the action column ends up at the end of
 * the grid. We want it as the very first.<br>
 * <br>
 * Note: columns that are set as frozen before calling init() will not show in
 * the context menu list of columns that can be shown or hidden.<br>
 * 
 * @param <T> The underlying bean for the grid.
 */
public class GridExtender<T> {

    private final Logger logger = LogManager.getLogger();
    public static final String ACTION_MENU = "_actionMenu";
    private Grid<T> grid;
    private String uniqueId;
    private List<String> columnsHiddenOnLoad;

    // For the additional column that optionally contains action filter and/or
    // context menu.
    private final Icon actionIcon = VaadinIcon.MENU.create();
    private Column<T> actionColumn = null;
    private boolean setActionIcon = false;
    private ComponentRenderer<Component, T> gridContextMenu;

    // Common method to set columns resizable.
    private boolean resizable = false;
    private final List<Column<T>> resizableColumns = new ArrayList<>();

    public GridExtender(final Grid<T> grid, final String uniqueId) {
        this.grid = grid;
        this.uniqueId = uniqueId;
        actionIcon.setColor(VaadinHelper.CARMA_BLUE);
        actionIcon.setSize("12px");
        grid.setColumnReorderingAllowed(true);
    }

    /**
     * Call after adding columns to the grid.
     * 
     * @param headersMap          A {@link Map} of {@link String} pairs so that
     *                            column keys can be matched to headings.<br>
     *                            This is an issue because V14+ doesn't have a
     *                            getHeaders method.
     * @param columnsHiddenOnLoad A {@link List} of {@link String} irems being the
     *                            key for a column that is to hidden on load,
     *                            despite any stored settings.
     */
    public void init(final Map<String, String> headersMap, final List<String> columnsHiddenOnLoad) {
        if (this.grid.getColumns().isEmpty()) {
            logger.warn("Columns not set for " + uniqueId
                    + ". Grid column order, width and visibility will not be stored or retreived.");
        }
        this.columnsHiddenOnLoad = columnsHiddenOnLoad;
        setColumnsResizable();
        addActionColumn();
        addActionItems(headersMap);
        configureSaveColumnWidths();
        configureSaveColumnVisible();
        configureSaveColumnOrder();
        setAllColumnsSortable();
    }

    private void configureSaveColumnWidths() {
        final String keyStub = uniqueId + "-width";
        final List<String> columnKeys = new ArrayList<>();
        this.grid.getColumns().forEach(column -> {
            if (column.isResizable()) {
                final String columnKey = column.getKey();
                columnKeys.add(keyStub + "-" + columnKey);
            }
        });
        final Map<String, String> savedWidths = getSavedWidths(columnKeys);

        this.grid.getColumns().forEach(column -> {
            final String key = column.getKey();
            final String setting = keyStub + "-" + key;
            final String savedWidth = savedWidths.get(setting);

            if (savedWidth != null && !savedWidth.isEmpty()) {
                setColumnWidth(grid.getColumnByKey(key), savedWidth);
            }
        });

        this.grid.addColumnResizeListener(e -> {
            final String key = e.getResizedColumn().getKey();
            // Strip any char so we only store a virtual number.
            final String width = e.getResizedColumn().getWidth().replaceAll("[a-z]", "");
            MemberSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + key, width);
        });
    }

    private Map<String, String> getSavedWidths(final List<String> columnKeys) {
        if (columnKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> savedWidths = MemberSettingsStorageFactory.getUserSettingsStorage().get(columnKeys);

        final Map<String, String> parsedWidths = new HashMap<>(savedWidths.size() * 2);
        savedWidths.entrySet().forEach(entry -> {
            final String width = entry.getValue();
            parsedWidths.put(entry.getKey(), width);
        });

        return parsedWidths;
    }

    private void setColumnWidth(final Column<?> column, final String width) {
        try {
            // Test the value (stripping the 'px' suffix at the same time).
            final Float colWidth = Float.valueOf(width.replace("px", ""));
            // Avoid setting proposed width < 0
            final String widthToFit = colWidth >= 0 ? String.valueOf(colWidth) : "0";
            column.setWidth(widthToFit + "px");
        } catch (NumberFormatException e) {
            logger.error(e.getMessage() + " " + column.getKey());
        }
    }

    private void configureSaveColumnVisible() {
        final String keyStub = uniqueId + "-visible";
        final List<String> columnKeys = new ArrayList<>();
        this.grid.getColumns().forEach(column -> {
            final String key = column.getKey();
            if (key != null) {
                columnKeys.add(keyStub + "-" + key);
            }
        });
        final Map<String, String> savedVisible = MemberSettingsStorageFactory.getUserSettingsStorage().get(columnKeys);

        this.grid.getColumns().forEach(column -> {
            final String key = column.getKey();
            final boolean columnNotVisible = columnsHiddenOnLoad != null ? columnsHiddenOnLoad.contains(key) : false;
            final String setting = keyStub + "-" + key;
            final String setVisible = columnNotVisible ? "false" : savedVisible.get(setting);
            if (setVisible != null && !setVisible.isEmpty()) {
                grid.getColumnByKey(key).setVisible(Boolean.parseBoolean(setVisible));
            }
        });
    }

    private void storeVisibiltyUpdate(final Column<T> column) {
        final String keyStub = uniqueId + "-visible";
        final boolean isVisible = column.isVisible();
        MemberSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + column.getKey(), "" + isVisible);
    }

    private void configureSaveColumnOrder() {
        logger.debug("Setting up: " + uniqueId);
        if (grid.isColumnReorderingAllowed()) {
            final String keyStub = uniqueId + "-order";

            final List<Column<T>> availableColumns = grid.getColumns();
            final String columns = MemberSettingsStorageFactory.getUserSettingsStorage().get(keyStub);
            if (availableColumns.size() > 0 && columns != null && !columns.isEmpty()) {
                final String[] parsedColumns = columns.split(", ?");
                if (parsedColumns.length > 0) {
                    try {
                        grid.setColumnOrder(calculateColumnOrder(availableColumns, parsedColumns));
                    } catch (IllegalArgumentException e) {
                        logger.error(
                                e.getMessage() + " Error caused by missing entry in TblUserSettings for " + keyStub);
                    }
                }
            }

            grid.addColumnReorderListener(event -> {
                final List<Column<T>> reorderedColumns = event.getColumns();
                if (reorderedColumns.size() > 0) {
                    String parsedColumns = "";
                    for (Column<T> column : reorderedColumns) {
                        parsedColumns += column.getKey() + ", ";
                    }

                    parsedColumns = parsedColumns.substring(0, parsedColumns.length() - 2);
                    MemberSettingsStorageFactory.getUserSettingsStorage().store(keyStub, "" + parsedColumns);
                }
            });
        }
    }

    /**
     * If a column order has already been saved for a user, but the columns for a
     * grid have been modified, then we need to remove any columns that no longer
     * exist and add any new columns to the list of visible columns.
     *
     * @param availableColumns the columns that are available in the grid
     * @param parsedColumns    the column order that has been restored from
     *                         preferences
     * @return the calculated order of columns with old removed and new added
     */
    private List<Column<T>> calculateColumnOrder(final List<Column<T>> availableColumns, final String[] parsedColumns) {
        final List<String> availableList = new ArrayList<>(availableColumns.size());
        for (Column<T> column : availableColumns) {
            if (column.getKey() != null) {
                availableList.add(column.getKey());
            }
        }
        final List<String> parsedList = new ArrayList<>(Arrays.asList(parsedColumns));

        // Remove old columns
        parsedList.retainAll(availableList);

        // Add new columns in the same index position as they were added to the
        // grid in
        final List<String> newList = new ArrayList<>();
        for (String column : newList) {
            parsedList.add(availableList.indexOf(column), column);
        }

        final List<Column<T>> orderedColumns = new ArrayList<>();
        parsedList.forEach(col -> {
            orderedColumns.add(this.grid.getColumnByKey(col));
        });

        return orderedColumns;
    }

    private void addActionColumn() {
        // Only add the action icon in the header if it's been set.
        final HorizontalLayout header = new HorizontalLayout(setActionIcon ? actionIcon : new Span());
        header.setJustifyContentMode(JustifyContentMode.END);

        // Take a copy of the columns so we can reorder with the action column as first
        // column.
        final List<Column<T>> columns = grid.getColumns();
        // Check if the column has already been set. If not, add the column, but only if
        // either context menu supplied, or action menu is required.
        actionColumn = grid.getColumnByKey(ACTION_MENU);
        if (gridContextMenu == null && !setActionIcon) {
            return;
        } else {
            if (actionColumn == null) {
                if (gridContextMenu != null) {
                    actionColumn = grid.addColumn(getContextMenu()).setHeader(header).setWidth("25px").setFlexGrow(0)
                            .setFrozen(true).setKey(ACTION_MENU);
                } else if (setActionIcon) {
                    actionColumn = grid.addColumn(getNoContextMenu()).setHeader(header).setWidth("25px").setFlexGrow(0)
                            .setFrozen(true).setKey(ACTION_MENU);
                }
            } else {
                actionColumn.setHeader(header);
            }
        }
        // As we are adding this column after the other columns have been added, create
        // a new list, add the action column, then add all the previously added columns.
        // Then set the grid order to this.
        final List<Column<T>> newOrderedColumns = new ArrayList<>();
        // Add the the action column to first position
        newOrderedColumns.add(0, actionColumn);
        int count = 1;
        for (Column<T> column : columns) {
            if (column.getKey() != null) {
                // Now add all other columns, except action column, if it exists.
                if (!column.getKey().equals(actionColumn.getKey())) {
                    newOrderedColumns.add(column);
                }
            } else {
                logger.error("Column #" + count + " is missing it's key. Make sure all columns have keys set.");
                return;
            }
            count++;
        }
        grid.setColumnOrder(newOrderedColumns);
    }

    private void addActionItems(final Map<String, String> headersMap) {
        final ColumnActionContextMenu columnActionContextMenu = new ColumnActionContextMenu(actionIcon);
        grid.getColumns().forEach(column -> {
            if (column.getKey() != null) {
                final String header = Optional.ofNullable(headersMap).map(e -> e.get(column.getKey())).orElse(null);
                if (!column.isFrozen() && header != null) {
                    columnActionContextMenu.addColumnActionItem(header, column);
                }
            }
        });
    }

    private class ColumnActionContextMenu extends ContextMenu {
        private static final long serialVersionUID = 1L;

        public ColumnActionContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnActionItem(String label, Grid.Column<T> column) {
            final MenuItem menuItem = this.addItem(label, e -> {
                final boolean checked = e.getSource().isChecked();
                column.setVisible(checked);
                storeVisibiltyUpdate(column);
            });
            menuItem.setCheckable(true);
            // If the column is setVisible(false) for initial hiding, it may have a stored
            // setting that overrides that.
            // Get the stored setting and set the context menu to show the correct status.
            // Some columns are set to be hidden at load, despite stored settings. Find
            // these and override if present.
            final String keyStub = uniqueId + "-visible";
            final boolean columnNotVisible = columnsHiddenOnLoad != null ? columnsHiddenOnLoad.contains(column.getKey())
                    : false;
            final String storedVisibleSetting = columnNotVisible ? "false"
                    : MemberSettingsStorageFactory.getUserSettingsStorage().get(keyStub + "-" + column.getKey());
            boolean showColumn = false;
            if (storedVisibleSetting == "true" || column.isVisible()) {
                showColumn = true;
            } else if ((storedVisibleSetting == "false") || !column.isVisible()) {
                showColumn = false;
            }
            menuItem.setChecked(showColumn);
        }
    }

    /**
     * Setting this will add a component for each row to the action column and
     * allows a context menu to be activated from it.<br>
     * Note: this is only needed if adding a context menu button on a plain grid.
     * VaadinCrud.SearchableGrid has methods to do this.
     * 
     * @param gridContextMenu A {@link ComponentRenderer} that will add a component
     *                        for each row
     */
    public void setGridContextMenu(ComponentRenderer<Component, T> gridContextMenu) {
        this.gridContextMenu = gridContextMenu;
    }

    private ComponentRenderer<Component, T> getContextMenu() {
        return this.gridContextMenu;
    }

    private ComponentRenderer<Component, T> getNoContextMenu() {
        return new ComponentRenderer<>(record -> {
            return new Span();
        });
    }

    /**
     * Setting this will add an icon in the action column header allowing columns to
     * be shown or hidden.
     */
    public void setActionIcon() {
        setActionIcon = true;
    }

    private void setColumnsResizable() {
        if (resizable) {
            // Never allow Action Menu column to be resizable.
            grid.getColumns().forEach(column -> {
                if (column.getKey() != null && !column.getKey().equalsIgnoreCase(ACTION_MENU)) {
                    column.setResizable(true);
                    column.setFlexGrow(0);
                }
            });
        } else if (!resizableColumns.isEmpty()) {
            resizableColumns.forEach(column -> {
                if (!column.getKey().equalsIgnoreCase(ACTION_MENU)) {
                    column.setResizable(true);
                    column.setFlexGrow(0);
                }
            });
        } else {
            // Set columns resizable if they don't have setFlexGrow(0)
            grid.getColumns().forEach(column -> {
                if (column.getKey() != null && !column.getKey().equalsIgnoreCase(ACTION_MENU)) {
                    column.setResizable(column.getFlexGrow() != 0);
                }
            });
        }
    }

    /**
     * Convenience method to set all columns (except the action column) to be
     * resizeable.
     * 
     * @param resizable A <code>boolean</code>. True to set all columns resizeable,
     *                  false to leave them locked to the user.
     */
    public void setAllColumnsResizable(final boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Method to set columns resizeable, but only the ones contained in the List
     * 
     * @param columns A {@link List} of {@link Column}s that will be set resizeable.
     */
    public void setSelectedColumnsResizable(List<Column<T>> columns) {
        this.resizableColumns.clear();
        this.resizableColumns.addAll(columns);
    }

    /**
     * Sets all columns sortable, except the action menu (if included).
     */
    public void setAllColumnsSortable() {
        this.grid.getColumns().forEach(column -> {
            column.setSortable(!column.getKey().equalsIgnoreCase(ACTION_MENU));
        });
    }

    /**
     * Set column(s) non-sortable.
     * 
     * @param keys The list of keys for columns. If the key name is incorrect, or
     *             not added to the column, there is no change to column sorting
     *             status.
     */
    public void setColumnsNonSortable(Set<String> keys) {
        keys.forEach(key -> {
            final Column<?> column = this.grid.getColumnByKey(key);
            // Check in case an key has not been set for a column
            if (column != null) {
                column.setSortable(false);
            }
        });
    }
}