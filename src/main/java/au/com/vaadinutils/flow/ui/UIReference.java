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

    public UIReference(final UI ui) {
        uiRef = new WeakReference<>(ui);
    }

    public void access(final Command command) {
        final UI ui = uiRef.get();
        if (ui != null) {
            ui.access(command);
        }
    }

    public Stream<Component> getChildren() {
        final UI ui = uiRef.get();
        if (ui != null) {
            return ui.getChildren();
        }
        return new LinkedList<Component>().stream();
    }

    public Page getPage() {
        final UI ui = uiRef.get();
        if (ui != null) {
            return ui.getPage();
        }
        return null;
    }

    public void setPollInterval(final int intervalInMillis) {
        final UI ui = uiRef.get();
        if (ui != null) {
            ui.setPollInterval(intervalInMillis);
        }
    }

    public void remove(final Component component) {
        final UI ui = uiRef.get();
        if (ui != null) {
            ui.remove(component);
        }
    }

    public void push() {
        final UI ui = uiRef.get();
        if (ui != null) {
            ui.push();
        }
    }
}