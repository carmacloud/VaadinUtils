package au.com.vaadinutils.flow.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import au.com.vaadinutils.flow.errorhandling.ErrorWindow;
import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;

public class InputFormDialog extends Dialog {

    private static final long serialVersionUID = -7109252996175845038L;
    private final Logger logger = LogManager.getLogger();
    private final HorizontalLayout buttonLayout;
    private final Button cancelButton;
    private final Button ok;
    private boolean validationError = false;
    private boolean layoutIsForm = false;

    public InputFormDialog(final String title, final HasValidation primaryFocusField, final Component form,
            final InputFormDialogRecipient recipient) {
        this.add(new Text(title));
        this.setModal(true);

        this.setClosable(false);
        this.setResizable(false);

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(false);
        layout.setWidthFull();
        layout.addAndExpand(form);

        buttonLayout = new HorizontalLayout();
        buttonLayout.setMargin(false);
        buttonLayout.setPadding(false);
        buttonLayout.setSpacing(true);
        buttonLayout.setHeight("30px");

        cancelButton = createCancelButton(recipient);

        ok = createOkButton(form, recipient);
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ok.setId("Ok");
        ok.addClickShortcut(Key.ENTER, KeyModifier.ALT);
        ok.addThemeName("default");

        buttonLayout.add(cancelButton, ok);

        layout.add(buttonLayout);
        layout.setAlignItems(Alignment.END);

        this.add(layout);
        this.open();

        if (primaryFocusField instanceof Focusable<?>) {
            ((Focusable<?>) primaryFocusField).focus();
        }

        if (form instanceof FormLayout) {
            setWidth("500px");
            layoutIsForm = true;
        }
    }

    private Button createOkButton(final Component form, final InputFormDialogRecipient recipient) {
        return new Button("OK", new ComponentEventListener<ClickEvent<Button>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentEvent(final ClickEvent<Button> event) {
                validationError = false;
                validateFormComponents(form);
                if (!validationError) {
                    recipient.onOK();
                }
                close();
            }
        });
    }

    private void validateFormComponents(final Component form) {
        if (!validationError && !layoutIsForm) {
            try {
                form.getChildren().forEach(child -> {
                    // Only validate if the component has the HasValidation interface. Otherwise
                    // it's possibly a layout.
                    if (child instanceof HasValidation) {
                        final boolean isInvalid = ((HasValidation) child).isInvalid();
                        final String errorMessage = ((HasValidation) child).getErrorMessage();
                        if (isInvalid) {
                            logger.warn("Form errors, Input Form closing '"
                                    + (errorMessage != null ? errorMessage : "Incorrect value") + "'");
                            VaadinHelper.notificationDialog(
                                    "Form errors, '" + (errorMessage != null ? errorMessage : "Incorrect value") + "'",
                                    NotificationType.WARNING);
                            validationError = true;
                        }
                    } else {
                        // Fields possibly wrapped in a layout. Pass the component in to find the
                        // sub-components that are to be validated.
                        validateFormComponents(child);
                    }
                });
            } catch (final Exception e) {
                ErrorWindow.showErrorWindow(e, getClass().getSimpleName());
                logger.error("Cannot validate on this component.");
            }
        }
    }

    private Button createCancelButton(final InputFormDialogRecipient recipient) {
        return new Button("Cancel", new ComponentEventListener<ClickEvent<Button>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentEvent(final ClickEvent<Button> event) {
                recipient.onCancel();
                close();
            }
        });
    }

    public void okOnly() {
        buttonLayout.remove(cancelButton);
    }

    public void setButtonsSpacing(final boolean spacing) {
        buttonLayout.setSpacing(spacing);
    }

    public void setOkButtonLabel(final String label) {
        ok.setText(label);
    }

    public void setCancelButtonLabel(final String label) {
        cancelButton.setText(label);
    }

    public void showOkButton(final boolean show) {
        ok.setVisible(show);
    }

    /**
     * By default, {@link Dialog} has no close component, but will close if clicked
     * outside of the dialog, or ESC is pressed.<br>
     * This InputForm is set closable by default.<br>
     * Switch this behaviour by passing the relevant <code>boolean</code> into this
     * method.
     * 
     * @param closable A <code>boolean</code>. If true, Input can be closed by
     *                 clicking outside the dialog, or pressing ESC.<br>
     *                 Otherwise closing only happens by cancelling, or clicking
     *                 'OK'.
     */
    public void setClosable(final boolean closable) {
        setCloseOnEsc(closable);
        setCloseOnOutsideClick(closable);
    }
}