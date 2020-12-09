package au.com.vaadinutils.crud;

/**
 * @deprecated Replaced in V14 migration.
 */
public enum CrudDisplayMode {
    VERTICAL {
        @Override
        public CrudPanelPair getContainer() {

            return new CrudPanelSplitPairVertical();
        }
    },
    HORIZONTAL {
        @Override
        public CrudPanelPair getContainer() {
            return new CrudPanelSplitPairHorizontal();
        }
    },
    HIDING {
        @Override
        public CrudPanelPair getContainer() {
            return new CrudPanelSplitPairHiding();
        }
    };

    abstract public CrudPanelPair getContainer();

}
