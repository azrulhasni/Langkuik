/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import com.azrul.langkuik.framework.dao.DAOQuery;
import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import java.util.Optional;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

/**
 *
 * @author azrulm
 */
public class FindRelationQuery<P, C> implements DAOQuery<P, C>, Serializable {

    //private EntityManagerFactory emf;
    private FindRelationParameter<P, C> parameter;

    public FindRelationQuery(FindRelationParameter<P, C> parameter) {
        this.parameter = parameter;
    }

    @Override
    public Collection doQuery(EntityManager em, Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        //this.emf = emf; 
        return getAllDependants(em, getParameter().getParentObject(),
                getParameter().getParentToCurrentField(), orderBy,  asc.orElse(Boolean.FALSE), startIndex.orElse(0), pageSize);
    }

    @Override
    public Long count(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        //this.emf = emf;
        return countDependants(em, getParameter().getParentObject(),
                getParameter().getParentToCurrentField());
    }

    private Collection<C> getAllDependants(EntityManager em, 
            P parentObject, 
            String parentField, 
            Optional<String> orderBy,
            Boolean asc, 
            Integer startIndex, 
            Optional<Integer> pageSize) {
        //EntityManager em = emf.createEntityManager();
       

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<C> criteria = cb.createQuery(getParameter().getChildClass());
        Root parent = criteria.from(parentObject.getClass());
        Join join = parent.join(parentField);
        Collection<C> results = new ArrayList<>();
//        Collection<Predicate> predicates = new ArrayList<>();

//        if (LangkuikExt.class.isAssignableFrom(searchClass)){
//            predicates.add(cb.not(cb.equal(parent.get("status"), Status.DELETED)));
//        }

         //order by
        orderBy.ifPresentOrElse(o->{
             Order order = null;
            if (asc) {
                order = cb.asc(join.get(o));
            } else {
                order = cb.desc(join.get(o));
            }
            if (LangkuikExt.class.isAssignableFrom(parameter.getChildClass())) {
                criteria.select(join).where(
                        cb.and(
                                cb.equal(parent, parentObject), 
                                cb.not(
                                        cb.or(
                                                cb.equal(join.get("status"), Status.DELETED),
                                                cb.equal(join.get("status"), Status.PREDRAFT)
                                        )
                                )
                        )
                ).orderBy(order);
                
             
            }else{
                criteria.select(join).where(cb.equal(parent, parentObject)).orderBy(order);
            }
        }, ()->{
            if (LangkuikExt.class.isAssignableFrom(parameter.getChildClass())) {
                criteria.select(join).where(
                        cb.and(
                                cb.equal(parent, parentObject), 
                                cb.not(
                                        cb.or(
                                                cb.equal(join.get("status"), Status.DELETED),
                                                cb.equal(join.get("status"), Status.PREDRAFT)
                                        )
                                )
                        )
                );
            }else{
                criteria.select(join).where(cb.equal(parent, parentObject));
            }
        });


        //do query
        results = pageSize.map(o->{
            return  em.createQuery(criteria).setFirstResult(startIndex).setMaxResults(o).getResultList();
        }).orElseGet(()->{
            return  em.createQuery(criteria).getResultList();
        });
        em.close();
        return results;
    }

    private Long countDependants(EntityManager em, P parentObject, String parentField) {
        //EntityManager em = emf.createEntityManager();
        Long count = null;
        try {
//            em.getTransaction().begin();
//            CriteriaBuilder builder = em.getCriteriaBuilder();
//            javax.persistence.criteria.CriteriaQuery<T> criteria = builder.createQuery(daoClass);
//            Root parent = criteria.from(parentObject.getClass());
//            criteria.select(parent.get(parentField)).where(parent.in(parentObject));
//            T result = em.createQuery(criteria).setFirstResult(1).setMaxResults(1).getSingleResult();
//            if (result != null) {

            //Handle 1 to 1 relationship
            if (!(FieldUtils.getField(parentObject.getClass(), parentField).getGenericType() instanceof ParameterizedType)) {
                Class classOfField = FieldUtils.getField(parentObject.getClass(), parentField).getType();
                String childId = EntityUtils.getIdentifierFieldName(classOfField, em);

                CriteriaBuilder cb = em.getCriteriaBuilder();
                javax.persistence.criteria.CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
                Root parent = criteria.from(parentObject.getClass());
                Path countJoin = parent.join(parentField);
                if (LangkuikExt.class.isAssignableFrom(parameter.getChildClass())) {
                     criteria.select(
                             cb.count(
                                     countJoin.get(childId))).where(
                                             cb.and(
                                                     cb.equal(parent, parentObject),
                                                     cb.not(
                                                             cb.or(
                                                                     cb.equal(countJoin.get("status"), Status.DELETED),
                                                                     cb.equal(countJoin.get("status"), Status.PREDRAFT)
                                                             )
                                                     )
                                             )
                                     );
                }else{
                     criteria.select(cb.count(countJoin.get(childId))).where(cb.equal(parent, parentObject));
                }
                count = em.createQuery(criteria).getSingleResult();
                em.close();
                if (count != null) {
                    return count;
                } else {
                    return 0L;
                }

            } else {
                Class classOfField = (Class) ((ParameterizedType) FieldUtils.getField(parentObject.getClass(), parentField).getGenericType()).getActualTypeArguments()[0];

                String childId = EntityUtils.getIdentifierFieldName(classOfField, em);

                CriteriaBuilder cb = em.getCriteriaBuilder();
                javax.persistence.criteria.CriteriaQuery<Long> countCriteria = cb.createQuery(Long.class);
                Root parentForCounting = countCriteria.from(parentObject.getClass());
                Join countJoin = parentForCounting.join(parentField);
                if (LangkuikExt.class.isAssignableFrom(parameter.getChildClass())) {
                    countCriteria.select(
                            cb.count(
                                    countJoin.get(childId))).where(
                                            cb.and(
                                                    parentForCounting.in(parentObject),
                                                    cb.not(
                                                           cb.or(
                                                                     cb.equal(countJoin.get("status"), Status.DELETED),
                                                                     cb.equal(countJoin.get("status"), Status.PREDRAFT)
                                                             )
                                                    )
                                            )
                                    );
                }else{
                    countCriteria.select(cb.count(countJoin.get(childId))).where(parentForCounting.in(parentObject));
                }

                count = em.createQuery(countCriteria).getSingleResult();
                em.close();
                if (count != null) {
                    return count;
                } else {
                    return 0L;
                }
            }
        } catch (Exception ex) {
            em.close();
            Logger.getLogger(FindRelationQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    /**
     * @return the parameter
     */
    public FindRelationParameter<P, C> getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(FindRelationParameter<P, C> parameter) {
        this.parameter = parameter;
    }

}
