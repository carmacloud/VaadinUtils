package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;

/**
 * Use Vaadin's Grid component instead
 */
public class TableCheckBoxSelect extends Table {
    public static final String TABLE_CHECK_BOX_SELECT = "TableCheckBoxSelect";
    private static final long serialVersionUID = -7559267854874304189L;
    protected MarkedIds markedIds = new MarkedIds();
    private boolean selectable = true;
    private final Set<ValueChangeListener> valueChangeListeners = new HashSet<ValueChangeListener>();
    protected int containerSize = 0;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public TableCheckBoxSelect() {
        initCheckboxMultiSelect();
        setImmediate(true);

        setId("TableCheckBoxSelect");

    }

    /**
     * call this method after adding your custom fields
     */
    public void initCheckboxMultiSelect() {

        this.addGeneratedColumn(TABLE_CHECK_BOX_SELECT, getGenerator());
        super.setMultiSelect(false);
        super.setSelectable(false);

    }

    @Override
    public void setMultiSelect(final boolean multi) {
        if (!multi) {
            throw new RuntimeException(
                    "This class is broken in single select mode, actually the single select code has been removed.\n\n Use TableCheckBoxSingleSelect instead!!!!\n\n");
        }

    }

    public void selectAll() {
        containerSize = getItemIds().size();
        markedIds.clear(false, containerSize);

        refreshRenderedCells();
        refreshRowCache();
        notifyValueChange();

    }

    public void deselectAll() {
        markedIds.clear(true, containerSize);

        refreshRenderedCells();
        refreshRowCache();
        notifyValueChange();

    }

    private Property.ValueChangeEvent getValueChangeEvent() {
        final Property.ValueChangeEvent event = new Property.ValueChangeEvent() {

            /**
             * 
             */
            private static final long serialVersionUID = 3393822114324878273L;

            @SuppressWarnings("rawtypes")
            @Override
            public Property getProperty() {
                return new Property() {

                    private static final long serialVersionUID = 8430716281101427107L;

                    @Override
                    public Object getValue() {
                        return getSelectedItems();
                    }

                    @Override
                    public void setValue(final Object newValue) throws ReadOnlyException {
                        throw new RuntimeException("Not implemented");
                    }

                    @Override
                    public Class getType() {
                        throw new RuntimeException("Not implemented");
                    }

                    @Override
                    public boolean isReadOnly() {
                        return true;
                    }

                    @Override
                    public void setReadOnly(final boolean newStatus) {
                        throw new RuntimeException("Not implemented");
                    }
                };
            }
        };
        return event;
    }

    @Override
    public void setColumnHeaders(final String... columnHeaders) {
        final Set<String> cols = new LinkedHashSet<String>();
        for (final String col : columnHeaders) {
            cols.add(col);
        }
        if (selectable) {
            cols.add("");
        }
        super.setColumnHeaders(cols.toArray(new String[] {}));

    }

    @Override
    public void setVisibleColumns(final Object... visibleColumns) {
        if (visibleColumns.length > 0) {
            final List<Object> cols = new LinkedList<Object>();
            for (final Object col : visibleColumns) {
                cols.add(col);
            }
            if (selectable) {
                cols.add(0, TABLE_CHECK_BOX_SELECT);
            }
            final Set<Object> uniqueCols = new LinkedHashSet<>();
            uniqueCols.addAll(cols);
            super.setVisibleColumns(uniqueCols.toArray());
            setColumnWidth(TABLE_CHECK_BOX_SELECT, 50);
        } else {
            // during initialisation it comes through here empty and if we add
            // ours in npe's out
            super.setVisibleColumns(visibleColumns);
        }

    }

    /**
     * use setSelectedValue instead, this method gets called before initialization
     */
    @Override
    public void setValue(final Object value) {
        final ArrayList<Object> v = new ArrayList<>(1);
        if (value != null)
            v.add(value);
        super.setValue(v);
    }

    @SuppressWarnings("unchecked")
    public void setSelectedValue(final Object value) {
        // If table is selectable with checkboxes then update the selected ids,
        // otherwise update the selected checkboxes
        if (selectable) {
            markedIds.clear(true, containerSize);
            markedIds.addAll((Collection<Long>) value);
        } else
            super.setValue(value);

        this.refreshRowCache();
    }

    @Override
    public boolean isMultiSelect() {
        return true;
    }

    /**
     * use disableSelectable instead
     */
    @Deprecated
    @Override
    public void setSelectable(final boolean s) {
        throw new RuntimeException("Use disableSelectable instead");
    }

    public void disableSelectable() {
        selectable = false;
        super.setSelectable(true);
        removeGeneratedColumn(TABLE_CHECK_BOX_SELECT);

    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    public Object getSelectedItems() {
        if (selectable == false) {
            return super.getValue();
        }

        if (markedIds.isTrackingSelected()) {
            return markedIds.getIds();
        }

        final TreeSet<Object> result = new TreeSet<Object>();
        result.addAll(getContainerDataSource().getItemIds());
        result.removeAll(markedIds.getIds());
        return result;
    }

    @Override
    public void addValueChangeListener(final ValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    /**
     * call getSelectedItems instead, can't use this method as Vaadins table calls
     * back to this method on a paint cycle, showing some items as selected
     */
    @Deprecated
    @Override
    public Object getValue() {
        return super.getValue();
    }

    protected ColumnGenerator getGenerator() {
        return new ColumnGenerator() {

            private static final long serialVersionUID = -6659059346271729122L;

            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {

                final CheckBox checkbox = new CheckBox();
                checkbox.setWidth("25");
                checkbox.setHeight("20");

                // important that the following code is executed before the
                // value change listener is added
                final boolean inList = markedIds.contains(itemId);
                checkbox.setValue(inList);
                checkbox.setId("checkboxSelect");
                if (!markedIds.isTrackingSelected()) {
                    checkbox.setValue(!inList);
                }

                checkbox.addValueChangeListener(new ValueChangeListener() {

                    private static final long serialVersionUID = 9170497247408214336L;

                    @Override
                    public void valueChange(final Property.ValueChangeEvent event) {

                        if ((Boolean) event.getProperty().getValue() == markedIds.isTrackingSelected()) {
                            markedIds.add(itemId);
                        } else {
                            markedIds.remove(itemId);
                        }

                        notifyValueChange();

                    }

                });
                checkbox.setImmediate(true);

                return checkbox;

            }
        };
    }

    protected void notifyValueChange() {
        for (final ValueChangeListener listener : valueChangeListeners) {
            listener.valueChange(getValueChangeEvent());
        }
        this.validateField();

    }

    private boolean validateField() {
        boolean valid = false;
        try {
            setComponentError(null);
            validate();
            valid = true;
        } catch (final InvalidValueException e) {
            setComponentError(new ErrorMessage() {

                private static final long serialVersionUID = -2976235476811651668L;

                @Override
                public String getFormattedHtmlMessage() {
                    return e.getHtmlMessage();
                }

                @Override
                public ErrorLevel getErrorLevel() {
                    return ErrorLevel.ERROR;
                }
            });
        }
        return valid;

    }

    public void addSelectionListener(final SelectionListener listener) {
        markedIds.addSelectionListener(listener);

    }
}
