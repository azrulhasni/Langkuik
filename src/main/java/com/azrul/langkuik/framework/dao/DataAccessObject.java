/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azrul.langkuik.framework.dao;

import java.util.Collection;
//import com.azrul.langkuik.framework.exception.DuplicateDataException;
import com.azrul.langkuik.framework.exception.EntityIsUsedException;
//import com.azrul.langkuik.framework.relationship.RelationManager;
import com.vaadin.flow.server.InputStreamFactory;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 *
 * @author azrulm
 *
 * @param <T>
 */
@Repository
public interface DataAccessObject<T> {
    //introspec
    Class<T> getType();
    T refresh(T entity);
   
    //Create
    //T createNew(String tenantId) throws DuplicateDataException; //transient
    //T createNew(boolean giveId, String tenantId) throws DuplicateDataException ; //transient
    //T createAndSave();
    
    //non persistennt
    Optional<T> create(Class<T> c, Optional<String> tenantId, String creatorId, Optional<String> parentTranxId);
    Optional<T> createAndSave(Class<T> c, Optional<String> tenantId, String creatorId, Optional<String> parentTranxId);
    <P> Optional<Dual<P,T>> createAssociateAndSave(Class<T> childClass,P parentBean,String parentToNewBeanField, Optional<String> tenantId, String creatorId);
    //<P> T createAndSave(P parentObject, String parentToCurrentField, RelationManager<P, T> relationManager);
    //<P> T createAndSave(DaoParameter<P, T> parameter);
    
    //Save
    Optional<T> save(T newObject);
    //Object saveWithRelation(Object newBean, Object parentBean, String parentToNewBeanField,RelationManager relationManager) throws DuplicateDataException;
    
    //update
//    <P> P associate(FindRelationParameter<P,T> frParam, T newBean, T oldBean);
//    <P> P associate(FindRelationParameter<P,T> frParam, Collection<T> newBeans);
    <P> Optional<Dual<P,T>> saveAndAssociate(T newBean, P parentBean, String parentToNewBeanField);
    <P> Set<Dual<P,T>> saveAndAssociate(Set<T> newBeans, P parentBean, String parentToNewBeanField);
    
    <P> Optional<P> unlink(FindRelationParameter<P,T> frParam, Collection<T> oldBean, Rule3Inputs canUnlink);
    Optional<Object> unlinkAndDelete(Collection oldBeans, Object parentBean, String parentToNewBeanField,  Rule3Inputs canDelete);
    <P> Optional<P> unlinkAndDelete(FindRelationParameter<P,T> frParam, Collection< T> oldBeans,  Rule3Inputs canDelete);
    void delete(Collection<T> entities,  Rule1Input canDelete) throws EntityIsUsedException;
    void delete(T entity,  Rule1Input canDelete) throws EntityIsUsedException;

    //navigate
//    <P> Long count(P parentObject, String parentObjectField);
    //Optional<T> find(Object id, Optional<String> tenantId);
   
    //search
    <P> Collection<T> searchResultAlreadyInParent(Collection<T> searchResult, Class<T> daoClass, P parentObject, String parentToChildrenField);
    <P> Long countQueryResult(DAOQuery<P, T> query, Optional<String> tenantId, Optional<String> worklist);
    <P> Collection<T> runQuery(DAOQuery<P, T> query, Optional<String> orderBy, Optional<Boolean> asc, Optional<Integer> startIndex, Optional<Integer> pageSize,Optional<String> tenantId, Optional<String> worklist);
   // <P> Long countQueryResult(DAOQuery<P, T> query);
    <P> Collection<T> runQuery(DAOQuery<P, T> query);
   
    InputStreamFactory getInputStreamFactory(BlobContainer b);
    
    
    //audit
    //List<AuditedEntity> getAuditData(T bean,int startIndex, int offset, List<String> targetFields);
    boolean isAuditable(Class aclass );
    void massIndex();
}
