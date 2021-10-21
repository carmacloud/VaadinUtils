package au.com.vaadinutils.errorHandling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.screenshot.Screenshot;
import org.vaadin.addons.screenshot.ScreenshotImage;
import org.vaadin.addons.screenshot.ScreenshotListener;
import org.vaadin.addons.screenshot.ScreenshotMimeType;

import com.google.common.base.Stopwatch;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.mpr.LegacyWrapper;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Retain, but might will be adapted for Flow
 */
public class ErrorWindow {
    public static final String ERROR_WINDOW_CLOSE_BUTTON = "ErrorWindowCloseButton";
    private Label uploadStatus = new Label("&nbsp;", ContentMode.HTML);
    private static String viewName;

    static Logger logger = LogManager.getLogger();

    /**
     * throttle for sending emails about errors the user hasn't seen. Allow bursting
     * to 20 emails in a minute, over the long term limit to 1 email per minute
     */
    final static ErrorRateController emailRateController = new ErrorRateController(20, 1, TimeUnit.MINUTES);

    public ErrorWindow() {
    }

    ErrorWindow(boolean noUI) {
    }

    public static void showErrorWindow(Throwable e, String name) {
        viewName = name;
        new ErrorWindow(true).internalShowErrorWindow(e);
    }

    static final ThreadLocal<String> lastSeenError = new ThreadLocal<>();

