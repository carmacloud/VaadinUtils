package au.com.vaadinutils.crud;

import com.google.common.base.Preconditions;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Grid.AbstractRenderer;

/**
 * Replaced in V14 migration.
 */
public class GridHeadingToPropertyId {
    private final String heading;
    private final String propertyId;
    private final PropertyValueGenerator<?> columnGenerator;
    private Integer width;
    private boolean defaultVisibleState = true;
    private boolean lockedState = false;

    private AbstractRenderer<?> renderer = null;
    private Converter<String, ?> converter;

    /**
     * Instantiates a new heading to property id.
     *
     * @param heading             the column heading that will be displayed
     * @param headingPropertyId   the heading property id
     * @param columnGenerator     the column generator
     * @param defaultVisibleState whether the column is visible by default
     * @param lockedState         whether the visibility of a column can be modified
     * @param width               the width of the column
     */
    GridHeadingToPropertyId(final String heading, final String propertyId,
            final PropertyValueGenerator<?> columnGenerator, final boolean defaultVisibleState,
            final boolean lockedState, final Integer width) {
        Preconditions.checkNotNull(propertyId);
        this.heading = heading;
        this.propertyId = propertyId;
        this.columnGenerator = columnGenerator;
        this.defaultVisibleState = defaultVisibleState;
        this.lockedState = lockedState;
        this.width = width;
    }

    static final class Builder {
        private final String heading;
        private final String propertyId;
        private PropertyValueGenerator<?> columnGenerator = null;
        private Integer width;
        private boolean defaultVisibleState = true;
        private boolean lockedState = false;
        private AbstractRenderer<?> renderer = null;
        private Converter<String, ?> converter = null;

        Builder(final String heading, final String propertyId) {
            this.heading = heading;
            this.propertyId = propertyId;
        }

        GridHeadingToPropertyId build() {
            final GridHeadingToPropertyId tmp = new GridHeadingToPropertyId(heading, propertyId, columnGenerator,
                    defaultVisibleState, lockedState, width);
            tmp.setRenderer(renderer);
            tmp.setConverter(converter);
            return tmp;
        }

        public Builder setLockedState(final boolean lockedState) {
            this.lockedState = lockedState;
            return this;
        }

        public Builder setDefaultVisibleState(final boolean defaultVisibleState) {
            this.defaultVisibleState = defaultVisibleState;
            return this;
        }

        public Builder setWidth(final Integer width) {
            this.width = width;
            return this;
        }

        public Builder setColumnGenerator(final PropertyValueGenerator<?> columnGenerator) {
            this.columnGenerator = columnGenerator;
            return this;
        }

        public Builder setRenderer(final AbstractRenderer<?> renderer) {
            this.renderer = renderer;
            return this;

        }

        public Builder setConverter(final Converter<String, ?> converter) {
            this.converter = converter;
            return this;

        }

    }

    public GridHeadingToPropertyId setVisibleByDefault(final boolean defaultVisibleState) {
        this.defaultVisibleState = defaultVisibleState;
        return this;
    }

    public void setConverter(final Converter<String, ?> converter) {
        this.converter = converter;

    }

    public void setRenderer(final AbstractRenderer<?> renderer) {
        this.renderer = renderer;

    }

    public GridHeadingToPropertyId setLocked() {
        lockedState = true;
        return this;
    }

    public GridHeadingToPropertyId setWidth(final Integer width) {
        this.width = width;
        return this;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getHeader() {
        return heading;
    }

    public PropertyValueGenerator<?> getColumnGenerator() {
        return columnGenerator;
    }

    /**
     * returns true if the column is a virtual table column and not in the
     * underlying container.
     * 
     * @return
     */
    public boolean isGenerated() {
        return columnGenerator != null;
    }

    public Integer getWidth() {
        return width;
    }

    public boolean isVisibleByDefault() {
        return defaultVisibleState;
    }

    public boolean isLocked() {
        return lockedState;
    }

    public AbstractRenderer<?> getRenderer() {
        return renderer;
    }

    public Converter<String, ?> getConverter() {
        return converter;
    }
}
