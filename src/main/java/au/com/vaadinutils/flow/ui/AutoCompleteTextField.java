package au.com.vaadinutils.flow.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import au.com.vaadinutils.flow.helper.VaadinHelper;

public class AutoCompleteTextField<E> extends HorizontalLayout {

    private static final long serialVersionUID = -6634513296678504250L;
    final Logger logger = LogManager.getLogger();
    private final Popover popover = new Popover();
    private final TextField field = new TextField();
    private final Icon icon = VaadinIcon.SEARCH.create();
    private final Map<E, String> options = new LinkedHashMap<>();
    private AutoCompleteQueryListener<E> listener;
    private AutoCompleteOptionSelected<E> optionListener;

    private long dropDownWidth = 120;

    /**
     * <pre> {@code sample usage
     * 
     * AutoCompleteTextField<PostCode> suburb = new AutoCompleteTextField<>();
     * suburb.init(component, "Label", "_link");
     * 
     * suburb.setQueryListener(new AutoCompleteQueryListener<PostCode>() {
     * 
     * &#64;Override public void handleQuery(AutoCompleteTextField<PostCode>
     * field,String queryText) { field.addOption(new PostCode(3241),"Title"); } });
     * 
     * suburb.setOptionSelectionListener(new AutoCompleteOptionSelected<PostCode>()
     * {
     * 
     * &#64;Override public void optionSelected(AutoCompleteTextField<PostCode>
     * field, PostCode option) { field.setValue(option.getSuburb()); } }); } </pre>
     * 
     */
    public AutoCompleteTextField() {
        setSpacing(false);
        setPadding(false);
        setMargin(false);
        field.setWidthFull();
        add(field);
    }

    /**
     *
     * @param enterListener The {@link EnterListener} to allow the value in the
     *                      {@link TextField} to be passed back to the calling
     *                      class.<br>
     *                      Note: Added to allow Search Text field on SearchView to
     *                      initiate a search using the current contents of the
     *                      field.<br>
     *                      It may have unintended consequences if used in another
     *                      screen.
     */
    public void addEnterKeyListener(final EnterListener enterListener) {
        // Create a tiny invisible icon so it squeezes in beside the search field.
        icon.setSize("1px");
        icon.setColor(VaadinHelper.CARMA_WHITE);

        icon.addClickShortcut(Key.ENTER);
        icon.addClickListener(e -> {
            // Clear list and close
            popover.removeAll();
            popover.close();
            // Pass back value that is in the text field.
            enterListener.value(field.getValue());
        });
        setAlignItems(Alignment.CENTER);
        add(icon);
    }

    public interface EnterListener {
        void value(String value);
    }

    /**
     * Need to call this to have the popup list initialised and to tie the popup to
     * the field.
     * 
     * @param component    A layout that extends {@link HasComponents} for linking
     *                     the popup.
     * @param fieldCaption A {@link String} that can be empty or null if not
     *                     required.
     * @param listCaption  A {@link String} that the popup uses to link to the
     *                     field.
     */
    public void init(final HasComponents component, final String fieldCaption, final String listCaption) {
        Preconditions.checkNotNull(listCaption, "List Caption is required to link the popup to the field.");
        Preconditions.checkArgument(listCaption.length() > 0,
                "List Caption is required to link the popup to the field.");
        field.setClassName(listCaption);
        field.setId(listCaption);
        field.setLabel(fieldCaption);
        field.setClearButtonVisible(true);
        popover.setFor(listCaption);

        component.add(popover);

        // Set as Lazy with a default timeout of 400 ms. Use setValueChangeTimeout() to
        // override.
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.addValueChangeListener(valueChangeListener -> {
            if (valueChangeListener.isFromClient()) {
                if (listener != null) {
                    options.clear();
                    popover.removeAll();
                    listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
                }
            }

            if (!options.isEmpty()) {
                if (valueChangeListener.isFromClient()) {
                    showOptionMenu();
                }
            } else {
                popover.removeAll();
            }
        });
    }

    private void showOptionMenu() {
        popover.removeAll();
        popover.open();
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setWidth(dropDownWidth, Unit.PIXELS);
        layout.setId(this.getClass().getSimpleName() + "-Layout");
        for (final E item : options.keySet()) {
            final String label = options.get(item);
            final Span labelHeader = new Span(label);
            labelHeader.setId(label);
            final Div div = new Div(labelHeader);
            layout.add(div);
            div.addClickListener(e -> {
                optionListener.optionSelected(AutoCompleteTextField.this, item);
                // Clear list and close
                popover.removeAll();
                popover.close();
            });
        }

        popover.add(layout);
    }

    public long getDropDownWidth() {
        return dropDownWidth;
    }

    public void setDropDownWidth(final long dropDownWidth) {
        this.dropDownWidth = dropDownWidth;
    }

    public void setOptionSelectionListener(final AutoCompleteOptionSelected<E> listener) {
        this.optionListener = listener;
    }

    public void removeOptionSelectionListener() {
        optionListener = null;
    }

    public void setQueryListener(final AutoCompleteQueryListener<E> listener) {
        this.listener = listener;
    }

    public void removeQueryListener() {
        listener = null;
    }

    public void addOption(final E option, final String optionLabel) {
        options.put(option, optionLabel);
    }

    public void hideAutoComplete() {
        popover.close();
    }

    public void setValueChangeTimeout(final int delay) {
        field.setValueChangeTimeout(delay);
    }

    public void setFieldWidth(final String width) {
        field.setWidth(width);
    }

    public TextField getField() {
        return field;
    }

    public void addValueChangeListener(
            final ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> event) {
        field.addValueChangeListener(event);
    }
}