/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
//import com.azrul.langkuik.framework.exception.DuplicateDataException;
import com.azrul.langkuik.framework.exception.EntityIsUsedException;
//import com.azrul.langkuik.framework.generator.Generator;
import com.azrul.langkuik.framework.relationship.RelationManager;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.relationship.DefaultRelationManagerFactory;
import com.azrul.langkuik.framework.relationship.RelationManagerFactory;
import com.azrul.langkuik.framework.relationship.RelationUtils;
import com.azrul.langkuik.framework.standard.IdGenerator;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import com.vaadin.flow.server.InputStreamFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.Transactional;
import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.envers.Audited;
import org.hibernate.exception.ConstraintViolationException;
//import org.hibernate.Session;
//import org.hibernate.envers.Audited;
//import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Repository;

/**
 *
 * @author azrulm
 * @param <T>
 * 
 */
@Repository
@Transactional
public class DataAccessObjectImpl<T> implements DataAccessObject<T>, Serializable {

    //private EntityManagerFactory emf;
    @PersistenceContext
    EntityManager em;

    @PersistenceUnit()
    EntityManagerFactory emf;

    private RelationManagerFactory relationMgrFactory = new DefaultRelationManagerFactory();

    private Class<T> classOfEntity;
//    private static final ThreadLocal<EntityManager> threadLocal;
//
//    static {
//        threadLocal = new ThreadLocal<EntityManager>();
//    }

    public DataAccessObjectImpl() {
    }

    public DataAccessObjectImpl(Class<T> daoClass) {
        //this.emf = emf;
        this.classOfEntity = daoClass;
    }

    @Override
    public Class<T> getType() {
        return getClassOfEntity();
    }

    /*public static void massIndexDatabaseForSearch(EntityManagerFactory emf) {
        try {
            EntityManager em = emf.createEntityManager();
            List<Class> classes = new ArrayList<>();
            for (EntityType e : emf.getMetamodel().getEntities()) {
                Class c = e.getJavaType();
                if (c == null) {
                    continue;
                }
                if (c.isAnnotationPresent(Index.class)) {
                    classes.add(c);
                }
            }

            FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
            fullTextEntityManager.createIndexer(classes.toArray(new Class[]{}))
                    .batchSizeToLoadObjects(30)
                    .optimizeAfterPurge(true)
                    .optimizeOnFinish(true)
                    .threadsToLoadObjects(4)
                    .cacheMode(CacheMode.NORMAL) // defaults to CacheMode.IGNORE
                    .startAndWait();
        } catch (InterruptedException ex) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
//    public Optional<T> find(Object id, Optional<String> tenantId) {
//        T bean = null;
//        
//        EntityManager em = emf.createEntityManager();
//        bean = em.find(getClassOfEntity(), id);
//        if (tenantId != null) {
//            Field tenantField = EntityUtils.getTenantFieldName(bean.getClass());
//            if (tenantField != null) {
//                try {
//                    String tenantIdFromBean = (String) tenantField.get(bean);
//                    if (tenantIdFromBean != null) {
//                        if (!tenantIdFromBean.equals(tenantId)) {
//                            return ;
//                        }
//                    } else {
//                        return null;
//                    }
//                } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IllegalAccessException ex) {
//                    Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//        }
//        em.close();
//        return bean;
//    }
    //non persistent
    @Override
    public Optional<T> create(Class<T> c, Optional<String> tenantId, String creatorId, Optional<String> parentTranxId) {
        EntityManager em = emf.createEntityManager(); //Still need entity manager for ID
        try {
            em.getTransaction().begin();
            Optional<T> bean = createNew(c, true, creatorId, tenantId, parentTranxId, em);//call default constructor
            em.getTransaction().commit();
            return bean;
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();

        }
        return Optional.empty();
    }

    //no persistence
    private Optional<T> createNew(Class<T> clazz, boolean withId, String creatorId, Optional<String> tenantId, Optional<String> parentTranxId, EntityManager em) {
        //EntityManager em = emf.createEntityManager();
        try {
            final T bean = EntityUtils.createNewObject(clazz);
            LangkuikExt ext = (LangkuikExt) bean;
            ext.setCreatorId(creatorId);
            ext.setOwnerId(creatorId);
            ext.setTenantId(tenantId.orElse(null));
            ext.setStatus(Status.DRAFT);
            T beanWithTransxId = parentTranxId.map(t -> EntityUtils.setTranxId(bean, t))
                    .orElseGet(() -> EntityUtils.setTranxId(bean));

            //find an Id
            if (withId == true) {
                for (Field field : FieldUtils.getAllFields(clazz).values()) {
                    if (field.getAnnotation(Id.class) != null) {
                        IdGenerator generator = new IdGenerator();
                        generator = em.merge(generator);
                        em.flush();
                        field.setAccessible(true);
                        field.set(beanWithTransxId, generator.getValue());
                        break;
                    }
                }
            }

            return Optional.of(beanWithTransxId);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);

//            if (ex.getCause() != null) {
//                if (ex.getCause().getCause() != null) {
//                    if (ex.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
//                        throw new DuplicateDataException();
//                    }
//                }
//            }
        }
        return Optional.empty();

    }

    /*//no persistence
    public T createNew(String tenantId) throws DuplicateDataException {
        return createNew(getClassOfEntity(), true, tenantId);
    }

