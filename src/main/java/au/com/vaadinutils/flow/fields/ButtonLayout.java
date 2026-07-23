package au.com.vaadinutils.flow.fields;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import au.com.vaadinutils.flow.helper.VaadinHelper;

public class ButtonLayout extends HorizontalLayout {

    private static final long serialVersionUID = -6622071406939795620L;
    private final Icon infoIcon = VaadinIcon.QUESTION_CIRCLE_O.create();
    private final Button primaryButton = new Button("Primary");
    private final Button secondaryButton = new Button("Secondary");

    private ShortcutRegistration primaryShortCut;

    public ButtonLayout() {
        addClassName("navigation-button-layout");
        setSpacing(true);
        setPadding(false);
        setMargin(false);

        infoIcon.setVisible(false);
        add(infoIcon);
        infoIcon.setSize("20px");
        infoIcon.setColor(VaadinHelper.CARMA_BLUE);

        add(secondaryButton, primaryButton);

        setVerticalComponentAlignment(Alignment.CENTER, infoIcon);
        // Position all components to the end.
        setJustifyContentMode(JustifyContentMode.END);
        setWidthFull();

        primaryButton.setDisableOnClick(true);
        secondaryButton.setDisableOnClick(true);
    }

    public Button getPrimaryButton() {
        return primaryButton;
    }

    public Button getSecondaryButton() {
        return secondaryButton;
    }

    public Icon getInfoIcon() {
        return infoIcon;
    }

    public void disableEnterShortcut() {
        primaryShortCut.remove();
    }

    public void enableEnterShortcut() {
        primaryShortCut = primaryButton.addClickShortcut(Key.ENTER);
    }
}