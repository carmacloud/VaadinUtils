package au.com.vaadinutils.flow.util;

import au.com.vaadinutils.flow.ui.UIReference;

public abstract class ProgressBarTask<T> {
    private ProgressTaskListener<T> listener;

    protected final UIReference ui;

    public ProgressBarTask(ProgressTaskListener<T> listener, final UIReference ui2) {
        this.listener = listener;
        this.ui = ui2;
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

    public UIReference getUi() {
        return this.ui;
    }
}
