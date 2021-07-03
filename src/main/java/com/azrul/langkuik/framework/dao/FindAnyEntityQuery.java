/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import com.azrul.langkuik.framework.field.FieldUtils;
import com.azrul.langkuik.framework.standard.LangkuikExt;
import com.azrul.langkuik.framework.standard.Status;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
    private Optional<String> queryString;
    private Class<T> searchClass;
    private Collection<T> exclusion;
    private String dateFormat;

    //private Object parent = null;
    //private EntityManagerFactory emf;
    public FindAnyEntityQuery(Class<T> searchClass, String dateFormat) {
        this.searchClass = searchClass;
        this.searchTerms = new ArrayList<SearchTerm>();
        this.queryString = Optional.empty();
        this.dateFormat = dateFormat;
    }

    public FindAnyEntityQuery(Class<T> searchClass, String dateFormat,Collection<T> exclusion) {
        this(searchClass, dateFormat);
        if (exclusion == null) {
            this.exclusion = new ArrayList<>();
        } else {
            this.exclusion = exclusion;
        }
        //this.parent = parent;
    }

    public Collection doQuery(EntityManager em, Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        //this.emf = emf;
        return search(em, orderBy, asc.orElse(Boolean.FALSE), startIndex.orElse(0), pageSize, tenantId, worklist);
    }

    public Long count(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        //this.emf = emf;
        return countSearch(em, tenantId, worklist);
    }

    public void clearSearchTerms() {
        searchTerms.clear();
    }

    public void addSearchTerm(SearchTerm searchTerm) {
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

    private Long countSearch(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        String currentIdField = EntityUtils.<T>getIdentifierFieldName(searchClass, em);
        Long count = null;
        if (searchTerms.isEmpty()) {
            count = queryString.map(s -> { 
                FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
                org.apache.lucene.search.Query luceneQuery = buildSearchQueryBasedOnQueryString(s, exclusion, currentIdField, fullTextEntityManager, tenantId, worklist);
                FullTextQuery ftQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, this.searchClass);
                return Integer.valueOf(ftQuery.getResultSize()).longValue();
            }).orElseGet( () ->{ 
                return countAll(em, tenantId, worklist);
            });
        } else {
           FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
           org.apache.lucene.search.Query luceneQuery = buildSearchQueryBasedOnTerms(searchTerms, exclusion, currentIdField, fullTextEntityManager, tenantId, worklist);
           FullTextQuery ftQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, this.searchClass);
           return Integer.valueOf(ftQuery.getResultSize()).longValue();
        }
        
        if (count == null){
            return 0L;
        } else{
            return count;
        }
        
    }

    private Collection<T> getAll(EntityManager em, Optional<String> orderBy, Boolean asc, Integer startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        //EntityManager em = emf.createEntityManager();

        Collection<T> results = new ArrayList<>();

        String childId = EntityUtils.<T>getIdentifierFieldName(searchClass, em);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<T> criteria = cb.createQuery(searchClass);

        Root<T> root = criteria.from(searchClass);

        //add order,tenant and worklist
        List<Predicate> predicates = new ArrayList<>();
        
        //deal with tenant
        tenantId.ifPresent(s -> {
            predicates.add(cb.equal(root.get("tenantId"), s));
        });

        //deal with deleted
        if (LangkuikExt.class.isAssignableFrom(searchClass)) {
            predicates.add(cb.not(cb.equal(root.get("status"), Status.DELETED)));
        }
        
        //deal with pre-draft
        if (LangkuikExt.class.isAssignableFrom(searchClass)) {
            predicates.add(cb.not(cb.equal(root.get("status"), Status.PREDRAFT)));
        }

        //deal with worklist    
       worklist.ifPresent(w->
            predicates.add(cb.equal(root.get("worklist"), w)));
        
        //deal with exclusion
        if (!exclusion.isEmpty()) {
            predicates.add(cb.not(root.in(exclusion)));
        }
        
        
        
        Predicate[] token = new Predicate[1];
        CriteriaQuery<T> query = criteria.select(root);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(token)));
        }

        orderBy.ifPresentOrElse(s -> {
            if (asc == true) {
                query.orderBy(cb.asc(root.get(s)));
            } else {
                query.orderBy(cb.desc(root.get(s)));
            }
        }, () -> query.orderBy(cb.asc(root.get(childId))));

       
        
        results = pageSize.map(o-> {
            return em.createQuery(query).setFirstResult(startIndex).setMaxResults(o).getResultList();
        }).orElseGet(()->{
            return em.createQuery(query).setFirstResult(startIndex).getResultList();
        });
        //results = em.createQuery(query).setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
        return results;
    }

    private Long countAll(EntityManager em, Optional<String> tenantId, Optional<String> worklist) {
        //EntityManager em = emf.createEntityManager();

        //add tenant
        //--find tenant field
        String childId = EntityUtils.<T>getIdentifierFieldName(searchClass, em);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery<Long> criteria = cb.createQuery(Long.TYPE);
        Root<T> root = criteria.from(this.searchClass);

        List<Predicate> predicates = new ArrayList<>();
        
        
        //deal with tenant
        tenantId.ifPresent(s -> {
            predicates.add(cb.equal(root.get("tenantId"), s));
        });

        //deal with deleted
        if (LangkuikExt.class.isAssignableFrom(searchClass)) {
            predicates.add(cb.not(cb.equal(root.get("status"), Status.DELETED)));
        }
        
        //deal with pre-draft
        if (LangkuikExt.class.isAssignableFrom(searchClass)) {
            predicates.add(cb.not(cb.equal(root.get("status"), Status.PREDRAFT)));
        }

        //deal with worklist    
       worklist.ifPresent(w->
            predicates.add(cb.equal(root.get("worklist"), w)));
        
        //deal with exclusion
        if (!exclusion.isEmpty()) {
            predicates.add(cb.not(root.in(exclusion)));
        }
        
        
//        
//        Predicate[] token = new Predicate[1];
//        CriteriaQuery<T> query = criteria.select(root);
//        if (!predicates.isEmpty()) {
//            query.where(cb.and(predicates.toArray(token)));
//        }
        
//        tenantId.ifPresent(s-> predicates.add(cb.equal(root.get("tenantId"), s)));
//
//        //deal with deleted
//        if (LangkuikExt.class.isAssignableFrom(searchClass)) {
//            predicates.add(cb.not(cb.equal(root.get("status"), Status.DELETED)));
//        }
//        
//        //deal with tenant
//        tenantId.ifPresent(s -> {
//            predicates.add(cb.equal(root.get("tenantId"), s));
//        });
//
//        //deal with worklist
//        worklist.ifPresent(w->{
//            predicates.add(cb.equal(root.get("worklist"), w));
//        });
//
//        if (!exclusion.isEmpty()) {
//            predicates.add(cb.not(root.in(exclusion)));
//        }
        Predicate[] token = new Predicate[1];

        CriteriaQuery<Long> query = criteria.select(cb.count(root.get(childId)));
        if (!predicates.isEmpty()) {
            query.where(
                    cb.and(predicates.toArray(token)));
        }

//        tenantId.ifPresentOrElse(
//                s -> criteria.select(cb.count(root.get(childId))).where(cb.equal(root.get("tenantId"), s)),
//                 () -> criteria.select(cb.count(root.get(childId)))
//        );
        Long size = em.createQuery(query).getSingleResult();

        em.close();
        return size;
    }

    private Collection<T> search(EntityManager em, Optional<String> orderBy, Boolean asc, Integer startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {
        
        Collection<T> results = null;
        if (searchTerms.isEmpty()) {
            results = queryString.map(s -> {
               return runSearchQueryBasedOnQueryString(s, exclusion, em, orderBy, asc, startIndex, pageSize, tenantId, worklist);
            }).orElseGet(() -> { 
                return getAll(em, orderBy, asc, startIndex, pageSize, tenantId, worklist);
            });
        } else {
            results = runSearchQueryBasedOnTerms(searchTerms, exclusion, em, orderBy, asc, startIndex, pageSize, tenantId, worklist);
        }
        if (results==null){
            return new ArrayList<>();
        }else{
            return results;
        }
    }
    
    private Collection<T> runSearchQueryBasedOnQueryString(String queryString, Collection<T> exclusion,EntityManager em, Optional<String> orderBy, Boolean asc, Integer startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {

        //EntityManager em = emf.createEntityManager();
        String currentIdField = EntityUtils.getIdentifierFieldName(searchClass, em);
        String currentSearchIdField = EntityUtils.getIdentifierSearchFieldName4Sorting(searchClass, em).orElse(currentIdField);

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        org.apache.lucene.search.Query luceneQuery = buildSearchQueryBasedOnQueryString(queryString, exclusion, currentIdField, fullTextEntityManager, tenantId, worklist);
        
        //return runFullTextQuery(em, orderBy, asc, fullTextEntityManager, luceneQuery, startIndex, pageSize);
        final Sort sort =  orderBy.map(o -> {
            String orderBySearchField = EntityUtils.classFieldToSearchField4Sorting(searchClass, o).orElse(currentIdField);
            if (Long.class.equals(FieldUtils.getField(this.searchClass, o).getType())) {
                return new Sort(new SortField(orderBySearchField, SortField.Type.LONG, asc));
            } else if (Integer.class.equals(FieldUtils.getField(this.searchClass, o).getType())) {
               return new Sort(new SortField(orderBySearchField, SortField.Type.INT, asc));
            } else { //all else fail, sort as string
                return new Sort(new SortField(orderBySearchField, SortField.Type.STRING, asc));
            }
        }).orElseGet(()-> {
            if (currentIdField != null) {
                if (Long.class.equals(FieldUtils.getField(this.searchClass, currentIdField).getType())) {
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.LONG, asc));
                } else if (Integer.class.equals(FieldUtils.getField(this.searchClass, currentIdField).getType())) {
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.INT, asc));
                } else  { //all else fail, sort as string
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.STRING, asc));
                }
            }else{
               return Sort.INDEXORDER;
            }
        });
        
        final org.apache.lucene.search.Query lq = luceneQuery;
        Query jpaQuery = pageSize.map(o -> {
            return fullTextEntityManager.createFullTextQuery(lq, this.searchClass)
                .setSort(sort)
                .setFirstResult(startIndex)
                .setMaxResults(o);
        }).orElseGet(()->{
            return fullTextEntityManager.createFullTextQuery(lq, this.searchClass)
                .setSort(sort)
                .setFirstResult(startIndex);
        });

      
        List<T> results = jpaQuery.getResultList();
        fullTextEntityManager.close();

        return results;
    }

    private Collection<T> runSearchQueryBasedOnTerms(Collection<SearchTerm> searchTerms, Collection<T> exclusion, EntityManager em, Optional<String> orderBy, Boolean asc, Integer startIndex, Optional<Integer> pageSize, Optional<String> tenantId, Optional<String> worklist) {

        //EntityManager em = emf.createEntityManager();
        String currentIdField = EntityUtils.getIdentifierFieldName(searchClass, em);
        String currentSearchIdField = EntityUtils.getIdentifierSearchFieldName4Sorting(searchClass, em).orElse(currentIdField);

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        org.apache.lucene.search.Query luceneQuery = buildSearchQueryBasedOnTerms(searchTerms, exclusion, currentIdField, fullTextEntityManager, tenantId, worklist);
        
        //return runFullTextQuery(em, orderBy, asc, fullTextEntityManager, luceneQuery, startIndex, pageSize);
        final Sort sort =  orderBy.map(o -> {
            String orderBySearchField = EntityUtils.classFieldToSearchField4Sorting(searchClass, o).orElse(currentIdField);
            if (Long.class.equals(FieldUtils.getField(this.searchClass, o).getType())) {
                return new Sort(new SortField(orderBySearchField, SortField.Type.LONG, asc));
            } else if (Integer.class.equals(FieldUtils.getField(this.searchClass, o).getType())) {
               return new Sort(new SortField(orderBySearchField, SortField.Type.INT, asc));
            } else { //all else fail, sort as string
                return new Sort(new SortField(orderBySearchField, SortField.Type.STRING, asc));
            }
        }).orElseGet(()-> {
            if (currentIdField != null) {
                if (Long.class.equals(FieldUtils.getField(this.searchClass, currentIdField).getType())) {
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.LONG, asc));
                } else if (Integer.class.equals(FieldUtils.getField(this.searchClass, currentIdField).getType())) {
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.INT, asc));
                } else  { //all else fail, sort as string
                    return new Sort(new SortField(currentSearchIdField, SortField.Type.STRING, asc));
                } 
            }else{
               return Sort.INDEXORDER;
            }
        });
        
        final org.apache.lucene.search.Query lq = luceneQuery;
        Query jpaQuery = pageSize.map(o -> {
            return fullTextEntityManager.createFullTextQuery(lq, this.searchClass)
                .setSort(sort)
                .setFirstResult(startIndex)
                .setMaxResults(o);
        }).orElseGet(()->{
            return fullTextEntityManager.createFullTextQuery(lq, this.searchClass)
                .setSort(sort)
                .setFirstResult(startIndex);
        });

      
        List<T> results = jpaQuery.getResultList();
        fullTextEntityManager.close();

        return results;
    }

    private org.apache.lucene.search.Query buildSearchQueryBasedOnTerms(Collection<SearchTerm> searchTerms,
            Collection<T> exclusion,
            String currentIdField,
            FullTextEntityManager fullTextEntityManager,
            Optional<String> tenantId,
            Optional<String> worklist) {

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(this.searchClass).get();

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

        tenantId.ifPresent(s
                -> bj.must(qb.keyword().onField("tenantId").matching(s).createQuery()));

        worklist.ifPresent(s
                -> bj.must(qb.keyword().onField("worklist").matching(s).createQuery()));

        //luceneQuery = bj.createQuery();
        QueryBuilder qbFilter = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(this.searchClass).get();

        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        if (!exclusion.isEmpty()) {
            for (T excluded : exclusion) {
                String idValue = EntityUtils.getIdentifierValue(excluded, fullTextEntityManager).toString();
                org.apache.lucene.search.Query filterOutIds = qbFilter.bool().must(qbFilter.keyword().onField(currentIdField).matching(idValue).createQuery()).createQuery();
                bqBuilder.add(filterOutIds, Occur.MUST_NOT);
            }
        }

        
        //deal with deleted and predraft
        if (LangkuikExt.class.isAssignableFrom(this.searchClass)) {
            //predicates.add(cb.not(cb.equal(root.get("status"), Status.DELETED)));
            org.apache.lucene.search.Query filterOutDeleted = qbFilter.bool().must(qbFilter.keyword().onField("status").matching(Status.DELETED).createQuery()).createQuery();
            //bj.must(qb.keyword().onField("status").matching(Status.DELETED).createQuery()).not();
            bqBuilder.add(filterOutDeleted, Occur.MUST_NOT);
            
            org.apache.lucene.search.Query filterOutPreDraft= qbFilter.bool().must(qbFilter.keyword().onField("status").matching(Status.PREDRAFT).createQuery()).createQuery();
            //bj.must(qb.keyword().onField("status").matching(Status.DELETED).createQuery()).not();
            bqBuilder.add(filterOutPreDraft, Occur.MUST_NOT);
        }

        return bqBuilder.add(bj.createQuery(), Occur.MUST).build();
    }

    private org.apache.lucene.search.Query buildSearchQueryBasedOnQueryString(String queryString,
            Collection<T> exclusion,
            String currentIdField,
            FullTextEntityManager fullTextEntityManager,
            Optional<String> tenantId,
            Optional<String> worklist) {

        List<String> searchFields = EntityUtils.getSearchFields(searchClass);
        org.apache.lucene.search.Query mainQuery = null;
        if (queryString.contains(":")) {
            try {
                CustomMultiFieldQueryParser parser = new CustomMultiFieldQueryParser(searchFields.toArray(new String[]{}),dateFormat, new StandardAnalyzer(), searchClass);
                parser.setDefaultOperator(QueryParser.Operator.OR);
                mainQuery = parser.parse(queryString);
            } catch (ParseException ex) {
                Logger.getLogger(FindAnyEntityQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            QueryBuilder qbMain = fullTextEntityManager.getSearchFactory()
                    .buildQueryBuilder().forEntity(this.searchClass).get();
            String[] searchFieldsArray = searchFields.toArray(new String[]{});
            mainQuery = qbMain.simpleQueryString().onFields(searchFieldsArray[0], searchFieldsArray)
                    .matching(queryString)
                    .createQuery();
        }

        QueryBuilder qbFilter = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(this.searchClass).get();
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        
        //deal with exclusiuon
        if (exclusion != null && exclusion.isEmpty() == false) {
            exclusion.stream()
                    .map((excluded) -> EntityUtils.getIdentifierValue(excluded, fullTextEntityManager).toString())
                    .map((idValue) -> qbFilter.bool().must(qbFilter.keyword().onField(currentIdField).matching(idValue).createQuery()).createQuery())
                    .forEachOrdered((filterOutIds) -> {
                        bqBuilder.add(filterOutIds, Occur.MUST_NOT);
                    });
        }
        
        //deal with worklist
        worklist.ifPresent(w->{
            org.apache.lucene.search.Query includeOnlyCurrentWorklist = qbFilter.bool().must(qbFilter.keyword().onField("worklist").matching(w).createQuery()).createQuery();
            bqBuilder.add(includeOnlyCurrentWorklist, Occur.MUST);
        });
        
        //deal with tenant
        tenantId.ifPresent(t->{
            org.apache.lucene.search.Query includeOnlyCurrentTenant = qbFilter.bool().must(qbFilter.keyword().onField("tenantId").matching(t).createQuery()).createQuery();
            bqBuilder.add(includeOnlyCurrentTenant, Occur.MUST);
        });
        
        //deal with deleted
        if (LangkuikExt.class.isAssignableFrom(this.searchClass)) {
            org.apache.lucene.search.Query filterOutDeleted = qbFilter.bool().must(qbFilter.keyword().onField("status").matching(Status.DELETED).createQuery()).createQuery();
            bqBuilder.add(filterOutDeleted, Occur.MUST_NOT);
            
            org.apache.lucene.search.Query filterOutPreDraft = qbFilter.bool().must(qbFilter.keyword().onField("status").matching(Status.PREDRAFT).createQuery()).createQuery();
            bqBuilder.add(filterOutPreDraft, Occur.MUST_NOT);
        }
        return bqBuilder.add(mainQuery, Occur.MUST).build();
    }

    /**
     * @return the queryString
     */
    public Optional<String> getQueryString() {
        return queryString;
    }

    /**
     * @param queryString the queryString to set
     */
    public void setQueryString(Optional<String> queryString) {
        this.queryString = queryString;
    }

//    private Object highlightText(org.apache.lucene.search.Query query, FullTextEntityManager ftem, Object target) {
//        Analyzer analyzer = ftem.getSearchFactory().getAnalyzer(target.getClass());
//        QueryScorer queryScorer = new QueryScorer(query);
//        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span>", "</span>");
//        Highlighter highlighter = new Highlighter(formatter, queryScorer);
//        for (Field field:target.getClass().getDeclaredFields()){
//            
//        }
//        //return highlighter.getBestFragment(analyzer, fieldName, text);
//    }
}


