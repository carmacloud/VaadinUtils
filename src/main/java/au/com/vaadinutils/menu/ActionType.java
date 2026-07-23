package au.com.vaadinutils.menu;

import org.apache.logging.log4j.Logger;

import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;

public enum ActionType {
    NAVIGATE {
        @Override
        public void createLeafItem(final MenuWrapper menubar, final Menu menu, final String displayName,
                final ViewMapping viewmap, final boolean addAtTop) {

            menubar.addItem(displayName, addAtTop, new MenuBar.Command() {
                private static final long serialVersionUID = 1L;

                @Override
                public void menuSelected(final MenuItem selectedItem) {
                    UI.getCurrent().getNavigator().navigateTo(viewmap.getViewName());
                }
            });
        }
    },
    URL {
        @Override
        public void createLeafItem(final MenuWrapper menubar, final Menu menu, final String displayName,
                final ViewMapping viewmap, final boolean addAtTop) {
            menubar.addItem(displayName, addAtTop, new MenuBar.Command() {
                private static final long serialVersionUID = 1L;

                @Override
                public void menuSelected(final MenuItem selectedItem) {
                    UI.getCurrent().getPage().open(menu.url(), "");
                }
            });

        }
    },
    URL_NEW_WINDOW {
        @Override
        public void createLeafItem(final MenuWrapper menubar, final Menu menu, final String displayName,
                final ViewMapping viewmap, final boolean addAtTop) {
            menubar.addItem(displayName, addAtTop, new MenuBar.Command() {
                private static final long serialVersionUID = 1L;

                @Override
                public void menuSelected(final MenuItem selectedItem) {
                    if (menu.windowSizer() != WindowSizerNull.class) {
                        // dynamically sized window!
                        try {
                            final WindowSizer sizer = menu.windowSizer().newInstance();
                            UI.getCurrent().getPage().open(menu.url(), menu.windowName(), sizer.width(), sizer.height(),
                                    BorderStyle.DEFAULT);
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error(e, e);
                        }

                    } else if (menu.width() > 0 && menu.height() > 0) {
                        UI.getCurrent().getPage().open(menu.url(), menu.windowName(), menu.width(), menu.height(),
                                BorderStyle.DEFAULT);

                    } else {
                        UI.getCurrent().getPage().open(menu.url(), menu.windowName());
                    }
                }
            });

        }
    };

    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    abstract public void createLeafItem(MenuWrapper menuItem, Menu menu, final String displayName,
            final ViewMapping viewmap, boolean addAtTop);

}
