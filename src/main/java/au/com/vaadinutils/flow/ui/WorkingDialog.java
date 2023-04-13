package au.com.vaadinutils.flow.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import au.com.vaadinutils.flow.listener.CancelListener;
import au.com.vaadinutils.flow.listener.ProgressListener;

/**
 * 
 * Displays a dialog designed to be shown when a long running task is in
 * progress.<br>
 * <br>
 * You can use WorkingDialog in one of two ways.<br>
 * <br>
 * 1. Create a task (implements CancelListener) and pass it in to this. A cancel
 * button displays allowing the task to be stopped prior to completion.<br>
 * When the task completes, it returns a call that can be used to close this
 * dialog.<br>
 * Note: The task will need to be run in a separate class (ProgressBarWorker<T>)
 * e.g.<br>
 * <code> final ProgressBarWorker<String> worker = new ProgressBarWorker<String>(task);
    worker.start();</code><br>
 * <br>
 * 
 * 2. Just create this with only header and contents and a dialog will display
 * with no cancel button.<br>
 * When the task completes, it returns a call that can be used to close this
 * dialog.<br>
 * Note: The task will need to be run in a separate class (ProgressBarWorker<T>)
 * e.g.<br>
 * <code> final ProgressBarWorker<String> worker = new ProgressBarWorker<String>(task);
    worker.start();</code><br>
 * <br>
 *
 */

public class WorkingDialog extends Dialog implements ProgressListener<String> {

    private static final long serialVersionUID = 8696022982897542946L;
    private Label messageLabel;
    private VerticalLayout content;
    private Button cancel;
    private CancelListener cancelListener;
    private VerticalLayout layout;

    private final UI ui;

    /**
     * Displays a dialog designed to be shown when a long running task is in
     * progress.
     *
     * @param caption
     * @param message
     */
    public WorkingDialog(String caption, String message) {
        this(caption, message, null);
    }

    /**
     * Display the Working Dialog with a Cancel Button. If the user clicks the
     * Cancel button the listener will be sent a cancel request.
     *
     * @param caption
     * @param message
     * @param listener
     */
    public WorkingDialog(String caption, String message, CancelListener listener) {
        this.ui = UI.getCurrent();
        this.setModal(true);
        this.setResizable(false);
        content = new VerticalLayout(new Html("<b>" + caption + "</b>"));
        this.setWidth("500px");
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        this.cancelListener = listener;

        layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setSizeFull();

        final VerticalLayout progressArea = new VerticalLayout();
        progressArea.setSizeFull();
        final ProgressBar progress = new ProgressBar();
        progressArea.add(progress);
        progress.setIndeterminate(true);
        messageLabel = new Label(message);
        messageLabel.setSizeFull();
        progressArea.add(messageLabel);
        layout.add(progressArea);
        content.add(layout);

        if (listener != null) {
            cancel = new Button("Cancel");
            cancel.addClickListener(e -> {
                cancelListener.cancel();
                this.close();
            });
            content.add(cancel);
            content.setHorizontalComponentAlignment(Alignment.END, cancel);
        }

        this.add(content);
        this.open();
    }

    @Override
    public void close() {
        ui.access(() -> {
            super.close();
        });
    }

    public void addUserComponent(final Component component) {
        ui.access(() -> {
            layout.add(component);
        });
    }

    @Override
    public void progress(int count, int max, final String message) {
        ui.access(() -> {
            messageLabel.setText(message);
        });
    }

    @Override
    public void complete(int sent) {
        ui.access(() -> {
            this.close();
        });
    }

    @Override
    public void itemError(Exception e, String status) {
        // Ignored.
    }

    @Override
    public void exception(Exception e) {
        ui.access(() -> {
            WorkingDialog.this.close();
        });
    }

    public void removeUserComponent(Component component) {
        ui.access(() -> {
            layout.remove(component);
        });
    }
}