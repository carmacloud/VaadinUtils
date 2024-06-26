package au.com.vaadinutils.menu;

import java.io.Serializable;

import com.vaadin.navigator.View;

/**
 * Will be removed once dependent classes are removed.
 */
public class ViewMapping implements Serializable {
    private static final long serialVersionUID = 1L;
    private String viewName;
    private Class<? extends View> view;

    public ViewMapping(final String viewName, final Class<? extends View> class1) {
        this.setViewName(viewName);
        this.setView(class1);
    }

    public Class<? extends View> getView() {
        return view;
    }

    public void setView(final Class<? extends View> view) {
        this.view = view;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(final String viewName) {
        this.viewName = viewName;
    }

    public boolean noHelp() {
        return false;
    }
}