package au.com.vaadinutils.flow.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;

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
 * @param <T> The underlying bean for the grid.
 */
public class MemberGridStorage<T> {

    private final Logger logger = LogManager.getLogger();
    private final Grid<T> grid;
    private String uniqueId;

    public MemberGridStorage(final Grid<T> grid, final String uniqueId) {
        this.grid = grid;
        this.uniqueId = uniqueId;

        if (grid.getColumns().isEmpty()) {
            logger.warn("Columns not set for " + uniqueId
                    + ". Grid column order, width and visibility will not be stored or retreived.");
        }

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
            if (column.isResizable()) {
                final String key = column.getKey();
                columnKeys.add(keyStub + "-" + key);
            }
        });
        final Map<String, String> savedVisible = MemberSettingsStorageFactory.getUserSettingsStorage().get(columnKeys);

        this.grid.getColumns().forEach(column -> {
            final String key = column.getKey();
            final String setting = keyStub + "-" + key;
            final String setVisible = savedVisible.get(setting);
            if (setVisible != null && !setVisible.isEmpty()) {
                grid.getColumnByKey(key).setVisible(!Boolean.parseBoolean(setVisible));
            }
        });

        // TODO LC: Cannot do until we implement a function to show/hide columns for
        // user. Flow provides no listeners for this.
//        grid.addColumnVisibilityChangeListener(event -> {
//            final Column column = event.getColumn();
//            final boolean isVisible = !column.isHidden();
//            MemberSettingsStorageFactory.getUserSettingsStorage().store(keyStub + "-" + column.getPropertyId(),
//                    "" + isVisible);
//        });
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
}
