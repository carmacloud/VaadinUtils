package au.com.vaadinutils.flow.ui;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.Command;

public class UIReference {

    final WeakReference<UI> uiRef;

    public UIReference(UI ui) {

        uiRef = new WeakReference<>(ui);
    }

    public void access(Command command) {
        UI ui = uiRef.get();
        if (ui != null) {
            ui.access(command);
        }

    }

    public Stream<Component> getChildren() {
        UI ui = uiRef.get();
        if (ui != null) {
            return ui.getChildren();
        }
        return new LinkedList<Component>().stream();
    }

    public Page getPage() {
        UI ui = uiRef.get();
        if (ui != null) {
            return ui.getPage();
        }
        return null;
    }

    public void setPollInterval(int intervalInMillis) {
        UI ui = uiRef.get();
        if (ui != null) {
            ui.setPollInterval(intervalInMillis);
        }

    }

    public void remove(Component component) {
        UI ui = uiRef.get();
        if (ui != null) {
            ui.remove(component);
        }
    }

    public void push() {
        UI ui = uiRef.get();
        if (ui != null) {
            ui.push();
        }

    }

}
