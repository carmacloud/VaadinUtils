package au.com.vaadinutils.flow.wizard;

import java.util.List;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.wizard.event.WizardCancelledEvent;
import au.com.vaadinutils.flow.wizard.event.WizardCompletedEvent;
import au.com.vaadinutils.flow.wizard.event.WizardProgressListener;
import au.com.vaadinutils.flow.wizard.event.WizardStepActivationEvent;
import au.com.vaadinutils.flow.wizard.event.WizardStepSetChangedEvent;

/**
 * This class manages the progress bar and the captions.
 */
public class WizardProgressBar extends VerticalLayout implements WizardProgressListener {

    private static final long serialVersionUID = -4600007265990076235L;
    private final Wizard wizard;
    private final ProgressBar progressBar = new ProgressBar();
    private final HorizontalLayout captionLayout = new HorizontalLayout();
    private int activeStepIndex;

    public WizardProgressBar(Wizard wizard) {
        this.wizard = wizard;
        setPadding(false);
        setMargin(false);
        progressBar.setWidth("100%");
        progressBar.setHeight("10px");
        progressBar.addClassName("WizardProgressBar");
        progressBar.addThemeName("transparent");
        captionLayout.setJustifyContentMode(JustifyContentMode.EVENLY);
        captionLayout.setSpacing(false);
        captionLayout.setWidth("100%");

        this.wizard.addStepChangeListener(e -> stepSetChanged(e));
        this.wizard.addCompletedChangeListener(e -> wizardCompleted(e));
        this.wizard.addCancelledChangeListener(e -> wizardCancelled(e));
        this.wizard.addStepActivationListener(e -> activeStepChanged(e));

        add(captionLayout, progressBar);
    }

    private void updateProgressBar() {
        int stepCount = captionLayout.getComponentCount();
        float progressValue = activeStepIndex / ((float) stepCount + 1);
        progressBar.setValue(progressValue);
    }

    private void updateStepCaptions() {
        captionLayout.removeAll();
        int index = 1;
        for (WizardStep step : wizard.getSteps()) {
            final Html label = createCaptionLabel(index, step);
            captionLayout.add(label);
            index++;
        }
    }

    private Html createCaptionLabel(int index, WizardStep step) {
        String labelCaption = index + ". " + step.getCaption();
        final String fontColour;
        if (wizard.isActive(step)) {
            fontColour = VaadinHelper.CARMA_DARK_BLUE;
            labelCaption = "<b>" + labelCaption + "</b>";
        } else if (wizard.isCompleted(step)) {
            fontColour = VaadinHelper.CARMA_DARK_BLACK;
        } else {
            fontColour = VaadinHelper.CARMA_LIGHT_GREY;
        }
        return new Html("<span><font color='" + fontColour + "'>" + labelCaption + "</font></span>");
    }

    private void updateProgressAndCaptions() {
        updateStepCaptions();
        updateProgressBar();
    }

    @Override
    public void activeStepChanged(WizardStepActivationEvent event) {
        final List<WizardStep> allSteps = wizard.getSteps();
        activeStepIndex = allSteps.indexOf(event.getActivatedStep()) + 1;
        updateProgressAndCaptions();
    }

    @Override
    public void stepSetChanged(WizardStepSetChangedEvent event) {
        updateProgressAndCaptions();
    }

    @Override
    public void wizardCompleted(WizardCompletedEvent event) {
        progressBar.setValue(1.0f);
        updateStepCaptions();
    }

    @Override
    public void wizardCancelled(WizardCancelledEvent event) {
        // Not used.
    }
}
