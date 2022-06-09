package au.com.vaadinutils.helper;

import com.vaadin.flow.data.binder.Binder;

/**
 * For using in a simple {@link Binder} implementations when a single object is
 * needed.
 * 
 * @param <T>
 */
public class Bean<T> {
    private T bean;

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }
}
