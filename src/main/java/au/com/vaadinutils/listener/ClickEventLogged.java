package au.com.vaadinutils.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.vaadinutils.flow.errorhandling.ErrorWindow;

/**
 * Not used.
 */
public class ClickEventLogged {
    static public abstract class ClickListener implements com.vaadin.ui.Button.ClickListener {
        private static final long serialVersionUID = 7420365324169589382L;

        transient Logger logger = LogManager.getLogger(ClickListener.class);

        abstract public void clicked(com.vaadin.ui.Button.ClickEvent event);

        @Override
        public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
            try {
                clicked(event);
            } catch (final Throwable e) {
                ErrorWindow.showErrorWindow(e, null);
            }
        }
    }

    static public class ClickAdaptor implements com.vaadin.ui.Button.ClickListener {
        private static final long serialVersionUID = 1L;

        transient Logger logger = LogManager.getLogger(ClickAdaptor.class);

        private com.vaadin.ui.Button.ClickListener listener = null;

        public ClickAdaptor(final com.vaadin.ui.Button.ClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
            try {
                listener.buttonClick(event);
            } catch (final Throwable e) {
                logger.error(e, e);
                throw new RuntimeException(e);
            }

        }
    }

}