    private void internalShowErrorWindow(Throwable error) {

        try {
            ViolationConstraintHandler.expandException(error);
        } catch (Throwable e) {
            error = e;
        }

        // Find the final cause
        String fullTrace = "";

        String causeClass = "";
        String id = "";

        final Date time = new Date();
        Throwable cause = null;
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t.getCause() == null) // We're at final cause
            {
                cause = t;
                fullTrace = extractTrace(t);

                causeClass = cause.getClass().getSimpleName();

                id = getCustomHashString(fullTrace);

                // include the build version in the hash to make hashes unique
                // between builds
                id += getBuildVersion();

                // prevent hashcode being negative
                Long hashId = new Long(id.hashCode()) + new Long(Integer.MAX_VALUE);
                id = "" + hashId;

                // add the message after the hash id is calculated
                fullTrace = "Cause: " + cause.getMessage() + "\n" + fullTrace;

            } else {
                logger.error(extractTrace(t));
            }
        }

        if (lastSeenError.get() != null && lastSeenError.get().equals(id)) {
            logger.error("Skipping repeated error " + error.getMessage());
            return;
        }

        lastSeenError.set(id);

        final String finalId = id;
        final String finalTrace = fullTrace;
        final String reference = UUID.randomUUID().toString();

        logger.error("Reference: " + reference + " Version: " + getBuildVersion() + " System: " + getSystemName() + " "
                + error, error);
        logger.error("Reference: " + reference + " " + cause, cause);

        final String finalCauseClass = causeClass;

        if (!isExempted(cause)) {
            if (UI.getCurrent() != null) {
                UI.getCurrent().access(() -> {
                    Stopwatch lastTime = (Stopwatch) UI.getCurrent().getSession()
                            .getAttribute("Last Time Error Window Shown");

                    // don't display the error window more than once every 2
                    // seconds
                    if (lastTime == null || lastTime.elapsed(TimeUnit.SECONDS) > 2) {

                        displayVaadinErrorWindow(finalCauseClass, finalId, time, finalId, finalTrace, reference);

                        UI.getCurrent().getSession().setAttribute("Last Time Error Window Shown",
                                Stopwatch.createStarted());
                    } else {
                        emailErrorWithoutShowing(time, finalId, finalTrace, reference);
                    }
                });
            } else {
                emailErrorWithoutShowing(time, finalId, finalTrace, reference);
            }
        } else {
            logger.error("Not Sending email or displaying error as cause is exempted.");
        }
    }

    private String getCustomHashString(String fullTrace) {
        try {
            return ErrorSettingsFactory.getErrorSettings().getCustomHashString(fullTrace);
        } catch (Exception e) {
            logger.error(e, e);
            return fullTrace;
        }

    }

    private String extractTrace(Throwable t) {
        String fullTrace = t.getClass().getCanonicalName() + "\n";
        for (StackTraceElement trace : t.getStackTrace()) {
            fullTrace += "at " + trace.getClassName() + "." + trace.getMethodName() + "(" + trace.getFileName() + ":"
                    + trace.getLineNumber() + ")\n";
        }
        fullTrace += "\n\n";
        return fullTrace;
    }

    private void emailErrorWithoutShowing(final Date time, final String finalId, final String finalTrace,
            final String reference) {
        // limit the number of errors that can be emailed without human
        // action. also suppress some types of errors
        if (emailRateController.acquire()) {
            try {
                final String supportEmail = getTargetEmailAddress();

                generateEmail(time, finalId, finalTrace, reference, "Error not displayed to user", supportEmail, "", "",
                        "", null);
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            logger.error("Not sending error email");
        }
    }

    boolean isExempted(Throwable cause) {
        Map<String, Set<String>> exemptedExceptions = new HashMap<>();
        exemptedExceptions.put("ClientAbortException", new HashSet<String>());
        exemptedExceptions.put("SocketException", new HashSet<String>());
        exemptedExceptions.put("UIDetachedException", new HashSet<String>());

        HashSet<String> suppressedRuntimeExceptions = new HashSet<String>();
        suppressedRuntimeExceptions.add("Couldn't attach to writer stream");
        exemptedExceptions.put("RuntimeException", suppressedRuntimeExceptions);

        HashSet<String> ioSet = new HashSet<String>();
        ioSet.add("Pipe closed");
        ioSet.add("Pipe not connected");
        exemptedExceptions.put("IOException", ioSet);

        Set<String> expectedMessage = exemptedExceptions.get(cause.getClass().getSimpleName());
        if (expectedMessage != null) {
            if (!expectedMessage.isEmpty()) {
                for (String message : expectedMessage) {
                    if (cause.getMessage().equalsIgnoreCase(message)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void displayVaadinErrorWindow(final String causeClass, final String id, final Date time,
            final String finalId, final String finalTrace, final String reference) {

        // generate screen shot!
        final Dialog window = new Dialog();
        final Screenshot screenshot = Screenshot.newBuilder().withLogging(true).withMimeType(ScreenshotMimeType.PNG)
                .build();
        screenshot.addScreenshotListener(new ScreenshotListener() {
            @Override
            public void screenshotComplete(ScreenshotImage image) {
                image.getImageData();
                showWindow(causeClass, id, time, finalId, finalTrace, reference, image.getImageData());
                window.close();

            }
        });

        window.add(new LegacyWrapper(screenshot));
        window.setResizable(false);

        UI.getCurrent().add(window);
        screenshot.setTargetComponent(null);
        screenshot.takeScreenshot();

    }

    private void showWindow(String causeClass, String id, final Date time, final String finalId,
            final String finalTrace, final String reference, final byte[] imageData) {
        final ConfirmDialog window = new ConfirmDialog();
        UI.getCurrent().add(window);
        window.setWidth("600px");
        window.setHeader("Error " + id);

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        final Label message = new Label(
                "<b>An error has occurred (" + causeClass + ").<br><br>Reference:</b> " + reference);
        message.setContentMode(ContentMode.HTML);

        final Label describe = new Label(
                "<b>Please describe what you were doing when this error occured (Optional)<b>");
        describe.setContentMode(ContentMode.HTML);

        final TextArea notes = new TextArea();
        notes.setWidth("100%");
        final String supportEmail = getTargetEmailAddress();

        final Button saveButton = new Button("Save");
        window.setConfirmButton(saveButton);
        saveButton.addClickListener(click -> {
            try {
                logger.info(getViewName());
                generateEmail(time, finalId, finalTrace, reference, notes.getValue(), supportEmail, getViewName(),
                        getUserName(), getUserEmail(), imageData);
            } catch (Exception e) {
                logger.error(e, e);
                Notification.show("Error sending error report", Type.ERROR_MESSAGE);
            } finally {
                window.close();
            }
        });
        saveButton.getElement().setAttribute("theme", "error primary");

        saveButton.setId(ERROR_WINDOW_CLOSE_BUTTON);

        layout.addComponent(message);
        layout.addComponent(describe);
        layout.addComponent(notes);
        layout.addComponent(uploadStatus);
        layout.addComponent(new Label("Information about this error will be sent to " + getSupportCompanyName()));
        final LegacyWrapper content = new LegacyWrapper(layout);
        content.setSizeFull();
        window.add(content);
        window.open();
    }

    private void generateEmail(final Date time, final String finalId, final String finalTrace, final String reference,
            final String notes, final String supportEmail, final String viewClass, final String user,
            final String userEmail, final byte[] imageData) {

        logger.error("Reference: " + reference + " " + notes);
        final String buildVersion = getBuildVersion();
        final String companyName = getSystemName();
        Runnable runner = new Runnable() {

            @Override
            public void run() {
                String subject = "";
                subject += "Error: " + finalId + " " + companyName + " ref: " + reference;

                ByteArrayOutputStream stream = null;
                String filename = null;
                String MIMEType = null;
                if (imageData != null) {
                    stream = new ByteArrayOutputStream();
                    try {
                        stream.write(imageData);
                        filename = "screen.png";
                        MIMEType = ScreenshotMimeType.PNG.getMimeType();
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
                }
                ErrorSettingsFactory.getErrorSettings().sendEmail(supportEmail, subject,
                        subject + "\n\nTime: " + time.toString() + "\n\nView: " + viewClass + "\n\nUser: " + user + " "
                                + userEmail + "\n\n" + "Version: " + buildVersion + "\n\n" + "User notes:" + notes
                                + "\n\n" + finalTrace,
                        stream, filename, MIMEType);

            }
        };

        new Thread(runner, "Send Error Email").start();
    }

    private String getViewName() {
       if(viewName != null) {
           return viewName;
       }
        return "Error getting View name";
    }

    private String getSupportCompanyName() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getSupportCompanyName();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting Support Company Name";
    }

    private String getTargetEmailAddress() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getTargetEmailAddress();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting Target Email Address";
    }

    private String getUserEmail() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getUserEmail();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting user email";
    }

    private String getBuildVersion() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getBuildVersion();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting build Version";
    }

    private String getUserName() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getUserName();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting user name";
    }

    private String getSystemName() {
        try {
            return ErrorSettingsFactory.getErrorSettings().getSystemName();
        } catch (Exception e) {
            logger.error(e, e);
        }
        return "Error getting System name";
    }
}
