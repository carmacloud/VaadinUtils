package au.com.vaadinutils.flow.fields;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import au.com.vaadinutils.flow.helper.VaadinHelper;

public class TimePicker extends CustomField<LocalDateTime> {

    private static final long serialVersionUID = 1229239105022198981L;
    private final Logger logger = LogManager.getLogger();
    private String title;

    // Fields
    // This is the field on the screen
    private TextField field;
    // This is the field in the popup screen.
    private TextField displayTime = new TextField();
    private final Icon icon = VaadinIcon.CLOCK.create();
    private final List<Registration> popupRegistrations = new ArrayList<>();

    // Formats
    public static final String TIME_FORMAT = "hh:mm a";
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern(TIME_FORMAT);
    private static final String EMPTY = "--:--";

    private LocalDateTime storedDate = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
    private LocalDateTime modifiedDate = storedDate;
    private Set<TimePickerValueChanged> listeners = new HashSet<TimePickerValueChanged>();
    private int hourToSet;
    private String periodClicked = "AM";
    private int minuteToSet = 0;

    public TimePicker(final String title) {
        this.title = title;
        setLabel(title);
        field = new TextField();
        field.setWidth("125px");
        field.setValue(EMPTY);

        icon.setColor(VaadinHelper.CARMA_BLUE);
        icon.setSize("15px");

        add(icon, field);

        icon.addClickListener(e -> {
            if (!isReadOnly()) {
                showPopupTimePicker();
            }
        });

        field.addValueChangeListener(e -> {
            updateTime(e);
        });
    }

