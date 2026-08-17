package au.com.vaadinutils.flow.fields;

import java.util.Optional;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;

public class NavigationLayoutBasic extends VerticalLayout {

    private static final long serialVersionUID = 1873531700509420953L;
    private VerticalLayout contentPanel = new VerticalLayout();
    protected ButtonLayout buttonLayout;
    private Button primaryButton;
    private Button secondaryButton;
    private boolean disableOnClick = false;
    private String primaryNotificationCaption = "Changes saved";
    private String primaryNotificationDescription = "Any changes you have made have been saved";
    private String secondaryNotificationCaption = "Changes discarded";
    private String secondaryNotificationDescription = "Any changes you have made have been discarded";
    private final VaadinHelper vaadinHelper = new VaadinHelper();

    public NavigationLayoutBasic() {
        this(null);
    }

    public NavigationLayoutBasic(final Component content) {
        this.setId(this.getClass().getSimpleName());
        this.setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        contentPanel.setPadding(false);
        contentPanel.setSpacing(false);
        contentPanel.setId(this.getClass().getSimpleName() + "-ContentPanel");

        if (content != null) {
            contentPanel.addAndExpand(content);
        }

        buildButtonLayout();
        enableEnterShortcut();

        this.addAndExpand(contentPanel);
        this.add(buttonLayout);
        this.setHorizontalComponentAlignment(Alignment.END, buttonLayout);
    }

    protected void buildButtonLayout() {
        buttonLayout = new ButtonLayout();
        primaryButton = buttonLayout.getPrimaryButton();
        secondaryButton = buttonLayout.getSecondaryButton();

        primaryButton.setText("Save");
        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        secondaryButton.setText("Cancel");
    }

    public void setContent(final Component content) {
        contentPanel.addAndExpand(content);
    }

    public Component getContent() {
        return contentPanel.getComponentAt(0);
    }

    public VerticalLayout getContentPanel() {
        return contentPanel;
    }

    public void removeContentPanel(final VerticalLayout contentPanel) {
        remove(contentPanel);
    }

    public void setContentPanel(final VerticalLayout contentPanel) {
        addAndExpand(contentPanel);
    }

    public void setButtonsVisible(final boolean visible) {
        buttonLayout.setVisible(visible);
    }

    public boolean isButtonsVisible() {
        return buttonLayout.isVisible();
    }

    public void setButtonsEnabled(final boolean enabled) {
        primaryButton.setEnabled(enabled);
        secondaryButton.setEnabled(enabled);
    }

    public Button getPrimaryButton() {
        return primaryButton;
    }

    public Button getSecondaryButton() {
        return secondaryButton;
    }

    public void addPrimaryClickListener(final PrimaryClickListener clickListener) {
        if (clickListener != null) {
            primaryButton.addClickListener(event -> {
                final boolean success = clickListener.click(event);
                if (success) {
                    showPrimaryNotification();
                }

                if (!disableOnClick) {
                    setDefaultButtonState();
                }
            });
        }
    }

    public void addSecondaryClickListener(final SecondaryClickListener clickListener) {
        if (clickListener != null) {
            secondaryButton.addClickListener(event -> {
                clickListener.click(event);

                showSecondaryNotification();

                if (!disableOnClick) {
                    setDefaultButtonState();
                }
            });
        }
    }

    public interface PrimaryClickListener {
        boolean click(final ClickEvent<?> event);
    }

    public interface SecondaryClickListener {
        void click(final ClickEvent<?> event);
    }

    private void showPrimaryNotification() {
        if (!Optional.ofNullable(primaryNotificationCaption).orElse("").isEmpty()
                || !Optional.ofNullable(primaryNotificationDescription).orElse("").isEmpty()) {
            vaadinHelper.notificationDialog(primaryNotificationCaption, primaryNotificationDescription,
                    NotificationType.TRAY);
        }
    }

    private void showSecondaryNotification() {
        if (!Optional.ofNullable(secondaryNotificationCaption).orElse("").isEmpty()
                || !Optional.ofNullable(secondaryNotificationDescription).orElse("").isEmpty()) {
            vaadinHelper.notificationDialog(secondaryNotificationCaption, secondaryNotificationDescription,
                    NotificationType.TRAY);
        }
    }

    public void setDefaultButtonState() {
        primaryButton.setEnabled(true);
        secondaryButton.setEnabled(true);
    }

    public void setDisableClick(final boolean disableOnClick) {
        this.disableOnClick = disableOnClick;
    }

    public void setPrimaryNotification(final String caption, final String description) {
        primaryNotificationCaption = caption;
        primaryNotificationDescription = description;
    }

    public void setSecondaryNotification(final String caption, final String description) {
        secondaryNotificationCaption = caption;
        secondaryNotificationDescription = description;
    }

    public void disableEnterShortcut() {
        buttonLayout.disableEnterShortcut();
    }

    public void enableEnterShortcut() {
        buttonLayout.enableEnterShortcut();
    }
}