package au.com.vaadinutils.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.DefaultQueryModifierDelegate;
import com.vaadin.data.Buffered;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.RowDescriptionGenerator;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import au.com.vaadinutils.crud.GridHeadingPropertySet;
import au.com.vaadinutils.crud.SearchableGrid;
import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.dao.NullFilter;
import au.com.vaadinutils.flow.dao.CrudEntity;

/**
 * Replaced in Vaadin 14 migration.
 */
public class TwinColumnSelect<C extends CrudEntity> extends CustomField<Collection<C>> {
    private static final long serialVersionUID = -4316521010865902678L;

    // private boolean initialised;
    private final SingularAttribute<C, ?> itemCaptionProperty;
    private final Class<C> itemClass;
    private Collection<C> sourceValue;
    @SuppressWarnings("rawtypes")
    private Class<? extends Collection> valueClass;
    private SearchableGrid<C, JPAContainer<C>> availableGrid;
    private JPAContainer<C> availableContainer;
    private Grid selectedGrid;
    private BeanContainer<Long, C> selectedBeans;
    private SingularAttribute<C, Long> beanIdField;

    private final HorizontalLayout mainLayout = new HorizontalLayout();
    private final Button addNewButton = new Button(FontAwesome.PLUS);
    private final Button addButton = new Button(">");
    private final Button removeButton = new Button("<");
    private final Button removeAllButton = new Button("<<");
    private final Button addAllButton = new Button(">>");

    private Filter baselineFilter;
    private Filter selectedFilter;

    private String availableColumnHeader = "Available";
    private String selectedColumnHeader = "Selected";

    private LinkedHashSet<ValueChangeListener<C>> valueChangeListeners = null;
    private CreateNewCallback<C> createNewCallback;

    private final boolean sortAscending = true;
    private boolean showAddRemoveAll;

    private static final float BUTTON_LAYOUT_WIDTH = 50;
    private static final float BUTTON_WIDTH = 45;
    private static final float DEFAULT_COLUMN_WIDTH = 200;
    private static final float DEFAULT_COLUMN_HEIGHT = 300;

    public TwinColumnSelect(final String caption, final SingularAttribute<C, ?> itemCaptionProperty) {
        this.setCaption(caption);
        this.itemCaptionProperty = itemCaptionProperty;
        itemClass = itemCaptionProperty.getDeclaringType().getJavaType();

        createSelectedGrid();
        createAvailableGrid();

        customizeGrids(selectedGrid, availableGrid);

        addNewButton.setVisible(false);
    }

