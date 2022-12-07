package au.com.vaadinutils.flow.wizard;

import com.vaadin.flow.component.Component;

/**
 * All classes that need to be added to a {@link Wizard} will need to implement
 * this class.<br>
 * This provides a consist interface for the actions and content control for the
 * wizard.
 */
public interface WizardStep {

    /**
     * Returns the caption of this WizardStep.
     * 
     * @return the caption of this WizardStep.
     */
    public String getCaption();

    /**
     * Returns the {@link Component} that is to be used as the actual content of
     * this WizardStep.
     * 
     * @return the content of this WizardStep as a Component.
     */
    public Component getContent();

    /**
     * Returns true if user is allowed to navigate forward past this WizardStep.
     * Typically this method is called when user clicks the Next button of the
     * {@link Wizard}.
     * 
     * @return true if user is allowed to navigate past this WizardStep.
     */
    public boolean onAdvance();

    /**
     * Returns true if user is allowed to navigate backwards from this WizardStep.
     * Typically this method is called when user clicks the Back button of the
     * {@link Wizard}.
     * 
     * @return true if user is allowed to navigate backwards from this WizardStep.
     */
    public boolean onBack();
}
