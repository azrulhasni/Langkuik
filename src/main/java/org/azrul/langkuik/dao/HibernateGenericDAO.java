/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.dao;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import org.azrul.langkuik.annotations.AutoIncrementConfig;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.framework.exception.EntityIsUsedException;
import org.azrul.langkuik.framework.generator.Generator;
import org.azrul.langkuik.framework.relationship.RelationManager;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.envers.Audited;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

/**
 *
 * @author azrulm
 * @param <T>
 */
public class HibernateGenericDAO<T> implements DataAccessObject<T>, Serializable {

    private EntityManagerFactory emf;
    private Class<T> classOfEntity;
//    private static final ThreadLocal<EntityManager> threadLocal;
//
//    static {
//        threadLocal = new ThreadLocal<EntityManager>();
//    }

    public HibernateGenericDAO() {
    }

    public HibernateGenericDAO(EntityManagerFactory emf, Class<T> daoClass) {
        this.emf = emf;
        this.classOfEntity = daoClass;
    }

    @Override
    public Class<T> getType() {
        return classOfEntity;
    }

    public static void massIndexDatabaseForSearch(EntityManagerFactory emf) {
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
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public T find(Object id, String tenantId) {
        T bean = null;
        EntityManager em = emf.createEntityManager();
        bean = em.find(classOfEntity, id);
        if (tenantId != null) {
            Field tenantField = EntityUtils.getTenantFieldName(bean.getClass());
            if (tenantField != null) {
                try {
                    String tenantIdFromBean = (String) tenantField.get(bean);
                    if (tenantIdFromBean != null) {
                        if (!tenantIdFromBean.equals(tenantId)) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        em.close();
        return bean;
    }

    //no persistence
    private T createNew(Class<T> clazz, boolean withId, String tenantId) throws DuplicateDataException {
        EntityManager em = emf.createEntityManager();
        try {
            T bean = clazz.getConstructor().newInstance(new Object[]{});

            //find an Id
            if (withId == true) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getAnnotation(WebField.class) != null) {
                        if (field.getAnnotation(WebField.class).autoIncrement() != null) {
                            AutoIncrementConfig autoIncConf = field.getAnnotation(WebField.class).autoIncrement();

                            Class genClass = autoIncConf.generator();
                            if (genClass != null && genClass.isInterface() == false) {
                                Generator generator = (Generator) genClass.getConstructor().newInstance(new Object[]{});
                                em.getTransaction().begin();
                                generator = em.merge(generator);
                                em.flush();
                                field.setAccessible(true);
                                field.set(bean, generator.getValue());
                                em.getTransaction().commit();
                            }
                        }

                    }
                }
            }

            //set tenant
            if (tenantId != null && !("").equals(tenantId)) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getAnnotation(WebField.class) != null) {
                        if (field.getAnnotation(WebField.class).tenantId() == true) {
                            field.setAccessible(true);
                            field.set(bean, tenantId);
                        }
                    }
                }
            }
            return bean;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            if (ex.getCause() != null) {
                if (ex.getCause().getCause() != null) {
                    if (ex.getCause().getCause().getClass().equals(ConstraintViolationException.class)) {
                        throw new DuplicateDataException();
                    }
                }
            }
        }
        em.close();
        return null;

    }

    //no persistence
    public T createNew(String tenantId) throws DuplicateDataException {
        return createNew(classOfEntity, true, tenantId);
    }

    //no persistence
    public T createNew(boolean giveId, String tenantId) throws DuplicateDataException {
        T bean = createNew(classOfEntity, giveId, tenantId);
        return bean;
    }

    @Override
    public void delete(T entity) throws EntityIsUsedException {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            boolean isIndexed = classOfEntity.isAnnotationPresent(Indexed.class);

            em.remove(refresh(em, entity));
            if (isIndexed) {
                ftem.purge(classOfEntity, (Serializable) EntityUtils.getIdentifierValue(entity, emf));
            }

            em.flush();
            em.getTransaction().commit();
        } catch (ConstraintViolationException cve) {
            em.getTransaction().rollback();
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, cve);
            EntityIsUsedException e = new EntityIsUsedException();
            e.initCause(cve);
            throw e;
        } catch (Exception e) {
            em.getTransaction().rollback();
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
        }
        em.close();
    }

    @Override
    public void delete(Collection<T> entities) throws EntityIsUsedException {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            boolean isIndexed = classOfEntity.isAnnotationPresent(Indexed.class);
            for (T entity : entities) {
                em.remove(em.merge(entity));
                if (isIndexed) {
                    ftem.purge(classOfEntity, (Serializable) EntityUtils.getIdentifierValue(entity, emf));
                }
            }
            em.flush();
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            em.getTransaction().rollback();
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, pe);
            if (ConstraintViolationException.class.equals(pe.getCause().getClass())) {
                EntityIsUsedException e = new EntityIsUsedException();
                e.initCause(pe);
                throw e;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
        }
        em.close();
    }

    @Override
    public T save(T newObject) throws DuplicateDataException {

        EntityManager em = emf.createEntityManager();

        T savedObject = null;
        try {
            em.getTransaction().begin();
            savedObject = em.merge(newObject);
            em.getTransaction().commit();
            return savedObject;

        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
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
//            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
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
//            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
    @Override
    public Object createAndSave(Class c, String tenantId) throws DuplicateDataException {
        EntityManager em = emf.createEntityManager();
        try {
            Object bean = createNew(c, true, tenantId);//call default constructor
            em.getTransaction().begin();
            em.persist(bean);
            em.flush();
//            if (bean.getClass().isAnnotationPresent(Indexed.class)) {
//                FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//                ftem.index(bean);
//            }
            em.getTransaction().commit();
            return bean;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
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
//            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return newObject;
//    }
    @Override
    public <P> Collection runQuery(DAOQuery<P, T> query, String orderBy, boolean asc, int startIndex, int offset, String tenantId) {
        return query.doQuery(emf, orderBy, asc, startIndex, offset, tenantId);
    }

    @Override
    public <P> Long countQueryResult(DAOQuery<P, T> query, String tenantId) {
        return query.count(emf, tenantId);

    }

    public <P> Collection<T> searchResultAlreadyInParent(Collection<T> searchResult, Class<T> daoClass, P parentObject, String parentToChildrenField) {
        try {
            EntityManager em = emf.createEntityManager();

            Session session = (Session) em.getDelegate();
            String currentIdField = session.getSessionFactory().getClassMetadata(daoClass).getIdentifierPropertyName();

            Collection searchResultIds = new ArrayList();
            for (T a : searchResult) {
                Field idField = a.getClass().getDeclaredField(currentIdField);
                idField.setAccessible(true);
                searchResultIds.add(idField.get(a));
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            javax.persistence.criteria.CriteriaQuery<T> criteria = cb.createQuery(daoClass);
            Root parent = criteria.from(parentObject.getClass());
            Join join = parent.join(parentToChildrenField);

            criteria.select(join).where(cb.and(cb.equal(parent, parentObject), join.get(currentIdField).in(searchResultIds)));
            List<T> filteredSearchResult = em.createQuery(criteria).getResultList();
            session.close();

            return filteredSearchResult;
        } catch (IllegalArgumentException |
                SecurityException |
                NoSuchFieldException |
                IllegalAccessException ex) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<T>();
    }

    @Override
    public <P> P associate(FindRelationParameter<P, T> feParam, Collection<T> newBeans) {
        return associate(newBeans, feParam.getParentObject(),
                feParam.getParentToCurrentField(),
                feParam.getRelationManager());

    }

    @Override
    public <P> T saveAndAssociate(T newBean, P parentBean, String parentToNewBeanField, RelationManager<P, T> relationManager) throws DuplicateDataException {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            T newBeanFromDB = em.merge(newBean);

            //P parentBeanFromDB = em.merge(parentBean);
            P parentBeanFromDB = refresh(em, parentBean);

            relationManager.link(parentBeanFromDB, newBeanFromDB, parentToNewBeanField, em);
            em.merge(newBeanFromDB);

            em.flush();
            if (parentBeanFromDB != null && newBeanFromDB != null) {
                ftem.index(parentBeanFromDB);
                ftem.index(newBeanFromDB);
            }

            em.getTransaction().commit();
            return newBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
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
    }

    protected <P> P associate(Collection<T> newBeans, P parentBean, String parentToNewBeanField, RelationManager<P, T> relationManager) {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            List<T> newBeansFromDB = new ArrayList<T>();
            //P parentBeanFromDB = em.merge(parentBean);
            P parentBeanFromDB = refresh(em, parentBean);

            for (T newBean : newBeans) {
                T newBeanFromDB = refresh(em, newBean);
                relationManager.link(parentBeanFromDB, newBeanFromDB, parentToNewBeanField, em);
                em.merge(newBeanFromDB);
                newBeansFromDB.add(newBeanFromDB);
            }
            //em.merge(parentBeanFromDB);
            em.flush();
            ftem.index(parentBeanFromDB);
            for (T newBean : newBeansFromDB) {
                ftem.index(newBean);
            }
            em.getTransaction().commit();
            return parentBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return null;
    }

    private <E> E refresh(EntityManager em, final E entity) {
        Object idValue = EntityUtils.getIdentifierValue(entity, emf);
        if (idValue == null) {
            return null; //cannot refresh something that does exist not in DB
        }
        try {
            E parentBeanFromDB = em.find((Class<E>) entity.getClass(), idValue);
            return parentBeanFromDB;
        } catch (Exception e) {
            //object does not exist
            return null;
        }
    }

    @Override
    public T refresh(T entity) {
        EntityManager em = emf.createEntityManager();
        try {
            T entityFromDB = refresh(em, entity);
            return entityFromDB;
        } finally {
            em.close();
        }
    }

    @Override
    public <P> P associate(FindRelationParameter<P, T> feParam, T newBean, T oldBean) {
        return associate(newBean, oldBean, feParam.getParentObject(), feParam.getParentToCurrentField(), feParam.getRelationManager());

    }

    @Override
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
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
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
    }

    protected <P> P associate(T newBean, T oldBean, P parentBean, String parentToBeanField, RelationManager<P, T> relationManager) {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        try {
            em.getTransaction().begin();
            T oldBeanFromDB = refresh(em, oldBean);
            T newBeanFromDB = refresh(em, newBean);

            P parentBeanFromDB = refresh(em, parentBean);

            if (oldBeanFromDB != null) {
                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToBeanField, em);
            }
            relationManager.link(parentBeanFromDB, newBeanFromDB, parentToBeanField, em);
            em.merge(newBeanFromDB);
            em.merge(parentBeanFromDB);
            if (oldBeanFromDB != null) {
                em.merge(oldBeanFromDB);
            }
            em.flush();
            if (newBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(newBeanFromDB);
            }

            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(parentBeanFromDB);
            }
            if (oldBeanFromDB != null) {
                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                    ftem.index(oldBeanFromDB);
                }
            }
            em.getTransaction().commit();
            return parentBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return null;
    }

    @Override
    public <P> P unlink(FindRelationParameter<P, T> feParam, Collection< T> oldBeans) {
        return unlink(oldBeans,
                feParam.getParentObject(),
                feParam.getParentToCurrentField(),
                feParam.getRelationManager());

    }

//    private <P> P unlinkAndDelete(T oldBean, P parentBean, RelationManager<P, T> relationManager) {
//        EntityManager em = emf.createEntityManager();
//        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
//
//        try {
//            em.getTransaction().begin();
//
//            P parentBeanFromDB = refresh(em, parentBean);
//            T oldBeanFromDB = refresh(em, oldBean);
//            relationManager.unlink(parentBeanFromDB, oldBeanFromDB, em);
//            em.remove(oldBeanFromDB);
//
//            em.merge(parentBeanFromDB);
//
//            em.flush();
//            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.index(parentBeanFromDB);
//            }
//
//            if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
//                ftem.purge(classOfEntity, (Serializable) getIdentifierValue((T) oldBeanFromDB));
//            }
//
//            em.getTransaction().commit();
//
//            return parentBeanFromDB;
//        } catch (Exception e) {
//            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
//            em.getTransaction().rollback();
//        } finally {
//            em.close();
//        }
//        return null;
//    }
    @Override
    public <P> P unlinkAndDelete(FindRelationParameter<P, T> feParam, Collection< T> oldBeans) {
        return (P) unlinkAndDelete(oldBeans,
                feParam.getParentObject(),
                feParam.getParentToCurrentField(),
                feParam.getRelationManager());
    }

    @Override
    public Object unlinkAndDelete(Collection oldBeans, Object parentBean, String parentToNewBeanField, RelationManager relationManager) {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

        try {
            em.getTransaction().begin();
            List oldBeansFromDB = new ArrayList();
            Object parentBeanFromDB = refresh(em, parentBean);
            for (Object oldBean : oldBeans) {
                Object oldBeanFromDB = refresh(em, oldBean);
                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToNewBeanField, em);
                em.remove(oldBeanFromDB);
                oldBeansFromDB.add(oldBeanFromDB);
            }
            em.merge(parentBeanFromDB);

            em.flush();
            if (parentBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                ftem.index(parentBeanFromDB);
            }

            for (Object oldBeanFromDB : oldBeansFromDB) {
                if (oldBeanFromDB.getClass().isAnnotationPresent(Indexed.class)) {
                    ftem.purge(classOfEntity, (Serializable) EntityUtils.getIdentifierValue((T) oldBeanFromDB, emf));
                }
            }
            em.getTransaction().commit();

            return parentBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return null;
    }

    private <P> P unlink(Collection<T> oldBeans, P parentBean, String parentToCurrentField, RelationManager<P, T> relationManager) {
        EntityManager em = emf.createEntityManager();
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);

        try {
            em.getTransaction().begin();
            List<T> oldBeansFromDB = new ArrayList<T>();
            P parentBeanFromDB = refresh(em, parentBean);
            for (T oldBean : oldBeans) {
                T oldBeanFromDB = refresh(em, oldBean);
                relationManager.unlink(parentBeanFromDB, oldBeanFromDB, parentToCurrentField, em);
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

            return parentBeanFromDB;
        } catch (Exception e) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return null;
    }

    public boolean isAuditable(Class aclass) {
        if (aclass.isAnnotationPresent(Audited.class)) {
            return true;
        }
        for (Field field : aclass.getDeclaredFields()) {
            if (field.getAnnotation(Audited.class) != null) {
                return true;
            }
        }
        return false;
    }

}