    protected LocalDateTime parseDate(final String value) {
        if (value == null || EMPTY.equals(value)) {
            return null;
        }

        LocalTime ld;
        try {
            setErrorMessage("");
            setInvalid(false);
            ld = LocalTime.parse(value, dtf);
            modifiedDate = modifiedDate.with(ld);
            return modifiedDate;
        } catch (final DateTimeParseException e) {
            setErrorMessage("Time format is " + TIME_FORMAT);
            setInvalid(true);
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    protected LocalDateTime generateModelValue() {
        if (parseDate(field.getValue()) != null) {
            super.setValue(modifiedDate);
            return modifiedDate;
        }
        return getValue();
    }

    @Override
    protected void setPresentationValue(final LocalDateTime newPresentationValue) {
        field.setValue(newPresentationValue == null ? EMPTY : dtf.format(newPresentationValue));
    }

    @Override
    public void setValue(final LocalDateTime value) {
        modifiedDate = storedDate = value;
        super.setValue(value);
    }

    @Override
    public void setReadOnly(final boolean value) {
        field.setReadOnly(value);
        icon.setColor(value ? VaadinHelper.CARMA_LIGHT_GREY : VaadinHelper.CARMA_BLUE);
//        super.setReadOnly(value);
    }

    private void showPopupTimePicker() {
        displayTime.setValue(field.getValue());
        LocalDateTime parsedDate;
        try {
            parsedDate = parseDate(field.getValue());
            hourToSet = parsedDate.getHour();
            minuteToSet = parsedDate.getMinute();
        } catch (final DateTimeException e1) {
            logger.error(e1.getMessage());
        }
        final Dialog window = new Dialog();
        window.setModality(ModalityMode.VISUAL);
        window.setResizable(false);
        window.setWidth("375px");
        window.setCloseOnEsc(false);
        window.setCloseOnOutsideClick(false);

        final HorizontalLayout layout = new HorizontalLayout();
        layout.setId("Layout");
        layout.setPadding(false);
        layout.setSpacing(false);
        window.add(new Html("<b>" + title + "</b>"));

        final VerticalLayout hourAndAmPmLayout = new VerticalLayout();
        hourAndAmPmLayout.setId("HourAndAmPmLayout");
        hourAndAmPmLayout.setPadding(false);
        hourAndAmPmLayout.setSpacing(false);
        hourAndAmPmLayout.setWidth("200px");

        final Span hourLabel = new Span("Hour");
        hourAndAmPmLayout.add(hourLabel);

        final HorizontalLayout hourPanelsLayout = new HorizontalLayout();
        hourPanelsLayout.setId("HourPanelsLayout");
        hourPanelsLayout.setPadding(false);
        hourPanelsLayout.setSpacing(false);

        hourAndAmPmLayout.add(hourPanelsLayout);

        final HorizontalLayout hourButtonPanel = new HorizontalLayout();
        hourButtonPanel.setId("HourButtonPanel");
        hourButtonPanel.setSpacing(false);
        hourButtonPanel.setPadding(false);
        addHourButtons(hourButtonPanel, 2, 6);

        final VerticalLayout amPmButtonPanel = new VerticalLayout();
        amPmButtonPanel.setId("AmPmButtonPanel");
        addAmPmButtons(amPmButtonPanel);

        hourPanelsLayout.add(hourButtonPanel, amPmButtonPanel);

        final VerticalLayout minuteLayout = new VerticalLayout();
        minuteLayout.setId("MinuteLayout");
        minuteLayout.setSpacing(false);
        minuteLayout.setPadding(false);
        final Span minuteLabel = new Span("Minute");
        minuteLabel.setWidth("45px");
        minuteLayout.add(minuteLabel);

        final HorizontalLayout minutePanel = new HorizontalLayout();
        minutePanel.setId("minutePanel");
        minutePanel.setSpacing(false);
        minutePanel.setPadding(false);
        addMinuteButtons(minutePanel, 2, 4);

        minuteLayout.add(minutePanel);

        displayTime.setWidth("100px");

        Registration reg = displayTime.addValueChangeListener(e -> {
            updateTime(e);
        });
        popupRegistrations.add(reg);

        layout.add(hourAndAmPmLayout, minuteLayout);

        final HorizontalLayout okCancel = new HorizontalLayout();
        okCancel.setId("OkCancel");
        okCancel.setSpacing(true);
        okCancel.setPadding(false);
        final Button ok = new Button("OK");
        reg = ok.addClickListener(e -> {
            field.setValue(displayTime.getValue());
            window.close();
        });
        popupRegistrations.add(reg);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.setWidth("75px");

        final Button cancel = new Button("Cancel");
        reg = cancel.addClickListener(e -> {
            window.close();
        });
        popupRegistrations.add(reg);
        cancel.setWidth("75px");

        final Button clear = new Button("Clear");
        reg = clear.addClickListener(e -> {
            clearValue();
        });
        popupRegistrations.add(reg);
        clear.setWidth("75px");

        okCancel.add(displayTime, cancel, clear, ok);
        window.add(layout, okCancel);
        window.open();
        window.addDialogCloseActionListener(r -> {
            popupRegistrations.forEach(reg1 -> reg1.remove());
        });
    }

    private void updateTime(final ComponentValueChangeEvent<TextField, String> e) {
        if (e.isFromClient()) {
            LocalDateTime parsedDate;
            try {
                parsedDate = parseDate(e.getValue());
                if (parsedDate != null) {
                    hourToSet = parsedDate.getHour();
                    minuteToSet = parsedDate.getMinute();
                    periodClicked = e.getValue().substring(6, e.getValue().length());
                    setNewValue();
                }
            } catch (final DateTimeException e1) {
                logger.error(e1.getMessage());
            }
        }
        valueChanged(modifiedDate);
    }

    private void addHourButtons(final HorizontalLayout hourButtonPanel, final int rows, final int cols) {
        final String[] numbers = new String[] { "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" };
        for (int col = 0; col < cols; col++) {
            final VerticalLayout rowsLayout = new VerticalLayout();
            rowsLayout.setId("RowLayout" + col);
            rowsLayout.setSpacing(false);
            rowsLayout.setPadding(false);
            rowsLayout.setJustifyContentMode(JustifyContentMode.START);
            for (int row = 0; row < rows; row++) {
                final Button button = new Button("" + numbers[col + (row * cols)]);
                button.addThemeVariants(ButtonVariant.LUMO_ICON);
                rowsLayout.add(button);
                button.addClickListener(e -> {
                    hourToSet = Integer.parseInt(button.getText());
                    setNewValue();
                });
            }
            hourButtonPanel.add(rowsLayout);
        }
    }

    private void addAmPmButtons(final VerticalLayout amPmButtonPanel) {
        amPmButtonPanel.setId("RowLayout");
        amPmButtonPanel.setSpacing(false);
        amPmButtonPanel.setPadding(false);
        amPmButtonPanel.setMargin(false);
        amPmButtonPanel.setJustifyContentMode(JustifyContentMode.START);
        final Button am = new Button("AM");
        am.addThemeVariants(ButtonVariant.LUMO_ICON);
        final Button pm = new Button("PM");
        pm.addThemeVariants(ButtonVariant.LUMO_ICON);
        amPmButtonPanel.add(am, pm);
        am.addClickListener(e -> {
            periodClicked = "AM";
            setNewValue();
        });
        pm.addClickListener(e -> {
            periodClicked = "PM";
            setNewValue();
        });
    }

    private void addMinuteButtons(final HorizontalLayout minuteButtonPanel, final int rows, final int cols) {
        final String[] numbers = new String[] { "00", "10", "15", "20", "30", "40", "45", "50" };
        for (int col = 0; col < cols; col++) {
            final VerticalLayout rowsLayout = new VerticalLayout();
            rowsLayout.setId("RowLayout" + col);
            rowsLayout.setSpacing(false);
            rowsLayout.setPadding(false);
            rowsLayout.setJustifyContentMode(JustifyContentMode.START);
            for (int row = 0; row < rows; row++) {
                final Button button = new Button("" + numbers[row + (col * rows)]);
                button.addThemeVariants(ButtonVariant.LUMO_ICON);
                rowsLayout.add(button);
                button.addClickListener(e -> {
                    final String title = button.getText();
                    minuteToSet = Integer.parseInt(title);
                    setNewValue();
                });
            }
            minuteButtonPanel.add(rowsLayout);
        }
    }

    private void setNewValue() {
        hourToSet = ("PM".equals(periodClicked) ? hourToSet + 12 : (hourToSet > 12 ? hourToSet - 12 : hourToSet));
        modifiedDate = modifiedDate.withHour(hourToSet).withMinute(minuteToSet);
        setValue(modifiedDate);
        displayTime.setValue(modifiedDate.format(dtf));
    }

    private void clearValue() {
        field.setValue(EMPTY);
        displayTime.setValue(EMPTY);
    }

    public void addListener(final TimePickerValueChanged listener) {
        listeners.add(listener);
    }

    private void valueChanged(final LocalDateTime value) {
        for (final Iterator<TimePickerValueChanged> iterator = listeners.iterator(); iterator.hasNext();) {
            final TimePickerValueChanged timePickerValueChanged = iterator.next();
            timePickerValueChanged.valueChanged(value);
        }
    }

    public interface TimePickerValueChanged {
        void valueChanged(LocalDateTime value);
    }
}