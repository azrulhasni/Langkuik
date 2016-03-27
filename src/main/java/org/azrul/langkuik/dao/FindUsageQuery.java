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
package org.azrul.langkuik.dao;

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
import org.azrul.langkuik.framework.relationship.EntityRelationUtils;

/**
 *
 * @author azrulm
 * @param <T>
 */
public class FindUsageQuery<P, C> implements DAOQuery<P, C>, Serializable {

    private C currentBean;
    private Class<P> parentClass;
    private EntityManagerFactory emf;

    public FindUsageQuery(EntityManagerFactory emf, C currentBean, Class<P> parentClass) {
        this.emf = emf;
        this.currentBean = currentBean;
        this.parentClass = parentClass;
    }

    
     @Override
    public Collection doQuery(EntityManagerFactory emf,String orderBy, boolean asc, int startIndex, int offset) {
        this.emf = emf;
        return findUsage(getCurrentBean(),getParentClass(), orderBy, asc, startIndex, offset);
    }

    @Override
    public Long count(EntityManagerFactory emf) {
        this.emf = emf;
       return countUsage(getCurrentBean(),getParentClass());
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

    private Set<P> findUsage(C currentBean, Class<P> parentClass, String orderBy, boolean asc, int startIndex, int offset) {
        Set<P> results = new HashSet<>();
        List<Field> fields = EntityRelationUtils.getParentChildFields(parentClass, currentBean.getClass()); //order is important. Current entity is always get pointed by = child
        if (fields.isEmpty() == false) {
            EntityManager em = emf.createEntityManager();

            for (Field field : fields) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<P> cq = cb.createQuery(parentClass);
                Root root = cq.from(parentClass);
                Join join = root.join(field.getName());
                cq.where(
                        cb.equal(
                                join.get(
                                        EntityUtils.getIdentifierFieldName(currentBean.getClass(), emf)
                                ),
                                EntityUtils.getIdentifierValue(currentBean, emf)
                        )
                );
                Order order = null;
                //order by
                if (orderBy == null) {
                    orderBy = EntityUtils.getIdentifierFieldName(parentClass, emf);
                }
                if (orderBy != null) {
                    if (asc) {
                        order = cb.asc(root.get(orderBy));
                    } else {
                        order = cb.desc(root.get(orderBy));
                    }
                }
                //join
                if (orderBy != null) {
                    cq.orderBy(order);
                }
                //do query
                results.addAll(em.createQuery(cq).setFirstResult(startIndex).setMaxResults(offset).getResultList());
            }

            em.close();
        }
        return results;

    }

    private Long countUsage(C currentBean, Class<P> parentClass) {
        Long count = null;
        List<Field> fields = EntityRelationUtils.getParentChildFields(parentClass, currentBean.getClass()); //order is important. Current entity is always get pointed by = child
        if (fields.isEmpty() == false) {
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.TYPE);

            Root root = cq.from(parentClass);
            cq.select(cb.count(root.get(EntityUtils.getIdentifierFieldName(parentClass, emf))));

            for (Field field : fields) {
                Join join = root.join(field.getName());
                cq.where(
                        cb.equal(
                                join.get(
                                        EntityUtils.getIdentifierFieldName(currentBean.getClass(), emf)
                                ),
                                EntityUtils.getIdentifierValue(currentBean, emf)
                        )
                );
            }

            count = em.createQuery(cq).getSingleResult();
        }
        if (count != null) {
            return count;
        } else {
            return 0L;
        }
    }

   

}
