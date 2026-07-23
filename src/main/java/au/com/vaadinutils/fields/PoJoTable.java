package au.com.vaadinutils.fields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * A simple class designed to display a PoJo in a table
 * 
 * By default each of the public getters are displayed in order as a column.
 * 
 * You can pass in a set of visible columns where the names correspond to the
 * getters name.
 * 
 * @author bsutton
 * 
 *         Replaced in Vaadin 14 migration.
 *
 */
public class PoJoTable<T> extends Table {
    private static final long serialVersionUID = 1L;

    transient Logger logger = LogManager.getLogger(PoJoTable.class);

    private Class<T> pojoClass;

    private ArrayList<Column> columns;

    private String[] visibleColumns;

    public PoJoTable() {

    }

    /**
     * @param clazz          the class of the Pojo that is to be displayed.
     * @param visibleColumns the list of columns from the pojo (getters) that are to
     *                       be displayed.
     */
    public PoJoTable(final Class<T> clazz, final String[] visibleColumns) {
        this.pojoClass = clazz;
        this.visibleColumns = visibleColumns;
        initColumns();
    }

    public void addRow(final T pojo) {
        Item item = this.getItem(pojo);
        // If the same item is sent again we just update it.
        if (item == null)
            item = this.addItem(pojo);

        for (final Column column : columns) {
            try {
                @SuppressWarnings("unchecked")
                final Property<Object> property = item.getItemProperty(column.name);
                property.setValue(column.getValue(pojo));
            } catch (final RuntimeException e) {
                logger.error(e, e);
                throw e;
            }
        }
    }

    private void initColumns() {
        final Class<T> objClass = this.pojoClass;
        columns = new ArrayList<Column>();

        // Get the public methods associated with this class.
        final Method[] methods = objClass.getMethods();
        for (final Method method : methods) {
            final String name = method.getName();
            if (name.startsWith("get") || name.startsWith("is")) {
                final Column column = new Column(method);
                if (this.visibleColumns == null || isVisible(column)) {
                    columns.add(column);
                    this.addContainerProperty(column.name, column.type, null);
                }
            }
        }
        if (columns.size() == 0)
            throw new IllegalArgumentException("The PoJo class does not have any public getters.");

        // check all of the visible columsn are available.
        if (columns.size() != visibleColumns.length) {
            for (final String visible : visibleColumns) {
                boolean found = false;
                for (final Column column : columns) {
                    if (column.name.equals(visible)) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    throw new IllegalArgumentException(
                            "The getter was not found for the required visible column " + visible + ".");
                }
            }
        }

    }

    private boolean isVisible(final Column column) {
        boolean isVisible = false;
        for (final String name : visibleColumns) {
            if (column.getName().equals(name))
                isVisible = true;
        }
        return isVisible;
    }

    class Column {
        private String name;
        private final Method method;
        private final Class<?> type;

        public Column(final Method method) {
            this.method = method;
            final String getterName = method.getName();
            if (getterName.startsWith("get")) {
                this.name = getterName.substring(3);
            } else {
                // must be is
                this.name = getterName.substring(2);
            }
            this.type = method.getReturnType();
        }

        public Object getName() {
            return name;
        }

        public Object getValue(final T pojo) {
            Object ret = null;
            try {
                ret = method.invoke(pojo);
            } catch (final IllegalAccessException e) {
                logger.error(e, e);
            } catch (final InvocationTargetException e) {
                logger.error(e, e);
            } catch (final IllegalArgumentException e) {
                logger.error(e, e);
            }

            return ret;
        }

    }
}
