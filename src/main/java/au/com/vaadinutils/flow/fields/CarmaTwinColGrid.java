package au.com.vaadinutils.flow.fields;

import java.util.List;

import com.flowingcode.vaadin.addons.twincolgrid.TwinColGrid;
import com.vaadin.flow.component.grid.GridVariant;

public class CarmaTwinColGrid<T> extends TwinColGrid<T> {

    private static final long serialVersionUID = -5818256742176989262L;

    public CarmaTwinColGrid() {
        getAvailableGrid().addThemeVariants(GridVariant.LUMO_COMPACT);
        getSelectionGrid().addThemeVariants(GridVariant.LUMO_COMPACT);
        getAvailableGrid().addThemeName("grid-selection-theme");
        getSelectionGrid().addThemeName("grid-selection-theme");
    }

    public CarmaTwinColGrid(final List<T> availablePersons) {
        super(availablePersons);
    }
}