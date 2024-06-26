package au.com.vaadinutils.fields;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.sort.Sort;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import au.com.vaadinutils.crud.AdvancedSearchListener;
import au.com.vaadinutils.crud.CrudEntity;
import au.com.vaadinutils.crud.GridHeadingPropertySet;
import au.com.vaadinutils.crud.GridHeadingPropertySet.Builder;
import au.com.vaadinutils.crud.GridHeadingPropertySetIfc;
import au.com.vaadinutils.crud.SearchableGrid;
import au.com.vaadinutils.dao.JpaBaseDao;

/**
 * Replaced in V14 migration.
 */
public class ComboBoxWithSearchFieldChooserWindow<T extends CrudEntity, C extends Indexed & Filterable> {
    private String caption;
    private ChooserListener listener;
    private SearchableGrid<T, C> grid;
    private C localContainer;

    Builder<T> builder = new GridHeadingPropertySet.Builder<>();
    private Class<? extends T> type;
    private GridHeadingPropertySetIfc<T> headingProps;
    private VerticalLayout holder;
    private String[] sortColumns;

    Logger logger = LogManager.getLogger();
    private Filter baseFilters;
    protected AdvancedSearchContentProvider advancedSearchProvider;
    private AdvancedSearchListener advancedSearchListener;
    private Button noneButton;

    public interface IndexedAndFilterable extends Indexed, Filterable {

    }

    /**
     * 
     * @param listener
     * @param type
     * @param caption
     * @param container
     * @param headingBuilder
     * @param sortColumns    - also used as the search columns
     */
    public ComboBoxWithSearchFieldChooserWindow(final ChooserListener listener, final Class<? extends T> type,
            final String caption, final C container, final Builder<T> headingBuilder, final String[] sortColumns) {
        this(listener, type, caption, container, headingBuilder, sortColumns, null, null);
    }

    /**
     * 
     * @param listener
     * @param type
     * @param caption
     * @param container
     * @param headingBuilder
     * @param sortColumns            -also used as the search columns
     * @param advancedSearchProvider
     * @param advancedSearchListener
     */
    @SuppressWarnings("unchecked")
    public ComboBoxWithSearchFieldChooserWindow(final ChooserListener listener, final Class<? extends T> type,
            final String caption, final C container, final Builder<T> headingBuilder, final String[] sortColumns,
            final AdvancedSearchContentProvider advancedSearchProvider,
            final AdvancedSearchListener advancedSearchListener) {

        this.caption = caption;
        this.listener = listener;
        this.type = type;
        this.sortColumns = sortColumns;
        if (sortColumns == null || sortColumns.length == 0) {
            this.sortColumns = new String[] { "name" };
        }

        if (container != null) {
            this.localContainer = container;
        } else {
            this.localContainer = (C) JpaBaseDao.getGenericDao(type).createVaadinContainer();
        }

        if (headingBuilder != null) {
            builder = headingBuilder;
        } else {
            builder.createColumn("Action", "name").setLockedState(true).addColumn().build();

        }
        headingProps = builder.build();

        this.advancedSearchProvider = advancedSearchProvider;
        this.advancedSearchListener = advancedSearchListener;

    }

    @SuppressWarnings("unchecked")
    public Component getPopupContent2() {

        grid = new SearchableGrid<T, C>() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public GridHeadingPropertySetIfc<T> getHeadingPropertySet() {

                return headingProps;
            }

            @Override
            protected Filter getContainerFilter(final String filterString, final boolean advancedSearchActive) {
                final List<Filter> filters = new ArrayList<>();

                if (baseFilters != null) {
                    filters.add(baseFilters);
                }

                if (filterString.length() > 0) {
                    final List<Filter> orFilters = new LinkedList<>();

                    for (final String search : sortColumns) {
                        // protect against empty filters or fields
                        if (StringUtils.isNotBlank(search) && StringUtils.isNotBlank(filterString)) {
                            if (localContainer.getType(search).isAssignableFrom(String.class)) {
                                orFilters.add(new SimpleStringFilter(search, filterString, true, false));
                            } else {
                                logger.warn("Can't text search on nested field '{}'", search);
                            }
                        }
                    }
                    filters.add(new Or(orFilters.toArray(new Filter[] {})));

                }

                if (filters.size() == 0) {
                    return null;
                }

                return new And(filters.toArray(new Filter[] {}));
            }

            @Override
            protected String getTitle() {
                return null;
            }

            @Override
            public C getContainer() {

                return localContainer;

            }

            @Override
            protected AbstractLayout getAdvancedSearchLayout() {
                if (advancedSearchProvider == null) {
                    return null;
                }
                return advancedSearchProvider.getAdvancedSearchLayout();
            }

            @Override
            protected AdvancedSearchListener getAdvancedSearchListener() {
                return advancedSearchListener;
            }

        };

        grid.init(caption, (Class<T>) type);
        // grid.getGrid().removeHeaderRow(0);

        final ItemClickListener itemClickListener = new ItemClickListener() {

            private static final long serialVersionUID = -4659382639878762429L;

            @Override
            public void itemClick(final ItemClickEvent event) {
                listener.selected(event.getItemId());

            }

        };

        final Sort sort = Sort.by(sortColumns[0]);
        for (int i = 1; i < sortColumns.length; i++) {

            sort.then(sortColumns[i]);
        }

        grid.sort(sort);

        grid.setSelectionMode(SelectionMode.SINGLE);

        grid.addItemClickListener(itemClickListener);
        grid.setSizeFull();

        holder = new VerticalLayout();
        holder.setWidth("100%");
        holder.setHeight("300");
        holder.addComponent(grid);

        grid.setId("PopupGrid");

        return holder;
    }

    void select(final T currentValue) {
        grid.setSearchFilterText("");
        if (currentValue != null) {
            Object id = currentValue;
            // BeanItemContainer uses the entity as the ID, JPAContainer uses
            // the ID
            if (!(grid.getContainerDataSource() instanceof BeanItemContainer)) {
                id = currentValue.getId();
            }
            if (grid.getContainerDataSource().containsId(id)) {

                grid.select(id);
                grid.getGrid().scrollTo(id);
            }

        }
        grid.focus();

    }

    public void setContainerFilters(final Filter filter) {
        baseFilters = filter;

        localContainer.removeAllContainerFilters();
        localContainer.addContainerFilter(filter);

    }

    public void setNullSelectionAllowed(final boolean b, final String prompt) {
        if (b) {
            if (noneButton == null) {
                noneButton = new Button(StringUtils.defaultString(prompt, "Select None"));
                noneButton.setWidth("100%");
                noneButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
                noneButton.addClickListener(new ClickListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void buttonClick(final ClickEvent event) {
                        listener.selected(null);
                        grid.select(null);

                    }
                });
                grid.addComponent(noneButton);
            }
        }
    }

    public boolean containerContains(final Object id) {

        return grid.getContainerDataSource().containsId(id);
    }

    public void showAdvancedSearch(final boolean show) {
        grid.showAdvancedSearch(show);

    }

    public void triggerFilter() {
        grid.triggerFilter();
    }

}
