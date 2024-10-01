package au.com.vaadinutils.dao;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.lazyquerycontainer.EntityContainer;

import com.google.common.base.Preconditions;
import com.vaadin.addon.jpacontainer.JPAContainer;

import au.com.vaadinutils.flow.dao.CrudEntity;
import au.com.vaadinutils.flow.dao.GenericDao;

public class JpaBaseDao<E, K> implements GenericDao<E, K> {
    protected Class<E> entityClass;
    protected final static int MAX_PARAMETERS = 2000;
    protected final Logger logger = LogManager.getLogger();

    public interface Condition<E> {

        Condition<E> and(Condition<E> c1);

        Predicate getPredicates();

        Condition<E> or(Condition<E> c1);
    }

    static public <E> JpaBaseDao<E, Long> getGenericDao(final Class<E> class1) {
        return new JpaBaseDao<>(class1);
    }

    @SuppressWarnings("unchecked")
    public JpaBaseDao() {

        // hack to get the derived classes Class type.
        final ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        Preconditions.checkNotNull(genericSuperclass);
        this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[0];
        Preconditions.checkNotNull(this.entityClass);
    }

    /**
     * it's very important that we don't retain a reference to the entitymanager, as
     * when you instance this class and then use it in a closure you will end up
     * trying to access a closed entitymanager
     *
     * @return
     */
    public static EntityManager getEntityManager() {
        final EntityManager em = EntityManagerProvider.getEntityManager();

        Preconditions.checkNotNull(em,
                "Entity manager has not been initialized, if you are using a worker thread you will have to call EntityManagerProvider.createEntityManager()");

        Preconditions.checkState(em.isOpen(),
                "The entity manager is closed, this can happen if you instance this class "
                        + "and then use it in a closure when the closure gets called on a "
                        + "separate thread or servlet request");

        return em;

    }

    public JpaBaseDao(final Class<E> class1) {
        entityClass = class1;
    }

    @Override
    public void persist(final E entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public E merge(final E entity) {
        return getEntityManager().merge(entity);
    }

    @Override
    public void remove(final E entity) {
        getEntityManager().remove(entity);
    }

    public E findById(final Integer id) {
        if (id == null) {
            // moved the logger to here, so it isn't needlessly constructed for
            // every JpaBaseDao Object
            final Logger logger = LogManager.getLogger();
            logger.warn("Null key provided for findById on entity " + entityClass);
            if (logger.isDebugEnabled()) {
                final Exception e = new Exception("Null Key Provided for entity " + entityClass);
                logger.debug(e, e);
            }
            return null;
        }
        return getEntityManager().find(entityClass, new Long(id));
    }

    public <T> JpaDslSelectAttributeBuilder<E, T> select(final SingularAttribute<? super E, T> attribute) {
        return new JpaDslSelectAttributeBuilder<>(entityClass, attribute);
    }

    public Collection<E> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            // moved the logger to here, so it isn't needlessly constructed for
            // every JpaBaseDao Object
            final Logger logger = LogManager.getLogger();
            logger.warn("No keys provided for findById on entity " + entityClass);
            if (logger.isDebugEnabled()) {
                final Exception e = new Exception("No keys Provided for entity " + entityClass);
                logger.debug(e, e);
            }
            return null;
        }

        final JpaDslBuilder<E> q = select();
        q.where(q.in(getIdField(), ids));

