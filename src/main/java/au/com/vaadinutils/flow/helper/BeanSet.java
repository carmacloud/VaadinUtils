package au.com.vaadinutils.flow.helper;

import java.util.Set;

import com.vaadin.flow.data.binder.Binder;

/**
 * For using in a simple {@link Binder} implementations when a {@link Set} of
 * objects is needed.
 *
 * @param <E>
 */
public class BeanSet<E> {

    private Set<E> beanSet;

    public Set<E> getBeanSet() {
        return this.beanSet;
    }

    public void setBeanSet(Set<E> beanSet) {
        this.beanSet = beanSet;
    }
}
