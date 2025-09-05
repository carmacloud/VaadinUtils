package au.com.vaadinutils.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaDslBuilder<E> extends JpaDslAbstract<E, E> {
    public JpaDslBuilder(final Class<E> entityClass) {
        this.entityClass = entityClass;
        builder = getEntityManager().getCriteriaBuilder();

        criteria = builder.createQuery(entityClass);
        root = criteria.from(entityClass);
        criteria.select(root);
    }

    /**
     * constructor specifically for the JpaContainerDelegate usage
     * 
     * @param query
     * @param entityClass
     */
    @SuppressWarnings("unchecked")
    public JpaDslBuilder(final CriteriaQuery<E> query, final Class<E> entityClass) {
        this.entityClass = entityClass;
        builder = getEntityManager().getCriteriaBuilder();

        criteria = query;
        root = (Root<E>) criteria.getRoots().iterator().next();

        isJpaContainerDelegate = true;
    }
}
