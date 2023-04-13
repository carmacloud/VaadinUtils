package au.com.vaadinutils.flow.util;

import com.vaadin.flow.component.UI;

public abstract class ProgressBarTask<T> {
    private ProgressTaskListener<T> listener;

    protected final UI ui;

    public ProgressBarTask(ProgressTaskListener<T> listener, final UI ui) {
        this.listener = listener;
        this.ui = ui;
    }

    abstract public void run();

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
