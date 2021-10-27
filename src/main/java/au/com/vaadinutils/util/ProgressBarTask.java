package au.com.vaadinutils.util;

import com.vaadin.flow.component.UI;

public abstract class ProgressBarTask<T> {
    private ProgressTaskListener<T> listener;

    final UI ui;

    public ProgressBarTask(ProgressTaskListener<T> listener, final UI ui) {
        this.listener = listener;
        this.ui = ui;
    }

    public void run() {
        runUI(ui);
    }

    /**
     * Changed overload method to make it explicit that you need to use the passed
     * UI as calls to UI.getCurrent() will fail on a background thread such as the
     * on the ProgressBarTask is normally called within.
     * 
     * @param ui
     */
    abstract public void runUI(UI ui);

    protected void taskComplete(final int sent) {
        ui.access(() -> {
            listener.taskComplete(sent);
        });
    }

    public void taskProgress(final int count, final int max, final T status) {
        ui.access(() -> {
            listener.taskProgress(count, max, status);
        });
    }

    public void taskItemError(final T status) {
        ui.access(() -> {
            listener.taskItemError(status);
        });
    }

    public void taskException(final Exception e) {
        ui.access(() -> {
            listener.taskException(e);
        });
    }
}
