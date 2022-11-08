package au.com.vaadinutils.flow.helper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public class VaadinHelper {

    /**
     * Determines the position and colour of the {@link Notification}. The 4 types
     * are Error, Warning, Tray and Info.
     */
    public enum NotificationType {
        ERROR, WARNING, TRAY, INFO
    }

    private final static Logger logger = LogManager.getLogger();

    /**
     * Standard carma colour blue
     */
    public static final String CARMA_BLUE = "#0066CC";
    /**
     * Standard carma clour dark blue
     */
    public static final String CARMA_DARK_BLUE = "#002C82";
    /**
     * Standard carma colour red for error
     */
    public static final String CARMA_ERROR = "#CC0000";
    /**
     * Standard carma colour red
     */
    public static final String CARMA_RED = "#800000";
    /**
     * Standard carma colour green
     */
    public static final String CARMA_GREEN = "#008000";
    /**
     * Standard carma colour red for orange
     */
    public static final String CARMA_ORANGE = "#FF9900";
    private static final DatePickerI18n DATE_FORMAT_I18N = new DatePickerI18n();

//    public static final String

    /**
     * Given the full file path (on the local filesystem) to a data file, return the
     * contents as an array of <code>byte</code> data.
     * 
     * @param filePath A {@link String} being the full file path.
     * @return An array of <code>byte</code> data, or null if the file could not be
     *         found.
     */
    public static byte[] getResourceBytes(final String filePath) {
        final File file = new File(filePath);
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return fileContent;
        } catch (IOException e) {
            logger.error("File not found for file path '" + filePath + "'");
            return null;
        }
    }

    /**
     * Returns a {@link StreamResource} given a <code>byte</code> array of data.
     * 
     * @param filePath A {@link String} being the full file path.
     * @return A {@link StreamResource} or null if there is no <code>byte</code>
     *         array of data.
     * @throws IOException Thrown if there are any IO errors during processing.
     */
    public static StreamResource getStreamResource(final String filePath) throws IOException {
        final byte[] pdfBytes = getResourceBytes(filePath);
        if (pdfBytes != null) {
            return new StreamResource(FilenameUtils.getName(filePath), () -> new ByteArrayInputStream(pdfBytes));
        } else {
            return null;
        }
    }

    // Dates
    public static Date convertFromLocalDateTime(final LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime convertToLocalDateTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date convertFromLocalDate(LocalDate dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate convertToLocalDate(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Method to display a {@link Notification} with a caption, the length of time
     * to display (0 displays Notification until user closes the screen) and the
     * {@link NotificationType}.
     * 
     * @param caption  A {@link String} being the caption.
     * @param duration An <code>int</code> being the duration in milliseconds the
     *                 {@link Notification} is displayed.
     * @param type     A {@link NotificationType} that determines the colour and
     *                 positioning of the {@link Notification}.
     */
    public static void notificationDialog(final String caption, final int duration, final NotificationType type) {
        createNotification(caption, duration, type, new Span());
    }

    /**
     * Method to display a {@link Notification} with a caption, optional message
     * body, the length of time to display (0 displays Notification until user
     * closes the screen) and the {@link NotificationType}.
     * 
     * @param caption  A {@link String} being the caption.
     * @param message  A {@link String} being the message body.
     * @param duration An <code>int</code> being the duration in milliseconds the
     *                 {@link Notification} is displayed.
     * @param type     A {@link NotificationType} that determines the colour and
     *                 positioning of the {@link Notification}.
     */
    public static void notificationDialog(final String caption, final String message, final int duration,
            final NotificationType type) {
        final Span contents = new Span(new Text(message));
        createNotification(caption, duration, type, contents);
    }

    /**
     * Method to display a {@link Notification} with a caption, optional component,
     * the length of time to display (0 displays Notification until user closes the
     * screen) and the {@link NotificationType}.
     * 
     * @param caption   A {@link String} being the caption.
     * @param component A {@link Component} being a layout or field to embed in the
     *                  {@link Notification}.
     * @param duration  An <code>int</code> being the duration in milliseconds the
     *                  {@link Notification} is displayed.
     * @param type      A {@link NotificationType} that determines the colour and
     *                  positioning of the {@link Notification}.
     */
    public static void notificationDialog(final String caption, final Component component, final int duration,
            final NotificationType type) {
        final Span contents = new Span(component);
        createNotification(caption, duration, type, contents);
    }

    private static void createNotification(final String caption, final int duration, final NotificationType type,
            final Span contents) {
        final Notification notification = new Notification();
        final HorizontalLayout header = new HorizontalLayout(new Text(caption));
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(false);
        header.setWidthFull();
        final String textColour;
        Position position = Position.MIDDLE;
        switch (type) {
        case ERROR:
            header.addClassName("notification-error");
            textColour = CARMA_ERROR;
            break;
        case WARNING:
            header.addClassName("notification-warning");
            textColour = CARMA_BLUE;
            break;
        case TRAY:
            header.addClassName("notification-tray");
            textColour = "#0f0ff5";
            position = Position.BOTTOM_END;
            break;
        default:
            header.addClassName("notification-info");
            textColour = "#000000";
            break;
        }

        final VerticalLayout layout = new VerticalLayout(header, contents);
        layout.setMinWidth("100px");
        layout.setMaxWidth("300px");
        notification.add(layout);
        notification.setDuration(duration);
        notification.setPosition(position);

        if (duration == 0) {
            header.add(addCloseButton(notification, textColour));
            layout.getElement().setProperty("title", "To close, click the X icon.");
        }

        notification.addDetachListener(listener -> {
            notification.close();
        });
        notification.open();
    }

    private static Icon addCloseButton(final Notification notification, final String iconColour) {
        final Icon icon = VaadinIcon.CLOSE.create();
        icon.setColor(iconColour);
        icon.setSize("12px");

        icon.addClickListener(event -> {
            notification.close();
        });

        icon.getElement().getStyle().set("margin-left", "auto");

        return icon;
    }

    /**
     * Helper method to create a {@link DatePickerI18n} custom date format.<br>
     * The supplied date parameter is the one the {@link DatePicker} will use to
     * display the date.<br>
     * Additional date formats of
     * <code>"dd-MMM-yyyy", "dd/MMM/yyyy", "dd-MM-yyyy", "dd-mm-yyyy" </code> are
     * added.<br>
     * The {@link DatePicker} value can be edited in these formats. <br>
     * Note: if the primary date format is not supplied, it is set as "dd-MMM-yy".
     * 
     * @param primaryDateFormat A {@link String} being the custom format required.
     *                          This format is the one the {@link DatePicker} will
     *                          use to display the date.
     * @return A {@link DatePickerI18n} object with the custom pattern and default
     *         additional date formats..
     */
    public static DatePickerI18n setCustomDateFormats(final String primaryDateFormat) {
        final String[] additionalDateFormats = { "dd/MM/yyyy", "dd/MMM/yyyy", "dd-MM-yyyy", "dd-MMM-yyyy" };
        DATE_FORMAT_I18N.setDateFormats(
                primaryDateFormat != null && !primaryDateFormat.isEmpty() ? primaryDateFormat : "dd-MMM-yy",
                additionalDateFormats);
        return DATE_FORMAT_I18N;
    }
}