    //no persistence
    public T createNew(boolean giveId, String tenantId) throws DuplicateDataException {
        T bean = createNew(getClassOfEntity(), giveId, tenantId);
        return bean;
    }*/
    public void update(Class<T> tclass, String updatedField, String updateValue, String whereField, String whereValue) {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();

            em.getTransaction().commit();
        } catch (ConstraintViolationException cve) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, cve);
            EntityIsUsedException e = new EntityIsUsedException();
            e.initCause(cve);
        } catch (Exception e) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        em.close();
    }

    @Override
    public void delete(T entity, Rule1Input canDelete) throws EntityIsUsedException {
        if (canDelete.runPredicate(entity) == false) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            boolean isIndexed = entity.getClass().isAnnotationPresent(Indexed.class);

            em.remove(refresh(em, entity).orElseThrow());
            if (isIndexed) {
                //ftem.purge(classOfEntity, (Serializable) EntityUtils.getIdentifierValue(entity, emf));
                ftem.purge(entity.getClass(), (Serializable) EntityUtils.getIdentifierValue(entity, em));
            }

            em.flush();
            em.getTransaction().commit();
        } catch (ConstraintViolationException cve) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, cve);
            EntityIsUsedException e = new EntityIsUsedException();
            e.initCause(cve);
            throw e;
        } catch (Exception e) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        em.close();
    }

    @Override
    public void delete(Collection<T> entities, Rule1Input canDelete) throws EntityIsUsedException {
        boolean canDeleteFlag = true;
        for (T ent : entities) {
            if (canDelete.runPredicate(ent) == false) {
                canDeleteFlag = canDeleteFlag && false;
            }
        }
        if (canDeleteFlag == false) {
            return;
        }
        if (entities.isEmpty()) {
            return;
        }
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();

            for (T entity : entities) {
                LangkuikExt e = (LangkuikExt) entity;
                e.setStatus(Status.DELETED);
                save(em, e);
            }
            em.flush();
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, pe);
            if (ConstraintViolationException.class.equals(pe.getCause().getClass())) {
                EntityIsUsedException e = new EntityIsUsedException();
                e.initCause(pe);
                throw e;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        em.close();
    }

    @Override
    public Optional<T> save(T newObject) {

        EntityManager em = emf.createEntityManager();
        T savedObject = null;
        try {
            em.getTransaction().begin();
            savedObject = save(em, newObject);
            em.getTransaction().commit();
            return Optional.of(savedObject);

        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

//            if (e.getCause() != null) {
//                if (e.getCause().getCause() != null) {
//                    if (e.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
//                        throw new DuplicateDataException();
//                    }
//                }
//            }
        } finally {

            em.close();
        }
        return Optional.empty();
    }

