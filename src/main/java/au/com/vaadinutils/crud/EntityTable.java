package au.com.vaadinutils.crud;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.flow.errorhandling.ErrorWindow;

import org.apache.logging.log4j.LogManager;

/**
 * @deprecated Replaced in V14 migration.
 */
public class EntityTable<E> extends Table implements EntityList<E> {

    private static final long serialVersionUID = 1L;
    private JPAContainer<E> entityContainer;
    private RowChangeListener<E> rowChangeListener;
    private HeadingPropertySet columnConfiguration;

    transient Logger logger = LogManager.getLogger(EntityTable.class);

    public EntityTable(JPAContainer<E> entityContainer, HeadingPropertySet headingPropertySet) {
        this.entityContainer = entityContainer;
        this.columnConfiguration = headingPropertySet;
        addStyleName(ValoTheme.TABLE_COMPACT);
        this.setContainerDataSource(entityContainer);
        addRightClickSelect();

    }

    @Override
    public Object prevVisibleItemId(Object itemId) {
        Object prev = null;
        for (Object id : getVisibleItemIds()) {
            if (id.equals(itemId)) {
                return prev;
            }
            prev = id;

        }
        return prev;
    }

    @Override
    public void setRowChangeListener(RowChangeListener<E> rowChangeListener) {
        this.rowChangeListener = rowChangeListener;
    }