    private void createSelectedGrid() {
        selectedGrid = new Grid();
        selectedGrid.setContainerDataSource(createSelectedContainer());

        selectedGrid.setWidth(DEFAULT_COLUMN_WIDTH, Unit.PIXELS);
        selectedGrid.setHeight(DEFAULT_COLUMN_HEIGHT, Unit.PIXELS);

        selectedGrid.addItemClickListener(new ItemClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(final ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    removeButton.click();
                }
            }
        });

        createSelectedHeadings().applyToGrid(selectedGrid);

        // initialised = true;
    }

    protected void customizeGrids(final Grid selectedGrid, final SearchableGrid<C, JPAContainer<C>> availableGrid2) {
        // Overload this method a do some customization of the grids.

    }

    protected GridHeadingPropertySet<C> createSelectedHeadings() {
        return new GridHeadingPropertySet.Builder<C>()
                .addColumn(selectedColumnHeader, itemCaptionProperty.getName(), true, true).build();
    }

    private BeanContainer<Long, C> createSelectedContainer() {
        final Metamodel metaModel = EntityManagerProvider.getEntityManager().getMetamodel();
        final EntityType<C> type = metaModel.entity(itemClass);
        beanIdField = type.getDeclaredId(Long.class);
        selectedBeans = new BeanContainer<>(itemClass);
        selectedBeans.setBeanIdProperty(beanIdField.getName());
        sortSelectedBeans();

        return selectedBeans;
    }

    protected GridHeadingPropertySet<C> createAvailableHeadings() {
        return new GridHeadingPropertySet.Builder<C>()
                .addColumn(availableColumnHeader, itemCaptionProperty.getName(), true, true).build();
    }

    protected void createAvailableGrid() {
        createAvailableContainer();
        // TODO: Add proper uniqueId
        availableGrid = new SearchableGrid<C, JPAContainer<C>>(this.getClass().getSimpleName(), itemClass) {
            private static final long serialVersionUID = 1L;

            @Override
            public GridHeadingPropertySet<C> getHeadingPropertySet() {
                return createAvailableHeadings();
            }

            @Override
            public JPAContainer<C> getContainer() {
                return availableContainer;
            }

            @Override
            protected Filter getContainerFilter(final String filterString, final boolean advancedSearchActive) {
                Filter searchFilter = null;
                if (filterString != null && filterString.length() > 0) {
                    searchFilter = getSearchFilter(filterString);
                }

                return NullFilter.and(baselineFilter, selectedFilter, searchFilter);
            }

            @Override
            protected String getTitle() {
                return null;
            }
        };

        // Needs to be here after availableContainer creation,
        // otherwise sorting goes away
        sortAvailableContainer();

        availableGrid.addItemClickListener(new ItemClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void itemClick(final ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    addButton.click();
                }
            }
        });

        availableGrid.setWidth(DEFAULT_COLUMN_WIDTH, Unit.PIXELS);
        availableGrid.setHeight(DEFAULT_COLUMN_HEIGHT, Unit.PIXELS);
    }

    private JPAContainer<C> createAvailableContainer() {
        availableContainer = JpaBaseDao.getGenericDao(itemClass).createVaadinContainer();
        sortAvailableContainer();

        return availableContainer;
    }

    // @Override
    // public void beforeClientResponse(boolean initial)
    // {
    // super.beforeClientResponse(initial);
    // if (!initialised)
    // {
    // // TODO: Add proper uniqueId
    // new GridHeadingPropertySet.Builder<C>()
    // .addColumn(selectedColumnHeader, itemCaptionProperty.getName(), true,
    // true).build()
    // .applyToGrid(selectedGrid, this.getClass().getSimpleName());
    // initialised = true;
    // }
    // }

    @Override
    protected Component initContent() {
        mainLayout.addComponent(availableGrid);
        final VerticalLayout buttonLayout = buildButtons();
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(selectedGrid);
        mainLayout.setExpandRatio(availableGrid, 1);
        mainLayout.setExpandRatio(selectedGrid, 1);

        return mainLayout;
    }

    private VerticalLayout buildButtons() {
        addButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
        removeButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
        addAllButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
        removeAllButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);
        addNewButton.setWidth(BUTTON_WIDTH, Unit.PIXELS);

        addButton.addClickListener(addClickListener());
        removeButton.addClickListener(removeClickListener());
        addAllButton.addClickListener(addAllClickListener());
        removeAllButton.addClickListener(removeAllClickListener());
        addNewButton.addClickListener(addNewClickListener());

        final VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setWidth(BUTTON_WIDTH, Unit.PIXELS);
        buttonLayout.addComponents(addButton, removeButton, addAllButton, removeAllButton, addNewButton);

        return buttonLayout;
    }

    public void setEnabledAddAllButton(final boolean enabled) {
        addAllButton.setVisible(enabled);
        addAllButton.setEnabled(enabled);
    }

    public void setEnabledAddButton(final boolean enabled) {
        // issue encountered : even when setting visible to false, the button
        // still appears on the page but setEnabled has no issue
        addButton.setVisible(enabled);
        addButton.setEnabled(enabled);
    }

    public void setShowAddRemoveAll(final boolean show) {
        showAddRemoveAll = show;
        addAllButton.setVisible(show);
        removeAllButton.setVisible(show);
    }

    public void setAddNewAllowed(final CreateNewCallback<C> createNewCallback) {
        addNewButton.setVisible(true);
        this.createNewCallback = createNewCallback;
    }

    @Override
    public void setWidth(final float width, final Unit unit) {
        super.setWidth(width, unit);
        if (mainLayout != null && selectedGrid != null && availableGrid != null) {
            mainLayout.setWidth(width, unit);

            selectedGrid.setWidth(((width - 5) / 2) - (BUTTON_LAYOUT_WIDTH / 2), unit);
            availableGrid.setWidth(((width - 5) / 2) - (BUTTON_LAYOUT_WIDTH / 2), unit);
        }
    }

    @Override
    public void setHeight(final String height) {
        super.setHeight(height);
        selectedGrid.setHeight(height);
        availableGrid.setHeight(height);
        mainLayout.setHeight(height);
    }

    @Override
    public void setSizeFull() {
        super.setSizeFull();
        mainLayout.setSizeFull();
        selectedGrid.setSizeFull();
        availableGrid.setSizeFull();
    }

    @Override
    public void setReadOnly(final boolean b) {
        super.setReadOnly(b);
        selectedGrid.setReadOnly(b);
        availableGrid.setVisible(!b);
        addButton.setVisible(!b);
        removeButton.setVisible(!b);
        if (showAddRemoveAll) {
            addAllButton.setVisible(!b);
            removeAllButton.setVisible(!b);
        }
    }

    public void sortAvailableContainer() {
        availableContainer.sort(new Object[] { itemCaptionProperty.getName() }, new boolean[] { sortAscending });
    }

    public void sortSelectedBeans() {
        selectedBeans.sort(new Object[] { itemCaptionProperty.getName() }, new boolean[] { sortAscending });
    }

    public interface ValueChangeListener<C> {
        void valueChanged(Collection<C> value);
    }

    public void addValueChangeListener(final ValueChangeListener<C> listener) {
        if (valueChangeListeners == null) {
            valueChangeListeners = new LinkedHashSet<>();
        }
        valueChangeListeners.add(listener);
    }

    public void setFilter(final Filter filter) {
        baselineFilter = filter;
        availableContainer.setFireContainerItemSetChangeEvents(false);
        availableContainer.removeAllContainerFilters();
        availableContainer.setFireContainerItemSetChangeEvents(true);
        availableContainer.addContainerFilter(filter);
    }

    public void setFilterDelegate(final DefaultQueryModifierDelegate defaultQueryModifierDelegate) {
        availableContainer.setQueryModifierDelegate(defaultQueryModifierDelegate);
    }

    @Override
    public void commit() throws Buffered.SourceException, InvalidValueException {
        super.commit();
        final Collection<C> tmp = getConvertedValue();

        // avoid possible npe
        if (sourceValue == null) {
            sourceValue = tmp;
        }

        // add missing
        for (final C c : tmp) {
            if (!sourceValue.contains(c)) {
                sourceValue.add(c);
            }
        }

        // remove unneeded
        final Set<C> toRemove = new HashSet<>();
        for (final C c : sourceValue) {
            if (!tmp.contains(c)) {
                toRemove.add(c);
            }
        }
        sourceValue.removeAll(toRemove);
    }

    @Override
    public boolean isModified() {
        final Collection<C> convertedValue = getConvertedValue();
        Preconditions.checkNotNull(convertedValue,
                "If you look at getConvertedValue, you'll see convertedValue can never be null");

        if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue.size() > 0)) {
            return true;
        }
        if ((sourceValue == null || sourceValue.size() == 0) && (convertedValue.size() == 0)) {
            return false;
        }
        final boolean equal = convertedValue.containsAll(sourceValue) && sourceValue.containsAll(convertedValue);
        return !equal;
    }

    @Override
    protected void setInternalValue(final Collection<C> newValue) {
        if (newValue != null) {
            valueClass = newValue.getClass();
        }
        super.setInternalValue(newValue);

        selectedBeans.removeAllItems();
        if (newValue != null) {
            selectedBeans.addAll(newValue);
        }
        sourceValue = getConvertedValue();

        refreshSelected();
        sortSelectedBeans();
    }

    @Override
    public Collection<C> getConvertedValue() {
        Collection<C> selected;
        if (valueClass != null && List.class.isAssignableFrom(valueClass)) {
            selected = new LinkedList<>();
        } else {
            selected = new HashSet<>();
        }

        for (final Long id : selectedBeans.getItemIds()) {
            selected.add(selectedBeans.getItem(id).getBean());
        }

        return selected;
    }

    @Override
    public Collection<C> getValue() {
        return getConvertedValue();
    }

    @Override
    public Collection<C> getInternalValue() {
        return getConvertedValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Collection<C>> getType() {
        // Had to remove this as maven can't compile it.
        // return (Class<? extends Collection<C>>) a.getClass();
        return (Class<Collection<C>>) (Class<?>) Collection.class;
    }

    private void refreshSelected() {
        final List<Long> selectedIds = selectedBeans.getItemIds();
        if (selectedIds.size() == 1) {
            selectedFilter = new Not(new Compare.Equal(beanIdField.getName(), selectedIds.get(0)));
            availableGrid.triggerFilter();
            return;
        }

        final Vector<Filter> filters = new Vector<>();
        for (final Long id : selectedIds) {
            filters.add(new Compare.Equal(beanIdField.getName(), id));
        }
        selectedFilter = new Not(new Or(filters.toArray(new Filter[filters.size()])));
        availableGrid.triggerFilter();
    }

    public Collection<C> getSourceValue() {
        return sourceValue;
    }

    protected Grid getSelectedGrid() {
        return selectedGrid;
    }

    protected void addAction(final Long id) {
        final C item = JpaBaseDao.getGenericDao(itemClass).findById(id);
        if (item != null) {
            selectedBeans.addBean(item);
            if (valueChangeListeners != null) {
                for (final ValueChangeListener<C> listener : valueChangeListeners) {
                    listener.valueChanged(getValue());
                }
            }
        }
    }

    protected void removeAction(final Long id) {
        selectedBeans.removeItem(id);
        if (valueChangeListeners != null) {
            for (final ValueChangeListener<C> listener : valueChangeListeners) {
                listener.valueChanged(getValue());
            }
        }
    }

    protected void removeAllAction() {
        selectedBeans.removeAllItems();
        if (valueChangeListeners != null) {
            for (final ValueChangeListener<C> listener : valueChangeListeners) {
                listener.valueChanged(getValue());
            }
        }
    }

    public void setSelectedRowDescriptionGenerator(final RowDescriptionGenerator generator) {
        selectedGrid.setRowDescriptionGenerator(generator);
    }

    public String getAvailableColumnHeader() {
        return availableColumnHeader;
    }

    public void setAvailableColumnHeader(final String availableColumnHeader) {
        this.availableColumnHeader = availableColumnHeader;
    }

    public SearchableGrid<C, JPAContainer<C>> getAvailableGrid() {
        return availableGrid;
    }

    public void setAvailableGrid(final SearchableGrid<C, JPAContainer<C>> availableGrid) {
        this.availableGrid = availableGrid;
    }

    public String getSelectedColumnHeader() {
        return selectedColumnHeader;
    }

    public void setSelectedColumnHeader(final String selectedColumnHeader) {
        this.selectedColumnHeader = selectedColumnHeader;
    }

    protected ClickListener addClickListener() {
        return new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                final Collection<Object> ids = availableGrid.getSelectedRows();
                if (!ids.isEmpty()) {
                    for (final Object id : ids) {
                        addAction((Long) id);
                    }

                    availableGrid.select(null);
                    refreshSelected();
                    sortSelectedBeans();
                }

            }
        };
    }

    protected ClickListener addAllClickListener() {
        return new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                final Collection<Object> ids = availableContainer.getItemIds();
                if (!ids.isEmpty()) {
                    for (final Object id : ids) {
                        addAction((Long) id);
                    }

                    availableGrid.select(null);
                    refreshSelected();
                    sortSelectedBeans();
                }
            }
        };
    }

    protected ClickListener removeClickListener() {
        return new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                final Collection<Object> ids = selectedGrid.getSelectedRows();
                if (!ids.isEmpty()) {
                    for (final Object id : ids) {
                        removeAction((Long) id);
                    }
                    selectedGrid.select(null);
                    refreshSelected();
                }

            }
        };
    }

    protected ClickListener removeAllClickListener() {
        return new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                removeAllAction();
                refreshSelected();
            }
        };
    }

    protected ClickListener addNewClickListener() {
        return new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                createNewCallback.createNew(new RefreshCallback() {
                    @Override
                    public void refresh() {
                        availableContainer.refresh();
                    }
                });
            }
        };
    }

    protected Filter getSearchFilter(final String filterString) {
        return new SimpleStringFilter(itemCaptionProperty.getName(), filterString, true, false);
    }

    protected void triggerFilterForAvailableGrid() {
        availableGrid.triggerFilter();
    }

    @Override
    public void focus() {
        availableGrid.focus();
    }
}
