package au.com.vaadinutils.crud;

import java.util.Collection;

import com.vaadin.data.Container;

import au.com.vaadinutils.fields.TableCheckBoxSelect;

/**
 * Replaced in V14 migration.
 */
public class SelectableEntityTable<E> extends TableCheckBoxSelect {
    // private static transient Logger logger =
    // LogManager.getLogger(SelectableEntityTable.class);

    private static final long serialVersionUID = 1L;
    private final String uniqueId;

    public SelectableEntityTable(final Container.Filterable childContainer, final HeadingPropertySet headingPropertySet,
            final String uniqueId) {
        this.uniqueId = uniqueId;
        setContainerDataSource(childContainer);
        buildSelectableContainer(headingPropertySet);

    }

    /**
     * copy the containers items into a new IndexContainer which also contains the
     * 'selectable' property.
     * 
     * @param entityContainer2
     * @param headingPropertySet2
     * @return
     */
    private void buildSelectableContainer(final HeadingPropertySet visibleColumns) {
        visibleColumns.applyToTable(this, uniqueId);

        setColumnHeader(TableCheckBoxSelect.TABLE_CHECK_BOX_SELECT, "");

    }

    /**
     * returns an array of selected entities.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Collection<Long> getSelectedIds() {
        return (Collection<Long>) super.getSelectedItems();
    }

    public void applyFilter(final Filter filter) {
        /* Reset the filter for the Entity Container. */
        resetFilters();
        ((Container.Filterable) getContainerDataSource()).addContainerFilter(filter);

    }

    protected void resetFilters() {
        ((Container.Filterable) getContainerDataSource()).removeAllContainerFilters();
    }

}
