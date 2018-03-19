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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.azrul.langkuik.annotations.WebField;
import org.azrul.langkuik.dao.AuditTrailQuery;
import org.azrul.langkuik.dao.DAOQuery;
import org.azrul.langkuik.dao.DataAccessObject;
import org.azrul.langkuik.framework.PageParameter;
import org.azrul.langkuik.framework.audit.AuditedEntity;
import org.azrul.langkuik.framework.audit.AuditedField;
import org.azrul.langkuik.security.role.EntityRight;
import org.azrul.langkuik.security.role.FieldState;
import org.azrul.langkuik.security.role.UserSecurityUtils;

/**
 *
 * @author azrulm
 */
public class AuditTrailDataTable<C> extends DataTable<C> {

    private DataAccessObject<AuditedEntity> dao;
    private final DAOQuery<AuditedEntity, AuditedEntity> daoQuery;
    //private final Set<String> currentUserRoles;
    private final SimpleDateFormat dateTimeFormat;
    private final SimpleDateFormat dateFormat;
    private final BeanUtils beanUtils;
    private final EntityRight entityOperation;

    public void refresh() {
        Collection<AuditedEntity> auditedEntities = dao.runQuery(daoQuery, orderBy, asc, currentTableIndex, this.itemCountPerPage,UserSecurityUtils.getCurrentTenant());
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());
        displayPageNumber();