//    @Override
//    public T save(T newObject) {
//        EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//
//        T savedObject = null;
//        try {
//            em.getTransaction().begin();
//            savedObject = em.merge(newObject);
//            em.flush();
//            if (savedObject.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(savedObject);
//                ftem.flushToIndexes();
//            }
//            em.getTransaction().commit();
//            return savedObject;
//        } catch (Exception e) {
//            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//
//            em.close();
//        }
//        return null;
//    }
//    @Override
//    public <P> T createAndSave(DaoParameter<P, T> parameter) {
//        if (parameter instanceof FindRelationParameter) {
//            FindRelationParameter<P, T> feParam = (FindRelationParameter<P, T>) parameter;
//            return createAndSave(feParam.getParentObject(), feParam.getParentToCurrentField(), feParam.getRelationManager());
//        } else {
//            return createAndSave();
//        }
//    }
//
//    @Override
//    public <P> T createAndSave(P parentBean, String parentToCurrentField, RelationManager<P, T> relationManager) {
//        T newBean = createNew();
//        EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//        try {
//
//            em.getTransaction().begin();
//            
//            T newBeanFromDB = em.merge(newBean);
//            P parentBeanFromDB = refresh(em, parentBean);
//
//            if (relationManager != null) {
//                relationManager.link(parentBeanFromDB, newBeanFromDB, parentToCurrentField, em);
//            }
//            em.merge(parentBeanFromDB);
//            em.merge(newBeanFromDB);
//            em.flush(); // must flush before index
//            if (newBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(newBeanFromDB);
//            }
//            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                if (relationManager != null) {
//                    ftem.index(parentBeanFromDB);
//                }
//            }
//            em.getTransaction().commit();
//            return newBeanFromDB;
//        } catch (Exception e) {
//            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
    @Override
    public Optional<T> createAndSave(Class<T> c, Optional<String> tenantId, String creatorId, Optional<String> parentTranxId) {
        EntityManager em = emf.createEntityManager();
        try {

            em.getTransaction().begin();
            Optional<T> obean = createNew(c, true, creatorId, tenantId, parentTranxId, em);//call default constructor

            T bean = saveNew(em, obean.orElseThrow());
            em.getTransaction().commit();
            return Optional.of(bean);
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

        } finally {
            em.close();

        }
        return Optional.empty();
    }

    private <Z> Z saveNew(EntityManager em1, Z bean) {
        em1.persist(bean);
        em1.flush();
        if (bean.getClass().isAnnotationPresent(Indexed.class)) {
            FullTextEntityManager ftem = Search.getFullTextEntityManager(em1);
            ftem.index(bean);
        }
        return bean;
    }

    private <Z> Z save(EntityManager em1, Z bean) {
        bean = em1.merge(bean);
        em1.flush();
        if (bean.getClass().isAnnotationPresent(Indexed.class)) {
            FullTextEntityManager ftem = Search.getFullTextEntityManager(em1);
            ftem.index(bean);
        }
        return bean;
    }

