package au.com.vaadinutils.dao;

import java.util.List;

import au.com.vaadinutils.dao.JpaBaseDao.Condition;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

public class JpaDslSubqueryBuilder<P, E> extends JpaDslBuilder<E> {
    private Subquery<E> subQuery;
    private Root<P> parentRoot;

    public JpaDslSubqueryBuilder(final Class<E> entityClass, final CriteriaQuery<?> query, final Root<P> parentRoot) {
        super(entityClass);
        criteria = null;

        this.parentRoot = parentRoot;

        subQuery = query.subquery(entityClass);

        root = subQuery.from(entityClass);
        subQuery.select(root);
    }

    /**
     * join on parent.child = child
     * 
     * @param parentAttrib
     * @return
     */
    public AbstractCondition<E> joinParentQueryOnParentAttrib(final SingularAttribute<P, E> parentAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot.get(parentAttrib), root);
            }
        };
    }

    /**
     * join on parent.child = child
     * 
     * @param parentAttrib
     * @return
     */
    public AbstractCondition<E> joinParentQueryOnParentAttrib(final SetAttribute<P, E> parentAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot.get(parentAttrib), root);
            }
        };
    }

    /**
     * join on parent = child.parent
     * 
     * @param subQueryAttrib
     * @return
     */
    public AbstractCondition<E> joinParentQueryOnSubAttrib(final SingularAttribute<E, P> subQueryAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot, root.get(subQueryAttrib));
            }
        };
    }

    /**
     * join on parent = child.parent
     * 
     * @param subQueryAttrib
     * @return
     */
    public AbstractCondition<E> joinParentQueryOnSubAttrib(final SetAttribute<E, P> subQueryAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot, root.get(subQueryAttrib));
            }
        };
    }

    /**
     * join on parent.someattrib = child.someattrib
     * 
     * @param parentAttrib
     * @param subQueryAttrib
     * @return
     */
    public <V> AbstractCondition<E> joinParentQuery(final SingularAttribute<P, V> parentAttrib,
            final SetAttribute<E, V> subQueryAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot.get(parentAttrib), root.get(subQueryAttrib));
            }
        };
    }

    /**
     * join on parent.someattrib = child.someattrib
     * 
     * @param parentAttrib
     * @param subQueryAttrib
     * @return
     */
    public <V> AbstractCondition<E> joinParentQuery(final SingularAttribute<P, V> parentAttrib,
            final SingularAttribute<E, V> subQueryAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot.get(parentAttrib), root.get(subQueryAttrib));
            }
        };
    }

    /**
     * join on parent.child.someattrib = child.someattrib
     * 
     * @param subQueryAttrib
     * @param parentAttrib
     * 
     * @return
     */
    public <V, J> Condition<E> joinParentQuery(final SingularAttribute<P, V> parentAttrib, final JoinBuilder<E, J> join,
            final SingularAttribute<J, V> subQueryAttrib) {
        return new AbstractCondition<E>() {

            @Override
            public Predicate getPredicates() {
                return builder.equal(parentRoot.get(parentAttrib), getJoin(join).get(subQueryAttrib));
            }
        };
    }

    public Subquery<E> getSubQuery() {
        if (predicate != null) {
            subQuery.where(predicate);
        }
        return subQuery;
    }

    @Override
    public int delete() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public Long count() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public List<E> getResultList() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public E getSingleResultOrNull() {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public E getSingleResult() {
        throw new RuntimeException("Not Implemented");
    }
}
