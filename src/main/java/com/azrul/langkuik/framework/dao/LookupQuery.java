/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author azrul
 */
public class LookupQuery<L, M> implements DAOQuery {

    private Class<L> lookupEntity;
    private Optional<String> fieldName;
    private Optional<String> filterByFieldName;
    private Optional<M> matchingValue;

    public LookupQuery(Class<L> lookupEntity,
            String fieldName,
            String filterByFieldName,
            M matchingValue) {
        this.lookupEntity = lookupEntity;
        this.fieldName = Optional.of(fieldName);
        this.filterByFieldName = Optional.ofNullable(filterByFieldName);
        this.matchingValue = Optional.ofNullable(matchingValue);
    }

    public LookupQuery(Class<L> lookupEntity,
            String fieldName) {
        this.lookupEntity = lookupEntity;
        this.fieldName = Optional.of(fieldName);
        this.filterByFieldName = Optional.empty();
        this.matchingValue = Optional.empty();
    }

    public LookupQuery(Class<L> lookupEntity) {
        this.lookupEntity = lookupEntity;
        this.fieldName = Optional.empty();
        this.filterByFieldName = Optional.empty();
        this.matchingValue = Optional.empty();
    }

    @Override
    public Collection doQuery(EntityManager em, Optional orderBy, Optional oasc, Optional ostartIndex, Optional pageSize, Optional tenantId, Optional worklist) {
        Boolean asc = (Boolean) oasc.orElse(Boolean.TRUE);
        Integer startIndex = (Integer)ostartIndex.orElse(0);
        CriteriaBuilder cb = em.getCriteriaBuilder();

        if (fieldName.isPresent()) {
            javax.persistence.criteria.CriteriaQuery<String> criteria = cb.createQuery(String.class);
             
            Root<L> root = criteria.from(lookupEntity);
            CriteriaQuery<String> query = this.filterByFieldName.map(filter -> {
                return matchingValue.map(matching -> {
                    return criteria.select(root.get(fieldName.get())).where(cb.equal(root.get(filter), matching)).distinct(true);
                }).orElseGet(() -> {
                    return criteria.select(root.get(fieldName.get())).distinct(true);
                });
            }).orElseGet(() -> {
                return criteria.select(root.get(fieldName.get())).distinct(true);
            });
            
            return em.createQuery(query).setHint("org.hibernate.cacheable", true)
                    .setFirstResult(startIndex).getResultList();
        } else {
            javax.persistence.criteria.CriteriaQuery<L> criteriaO = cb.createQuery(lookupEntity);
            Root<L> rootO = criteriaO.from(lookupEntity);
            CriteriaQuery<L> queryO = criteriaO.select(rootO);
            
            return em.createQuery(queryO).setFirstResult(startIndex).getResultList();
//               List<L> list = new ArrayList<>();
//               list.add(em.find(lookupEntity, 1001L));
//               return list;
        }
    }

    @Override
    public Long count(EntityManager em, Optional tenantId, Optional worklist) {
        return 0L;
    }

    
}
