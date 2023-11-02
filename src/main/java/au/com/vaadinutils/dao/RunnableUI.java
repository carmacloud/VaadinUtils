package au.com.vaadinutils.dao;

import com.vaadin.flow.component.UI;

/**
 * Designed to work with EntityManagerCallableUI
 *
 * This method allows you to execute a Callable passing in a Vaadin UI.
 *
 * @author bsutton
 *
 * @param <T>
 */
public abstract class RunnableUI implements Runnable {

    final private UI ui;

    public RunnableUI(UI ui) {
        this.ui = ui;
    }

    /**
     * throws Exception allows the call method to throw an exception. The exception
     * is chained from any exception thrown in the enclosed thread.
     */
    @Override
    public void run() {
        run(ui);
    }

    protected abstract void run(UI ui);

}