    /**
     * @param uniqueTableId -an id for this layout/table combination, it is used to
     *                      identify stored column settings in a key value map
     */
    @Override
    public void init(String uniqueTableId) {

        columnConfiguration.applyToTable(this, uniqueTableId);

        this.setSelectable(true);
        this.setImmediate(true);
        this.setColumnReorderingAllowed(true);

        this.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                try {
                    if (EntityTable.this.rowChangeListener != null) {
                        Object entityId = EntityTable.this.getValue();

                        if (entityId != null) // it can be null when a row is
                        // being
                        // deleted.
                        {
                            EntityItem<E> entity = EntityTable.this.entityContainer.getItem(entityId); // .getEntity();
                            EntityTable.this.rowChangeListener.rowChanged(entity);
                        } else {
                            EntityTable.this.rowChangeListener.rowChanged(null);
                        }
                    } else {
                        logger.warn("no row change listener exists");
                    }
                } catch (Exception e) {
                    logger.error("{} {}", this.getClass().getCanonicalName(), e.getMessage());
                    throw e;
                }
            }
        });
    }

    public void superChangeVariables(final Object source, final Map<String, Object> variables) {

    }

    /**
     * Hooking this allows us to veto the user selecting a new row. if there is a
     * rowChangeListener we will prevent the row change. it's up to the listener to
     * callback on superChangeVariables to perform the row change if row change
     * should be allowed.
     */
    @Override
    public void changeVariables(final Object source, final Map<String, Object> variables) {
        try {
            if (variables.containsKey("selected")) {

                if (EntityTable.this.rowChangeListener != null) {
                    EntityTable.this.rowChangeListener.allowRowChange(new RowChangeCallback() {
                        @Override
                        public void allowRowChange() {
                            EntityTable.super.changeVariables(source, variables);
                        }
                    });
                    markAsDirty();
                } else {
                    EntityTable.super.changeVariables(source, variables);
                }
            } else {
                super.changeVariables(source, variables);
            }
        } catch (Exception e) {
            ErrorWindow.showErrorWindow(e, null);
        }
    }

    @Override
    public EntityItem<E> getCurrent() {
        Object entityId = this.getValue();
        EntityItem<E> entity = null;
        if ((entityId instanceof UUID)) {
            logger.warn("UUID here, this may be ok and even common when in a child crud.");
        }

        if (entityId != null)// && !(entityId instanceof UUID))
        {
            try {
                if ((entityId instanceof UUID)) {
                    if (entityContainer.getItemIds().contains(entityId)) {
                        entity = this.entityContainer.getItem(entityId);
                    } else {
                        Exception e = new Exception("Trying to look up a non existent UUID");
                        logger.error(e, e);
                    }
                } else {
                    entity = this.entityContainer.getItem(entityId);
                }
            } catch (Exception e) {
                logger.warn(e, e);
            }
        }

        return entity;

    }

    /**
     * This nasty piece of work exists to stop the following exception being thrown.
     * java.lang.IllegalArgumentException: wrong number of arguments
     * sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * sun.reflect.NativeMethodAccessorImpl
     * .invoke(NativeMethodAccessorImpl.java:57)
     * sun.reflect.DelegatingMethodAccessorImpl
     * .invoke(DelegatingMethodAccessorImpl.java:43)
     * java.lang.reflect.Method.invoke(Method.java:606)
     * com.vaadin.addon.jpacontainer
     * .metadata.ClassMetadata.getPropertyValue(ClassMetadata.java:168)
     * com.vaadin.addon.jpacontainer.metadata.ClassMetadata.getPropertyValue(
     * ClassMetadata.java:343)
     * com.vaadin.addon.jpacontainer.PropertyList.getPropertyValue
     * (PropertyList.java:677)
     * com.vaadin.addon.jpacontainer.JPAContainerItem$ItemProperty
     * .getRealValue(JPAContainerItem.java:176)
     * com.vaadin.addon.jpacontainer.JPAContainerItem$ItemProperty
     * .getValue(JPAContainerItem.java:163)
     * com.vaadin.ui.Table.formatPropertyValue(Table.java:4012)
     * com.vaadin.ui.Table.getPropertyValue(Table.java:3956)
     * com.vaadin.ui.Table.parseItemIdToCells(Table.java:2308)
     * com.vaadin.ui.Table.getVisibleCellsNoCache(Table.java:2147)
     * com.vaadin.ui.Table.refreshRenderedCells(Table.java:1668)
     * com.vaadin.ui.Table.enableContentRefreshing(Table.java:3143)
     * com.vaadin.ui.Table.setContainerDataSource(Table.java:2712)
     * com.vaadin.ui.Table.setContainerDataSource(Table.java:2653)
     * au.org.scoutmaster.views.ContactTable.init(ContactTable.java:46)
     */
    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property<?> property) {
        if (property.getType() == Set.class) {
            return null;
        }
        try {
            property.getValue();

        } catch (Exception e) {
            return null;
        }
        String ret = null;
        try {
            ret = super.formatPropertyValue(rowId, colId, property);
        } catch (Exception e) {
            logger.error("value: " + property.getValue() + " type: " + property.getType(), e);
            ret = e.getMessage();
        }
        return ret;
    }

    @Override
    public void addGeneratedColumn(Object id, ColumnGenerator generatedColumn) {
        super.addGeneratedColumn(id, generatedColumn);

    }

    @Override
    public void setConverter(String name, Converter<String, ?> converter) {
        super.setConverter(name, converter);

    }

    @Override
    public void setColumnCollapsed(String name, boolean collapsed) {
        super.setColumnCollapsed(name, collapsed);

    }

    /**
     * When {@link #select(Object)} is called,
     * {@link RowChangeListener#allowRowChange(RowChangeCallback)} does not get
     * called. Call this method if you wish to check whether a row change is allowed
     * before selecting a new row.
     */
    public void selectAndCheckRowChangeAllowed(final Object itemId) {
        if (EntityTable.this.rowChangeListener != null) {
            EntityTable.this.rowChangeListener.allowRowChange(new RowChangeCallback() {
                @Override
                public void allowRowChange() {
                    EntityTable.super.select(itemId);
                }
            });
        } else {
            select(itemId);
        }
    }

    /**
     * Adds a listener to select the right clicked item in the table. This is needed
     * by ContextMenus.
     */
    private void addRightClickSelect() {
        this.addItemClickListener(new ItemClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(ItemClickEvent event) {
                if (event.getButton() == MouseButton.RIGHT) {
                    EntityTable.this.setValue(event.getItemId());
                }
            }
        });
    }
}
