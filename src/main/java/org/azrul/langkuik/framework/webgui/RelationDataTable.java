/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.azrul.langkuik.framework.webgui;

import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.azrul.langkuik.configs.Configuration;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.FindRelationParameter;
import org.azrul.langkuik.dao.FindRelationQuery;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.exception.DuplicateDataException;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.UserSecurityUtils;

/**
 *
 * @author azrulm
 * @param <P>
 * @param <C>
 */
public class RelationDataTable<P,C> extends DataTable<C> {
    private FindRelationParameter<P, C> findRelationParameter=null;
    
    public RelationDataTable(FindRelationQuery<P,C> daoQuery, 
            FindRelationParameter<P, C>  findRelationParameter,
            Class<C> classOfBean, 
            DataAccessObject<C> dao, 
            int noBeansPerPage,  
            //final Set<String> currentUserRoles,
            final EntityRight entityOperation,
            final PageParameter pageParameter) {
        this.findRelationParameter=findRelationParameter;
        createTablePanel(daoQuery, classOfBean, dao, noBeansPerPage/*, currentUserRoles*/,entityOperation,pageParameter);
    }
    
    
    
    public P deleteEntities(Collection<C> beans) {
        FindRelationQuery<P,C> query = (FindRelationQuery<P,C>)daoQuery;
        P parentBean = dao.unlinkAndDelete(findRelationParameter, beans);
        findRelationParameter.setParentObject(parentBean);
        query.setParameter(findRelationParameter);
        Collection<C> data = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant());
        if (data.isEmpty()) {
            data = new ArrayList<>();
            try {
                data.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
            } catch (DuplicateDataException ex) {
                Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);
                
                Logger.getLogger(RelationDataTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        itemContainer.setBeans(data);
        itemContainer.refreshItems();
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());
        
        int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
        if (bigTotal % itemCountPerPage == 0) {
            lastPage--;
        }
        int currentUpdatedPage = currentTableIndex / itemCountPerPage;
        pageLabel.setCaption(pageParameter.getLocalisedText("page.number",currentUpdatedPage + 1,lastPage + 1));
        table.setPageLength(itemCountPerPage);
        return parentBean;

    }

    public P dissociateEntities(Collection<C> beans) {
        FindRelationQuery<P,C> query = (FindRelationQuery<P,C>)daoQuery;
        P parentBean = dao.unlink(findRelationParameter, beans);
        findRelationParameter.setParentObject(parentBean);
        query.setParameter(findRelationParameter);
        Collection<C> data = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant());
        if (data.isEmpty()) {
            data = new ArrayList<>();
            try {
                data.add(dao.createNew(UserSecurityUtils.getCurrentTenant()));
            } catch (DuplicateDataException ex) {
                 Notification.show(pageParameter.getResourceBundle().getString("dialog.duplicateData"), Notification.Type.WARNING_MESSAGE);

                Logger.getLogger(RelationDataTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        itemContainer.setBeans(data);
        itemContainer.refreshItems();
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());
        
        int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
        if (bigTotal % itemCountPerPage == 0) {
            lastPage--;
        }
        int currentUpdatedPage = currentTableIndex / itemCountPerPage;
        pageLabel.setCaption(pageParameter.getLocalisedText("page.number",currentUpdatedPage + 1,lastPage + 1));
        table.setPageLength(itemCountPerPage);
        return parentBean;

    }

    public P associateEntities(Collection<C> entities, ChoiceType choiceType) {
        FindRelationQuery<P, C> query = (FindRelationQuery<P, C>)daoQuery;
        
        P parent = null;
        if (choiceType == ChoiceType.CHOOSE_ONE) {
            C firstEntity = entities.iterator().next();
            C oldEntity = itemContainer.firstItemId();
            parent = dao.associate(findRelationParameter, firstEntity, oldEntity);
        } else {
            parent = dao.associate(findRelationParameter, entities);
        }
        findRelationParameter.setParentObject(parent);
        query.setParameter(findRelationParameter);
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());
        itemContainer.setBeans(dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, itemCountPerPage,UserSecurityUtils.getCurrentTenant()));
        itemContainer.refreshItems();
        int beanLastPage = (int) Math.floor(bigTotal / itemCountPerPage);
        if (bigTotal % itemCountPerPage == 0) {
            beanLastPage--;
        }
        int beanCurrentUpdatedPage = currentTableIndex / itemCountPerPage;
        pageLabel.setCaption(pageParameter.getLocalisedText("page.number",(beanCurrentUpdatedPage + 1),(beanLastPage + 1)));
        table.setPageLength(itemCountPerPage);
        return parent;

    }
}