//    @Override
//    public T createAndSave() {
//        T newObject = createNew();
//        EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//        try {
//            em.getTransaction().begin();
//            em.persist(newObject);
//            em.flush();
//            if (newObject.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(newObject);
//            }
//            em.getTransaction().commit();
//        } catch (Exception e) {
//            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return newObject;
//    }
    @Override
    public <P> Collection<T> runQuery(DAOQuery<P, T> query,
            Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> offset, Optional<String> tenantId, Optional<String> worklist) {
        return query.doQuery(em, orderBy, asc, startIndex, offset, tenantId, worklist);
    }

    @Override
    public <P> Long countQueryResult(DAOQuery<P, T> query, Optional<String> tenantId, Optional<String> worklist) {
        return query.count(em, tenantId, worklist);

    }

    public <P> Collection<T> searchResultAlreadyInParent(Collection<T> searchResult, Class<T> daoClass, P parentObject, String parentToChildrenField) {
        try {
            EntityManager em = emf.createEntityManager();

            String currentIdField = EntityUtils.getIdentifierFieldName(daoClass, em);

            Collection searchResultIds = new ArrayList();
            for (T a : searchResult) {
                Field idField = FieldUtils.getField(a.getClass(), currentIdField);
                idField.setAccessible(true);
                searchResultIds.add(idField.get(a));
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            javax.persistence.criteria.CriteriaQuery<T> criteria = cb.createQuery(daoClass);
            Root parent = criteria.from(parentObject.getClass());
            Join join = parent.join(parentToChildrenField);

            criteria.select(join).where(cb.and(cb.equal(parent, parentObject), join.get(currentIdField).in(searchResultIds)));
            List<T> filteredSearchResult = em.createQuery(criteria).getResultList();

            return filteredSearchResult;
        } catch (IllegalArgumentException
                | SecurityException
                | IllegalAccessException ex) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

//    @Override
//    public <P> P associate(FindRelationParameter<P, T> feParam, Collection<T> newBeans) {
//        return associate(newBeans, feParam.getParentObject(),
//                feParam.getParentToCurrentField(),
//                feParam.getRelationManager());
//
//    }
    @Override
    public <P> Set<Dual<P, T>> saveAndAssociate(Set<T> newBeans, P parentBean, String parentToNewBeanField) {
        Class<T> childClass = RelationUtils.getRelationClass((Class<P>) parentBean.getClass(), parentToNewBeanField);
        RelationManager<P, T> relationManager = relationMgrFactory.create((Class<P>) parentBean.getClass(), childClass);
        EntityManager em = emf.createEntityManager();
        Set<Dual<P, T>> results = new HashSet<>();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            for (T bean : newBeans) {
                Dual<P, T> newBeansFromDB = linkOneObject(em, bean, parentBean, relationManager, parentToNewBeanField, ftem);
                results.add(newBeansFromDB);
            }
            em.getTransaction().commit();
            return results;
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

//            if (e.getCause() != null) {
//                if (e.getCause().getCause() != null) {
//                    if (e.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
//                        throw new DuplicateDataException();
//                    }
//                }
//            }
        } finally {
            em.close();
        }
        return new HashSet();
    }

    @Override
    public <P> Optional<Dual<P, T>> createAssociateAndSave(Class<T> childClass,
            P parentBean,
            String parentToNewBeanField,
            Optional<String> tenantId,
            String creatorId) {

        RelationManager<P, T> relationManager = relationMgrFactory.create((Class<P>) parentBean.getClass(), childClass);

        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            String parentTranxId = EntityUtils.getTranxId(parentBean);
            em.getTransaction().begin();
            Optional<T> newBean = createNew(childClass, true, creatorId, tenantId, Optional.of(parentTranxId), em);
            //T newBeanFromDB = save(em, newBean);
            Dual<P, T> newBeansFromDB = linkOneObject(em, newBean.orElseThrow(), parentBean, relationManager, parentToNewBeanField, ftem);
            //save(em,newBeansFromDB.getFirst());
            em.getTransaction().commit();
            return Optional.of(newBeansFromDB);
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

//            if (e.getCause() != null) {
//                if (e.getCause().getCause() != null) {
//                    if (e.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
//                        throw new DuplicateDataException();
//                    }
//                }
//            }
        } finally {
            em.close();
        }
        return Optional.empty();
    }

    @Override
    public <P> Optional<Dual<P, T>> saveAndAssociate(T newBean, P parentBean, String parentToNewBeanField) {
        Class<T> childClass = (Class<T>) newBean.getClass();
        RelationManager<P, T> relationManager = relationMgrFactory.create((Class<P>) parentBean.getClass(), childClass);

        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            Dual<P, T> newBeansFromDB = linkOneObject(em, newBean, parentBean, relationManager, parentToNewBeanField, ftem);

            em.getTransaction().commit();
            return Optional.of(newBeansFromDB);
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

//            if (e.getCause() != null) {
//                if (e.getCause().getCause() != null) {
//                    if (e.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
//                        throw new DuplicateDataException();
//                    }
//                }
//            }
        } finally {
            em.close();
        }
        return Optional.empty();
    }

    private <P> Dual<P, T> linkOneObject(EntityManager em1, T newBean, P parentBean, RelationManager<P, T> relationManager, String parentToNewBeanField, FullTextEntityManager ftem) {
        T newBeanFromDB = em1.merge(newBean);
        //P parentBeanFromDB = em1.merge(parentBean);
        P parentBeanFromDB = refresh(em1, parentBean).orElseThrow();
        relationManager.link(parentBeanFromDB, newBeanFromDB, parentToNewBeanField);
        //parentBeanFromDB = save(em1, parentBeanFromDB);
        newBeanFromDB = save(em1, newBeanFromDB);
        em1.flush();

        return new Dual(parentBeanFromDB, newBeanFromDB);
    }

