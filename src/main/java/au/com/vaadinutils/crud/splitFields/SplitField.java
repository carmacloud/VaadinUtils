package au.com.vaadinutils.crud.splitFields;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * @deprecated Replaced in V14 migration.
 */
public interface SplitField extends Component {

    Label getLabel();

    String getCaption();

    void hideLabel();
}
