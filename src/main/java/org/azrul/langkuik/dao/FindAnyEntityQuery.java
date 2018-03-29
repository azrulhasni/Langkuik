/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author azrulm
 */
public class FindAnyEntityQuery<T> implements DAOQuery<T, T>, Serializable {

    private Collection<SearchTerm> searchTerms;
    private Class<T> searchClass;
    private EntityManagerFactory emf;

    public FindAnyEntityQuery(Class<T> searchClass) {
        this.searchClass = searchClass;
        this.searchTerms = new ArrayList<SearchTerm>();
    }
    
    
    public Collection doQuery(EntityManagerFactory emf, String orderBy, boolean asc, int startIndex, int offset, String tenantId){
        this.emf = emf; 
        return search(getSearchTerms(), getSearchClass(), orderBy, asc, startIndex, offset, tenantId);
    }
    
    public Long count(EntityManagerFactory emf, String tenantId){
        this.emf = emf;
         return countSearch(getSearchTerms(), getSearchClass(),tenantId);
    }
    
    public void clearSearchTerms() {
        searchTerms.clear();
    }
    
    public void addSearchTerm(SearchTerm searchTerm){
        searchTerms.add(searchTerm);
    }

    /**
     * @return the searchTerms
     */
    private Collection<SearchTerm> getSearchTerms() {
        return searchTerms;
    }

    /**
     * @param searchTerms the searchTerms to set
     */
    private void setSearchTerms(Collection<SearchTerm> searchTerms) {
        this.searchTerms = searchTerms;
    }

    /**
     * @return the searchClass
     */
    private Class<T> getSearchClass() {
        return searchClass;
    }

    /**
     * @param searchClass the searchClass to set
     */
    private void setSearchClass(Class<T> searchClass) {
        this.searchClass = searchClass;
    }


    private Long countSearch(Collection<SearchTerm> searchTerms, Class<T> daoClass, String tenantId) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            return countAll(daoClass, tenantId);
        }
        EntityManager em = emf.createEntityManager();

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        org.apache.lucene.search.Query luceneQuery = buildSearchQuery(fullTextEntityManager, daoClass, searchTerms, tenantId, EntityUtils.getTenantFieldName(daoClass));
        FullTextQuery ftQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, daoClass);
        return new Long(ftQuery.getResultSize());
    }

    private Collection<T> getAll(Class<T> daoClass, String orderBy, boolean asc, int startIndex, int offset,  String tenantId) {
        EntityManager em = emf.createEntityManager();

//        ClassMetadata classMetadata = ((Session) em.getDelegate()).getSessionFactory().getClassMetadata(daoClass);
//        String childId = classMetadata.getIdentifierPropertyName();
        String childId = EntityUtils.getIdentifierFieldName(searchClass, emf);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> criteria = cb.createQuery(daoClass);

        Root<T> root = criteria.from(daoClass);
        //add tenant
        //--find tenant field
        Field tenantIdField = EntityUtils.getTenantFieldName(daoClass);
        
        //add order and tenant
        if (tenantIdField!=null){
             if (orderBy == null) {
                criteria.select(root).where(cb.equal(root.get(tenantIdField.getName()), tenantId)).orderBy(cb.asc(root.get(childId)));
            } else {
                if (asc == true) {
                    criteria.select(root).where(cb.equal(root.get(tenantIdField.getName()), tenantId)).orderBy(cb.asc(root.get(orderBy)));
                } else {
                    criteria.select(root).where(cb.equal(root.get(tenantIdField.getName()), tenantId)).orderBy(cb.desc(root.get(orderBy)));
                }
            }
        }else{
            if (orderBy == null) {
                criteria.select(root).orderBy(cb.asc(root.get(childId)));
            } else {
                if (asc == true) {
                    criteria.select(root).orderBy(cb.asc(root.get(orderBy)));
                } else {
                    criteria.select(root).orderBy(cb.desc(root.get(orderBy)));
                }
            }
        }
        Collection<T> results = em.createQuery(criteria).setFirstResult(startIndex).setMaxResults(offset).getResultList();

        em.close();
        return results;
    }

