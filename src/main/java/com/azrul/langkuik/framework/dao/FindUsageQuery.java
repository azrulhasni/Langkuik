/*
 * Copyright 2014 azrulm.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azrul.langkuik.framework.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import com.azrul.langkuik.framework.relationship.RelationUtils;
import com.azrul.langkuik.framework.standard.Status;
import java.util.Optional;

/**
 *
 * @author azrulm
 * @param <T>
 */
public class FindUsageQuery<P, C> implements DAOQuery<P, C>, Serializable {

    private C currentBean;
    private Class<P> parentClass;
    private String parentCurrentRelation;
    //private EntityManagerFactory emf;

    public FindUsageQuery(C currentBean, String parentCurrentRelation, Class<P> parentClass) {
        //this.emf = emf;
        this.currentBean = currentBean;
        this.parentClass = parentClass;
        this.parentCurrentRelation = parentCurrentRelation;
    }

    @Override
    public Collection doQuery(EntityManager em, Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        //this.emf = emf;
        return findUsage(em, getCurrentBean(), getParentCurrentRelation(), getParentClass(), orderBy, asc.orElse(Boolean.FALSE), startIndex.orElse(0), pageSize);
    }

    @Override
    public Long count(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        // this.emf = emf;
        return countUsage(em, getCurrentBean(), getParentCurrentRelation(), getParentClass());
    }

    /**
     * @return the currentBean
     */
    private C getCurrentBean() {
        return currentBean;
    }

    /**
     * @param currentBean the currentBean to set
     */
    private void setCurrentBean(C currentBean) {
        this.currentBean = currentBean;
    }

    /**
     * @return the parentClass
     */
    private Class<P> getParentClass() {
        return parentClass;
    }

    /**
     * @param parentClass the parentClass to set
     */
    private void setParentClass(Class<P> parentClass) {
        this.parentClass = parentClass;
    }

    private Set<P> findUsage(EntityManager em,
            C currentBean,
            String parentCurrentRelation,
            Class<P> parentClass,
            Optional<String> orderBy,
            boolean asc,
            Integer startIndex,
            Optional<Integer> pageSize) {

        Set<P> results = new HashSet<>();
//        List<Field> fields = RelationUtils.getParentChildFields(parentClass, currentBean.getClass()); //order is important. Current entity is always get pointed by = child
//        if (fields.isEmpty() == false) {
//            // EntityManager em = emf.createEntityManager();
//
//            for (Field field : fields) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<P> cq = cb.createQuery(parentClass);
        Root root = cq.from(parentClass);
        Join join = root.join(parentCurrentRelation);
        cq.where(
                cb.and(
                        cb.equal(
                                join.get(
                                        EntityUtils.getIdentifierFieldName(currentBean.getClass(), em)
                                ),
                                EntityUtils.getIdentifierValue(currentBean, em)
                        ),
                        cb.not(
                                cb.or(
                                        cb.equal(root.get("status"), Status.DELETED),
                                        cb.equal(root.get("status"), Status.PREDRAFT)
                                )
                        )
                )
        );

        orderBy.ifPresentOrElse(o -> {
            Order order = null;
            if (asc) {
                order = cb.asc(root.get(o));
            } else {
                order = cb.desc(root.get(o));
            }
            cq.orderBy(order);
        }, () -> {
            Order order = null;
            if (asc) {
                order = cb.asc(root.get(EntityUtils.getIdentifierFieldName(parentClass, em)));
            } else {
                order = cb.desc(root.get(EntityUtils.getIdentifierFieldName(parentClass, em)));
            }
            cq.orderBy(order);
        });

        //do query
        results.addAll(
                pageSize.map(o -> {
                    return em.createQuery(cq).setFirstResult(startIndex).setMaxResults(o).getResultList();
                }).orElseGet(() -> {
                    return em.createQuery(cq).setFirstResult(startIndex).getResultList();
                })
        );
//            }

        em.close();
//        }
        return results;

    }

    private Long countUsage(EntityManager em,
            C currentBean,
            String parentCurrentRelation,
            Class<P> parentClass) {
        Long count = null;
//        List<Field> fields = RelationUtils.getParentChildFields(parentClass, currentBean.getClass()); //order is important. Current entity is always get pointed by = child
//        if (fields.isEmpty() == false) {
        //EntityManager em = emf.createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.TYPE);

        Root root = cq.from(parentClass);
        cq.select(cb.count(root.get(EntityUtils.getIdentifierFieldName(parentClass, em))));

        //  for (Field field : fields) {
        Join join = root.join(parentCurrentRelation);
        cq.where(
                cb.and(
                        cb.equal(
                                join.get(
                                        EntityUtils.getIdentifierFieldName(currentBean.getClass(), em)
                                ),
                                EntityUtils.getIdentifierValue(currentBean, em)
                        ),
                        cb.not(
                                cb.or(
                                        cb.equal(root.get("status"), Status.DELETED),
                                        cb.equal(root.get("status"), Status.PREDRAFT)
                                )
                        )
                )
        );
        //}

        count = em.createQuery(cq).getSingleResult();
//        }
        if (count != null) {
            return count;
        } else {
            return 0L;
        }
    }

    /**
     * @return the parentCurrentRelation
     */
    public String getParentCurrentRelation() {
        return parentCurrentRelation;
    }

    /**
     * @param parentCurrentRelation the parentCurrentRelation to set
     */
    public void setParentCurrentRelation(String parentCurrentRelation) {
        this.parentCurrentRelation = parentCurrentRelation;
    }

}
