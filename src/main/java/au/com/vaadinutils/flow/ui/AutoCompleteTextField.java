package au.com.vaadinutils.flow.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public class AutoCompleteTextField<E> extends TextField {

    private static final long serialVersionUID = -6634513296678504250L;
    private final Popup popup = new Popup();
    private final Map<E, String> options = new LinkedHashMap<>();
    private AutoCompleteQueryListener<E> listener;
    private AutoCompleteOptionSelected<E> optionListener;

    private long dropDownWidth = 120;

    /**
     * <pre>
     * {@code
     * sample usage
     * 
     * 	AutoCompleteTextField<PostCode> suburb = new AutoCompleteTextField<>();
     * 
     * suburb.setQueryListener(new AutoCompleteQueryListener<PostCode>()
     * {
     * 
     * 	    &#64;Override
     * 	    public void handleQuery(AutoCompleteTextField<PostCode> field,String queryText)
     * 	    {
     * 		    field.addOption(new PostCode(3241),"Title");
     * 	    }
     * 	});
     * 
     * 	suburb.setOptionSelectionListener(new AutoCompleteOptionSelected<PostCode>()
     * 	{
     * 	    
     * 	    &#64;Override
     * 	    public void optionSelected(AutoCompleteTextField<PostCode> field, PostCode option)
     * 	    {
     * 		field.setValue(option.getSuburb());
     * 	    }
     * 	});
     * }
     * </pre>
     * 
     */
    public AutoCompleteTextField() {
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
        addKeyDownListener(Key.ENTER, e -> {
            // Clear list and hide
            popup.removeAll();
            popup.hide();
            // Pass back value that is in the text field.
            enterListener.value(getValue());
        });
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
        setClassName(listCaption);
        setId(listCaption);
        setLabel(fieldCaption);
        setClearButtonVisible(true);
        popup.setFor(listCaption);

        component.add(popup);

        // Set as Lazy and if also set, there can be a timeout value.
        setValueChangeMode(ValueChangeMode.LAZY);
        addValueChangeListener(valueChangeListener -> {
            if (listener != null) {
                options.clear();
                popup.removeAll();
                listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
            }

            if (!options.isEmpty()) {
                if (valueChangeListener.isFromClient()) {
                    showOptionMenu();
                }
            } else {
                popup.removeAll();
            }
        });
    }

    private void showOptionMenu() {
        popup.removeAll();
        popup.show();
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setWidth(dropDownWidth, Unit.PIXELS);
        for (final E item : options.keySet()) {
            final String label = options.get(item);
            final Label labelHeader = new Label(label);
            labelHeader.setId(label);
            final Div div = new Div(labelHeader);
            layout.add(div);
            div.addClickListener(e -> {
                optionListener.optionSelected(AutoCompleteTextField.this, item);
                // Clear list and hide
                popup.removeAll();
                popup.hide();
            });
        }

        popup.add(layout);
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
        popup.hide();
    }
}