        return q.getResultList();
    }

    @Override
    public E findById(final K id) {
        if (id == null) {
            // moved the logger to here, so it isn't needlessly constructed for
            // every JpaBaseDao Object
            final Logger logger = LogManager.getLogger();
            logger.warn("Null key provided for findById on entity " + entityClass);
            if (logger.isDebugEnabled()) {
                final Exception e = new Exception("Null Key Provided for entity " + entityClass);
                logger.debug(e, e);
            }
            return null;
        }
        return getEntityManager().find(entityClass, id);
    }

    protected E findSingleBySingleParameter(final String queryName, final SingularAttribute<E, String> paramName,
            final String paramValue) {
        E entity = null;
        final Query query = getEntityManager().createNamedQuery(queryName);
        JpaSettings.setQueryHints(query);
        query.setParameter(paramName.getName(), paramValue);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        final List<E> entities = query.getResultList();
        if (entities.size() > 0) {
            entity = entities.get(0);
        }
        return entity;
    }

    protected E findSingleBySingleParameter(final String queryName, final String paramName, final String paramValue) {
        E entity = null;
        final Query query = getEntityManager().createNamedQuery(queryName);
        JpaSettings.setQueryHints(query);
        query.setParameter(paramName, paramValue);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        final List<E> entities = query.getResultList();
        if (entities.size() > 0) {
            entity = entities.get(0);
        }
        return entity;
    }

    protected List<E> findListBySingleParameter(final String queryName, final String paramName,
            final Object paramValue) {
        final Query query = getEntityManager().createNamedQuery(queryName);
        JpaSettings.setQueryHints(query);
        query.setParameter(paramName, paramValue);
        @SuppressWarnings("unchecked")
        final List<E> entities = query.getResultList();
        return entities;
    }

    /**
     * Runs the given query returning all entities that matched by the query.
     *
     * @param queryName
     * @return
     */
    protected List<E> findList(final String queryName) {
        final Query query = getEntityManager().createNamedQuery(queryName);
        JpaSettings.setQueryHints(query);
        @SuppressWarnings("unchecked")
        final List<E> entities = query.getResultList();
        return entities;
    }

    @Override
    public List<E> findAll() {
        return findAll(null);
    }

    @Override
    public List<E> findAllByIds(final SingularAttribute<E, Long> idAttribute, final Collection<K> idsToFind) {

        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);

        // TODO: This would be better is all entities extended BaseCrudEntity, then it
        // would look like BaseCrudEntity_.id instead of "id"
        criteria.where(root.get(idAttribute).in(idsToFind));

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();

    }

    /**
     * Returns all rows ordered by the given set of entity attribues.
     *
     * You may pass in an array of attributes and a order by clause will be added
     * for each attribute in turn e.g. order by order[0], order[1] ....
     */
    @Override
    public List<E> findAll(final SingularAttribute<E, ?> order[]) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);
        if (order != null) {
            final List<Order> ordering = new LinkedList<>();
            for (final SingularAttribute<E, ?> field : order) {
                ordering.add(builder.asc(root.get(field)));

            }
            criteria.orderBy(ordering);
        }

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    /**
     * Returns all rows ordered by the given set of entity attribues.
     *
     * @param order         You may pass in an array of attributes and a order by
     *                      clause will be added for each attribute in turn e.g.
     *                      order by order[0], order[1] ....
     *
     * @param sortAscending An array of booleans that must be the same size as the
     *                      order. The sort array controls whether each attribute
     *                      will be sorted ascending or descending.
     *
     */
    public List<E> findAll(final SingularAttribute<E, ?> order[], final boolean sortAscending[]) {
        Preconditions.checkArgument(order.length == sortAscending.length,
                "Both arguments must have the same no. of array elements.");
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);

        final List<Order> ordering = new LinkedList<>();
        for (final SingularAttribute<E, ?> field : order) {
            if (sortAscending[ordering.size()] == true) {
                ordering.add(builder.asc(root.get(field)));
            } else {
                ordering.add(builder.desc(root.get(field)));
            }

        }
        criteria.orderBy(ordering);

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    public <V> E findOneByAttribute(final SingularAttribute<? super E, V> vKey, final V value) {
        final JpaDslBuilder<E> q = select();
        return q.where(q.eq(vKey, value)).getSingleResultOrNull();
    }

    public <V, SK> List<E> findAllByAttribute(final SingularAttribute<E, V> vKey, final V value,
            final SingularAttribute<E, SK> order) {
        return findAllByAttribute(vKey, value, order, null);
    }

    public <V, SK> List<E> findAllByAttribute(final SingularAttribute<E, V> vKey, final V value,
            final SingularAttribute<E, SK> order, final Integer limit) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);
        criteria.where(builder.equal(root.get(vKey), value));
        if (order != null) {
            criteria.orderBy(builder.asc(root.get(order)));
        }

        TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);
        if (limit != null) {
            query = query.setMaxResults(limit);
        }
        return query.getResultList();

    }

    public <SK> List<E> findAllByAttributeLike(final SingularAttribute<E, String> vKey, final String value,
            final SingularAttribute<E, SK> order) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);
        criteria.where(builder.like(root.<String>get(vKey), value));
        if (order != null) {
            criteria.orderBy(builder.asc(root.get(order)));
        }

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    /**
     * Find a single record by multiple attributes. Searches using AND.
     *
     * @param attributes AttributeHashMap of SingularAttributes and values
     * @return first matching entity
     */
    public E findOneByAttributes(final AttributesHashMap<E> attributes) {
        E ret = null;
        final List<E> results = findAllByAttributes(attributes, null);
        if (results.size() > 0) {
            ret = results.get(0);
        }

        return ret;
    }

    /**
     * Find multiple records by multiple attributes. Searches using AND.
     *
     * @param <SK>       attribute
     * @param attributes AttributeHashMap of SingularAttributes and values
     * @param order      SingularAttribute to order by
     * @return a list of matching entities
     */
    public <SK> List<E> findAllByAttributes(final AttributesHashMap<E> attributes,
            final SingularAttribute<E, SK> order) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);

        Predicate where = builder.conjunction();
        for (final Entry<SingularAttribute<E, Object>, Object> attr : attributes.entrySet()) {
            where = builder.and(where, builder.equal(root.get(attr.getKey()), attr.getValue()));
        }
        criteria.where(where);

        if (order != null) {
            criteria.orderBy(builder.asc(root.get(order)));
        }

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    /**
     * Find a single record by multiple attributes. Searches using OR.
     *
     * @param attributes AttributeHashMap of SingularAttributes and values
     * @return first matching entity
     */
    public E findOneByAnyAttributes(final AttributesHashMap<E> attributes) {
        E ret = null;
        final List<E> results = findAllByAnyAttributes(attributes, null);
        if (results.size() > 0) {
            ret = results.get(0);
        }

        return ret;
    }

    /**
     * Find multiple records by multiple attributes. Searches using OR.
     *
     * @param <SK>       attribute
     * @param attributes AttributeHashMap of SingularAttributes and values
     * @param order      SingularAttribute to order by
     * @return a list of matching entities
     */
    public <SK> List<E> findAllByAnyAttributes(final AttributesHashMap<E> attributes,
            final SingularAttribute<E, SK> order) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.select(root);

        Predicate where = builder.conjunction();
        for (final Entry<SingularAttribute<E, Object>, Object> attr : attributes.entrySet()) {
            where = builder.or(where, builder.equal(root.get(attr.getKey()), attr.getValue()));
        }
        criteria.where(where);

        if (order != null) {
            criteria.orderBy(builder.asc(root.get(order)));
        }

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    /**
     * get count of entity with a simple criteria
     *
     * @param vKey
     * @param value
     * @return
     */
    public <V> Long getCount(final SingularAttribute<E, V> vKey, final V value) {
        final CriteriaBuilder qb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        final Root<E> root = cq.from(entityClass);
        cq.select(qb.count(root));
        cq.where(qb.equal(root.get(vKey), value));

        final TypedQuery<Long> query = getEntityManager().createQuery(cq);
        JpaSettings.setQueryHints(query);

        return query.getSingleResult();
    }

    /**
     */
    public JPAContainer<E> createVaadinContainer() {
        final JPAContainer<E> container = new JPAContainer<>(entityClass);
        container.setEntityProvider(new BatchingPerRequestEntityProvider<>(entityClass));
        return container;
    }

    /**
     */
    public EntityContainer<E> createLazyQueryContainer() {
        final EntityManager em = getEntityManager();
        final boolean compositeItmes = true;

        final boolean detachedEntities = true;
        final String propertyId = getIdField().getName();
        final boolean applicationManagedTransactions = true;
        final EntityContainer<E> entityContainer = new EntityContainer<>(em, entityClass, propertyId, Integer.MAX_VALUE,
                applicationManagedTransactions, detachedEntities, compositeItmes);

        for (final Attribute<? super E, ?> attrib : getIdField().getDeclaringType().getAttributes()) {
            entityContainer.addContainerProperty(attrib.getName(), attrib.getJavaType(), null, true, true);
        }

        return entityContainer;
    }

    static public <T> SingularAttribute<T, Long> getIdField(final Class<T> type) {
        final Metamodel metaModel = getEntityManager().getMetamodel();
        final EntityType<T> entityType = metaModel.entity(type);
        return entityType.getDeclaredId(Long.class);
    }

    public SingularAttribute<E, Long> getIdField() {
        return getIdField(entityClass);
    }

    public void flushCache() {
        getEntityManager().getEntityManagerFactory().getCache().evict(entityClass);
    }

    public <V> int deleteAllByAttribute(final SingularAttribute<? super E, V> vKey, final V value) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);
        final Root<E> root = criteria.from(entityClass);
        criteria.where(builder.equal(root.get(vKey), value));

        final Query query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.executeUpdate();
    }

    public <V, J> List<E> findAllByAttributeJoin(final SingularAttribute<E, J> joinAttr,
            final SingularAttribute<J, V> vKey, final V value, final JoinType joinType) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        final Join<E, J> join = root.join(joinAttr, joinType);
        criteria.where(builder.equal(join.get(vKey), value));

        final TypedQuery<E> query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.getResultList();
    }

    public <V, J> int deleteAllByAttributeJoin(final SingularAttribute<J, V> vKey, final V value,
            final SingularAttribute<E, J> joinAttr) {
        final CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        final CriteriaDelete<E> criteria = builder.createCriteriaDelete(entityClass);
        final Root<E> root = criteria.from(entityClass);
        final Join<E, J> join = root.join(joinAttr, JoinType.LEFT);

        criteria.where(builder.equal(join.get(vKey), value));

//        getEntityManager().getClass();
        final Query query = getEntityManager().createQuery(criteria);
        JpaSettings.setQueryHints(query);

        return query.executeUpdate();

    }

    /**
     * @return the number of entities in the table.
     */
    public long getCount() {
        final String entityName = entityClass.getSimpleName();
        final Table annotation = entityClass.getAnnotation(Table.class);
        String tableName;
        if (annotation != null) {
            tableName = annotation.name();
        } else {
            tableName = entityName;
        }

        final String qry = "select count(" + entityName + ") from " + tableName + " " + entityName;
        final Query query = getEntityManager().createQuery(qry);
        JpaSettings.setQueryHints(query);
        final Number countResult = (Number) query.getSingleResult();
        return countResult.longValue();

    }

    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Override
    public void refresh(final E entity) {
        getEntityManager().refresh(entity);
    }

    public void detach(final E entity) {
        getEntityManager().detach(entity);
    }

    public JpaBaseDao<E, K>.FindBuilder findOld() {
        return new FindBuilder();
    }

    public JpaDslBuilder<E> select() {
        return new JpaDslBuilder<>(entityClass);
    }

    public JpaDslCountBuilder<E> selectCount() {
        return new JpaDslCountBuilder<>(entityClass);
    }

    public JpaDslTupleBuilder<E> selectTuple() {
        return new JpaDslTupleBuilder<>(entityClass);
    }

    public JpaDslBuilder<E> jpaContainerDelegate(final CriteriaQuery<E> criteria) {
        return new JpaDslBuilder<>(criteria, entityClass);
    }

    public class FindBuilder {
        final CriteriaBuilder builder = EntityManagerProvider.getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<E> criteria = builder.createQuery(entityClass);
        final Root<E> root = criteria.from(entityClass);
        final List<Predicate> predicates = new LinkedList<>();

        private Integer limit = null;
        private Integer startPosition = null;

        /**
         * specify that JPA should fetch child entities in a single query!
         *
         * @param field
         * @return
         */
        public <L> FindBuilder fetch(final SingularAttribute<E, L> field) {
            root.fetch(field, JoinType.LEFT);
            return this;
        }

        public <L> FindBuilder whereEqual(final SingularAttribute<E, L> field, final L value) {
            predicates.add(builder.equal(root.get(field), value));
            return this;
        }

        public <J, L> FindBuilder joinWhereEqual(final Join<E, J> join, final SingularAttribute<J, L> field,
                final L value) {
            predicates.add(builder.equal(join.get(field), value));
            return this;
        }

        public FindBuilder whereLike(final SingularAttribute<E, String> field, final String value) {
            predicates.add(builder.like(root.get(field), value));
            return this;
        }

        public <L extends Comparable<? super L>> FindBuilder whereGreaterThan(final SingularAttribute<E, L> field,
                final L value) {
            predicates.add(builder.greaterThan(root.get(field), value));
            return this;
        }

        public <L extends Comparable<? super L>> FindBuilder whereGreaterThanOrEqualTo(
                final SingularAttribute<E, L> field, final L value) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(field), value));
            return this;
        }

        public FindBuilder limit(final int limit) {
            this.limit = limit;
            return this;
        }

        public FindBuilder startPosition(final int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public FindBuilder orderBy(final SingularAttribute<E, ?> field, final boolean asc) {
            if (asc) {
                criteria.orderBy(builder.asc(root.get(field)));
            } else {
                criteria.orderBy(builder.desc(root.get(field)));
            }
            return this;
        }

        public <J> FindBuilder joinOrderBy(final Join<E, J> join, final SingularAttribute<J, ?> field,
                final boolean asc) {
            if (asc) {
                criteria.orderBy(builder.asc(join.get(field)));
            } else {
                criteria.orderBy(builder.desc(join.get(field)));
            }
            return this;
        }

        FindBuilder() {
            criteria.select(root);
        }

        public E getSingleResult() {
            limit(1);
            final TypedQuery<E> query = prepareQuery();
            return query.getSingleResult();
        }

        public List<E> getResultList() {
            final TypedQuery<E> query = prepareQuery();
            return query.getResultList();
        }

        private TypedQuery<E> prepareQuery() {
            Predicate filter = null;
            for (final Predicate predicate : predicates) {
                if (filter == null) {
                    filter = predicate;
                } else {
                    filter = builder.and(filter, predicate);
                }

            }
            if (filter != null) {
                criteria.where(filter);
            }
            final TypedQuery<E> query = EntityManagerProvider.getEntityManager().createQuery(criteria);
            JpaSettings.setQueryHints(query);
            if (limit != null) {
                query.setMaxResults(limit);
            }
            if (startPosition != null) {
                query.setFirstResult(startPosition);
            }
            return query;
        }

        public <L> FindBuilder whereNotEqueal(final SingularAttribute<E, L> field, final L value) {
            predicates.add(builder.notEqual(root.get(field), value));
            return this;
        }

        public <L> FindBuilder whereNotNull(final SingularAttribute<E, L> field) {
            predicates.add(builder.isNotNull(root.get(field)));
            return this;
        }

        public <L> FindBuilder whereNull(final SingularAttribute<E, L> field) {
            predicates.add(builder.isNull(root.get(field)));
            return this;

        }

        public Predicate like(final SingularAttribute<E, String> field, final String value) {
            return builder.like(root.get(field), value);
        }

        public <J> Join<E, J> join(final SingularAttribute<E, J> joinAttribute, final JoinType joinType) {
            return root.join(joinAttribute, joinType);
        }

        public <J> Predicate joinLike(final Join<E, J> join, final SingularAttribute<J, String> field,
                final String value) {
            return builder.like(join.get(field), value);
        }

        public FindBuilder whereAnd(final Predicate pred) {
            predicates.add(pred);
            return this;
        }

        public FindBuilder whereOr(final List<Predicate> orPredicates) {
            Predicate or = null;
            for (final Predicate pred : orPredicates) {
                if (or == null) {
                    or = pred;
                } else {
                    or = builder.or(or, pred);
                }
            }
            if (or != null) {
                predicates.add(or);
            }
            return this;
        }

        public <L extends Comparable<? super L>> FindBuilder whereLessThanOrEqualTo(final SingularAttribute<E, L> field,
                final L value) {
            predicates.add(builder.lessThanOrEqualTo(root.get(field), value));
            return this;
        }

        public <L extends Comparable<? super L>> Predicate greaterThanOrEqualTo(final SingularAttribute<E, L> field,
                final L value) {
            return builder.greaterThanOrEqualTo(root.get(field), value);
        }

        public <L> Predicate isNull(final SingularAttribute<E, L> field) {
            return builder.isNull(root.get(field));
        }
    }

    public List<E> getEntities(final int startIndex) {
        return getGenericDao(entityClass).select().startPosition(startIndex).getResultList();
    }

    public int getEntityCount() {
        return getGenericDao(entityClass).select().count().intValue();
    }

    public Collection<Long> getIds(final Collection<? extends CrudEntity> entities) {
        final Set<Long> ids = new HashSet<>();
        for (final CrudEntity entity : entities) {
            ids.add(entity.getId());
        }

        return ids;
    }

    @Override
    public void commitAndContinue() {
        EntityManagerProvider.commitAndContinue();

    }

    @Override
    public EntityTransaction getTransaction() {
        // TODO LC: Work out if we still need this?
        return null;
    }

    /**
     * Convenience method to split a {@link List} of {@link Long} ids into a number
     * of lists with the count <=2000. This prevents excessive records being
     * returned if we use min and max numbers and between to find records when
     * caching.<br>
     * If the list passed in is > allowed size, then the {@link Map} is populated
     * with as many lists as required to accommodate all the ids. If it is empty,
     * then the list of ids is < allowed size and can be processed as is.
     * 
     * @param ids A {@link List} of Long ids to be split into lists with length <
     *            2000.
     * @return A {@link Map} of {@link Integer}/ {@link List}< {@link Long}> pairs
     *         which are the split lists from the passed in list. If empty, the list
     *         was under the allowed size.
     */
    protected Map<Integer, List<Long>> createSplitLists(final List<Long> ids) {
        int remaining = ids.size();

        final Map<Integer, List<Long>> splitList = new HashMap<Integer, List<Long>>(5);
        if (remaining > MAX_PARAMETERS) {

            int start = 0;
            int end = MAX_PARAMETERS;
            int count = 1;
            while (remaining > 0) {
                final List<Long> splitIds = ids.subList(start, end);
                splitList.put(count, splitIds);
                if (remaining > MAX_PARAMETERS) {
                    remaining = ids.size() - end;
                    start = end;
                    end = start + (remaining > MAX_PARAMETERS ? MAX_PARAMETERS : remaining);
                } else {
                    start = end + 1;
                    end = end + remaining;
                    remaining = 0;
                }
                count++;
            }
        }

        return splitList;
    }
}