//    protected <P> P associate(Collection<T> newBeans, P parentBean, String parentToNewBeanField, RelationManager<P, T> relationManager) {
//        //EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//        try {
//            em.getTransaction().begin();
//            List<T> newBeansFromDB = new ArrayList<T>();
//            //P parentBeanFromDB = em.merge(parentBean);
//            P parentBeanFromDB = refresh(em, parentBean);
//
//            for (T newBean : newBeans) {
//                T newBeanFromDB = refresh(em, newBean);
//                relationManager.link(parentBeanFromDB, newBeanFromDB, parentToNewBeanField);
//                em.merge(newBeanFromDB);
//                newBeansFromDB.add(newBeanFromDB);
//            }
//            //em.merge(parentBeanFromDB);
//            em.flush();
//            ftem.index(parentBeanFromDB);
//            for (T newBean : newBeansFromDB) {
//                ftem.index(newBean);
//            }
//            em.getTransaction().commit();
//            return parentBeanFromDB;
//        } catch (Exception e) {
//            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
    private <E> Optional<E> refresh(EntityManager em, final E entity) {
        Object idValue = EntityUtils.getIdentifierValue(entity, em);
        if (idValue == null) {
            return Optional.empty(); //cannot refresh something that does exist not in DB
        }
        try {
            E parentBeanFromDB = em.find((Class<E>) entity.getClass(), idValue);
            return Optional.of(parentBeanFromDB);
        } catch (Exception e) {
            //object does not exist
            return Optional.empty();
        }
    }

    @Override
    public T refresh(T entity) {
        //EntityManager em = emf.createEntityManager();
        try {
            T entityFromDB = refresh(em, entity).orElseThrow();
            return entityFromDB;
        } finally {
            em.close();
        }
    }

//    @Override
//    public <P> P associate(FindRelationParameter<P, T> feParam, T newBean, T oldBean) {
//        return associate(newBean, oldBean, feParam.getParentObject(), feParam.getParentToCurrentField(), feParam.getRelationManager());
//
//    }

    /* @Override
    public Object saveWithRelation(Object newBean, Object parentBean, String parentToNewBeanField, RelationManager relationManager) throws DuplicateDataException {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            Object newBeanFromDB = em.merge(newBean);
            //Object newBeanFromDB = refresh(em, newBean);
            Object parentBeanFromDB = refresh(em, parentBean);

            relationManager.link(parentBeanFromDB, newBeanFromDB, parentToNewBeanField, em);
            em.merge(newBeanFromDB);
            em.merge(parentBeanFromDB);
            em.flush();
            if (newBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(newBeanFromDB);
            }
            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(parentBeanFromDB);
            }
            em.getTransaction().commit();
            return parentBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            if (e.getCause() != null) {
                if (e.getCause().getCause() != null) {
                    if (e.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
                        throw new DuplicateDataException();
                    }
                }
            }
        } finally {
            em.close();
        }
        return null;
    }*/
