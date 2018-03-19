/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.dao;

import java.util.Collection;
import java.util.List;
import org.azrul.langkuik.framework.audit.AuditedEntity;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.framework.exception.EntityIsUsedException;
import org.azrul.langkuik.framework.relationship.RelationManager;

/**
 *
 * @author azrulm
 *
 * @param <T>
 */
public interface DataAccessObject<T> {
    //introspec
    Class<T> getType();
    T refresh(T entity);
   
    //Create
    T createNew(String tenantId); //transient
    T createNew(boolean giveId, String tenantId); //transient
    //T createAndSave();
    Object createAndSave(Class c, String tenantId);
    //<P> T createAndSave(P parentObject, String parentToCurrentField, RelationManager<P, T> relationManager);
    //<P> T createAndSave(DaoParameter<P, T> parameter);
    
    //Save
    T save(T newObject);
    Object saveWithRelation(Object newBean, Object parentBean, String parentToNewBeanField,RelationManager relationManager);
    
    //update
    <P> P associate(FindRelationParameter<P,T> frParam, T newBean, T oldBean);
    <P> P associate(FindRelationParameter<P,T> frParam, Collection<T> newBeans);
    <P> T saveAndAssociate(T newBean, P parentBean, String parentToNewBeanField, RelationManager<P, T> relationManager) throws DuplicateDataException;
    <P> P unlink(FindRelationParameter<P,T> frParam, Collection<T> oldBean);
    Object unlinkAndDelete(Collection oldBeans, Object parentBean, String parentToNewBeanField, RelationManager relationManager);
    <P> P unlinkAndDelete(FindRelationParameter<P,T> frParam, Collection< T> oldBeans);
    void delete(Collection<T> entities) throws EntityIsUsedException;
    void delete(T entity) throws EntityIsUsedException;

    //navigate
//    <P> Long count(P parentObject, String parentObjectField);
    T find(Object id, String tenantId);
   
    //search
    <P> Collection<T> searchResultAlreadyInParent(Collection<T> searchResult, Class<T> daoClass, P parentObject, String parentToChildrenField);
    <P> Long countQueryResult(DAOQuery<P, T> query, String tenantId);
    <P> Collection<T> runQuery(DAOQuery<P, T> query, String orderBy, boolean asc, int startIndex, int offset,String tenantId);
    
    //audit
    //List<AuditedEntity> getAuditData(T bean,int startIndex, int offset, List<String> targetFields);
    boolean isAuditable(Class aclass );
    
}
