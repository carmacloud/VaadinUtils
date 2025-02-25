package au.com.vaadinutils.flow.helper;

import java.util.Set;

import com.vaadin.flow.data.binder.Binder;

/**
 * For using in a simple {@link Binder} implementations when a {@link Set} of
 * objects is needed.<br>
 * TODO LC: CAR-5635. Could be removed in favour of using a direct method to set
 * and get records.
 *
 * @param <E>
 */
public class BeanSet<E> {

    private Set<E> beanSet;

    public Set<E> getBeanSet() {
        return this.beanSet;
    }

    public void setBeanSet(final Set<E> beanSet) {
        this.beanSet = beanSet;
    }
}