        //add to table
        addToTable(auditedEntities/*, currentUserRoles*/, dateTimeFormat, beanUtils, entityOperation, dateFormat);

    }

    public void displayPageNumber() {
        int allDataLastPage = (int) Math.floor(bigTotal / itemCountPerPage);
        if (bigTotal % itemCountPerPage == 0) {
            allDataLastPage--;
        }
        int allDataCurrentUpdatedPage = currentTableIndex / itemCountPerPage;

        //add title to table
        pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (allDataCurrentUpdatedPage + 1), (allDataLastPage + 1)));
    }

    public AuditTrailDataTable(final AuditTrailQuery<C> auditQuery,
            Class<C> classOfBean,
            final DataAccessObject<AuditedEntity> dao,
            final int noBeansPerPage,
            //final Set<String> currentUserRoles,
            final PageParameter pageParameter,
            final String htmlTableLabel) {

        this.currentTableIndex = 0;
        this.dao = dao;
        this.itemCountPerPage = noBeansPerPage;
        this.daoQuery = auditQuery;
        this.pageParameter = pageParameter;
        this.htmlTableLabel = htmlTableLabel;
        //this.currentUserRoles = currentUserRoles;
        this.beanUtils = new BeanUtils();
        this.dateTimeFormat = new SimpleDateFormat(pageParameter.getConfig().get("dateTimeFormat"));
        this.dateFormat = new SimpleDateFormat(pageParameter.getConfig().get("dateFormat"));
        this.entityOperation = EntityRight.VIEW;

        table.setPageLength(noBeansPerPage);
        table.addContainerProperty(pageParameter.getLocalisedText("audittable.user"), String.class, null);
        table.addContainerProperty(pageParameter.getLocalisedText("audittable.modified.at"), String.class, null);
        table.addContainerProperty(pageParameter.getLocalisedText("audittable.operation"), String.class, null);
        bigTotal = dao.countQueryResult(daoQuery,UserSecurityUtils.getCurrentTenant());
        Collection<AuditedEntity> auditedEntities = dao.runQuery(auditQuery, orderBy, asc, currentTableIndex, noBeansPerPage,UserSecurityUtils.getCurrentTenant());
        addToTable(auditedEntities/*, currentUserRoles*/, dateTimeFormat, beanUtils, entityOperation, dateFormat);
        this.addComponent(table);
        pageLabel = new Label();
        displayPageNumber();
        HorizontalLayout allDataTableNav = new HorizontalLayout();

        Button firstPageBtn = new Button("<<", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event
            ) {
                int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                if (bigTotal % itemCountPerPage == 0) {
                    lastPage--;
                }

                if (currentTableIndex > 0) {
                    currentTableIndex = 0;
                    Collection<AuditedEntity> auditedEntities = dao.runQuery(auditQuery, orderBy, asc, currentTableIndex, noBeansPerPage,UserSecurityUtils.getCurrentTenant());
                    addToTable(auditedEntities, /*currentUserRoles,*/ dateTimeFormat, beanUtils, entityOperation, dateFormat);

                    table.setPageLength(itemCountPerPage);
                }
                int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
            }
        }
        );
        allDataTableNav.addComponent(firstPageBtn);

        Button leftPageBtn = new Button("<", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event
            ) {
                int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                if (bigTotal % itemCountPerPage == 0) {
                    lastPage--;
                }
                if (currentTableIndex > 0) {
                    currentTableIndex -= itemCountPerPage;
                    Collection<AuditedEntity> auditedEntities = dao.runQuery(auditQuery, orderBy, asc, currentTableIndex, noBeansPerPage,UserSecurityUtils.getCurrentTenant());
                    addToTable(auditedEntities/*, currentUserRoles*/, dateTimeFormat, beanUtils, entityOperation, dateFormat);
                    table.setPageLength(itemCountPerPage);
                }
                int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
            }
        }
        );
        allDataTableNav.addComponent(leftPageBtn);
        allDataTableNav.addComponent(pageLabel);

        Button rightPageBtn
                = new Button(">", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event
                    ) {
                        int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                        if (bigTotal % itemCountPerPage == 0) {
                            lastPage--;
                        }
                        int currentPage = currentTableIndex / itemCountPerPage;
                        if (currentPage < lastPage) {
                            currentTableIndex += itemCountPerPage;
                            Collection<AuditedEntity> auditedEntities = dao.runQuery(auditQuery, orderBy, asc, currentTableIndex, noBeansPerPage,UserSecurityUtils.getCurrentTenant());
                            addToTable(auditedEntities, /*currentUserRoles,*/ dateTimeFormat, beanUtils, entityOperation, dateFormat);
                            table.setPageLength(itemCountPerPage);
                        }
                        int currentUpdatedPage = currentTableIndex / itemCountPerPage;
                        pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (currentUpdatedPage + 1), (lastPage + 1)));
                    }
                }
                );
        allDataTableNav.addComponent(rightPageBtn);

        Button lastPageBtn = new Button(">>", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event
            ) {
                int lastPage = (int) Math.floor(bigTotal / itemCountPerPage);
                if (bigTotal % itemCountPerPage == 0) {
                    lastPage--;
                }
                int currentPage = currentTableIndex / itemCountPerPage;
                if (currentPage < lastPage) {
                    currentTableIndex = lastPage * itemCountPerPage;
                    Collection<AuditedEntity> auditedEntities = dao.runQuery(auditQuery, orderBy, asc, currentTableIndex, noBeansPerPage,UserSecurityUtils.getCurrentTenant());
                    addToTable(auditedEntities, /*currentUserRoles,*/ dateTimeFormat, beanUtils, entityOperation, dateFormat);
                    table.setPageLength(itemCountPerPage);
                }
                pageLabel.setCaption(pageParameter.getLocalisedText("page.number", (lastPage + 1), (lastPage + 1)));
            }
        }
        );
        allDataTableNav.addComponent(lastPageBtn);

        this.addComponent(allDataTableNav);
    }

    public void addToTable(Collection<AuditedEntity> auditedEntities, /*final Set<String> currentUserRoles,*/ SimpleDateFormat dateTimeFormat, BeanUtils beanUtils, EntityRight entityOperation, SimpleDateFormat dateFormat) throws UnsupportedOperationException, Property.ReadOnlyException {
        table.removeAllItems();
        for (AuditedEntity<C> auditedEntity : auditedEntities) {
            if (EntityRight.RESTRICTED.equals(UserSecurityUtils.getEntityRight(auditedEntity.getObject().getClass()))) {
                continue;
            }
            Object newItemId = table.addItem();
            Item row = table.getItem(newItemId);
            row.getItemProperty(pageParameter.getLocalisedText("audittable.user")).setValue(auditedEntity.getUserId());
            row.getItemProperty(pageParameter.getLocalisedText("audittable.modified.at")).setValue(dateTimeFormat.format(auditedEntity.getModifiedDate()));
            row.getItemProperty(pageParameter.getLocalisedText("audittable.operation")).setValue(auditedEntity.getOperation().toString());

            for (AuditedField<?> auditedField : auditedEntity.getAuditedFields()) {
                FieldState fieldState = beanUtils.calculateEffectiveFieldState(auditedField.getWebField().userMap(), entityOperation);
                if (FieldState.EDITABLE.equals(fieldState)
                        || FieldState.READ_ONLY.equals(fieldState)) {

                    String fieldName = null;
                    WebField webField = auditedField.getWebField();
                    if (webField != null) {
                        if (webField.name() != null) {
                            fieldName = webField.name();
                        }
                    }
                    if (fieldName == null) { //catch all
                        fieldName = auditedField.getWebField().name();
                    }

                    if (row.getItemProperty(fieldName).getType() == null) {
                        table.addContainerProperty(fieldName, String.class, null);
                        //row.getItemProperty(fieldName).setValue(fieldName + "[Old]");
                    }

                    if (auditedField.getValue() instanceof Date) {
                        if (auditedField.getValue() != null) {
                            row.getItemProperty(fieldName).setValue(dateFormat.format((Date) auditedField.getValue()));
                        }

                    } else {
                        if (auditedField.getValue() != null) {
                            row.getItemProperty(fieldName).setValue(auditedField.getValue().toString());
                        }

                    }
                }
            }
        }
    }

}
