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
    private HorizontalLayout buttonLayout;
    private Button cancelButton;
    private Button ok;
    private boolean validationError = false;
    private boolean layoutIsForm = false;

    public InputFormDialog(String title, HasValidation primaryFocusField, final Component form,
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
            public void onComponentEvent(ClickEvent<Button> event) {
                validationError = false;
                form.getChildren().forEach(child -> {
                    if (!validationError && !layoutIsForm) {
                        try {
                            final boolean isInvalid = ((HasValidation) child).isInvalid();
                            final String errorMessage = ((HasValidation) child).getErrorMessage();
                            if (isInvalid) {
                                logger.warn("Form errors, Input Form closing '" + errorMessage + "'");
                                VaadinHelper.notificationDialog("Form errors, '" + errorMessage + "'",
                                        NotificationType.WARNING);
                                validationError = true;
                            }
                        } catch (Exception e) {
                            ErrorWindow.showErrorWindow(e, getClass().getSimpleName());
                        }
                    }
                });
                if (!validationError) {
                    recipient.onOK();
                }
                close();
            }
        });
    }

    private Button createCancelButton(final InputFormDialogRecipient recipient) {
        return new Button("Cancel", new ComponentEventListener<ClickEvent<Button>>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                recipient.onCancel();
                close();
            }
        });
    }

    public void okOnly() {
        buttonLayout.remove(cancelButton);
    }

    public void setButtonsSpacing(boolean spacing) {
        buttonLayout.setSpacing(spacing);
    }

    public void setOkButtonLabel(String label) {
        ok.setText(label);
    }

    public void setCancelButtonLabel(String label) {
        cancelButton.setText(label);
    }

    public void showOkButton(boolean show) {
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
