package au.com.vaadinutils.crud;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;

/**
 * @deprecated Replaced in V14 migration.
 */
public interface CrudPanelPair {
    public void setFirstComponent(Component c);

    public void setSecondComponent(Component c);

    public Component getPanel();

    public void setSplitPosition(float pos);

    public void showFirstComponent();

    public void showSecondComponent();

    void setLocked(boolean locked);

    public void setSplitPosition(float pos, Unit units);
}
