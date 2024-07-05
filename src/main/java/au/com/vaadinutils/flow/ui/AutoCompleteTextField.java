package au.com.vaadinutils.flow.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

import au.com.vaadinutils.flow.helper.VaadinHelper;

public class AutoCompleteTextField<E> extends TextField {

    private static final long serialVersionUID = -6634513296678504250L;
    private final Popup popup = new Popup();
    private final Map<E, String> options = new LinkedHashMap<>();
    private AutoCompleteQueryListener<E> listener;
    private AutoCompleteOptionSelected<E> optionListener;

    private long dropDownWidth = 120;

    private final List<Registration> registrations = new ArrayList<Registration>();

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
     * The ContextMenu is removed from the parent after each interaction to allow
     * the default context menu to still work for copy/paste. However there is an
     * issue with the ContextMenuClosedEvent firing before the
     * ContextMenuItemClickEvent. This means that the ContextMenu can't be removed
     * from the parent in onContextMenuClosed, otherwise contextMenuItemClicked is
     * never entered. The effect of this is that the default context menu stops
     * working if the user clicks out of the auto complete context menu without
     * selecting anything.
     */
    public AutoCompleteTextField() {
        addDetachListener(listener -> {
            registrations.forEach(reg -> reg.remove());
        });
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
        if (listCaption != null) {
            setLabel(fieldCaption);
        }
        setClearButtonVisible(true);
        popup.setFor(listCaption);

        component.add(popup);

        // Set as Lazy and if also set, there can be a timeout value.
        setValueChangeMode(ValueChangeMode.LAZY);
        final Registration reg = addValueChangeListener(valueChangeListener -> {
            if (listener != null) {
                options.clear();
                listener.handleQuery(AutoCompleteTextField.this, valueChangeListener.getValue());
            }

            if (!options.isEmpty()) {
                if (valueChangeListener.isFromClient()) {
                    showOptionMenu();
                }
            }
        });
        registrations.add(reg);
    }

    private void showOptionMenu() {
        popup.removeAll();
        popup.show();
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setWidth(dropDownWidth, Unit.PIXELS);
        for (final E item : options.keySet()) {
            final String label = options.get(item);
            final Span span = new Span(
                    new Html("<font color='" + VaadinHelper.CARMA_DARK_BLACK + "'>" + label + "</font>"));
            span.setId(label);
            layout.add(span);
            final Registration reg = span.addClickListener(e -> {
                optionListener.optionSelected(AutoCompleteTextField.this, item);
                // Clear list and hide
                popup.removeAll();
                popup.hide();
            });
            registrations.add(reg);
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