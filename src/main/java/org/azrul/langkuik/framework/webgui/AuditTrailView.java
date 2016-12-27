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

package org.azrul.langkuik.framework.webgui;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import java.util.Set;
import org.azrul.langkuik.annotations.WebEntity;
import org.azrul.langkuik.dao.AuditTrailQuery;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.dao.HibernateGenericDAO;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.audit.AuditedEntity;
import org.azrul.langkuik.framework.webgui.breadcrumb.BreadCrumbBuilder;
import org.azrul.langkuik.security.role.UserSecurityUtils;

/**
 *
 * @author azrulm
 * @param <C>
 */
public class AuditTrailView<C> extends VerticalView {
    
    private final DataAccessObject<AuditedEntity> dao;
    private final PageParameter pageParameter;
    private final Class<C> classOfEntity;
    private final int noEntriesPerPage;
    private final C currentBean;
    private final String chosenField;
    
    public AuditTrailView(C currentBean, Class<C> classOfEntity, int noEntriesPerPage, PageParameter pageParameter) {
        this.currentBean = currentBean;
        this.classOfEntity = classOfEntity;
        this.noEntriesPerPage = noEntriesPerPage;
        this.pageParameter = pageParameter;
        this.dao = new HibernateGenericDAO<>(pageParameter.getEntityManagerFactory(), AuditedEntity.class);
        this.chosenField = "*";
    }
    
    public void enter(final ViewChangeListener.ViewChangeEvent vcevent) {
        setCurrentView(vcevent.getViewName());
        this.removeAllComponents();
        
        BeanUtils beanUtils = new BeanUtils();

        //determine user details
        //Set<String> currentUserRoles = UserSecurityUtils.getCurrentUserRoles();

        //determine entity rights 
        if (UserSecurityUtils.isCurrentUserAuditViewer(pageParameter.getConfig()) == false) {
            return;
        }

        //Build bread crumb
        BreadCrumbBuilder.buildBreadCrumb(vcevent.getNavigator(),
                pageParameter.getBreadcrumb(),
                pageParameter.getHistory());

        //set form
        FormLayout form = new FormLayout();

        //entity name
        String entityName = null;
        WebEntity webEntity = currentBean.getClass().getAnnotation(WebEntity.class);
        if (webEntity != null) {
            if (webEntity.name() != null) {
                entityName = webEntity.name();
            }
        }
        if (entityName == null) {
            entityName = currentBean.getClass().getName();
        }

        //add title
        Label title = new Label("<h2><u>" + pageParameter.getLocalisedText("view.auditttrail.title", beanUtils.getName(currentBean.getClass())) + "</u></h2>", ContentMode.HTML);
        form.addComponent(title);
        final AuditTrailQuery auditTrailQuery = new AuditTrailQuery(classOfEntity, currentBean, null);
        final AuditTrailDataTable auditTrailDataTable = new AuditTrailDataTable(
                auditTrailQuery,
                classOfEntity,
                dao,
                noEntriesPerPage,
                /*currentUserRoles,*/
                pageParameter,
                "");
        final PopupDateField startingFromField = new PopupDateField();
        startingFromField.setDateFormat(pageParameter.getConfig().get("dateFormat"));
        Button requeryAudit = new Button(pageParameter.getLocalisedText("audittable.run.query"));
        requeryAudit.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                auditTrailQuery.setStartingFrom(startingFromField.getValue());
                auditTrailDataTable.refresh();
            }
        });
        HorizontalLayout startingFromPanel = new HorizontalLayout();
        startingFromPanel.addComponent(startingFromField);
        startingFromPanel.addComponent(requeryAudit);
        form.addComponent(startingFromPanel);
        form.addComponent(auditTrailDataTable);
        
        Button backBtn = new Button(pageParameter.getLocalisedText("form.general.button.back"),
                new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        if (!pageParameter.getHistory().isEmpty()) {
                            String currentView = pageParameter.getHistory().pop().getViewHandle();
                            String lastView = pageParameter.getHistory().peek().getViewHandle();
                            vcevent.getNavigator().removeView(currentView);
                            vcevent.getNavigator().navigateTo(lastView);
                        }
                    }
                });
        form.addComponent(backBtn);
        backBtn.setId(backBtn.getCaption());
        this.addComponent(form);
    }
    
}
