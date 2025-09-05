package au.com.vaadinutils.dao;

import jakarta.persistence.criteria.Expression;

public class JpaDslCountBuilder<E> extends JpaDslAbstract<E, Long> {
    public JpaDslCountBuilder(final Class<E> entityClass) {
        this.entityClass = entityClass;
        builder = getEntityManager().getCriteriaBuilder();

        criteria = builder.createQuery(Long.class);
        root = criteria.from(entityClass);
    }

    @Override
    public Long count() {
        if (predicate != null) {
            criteria.where(predicate);
        }
        criteria.select(builder.count(root));

        return getEntityManager().createQuery(criteria).getSingleResult();
    }

    public Long countDistinct() {
        if (predicate != null) {
            criteria.where(predicate);
        }
        criteria.select(builder.countDistinct(root));

        return getEntityManager().createQuery(criteria).getSingleResult();
    }

    public Long countDistinct(final Expression<?> x) {
        if (predicate != null) {
            criteria.where(predicate);
        }
        criteria.select(builder.countDistinct(x));

        return getEntityManager().createQuery(criteria).getSingleResult();
    }
}