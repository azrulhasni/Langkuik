/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import org.azrul.langkuik.dao.DAOQuery;
import org.azrul.langkuik.framework.relationship.RelationManager;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;

/**
 *
 * @author azrulm
 */
public class FindRelationQuery<P, C> implements DAOQuery<P, C>, Serializable {

  
    private EntityManagerFactory emf;
    private FindRelationParameter<P,C> parameter;
    
    

    public FindRelationQuery(FindRelationParameter<P,C> parameter) {
        this.parameter = parameter;
    }
    
     @Override
    public Collection doQuery(EntityManagerFactory emf,String orderBy, boolean asc, int startIndex, int offset, String tenantId) {
        this.emf = emf; 
        return getAllDependants(getParameter().getParentObject()
                 ,getParameter().getParentToCurrentField(), orderBy, asc, startIndex, offset);
     }

    @Override
    public Long count(EntityManagerFactory emf, String tenantId) {
        this.emf = emf;
        return countDependants(getParameter().getParentObject()
                 ,getParameter().getParentToCurrentField());
    }

  
  

    private Collection<C> getAllDependants(P parentObject, String parentField, String orderBy, boolean asc, int startIndex, int offset) {
        EntityManager em = emf.createEntityManager();
        Order order = null;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<C> criteria = cb.createQuery(getParameter().getChildClass());
        Root parent = criteria.from(parentObject.getClass());
        Join join = parent.join(parentField);
        Collection<C> results = new ArrayList<>();

        //order by
        if (orderBy == null) {
            if (EntityUtils.isManagedEntity(getParameter().getChildClass(), emf)) {
                orderBy = EntityUtils.getIdentifierFieldName(getParameter().getChildClass(), emf);
            }
        }
        if (orderBy != null) {
            if (asc) {
                order = cb.asc(join.get(orderBy));
            } else {
                order = cb.desc(join.get(orderBy));
            }
        }

        //join
        if (orderBy == null) { //orderBy is still null
            criteria.select(join).where(cb.equal(parent, parentObject));
        } else {
            criteria.select(join).where(cb.equal(parent, parentObject)).orderBy(order);
        }

        //do query
        results = em.createQuery(criteria).setFirstResult(startIndex).setMaxResults(offset).getResultList();

        em.close();
        return results;
    }

    private Long countDependants(P parentObject, String parentField) {
        EntityManager em = emf.createEntityManager();
        Long count = null;
        try {
//            em.getTransaction().begin();
//            CriteriaBuilder builder = em.getCriteriaBuilder();
//            javax.persistence.criteria.CriteriaQuery<T> criteria = builder.createQuery(daoClass);
//            Root parent = criteria.from(parentObject.getClass());
//            criteria.select(parent.get(parentField)).where(parent.in(parentObject));
//            T result = em.createQuery(criteria).setFirstResult(1).setMaxResults(1).getSingleResult();
//            if (result != null) {
            if (!(parentObject.getClass().getDeclaredField(parentField).getGenericType() instanceof ParameterizedType)) {
                return 1L;
            }
            Class classOfField = (Class) ((ParameterizedType) parentObject.getClass().getDeclaredField(parentField).getGenericType()).getActualTypeArguments()[0];

            ClassMetadata classMetadata = ((Session) em.getDelegate()).getSessionFactory().getClassMetadata(classOfField);
            String childId = classMetadata.getIdentifierPropertyName();

            CriteriaBuilder countBuilder = em.getCriteriaBuilder();
            javax.persistence.criteria.CriteriaQuery<Long> countCriteria = countBuilder.createQuery(Long.class);
            Root parentForCounting = countCriteria.from(parentObject.getClass());
            countCriteria.select(countBuilder.count(parentForCounting.join(parentField).get(childId))).where(parentForCounting.in(parentObject));

            count= em.createQuery(countCriteria).getSingleResult();
            em.close();
            if (count!=null){
                return count;
            }else{
                return 0L;
            }
           
        } catch (Exception ex) {
            em.close();
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    /**
     * @return the parameter
     */
    public FindRelationParameter<P,C> getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(FindRelationParameter<P,C> parameter) {
        this.parameter = parameter;
    }

   

   

}
