package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

public class CarmaGrid<T> extends Grid<T> {

    private static final long serialVersionUID = -3836033279044014063L;

    public CarmaGrid() {
        addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_COLUMN_BORDERS, GridVariant.LUMO_ROW_STRIPES);
        addThemeName("grid-selection-theme");
    }
}