//    private Collection<T> searchAll(Class<T> daoClass, String tenantId) {
//
//        EntityManager em = emf.createEntityManager();
//
//        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
//        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
//                .buildQueryBuilder().forEntity(daoClass).get();
//        org.apache.lucene.search.Query luceneQuery = qb.all().createQuery();
//
//        Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, daoClass);
//        
//        List<T> results = jpaQuery.getResultList();
//        fullTextEntityManager.close();
//
//        return results;
//
//    }
//
    private Long countAll(Class<T> daoClass, String tenantId) {
        EntityManager em = emf.createEntityManager();
        
        //add tenant
        //--find tenant field
        Field tenantIdField = EntityUtils.getTenantFieldName(daoClass);

        ClassMetadata classMetadata = ((Session) em.getDelegate()).getSessionFactory().getClassMetadata(daoClass);
        String childId = classMetadata.getIdentifierPropertyName();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<Long> criteria = cb.createQuery(Long.TYPE);
        Root<T> root = criteria.from(daoClass);

        if (tenantIdField!=null){
               criteria.select(cb.count(root.get(childId))).where(cb.equal(root.get(tenantIdField.getName()), tenantId)); 
        }else{
            criteria.select(cb.count(root.get(childId)));
        }
        Long size = em.createQuery(criteria).getSingleResult();

        em.close();
        return size;
    }

    private Collection<T> search(Collection<SearchTerm> searchTerms, Class<T> daoClass, String orderBy, boolean asc, int startIndex, int offset, String tenantId) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            return getAll(daoClass, orderBy, asc, startIndex, offset, tenantId);
        }
        try {
            EntityManager em = emf.createEntityManager();

            FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

            //Session session = (Session) em.getDelegate();
            //String currentIdField = session.getSessionFactory().getClassMetadata(daoClass).getIdentifierPropertyName();
            String currentIdField = EntityUtils.getIdentifierFieldName(searchClass, emf);
            org.apache.lucene.search.Query luceneQuery = buildSearchQuery(fullTextEntityManager, daoClass, searchTerms,tenantId,EntityUtils.getTenantFieldName(daoClass));
            Sort sort = Sort.INDEXORDER;
            if (orderBy == null) {
                if (Long.TYPE.equals(daoClass.getDeclaredField(currentIdField).getType())) {
                    sort = new Sort(new SortField(currentIdField, SortField.LONG, asc));
                } else if (Integer.TYPE.equals(daoClass.getDeclaredField(currentIdField).getType())) {
                    sort = new Sort(new SortField(currentIdField, SortField.INT, asc));
                } else if (String.class.equals(daoClass.getDeclaredField(currentIdField).getType())) {
                    sort = new Sort(new SortField(currentIdField, SortField.STRING, asc));
                }
            } else {
                if (Long.TYPE.equals(daoClass.getDeclaredField(orderBy).getType())) {
                    sort = new Sort(new SortField(orderBy, SortField.LONG, asc));
                } else if (Integer.TYPE.equals(daoClass.getDeclaredField(orderBy).getType())) {
                    sort = new Sort(new SortField(orderBy, SortField.INT, asc));
                } else { //all else fail, sort as string
                    sort = new Sort(new SortField(orderBy, SortField.STRING, asc));
                }
            }

            Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, daoClass)
                    .setSort(sort)
                    .setFirstResult(startIndex)
                    .setMaxResults(offset);
            
            //-------------------------------------------------------------------
           
       
            
            //-------------------------------------------------------------------

            List<T> results = jpaQuery.getResultList();
            fullTextEntityManager.close();
            //Collection<T> allRes = searchAll(daoClass); //colllateral id 65

            return results;
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(HibernateGenericDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ArrayList<T>();
    }

    private org.apache.lucene.search.Query buildSearchQuery(FullTextEntityManager fullTextEntityManager, Class<T> daoClass, Collection<SearchTerm> searchTerms, String tenantId, Field tenantField) {
        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(daoClass).get();
        BooleanJunction bj = qb.bool();
        for (SearchTerm entry : searchTerms) {
            if (entry.getValue() instanceof Date) {
                bj.must(qb.range().onField(entry.getFieldName()).ignoreAnalyzer().below(entry.getValue()).createQuery());
                bj.must(qb.range().onField(entry.getFieldName()).ignoreAnalyzer().above(entry.getValue()).createQuery());
            } else {
                org.hibernate.search.annotations.Field searchFieldAnno = entry.getField().getAnnotation(org.hibernate.search.annotations.Field.class);
                if (searchFieldAnno.analyze() == Analyze.NO) {
                    bj.must(qb.keyword().onField(entry.getFieldName()).ignoreAnalyzer().matching(entry.getValue()).createQuery());
                } else {
                    bj.must(qb.keyword().fuzzy().onField(entry.getFieldName()).matching(entry.getValue()).createQuery());
                }
            }
        }
        if (tenantField!=null){
            bj.must(qb.keyword().onField(tenantField.getName()).matching(tenantId).createQuery());
        }
        org.apache.lucene.search.Query luceneQuery = bj.createQuery();
        return luceneQuery;
    }
}
