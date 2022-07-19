package au.com.vaadinutils.flow.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

/**
 * Plugin in methods for storing and retrieving column widths, order and
 * visibility.<br>
 * To use, call after adding columns, supplying the grid and a unique Id. This
 * class will do the rest.<br>
 * For the grid itself, the columns to be processed will need to have an
 * explicit key set, e.g. <br>
 * 
 * <pre>{@code
 * 
 * grid.addColumn(<ValueProvider>).setWidth("150px").setFlexGrow(0).setHeader("ColumnHeader")
 *         .setKey("UniqueColumnName");
 * }</pre>
 *
 * Also allows adding an icon in the header to provide column show/hide
 * toggle.<br>
 * In order to do this, the storage must be created before adding columns to the
 * grid, specifically calling the addColumnToggle method (or not if excluding),
 * and then calling init().<br>
 * Otherwise the grid is missing columns and/or the toggle icon is in the wrong
 * header column.
 * 
 * @param <T> The underlying bean for the grid.
 */
public class MemberGridStorage<T> {

    private final Logger logger = LogManager.getLogger();
    private Grid<T> grid;
    private String uniqueId;
    private final Icon toggleIcon = VaadinIcon.MENU.create();

    public MemberGridStorage(final Grid<T> grid, final String uniqueId) {
        this.grid = grid;
        this.uniqueId = uniqueId;
        toggleIcon.setColor("#0066CC");
        toggleIcon.setSize("12px");
    }

    /**
     * Call after adding columns to the grid.
     * 
     * @param headersMap A {@link Map} of {@link String} pairs so that column keys
     *                   can be matched to headings.<br>
     *                   This is an issue because V14+ doesn't have a getHeaders
     *                   method.
     */
    public void init(final Map<String, String> headersMap) {
        if (this.grid.getColumns().isEmpty()) {
            logger.warn("Columns not set for " + uniqueId
                    + ". Grid column order, width and visibility will not be stored or retreived.");
        }
        addToggleItems(headersMap);
        configureSaveColumnWidths();
        configureSaveColumnVisible();
        configureSaveColumnOrder();
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
            final String width = e.getResizedColumn().getWidth();
            MemberSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + key, width);
        });
    }

    private Map<String, String> getSavedWidths(final List<String> columnKeys) {
        if (columnKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> savedWidths = MemberSettingsStorageFactory.getUserSettingsStorage().get(columnKeys);

        final HashMap<String, String> parsedWidths = new HashMap<>();
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
            logger.error(e.getMessage());
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
            final String setting = keyStub + "-" + key;
            final String setVisible = savedVisible.get(setting);
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
                        logger.error(e.getMessage());
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

    /**
     * Call before adding columns to the grid so the toggle icon can be in the first
     * column.
     */
    public void addToggleColumn() {
        final HorizontalLayout header = new HorizontalLayout(toggleIcon);
        header.setJustifyContentMode(JustifyContentMode.END);

        grid.addColumn(new ComponentRenderer<>(type -> {
            return new Span();
        })).setHeader(header).setWidth("25px").setFlexGrow(0).setFrozen(true);
    }

    private void addToggleItems(final Map<String, String> headersMap) {
        final ColumnToggleContextMenu columnToggleContextMenu = new ColumnToggleContextMenu(toggleIcon);
        grid.getColumns().forEach(column -> {
            if (column.getKey() != null) {
                final String header = Optional.ofNullable(headersMap).map(e -> e.get(column.getKey()))
                        .orElse(column.getKey());
                columnToggleContextMenu.addColumnToggleItem(header, column);
            }
        });
    }

    private class ColumnToggleContextMenu extends ContextMenu {
        private static final long serialVersionUID = 1L;

        public ColumnToggleContextMenu(Component target) {
            super(target);
            setOpenOnClick(true);
        }

        void addColumnToggleItem(String label, Grid.Column<T> column) {
            final MenuItem menuItem = this.addItem(label, e -> {
                final boolean checked = e.getSource().isChecked();
                column.setVisible(checked);
                storeVisibiltyUpdate(column);
            });
            menuItem.setCheckable(true);
            menuItem.setChecked(column.isVisible());
        }
    }
}