//    protected <P> P associate(T newBean, T oldBean, P parentBean, String parentToBeanField, RelationManager<P, T> relationManager) {
//        //EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//        try {
//            em.getTransaction().begin();
//            T oldBeanFromDB = refresh(em, oldBean);
//            T newBeanFromDB = refresh(em, newBean);
//
//            P parentBeanFromDB = refresh(em, parentBean);
//
//            if (oldBeanFromDB != null) {
//                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToBeanField);
//            }
//            relationManager.link(parentBeanFromDB, newBeanFromDB, parentToBeanField);
//            em.merge(newBeanFromDB);
//            em.merge(parentBeanFromDB);
//            if (oldBeanFromDB != null) {
//                em.merge(oldBeanFromDB);
//            }
//            em.flush();
//            if (newBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(newBeanFromDB);
//            }
//
//            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(parentBeanFromDB);
//            }
//            if (oldBeanFromDB != null) {
//                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                    ftem.index(oldBeanFromDB);
//                }
//            }
//            em.getTransaction().commit();
//            return parentBeanFromDB;
//        } catch (Exception e) {
//            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
    @Override
    public <P> Optional<P> unlink(FindRelationParameter<P, T> feParam, Collection< T> oldBeans, Rule3Inputs canUnlink) {

        return unlink(oldBeans,
                feParam.getParentObject(),
                feParam.getParentToCurrentField(), canUnlink);

    }

    @Override
    public <P> Optional<P> unlinkAndDelete(FindRelationParameter<P, T> feParam, Collection< T> oldBeans, Rule3Inputs canUnlinkDelete) {
        return (Optional<P>) unlinkAndDelete(oldBeans,
                feParam.getParentObject(),
                feParam.getParentToCurrentField(), canUnlinkDelete);
    }

    @Override
    public Optional<Object> unlinkAndDelete(Collection oldBeans, Object parentBean, String parentToNewBeanField, Rule3Inputs canUnlinkDelete) {
        boolean canDeleteUnlinkFlag = true;
        for (Object oldBean : oldBeans) {
            if (canUnlinkDelete.runPredicate(parentBean, parentToNewBeanField, oldBean) == false) {
                canDeleteUnlinkFlag = canDeleteUnlinkFlag && false;
            }
        }
        if (canDeleteUnlinkFlag == false) {
            return Optional.empty();
        }

        EntityManager em = emf.createEntityManager();
        Class<T> childClass = RelationUtils.getRelationClass(parentBean.getClass(), parentToNewBeanField);
        RelationManager relationManager = relationMgrFactory.create(parentBean.getClass(), childClass);

        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

        try {
            em.getTransaction().begin();
            List oldBeansFromDB = new ArrayList();
            Object parentBeanFromDB = refresh(em, parentBean).orElseThrow();
            for (Object oldBean : oldBeans) {
                Object oldBeanFromDB = refresh(em, oldBean).orElseThrow();
                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToNewBeanField);
                oldBeansFromDB.add(oldBeanFromDB);
                LangkuikExt ext = (LangkuikExt) oldBeanFromDB;
                ext.setStatus(Status.DELETED);
                em.merge(oldBeanFromDB);
            }
            em.merge(parentBeanFromDB);

            em.flush();

            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(parentBeanFromDB);
            }

            for (Object oldBeanFromDB : oldBeansFromDB) {
                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                    ftem.index(oldBeanFromDB);
                }
            }

