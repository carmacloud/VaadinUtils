package au.com.vaadinutils.flow.errorhandling;

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
//import org.vaadin.addons.screenshot.Screenshot;
//import org.vaadin.addons.screenshot.ScreenshotImage;
//import org.vaadin.addons.screenshot.ScreenshotListener;
//import org.vaadin.addons.screenshot.ScreenshotMimeType;

import com.google.common.base.Stopwatch;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;
import au.com.vaadinutils.flow.jasper.AttachmentType;

/**
 * All finished, except need to find replacement for Screenshot addon.
 */
public class ErrorWindow {
    public static final String ERROR_WINDOW_CLOSE_BUTTON = "ErrorWindowCloseButton";
    private Html uploadStatus = new Html("<p>&nbsp;</p>");
    private static String viewName;

    private static Logger logger = LogManager.getLogger();

    /**
     * Throttle for sending emails about errors the user hasn't seen. Allow bursting
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
        final Map<String, Set<String>> exemptedExceptions = new HashMap<>(8);
        exemptedExceptions.put("ClientAbortException", new HashSet<String>());
        exemptedExceptions.put("SocketException", new HashSet<String>());
        exemptedExceptions.put("UIDetachedException", new HashSet<String>());

        final HashSet<String> suppressedRuntimeExceptions = new HashSet<String>(2);
        suppressedRuntimeExceptions.add("Couldn't attach to writer stream");
        exemptedExceptions.put("RuntimeException", suppressedRuntimeExceptions);

        final HashSet<String> ioSet = new HashSet<String>();
        ioSet.add("Pipe closed");
        ioSet.add("Pipe not connected");
        exemptedExceptions.put("IOException", ioSet);

        final Set<String> expectedMessage = exemptedExceptions.get(cause.getClass().getSimpleName());
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

        // TODO LC: Removed until a Screenshot addon replacement is found.
        // generate screen shot!
//        final Dialog window = new Dialog();
//        final Screenshot screenshot = Screenshot.newBuilder().withLogging(true).withMimeType(ScreenshotMimeType.PNG)
//                .build();
//        screenshot.addScreenshotListener(new ScreenshotListener() {
//            @Override
//            public void screenshotComplete(ScreenshotImage image) {
//                image.getImageData();
//              
//            }
//        });
        showWindow(causeClass, id, time, finalId, finalTrace, reference, null);
//        window.close();
//        window.add(new LegacyWrapper(screenshot));
//        window.setResizable(false);
//        window.open();
//        screenshot.setTargetComponent(null);
//        screenshot.takeScreenshot();
    }

    private void showWindow(String causeClass, String id, final Date time, final String finalId,
            final String finalTrace, final String reference, final byte[] imageData) {
        final ConfirmDialog window = new ConfirmDialog();
        window.setWidth("600px");
        window.setHeader("Error: " + id);

        final VerticalLayout layout = new VerticalLayout();
        final Html message = new Html(
                "<p>" + "<b>An error has occurred (" + causeClass + ").<br>Reference: </b>" + reference + "</p> ");

        final Html describe = new Html("<b>Please describe what you were doing when this error occured (Optional)<b>");

        final TextArea notes = new TextArea();
        notes.setWidth("100%");
        notes.setHeight("100px");
        final String supportEmail = getTargetEmailAddress();

        final Button saveButton = new Button("Save");
        window.setConfirmButton(saveButton);
        saveButton.addClickListener(click -> {
            try {
                logger.info(getViewName());
                generateEmail(time, finalId, finalTrace, reference, notes.getValue(), supportEmail, getViewName(),
                        getUserName(), getUserEmail(), imageData);
            } catch (Exception e) {
                VaadinHelper.notificationDialog("Error sending error report", NotificationType.ERROR);
                logger.error(e, e);
            } finally {
                window.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        saveButton.setId(ERROR_WINDOW_CLOSE_BUTTON);

        layout.add(message, describe, notes, uploadStatus);
        layout.add(new Label("Information about this error will be sent to " + getSupportCompanyName()));
        window.add(layout);
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
                String MIMEType = AttachmentType.TXT.getMIMETypeString();
                if (imageData != null) {
                    stream = new ByteArrayOutputStream();
                    try {
                        stream.write(imageData);
                        filename = "screen.png";
                        // TODO LC: Removed until Screenshot addon replacement is found.
//                        MIMEType = ScreenshotMimeType.PNG.getMimeType();
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
        if (viewName != null) {
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