//            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(parentBeanFromDB);
//            }
//
//            for (Object oldBeanFromDB : oldBeansFromDB) {
//                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                    ftem.purge(getClassOfEntity(), (Serializable) EntityUtils.getIdentifierValue((T) oldBeanFromDB, em));
//                }
//            }
            em.getTransaction().commit();

            return Optional.of(parentBeanFromDB);
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return Optional.empty();
    }

    private <P> Optional<P> unlink(Collection<T> oldBeans, P parentBean, String parentToCurrentField, Rule3Inputs canUnlink) {
        boolean canUnlinkFlag = true;
        for (Object oldBean : oldBeans) {
            if (canUnlink.runPredicate(parentBean, parentToCurrentField, oldBean) == false) {
                canUnlinkFlag = canUnlinkFlag && false;
            }
        }
        if (canUnlinkFlag == false) {
            return Optional.empty();
        }
        Class<T> childClass = RelationUtils.getRelationClass(parentBean.getClass(), parentToCurrentField);
        RelationManager relationManager = relationMgrFactory.create(parentBean.getClass(), childClass);

        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

        try {
            em.getTransaction().begin();
            List<T> oldBeansFromDB = new ArrayList<T>();
            P parentBeanFromDB = refresh(em, parentBean).orElseThrow();
            for (T oldBean : oldBeans) {
                T oldBeanFromDB = refresh(em, oldBean).orElseThrow();
                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToCurrentField);
                em.merge(oldBeanFromDB);
                oldBeansFromDB.add(oldBeanFromDB);
            }
            em.merge(parentBeanFromDB);

            em.flush();
            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(parentBeanFromDB);
            }

            for (T oldBeanFromDB : oldBeansFromDB) {
                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                    ftem.index(oldBeanFromDB);
                }
            }

            em.getTransaction().commit();

            return Optional.of(parentBeanFromDB);
        } catch (Exception e) {
            Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return Optional.empty();
    }

    public boolean isAuditable(Class aclass) {
        if (aclass.isAnnotationPresent(Audited.class)) {
            return true;
        }
        for (Field field : FieldUtils.getAllFields(aclass).values()) {
            if (field.getAnnotation(Audited.class) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the classOfEntity
     */
    public Class<T> getClassOfEntity() {
        return classOfEntity;
    }

    /**
     * @param classOfEntity the classOfEntity to set
     */
    public void setClassOfEntity(Class<T> classOfEntity) {
        this.classOfEntity = classOfEntity;
    }

    public void massIndex() {
        EntityManager em = emf.createEntityManager();//we need a fresh EM because we are calling this from StreamResource

        Metamodel mm = emf.getMetamodel();

// loop these, using getJavaType() from Type sub-interface to get 
// Class tokens for managed classes.
//        Set<ManagedType<?>> managedTypes = mm.getManagedTypes();
        FullTextEntityManager txtentityManager = Search.getFullTextEntityManager(em);
//        try{
//        for (ManagedType<?> mt:managedTypes){
//            Class<?> tclass = mt.getJavaType();
//            if (tclass.isAnnotationPresent(Indexed.class)){
//                List<?> results = em
//                          .createQuery("Select a from "+tclass.getName()+" a", tclass)
//                          .getResultList();
//                for (Object r:results){
//                    txtentityManager.index(r);
//                }
//            }
//        }
//        }finally{
//            txtentityManager.flushToIndexes();
//        }

        MassIndexer massIndexer = txtentityManager.createIndexer();
        massIndexer.purgeAllOnStart(true);
        try {
            massIndexer.startAndWait();
            // txtentityManager.
        } catch (InterruptedException e) {
            Logger.getLogger("mass reindexing interrupted: " + e.getMessage());
        } finally {
            txtentityManager.flushToIndexes();
            txtentityManager.close();
        }
    }

    //@Transactional
    @Override
    public InputStreamFactory getInputStreamFactory(BlobContainer b) {
        InputStreamFactory isf = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() {
                EntityManager em = emf.createEntityManager();//we need a fresh EM because we are calling this from StreamResource
                try {
                    em.getTransaction().begin();
                    BlobContainer o = refresh(em, b).orElseThrow();
                    byte[] bas = IOUtils.toByteArray(o.getBlobData().getBinaryStream());
                    em.getTransaction().commit();
                    return new ByteArrayInputStream(bas);
                } catch (Exception ex) {
                    em.getTransaction().rollback();
                    Logger.getLogger(DataAccessObjectImpl.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    em.close();
                }

                return null;
            }
        };
        return isf;
    }

    public <P> Long countQueryResult(DAOQuery<P, T> query) {
        return countQueryResult(query, Optional.empty(), Optional.empty());
    }

    public <P> Collection<T> runQuery(DAOQuery<P, T> query) {
        return runQuery(query, Optional.empty(), Optional.of(Boolean.FALSE), Optional.of(0), Optional.empty(),
                 Optional.empty(), Optional.empty());
    }

}
